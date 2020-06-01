(ns user
  (:require
   [clojure.spec.alpha           :as               cs]
   [clojure.spec.gen.alpha       :as               sg]
   [clojure.spec.test.alpha      :as               st]
   [midje.repl                   :refer          :all]
   [midje.experimental           :refer     [for-all]]
   [clojure.repl                 :refer          :all]
   [clojure.tools.namespace.repl :refer [refresh
                                         refresh-all]]
   [hydrox.core                :refer            :all]
   [futils.core                :refer            :all]
   [futils.named               :as              named]
   [futils.utils               :as              utils]
   [infra]))

(set! *warn-on-reflection* true)
(cs/check-asserts true)

(when (System/getProperty "nrepl.load")
  (require 'nrepl))

(defn test-all []
  (refresh)
  (load-facts :print-facts))

(comment 
(refresh-all)
(test-all)

)
