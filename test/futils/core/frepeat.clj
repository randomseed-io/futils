(ns futils.core.frepeat
  (:use midje.sweet)
  (:require [futils.core :refer [frepeat]]))

[[{:tag "frepeat-usage-basic" :title "Basic usage of the <code>frepeat</code>"}]]
^{:refer futils.core/frepeat :added "0.2"}
(fact
  
  (take 5 (frepeat (constantly :x)))
  => '(:x :x :x :x :x)
  
  (frepeat 5 (constantly :x))
  => '(:x :x :x :x :x)
  
  (frepeat 5 (fn [& {:keys [iteration]}] iteration))
  => '(1 2 3 4 5))

[[{:tag "frepeat-usage" :title "Using <code>frepeat</code> on anonymous functions"}]]
^{:refer futils.core/frepeat :added "0.2"}
(fact
  
  (def repeater #(inc (:previous (apply array-map %&))))
  (def numbers (frepeat repeater {:previous 0}))
  (take 5 numbers)
  => '(1 2 3 4 5)
  
  (frepeat 5 (fn [& {:keys [previous]
                     :or {previous 9}}]
               (inc previous)))
  => '(10 11 12 13 14))

[[{:tag "frepeat-usage-named" :title "Using <code>frepeat</code> on named functions"}]]
^{:refer futils.core/frepeat :added "0.2"}
(fact
  
  (defn repeater
    [& {:keys [previous iteration iterations a]}]
    [iteration iterations a])
  
  (frepeat 5 repeater {:a :something})
  => '([1 5 :something]
       [2 5 :something]
       [3 5 :something]
       [4 5 :something]
       [5 5 :something]))

[[{:tag "frepeat-usage-notfun" :title "Handling invalid values by <code>frepeat</code>"}]]
^{:refer futils.core/frepeat :added "0.1"}
(fact
  (def notfun)

  (frepeat)        => (throws clojure.lang.ArityException)
  (frepeat   1)    => (throws java.lang.ClassCastException)
  (frepeat nil)    => (throws java.lang.NullPointerException)
  (frepeat "a")    => (throws java.lang.ClassCastException)
  (frepeat notfun) => (throws java.lang.IllegalStateException)
  (frepeat String) => (throws java.lang.ClassCastException))
