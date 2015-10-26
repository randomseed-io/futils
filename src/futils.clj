(ns ^{:doc    "futils library, wrapping functions."
      :author "Paweł Wilk"}
    
    futils
  
  (:require [futils.utils :refer :all]))

;; used by farg-count-clj:
(def ^:private not-ampersand?  (partial not= '&))
(def ^:private obligatory-args (partial take-while not-ampersand?))
(def ^:private obligatory-argc (comp count obligatory-args))
(def ^:private fn-arg-counters (juxt count obligatory-argc))

;; used by farg-count-jvm:
(def ^:private ^:const is-function? #{"invoke" "doInvoke"})
(def ^:private variadic-fn?    (comp (partial = "doInvoke") method-name))
(def ^:private take-functions  (partial filter (comp is-function? method-name)))

;; used by farg-count-jvm and farg-count-clj:
(def ^:private variadic?       (comp true? first))
(def ^:private is-variadic?    (partial any? variadic?))
(def ^:private only-counters   (partial map last))
(def ^:private gen-fargs-map   (partial zipmap [:variadic :arities]))
(def ^:private to-sorted-set   (juxt first (comp (partial into (sorted-set)) last)))
(def ^:private gen-fargs-seq   (juxt is-variadic? only-counters))

(defn- count-and-mark-fn-args
  "Takes a sequence of vectors representing arities (first element represents
  the number of obligatory arguments and second the total number of arguments,
  including variadic parameter, if any). It produces a sequence in which each
  element is a vector representing an arity, which the first element is
  a boolean flag that informs whether the arity is variadic and the second is
  a number of its obligatory arguments.
  
  It is intented to be used by the farg-count-clj function."
  [arglists]
  (map #(let [[ca cr] (fn-arg-counters %)]
          [(not= cr ca) cr]) arglists))

(defn- count-and-mark-method-args
  "Takes a sequence of Java methods and produces a sequence of vectors. First
  elements are boolean flags that inform whether the method is variadic and
  second are the numbers of its obligatory arguments.
  
  It is intented to be used by the farg-count-jvm function."
  [m]
  (map #(let [v (variadic-fn? %)
              c (- (method-argc %) (if v 1 0))]
          [v c]) m))

(defn argc-clj
  "Uses the given Var's :arglists metadata value to determine the number of
  arguments taken by a function that the Var is bound to. See (doc argc)
  for more specific documentation."
  [varobj]
  (when-let [fun (ensure-fn varobj)]
    (some-> varobj
            meta :arglists
            count-and-mark-fn-args
            gen-fargs-seq
            to-sorted-set
            gen-fargs-map
            (assoc :f fun :engine :clj))))

(defn argc-jvm
  "Uses JVM reflection calls to determine number of arguments the given function
  takes. Returns a map. See (doc argc) for more specific documentation."
  [f]
  (when-let [fun (ensure-fn f)]
    (some-> fun
            class .getDeclaredMethods
            take-functions
            count-and-mark-method-args
            gen-fargs-seq
            to-sorted-set
            gen-fargs-map
            (assoc :f fun :engine :jvm))))

(defmacro argc
  "Determines the number of arguments that the given function takes and returns
  a map containing these keys:
  
  :f        – a function object  
  :arities  – a sorted set of obligatory argument counts for all arities,
  :variadic – a flag informing whether the widest arity is variadic,
  :engine   – :clj (if metadata were used to determine arities);
              :jvm (if Java reflection methods were used to determine arities).
  
  Variadic parameter is not counted as one of possible arguments. However, note
  that variadic arguments always beat any number of positional arguments when
  determining most wide arity.
  
  If the given argument cannot be used to obtain a Var bound to a functon or
  a function object then it returns nil."
  [f]
  (if (symbol? f)
    `(let [fvar# (resolve (quote ~f))]
       (or (when (var? fvar#) (argc-clj fvar#)) (argc-jvm ~f)))
    `(let [f# ~f]
       (if (var? f#)
         (or (argc-clj f#) (argc-jvm (deref f#)))
         (argc-jvm f#)))))

(defmacro frelax
  "Returns a variadic function object that calls the given function f, adjusting
  the number of passed arguments to a nearest arity of f. It cuts argument list
  or pads it with nil values if necessary.
  
  The arities will be obtained from metadata (if the given object is a symbol
  bound to a Var or a Var object itself) or using JVM reflection calls to
  anonymous class representing a function object (in case of function object).
  
  To determine the number of arguments function expects the nearest arity is
  picked up by matching a number of arguments actually passed with all arities.
  If there is no exact match then the next arity capable of taking all arguments
  is selected.
  
  If the expected number of arguments is lower than a number of arguments
  actually passed to a wrapper call, the exceeding ones will be ignored.
  
  If the declared number of arguments that the original function expects is
  higher than a number of arguments really passed then nil values will be placed
  as extra arguments.
  
  When a variadic function is detected and its variadic arity is the closest to
  a number of arguments passed then all of them will be used during a function
  call (no argument will be ignored).
  
  When there is a second argument of the value :verbose then it causes generated
  function to return a map containing all passed arguments (under the :args
  key), diagnostic information about original function's arities (:variadic,
  :arities and :f keys) and a result of calling the original function (:result
  key)."
  ([f]
   `(frelax ~f nil))
  ([f vflag]
   `(apply args-relax (mapcat identity (assoc (argc ~f)
                                              :verbose (= :verbose ~vflag))))))

(defn args-relax
  "Returns a variadic function object that calls the given function, adjusting
  the number of passed arguments to a nearest arity. It cuts argument list or
  pads it with nil values if necessary.
  
  It takes 3 named arguments assigned to keywords:
  
  :f        – a function object  
  :arities  – a sorted set of obligatory argument counts for all arities,
  :variadic – a flag informing whether the widest arity is variadic.
  
  It takes one optional named argument:
  
  :verbose – a switch (defaults to false) that if set to true causes wrapper to
             return a map containing arities information (in :variadic, :arities
             and :f keys) along with all arguments passed (:args key) and
             a result of calling the original function (:result key).
  
  To determine the number of arguments function expects the nearest arity is
  picked up by matching a number of arguments actually passed with all numbers
  from a set (passed as :arities keyword argument). If there is no exact match
  then the next arity capable of taking all arguments is selected.
  
  If the expected number of arguments is lower than a number of arguments
  actually passed to a wrapper call, the exceeding ones will be ignored.
  
  If the declared number of arguments that the original function expects is
  higher than a number of arguments really passed then nil values will be placed
  as extra arguments.
  
  When a variadic function is detected and its variadic arity is the closest to
  a number of arguments passed then all of them will be used during a function
  call."
  [& {:keys [:f :arities :variadic :debug] :as uber-args
      :or   {:verbose false, :variadic false}}]
  {:pre [(ifn? f) (not (empty? arities))]}
  (fn [& args]
    (let [carg (count args)
          expe (nearest-right arities carg)
          vari (and variadic (= expe (last arities)))
          padn (repeat (- expe carg) nil)
          tken (if vari (max expe carg) expe)
          adja (take tken (concat args padn))
          resu (apply f adja)]
      (if verbose (assoc uber-args :args args :result resu) resu))))

