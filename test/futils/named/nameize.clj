(ns futils.named.nameize
  (:use midje.sweet)
  (:require [futils.named :refer [nameize]]))

[[{:tag "nameize-usage" :title "Usage of <code>nameize</code>"}]]
^{:refer futils.named/nameize :added "0.6"}
(fact

  (def nreduce (nameize reduce [f coll] [f val coll]))
  (nreduce :f + :coll [1 2 3 4])         => 10
  (nreduce :f + :coll [1 2 3 4] :val 2)  => 12
  
  (def nfun (nameize #(list %1 %2) [a b]))
  (nfun :a 1 :b 2)
  => '(1 2)
  
  (def nfun (nameize (fn [& a] a) [a b] {:a 7 :b 8}))
  (nfun :b 2)
  => '(7 2)

  (defn fun [& more] more)

  (def nfun (nameize fun [a b c]))
  (nfun :a 1 :b 2 :c 3 :d 4)
  => '(1 2 3)

  (def nfun (nameize fun [a b c &rest]))
  (nfun :a 1 :b 2 :c 3 :d 4)
  => '(1 2 3 {:d 4})

  (def nfun (nameize fun [a &rest b c] {:a 1}))
  (nfun :b 2 :c 3 :d 4)
  => '(1 {:d 4} 2 3)

  (def nfun (nameize fun [a b c &rest]))
  (nfun :a 1 :b 2 :c 3)
  => '(1 2 3 nil))

[[{:tag "nameize-usage-multi" :title "Handling multiple arities by <code>nameize</code>"}]]
^{:refer futils.named/nameize :added "0.6"}
(fact

  (def nfun (nameize (fn [& a] a)
                     [a]           {:a 5}
                     [a c]
                     [a b &rest]
                     [a b c &rest] {:a 1 :e 5}))

  
  (nfun)                      => '(5)                   ; matched: [a]
  (nfun :a 1)                 => '(1)                   ; matched: [a]
  (nfun :a 1 :c 3)            => '(1 3)                 ; matched: [a c]
  (nfun :a 1 :b 2)            => '(1 2 nil)             ; matched: [a b &rest]
  (nfun :a 1 :b 2 :c 3)       => '(1 2 3 {:e 5})        ; matched: [a b c &rest]
  (nfun :a 1 :b 2 :c 3 :d 4)  => '(1 2 3 {:d 4 :e 5}))  ; matched: [a b c &rest]

[[{:tag "nameize-usage-notfun" :title "Handling invalid values by <code>nameize</code>"}]]
^{:refer futils.named/nameize :added "0.6"}
(fact
  (def notfun)
  (defn fun [a b] (list a b))
  
  (nameize notfun []) => (throws java.lang.AssertionError)
  
  (def nfun (nameize fun [a b]))
  (nfun :a 1)
  => (throws java.lang.IllegalArgumentException)
  
  (nameize fun [:a] [:b] [:a b])
  => (throws java.lang.IllegalArgumentException))
