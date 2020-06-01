(ns futils.test.main
  (:require
   [midje.repl                   :refer          :all]
   [midje.experimental           :refer     [for-all]]
   [clojure.repl                 :refer          :all]
   [clojure.tools.namespace.repl :refer [refresh
                                         refresh-all]]))

(defn -main []
  (load-facts :print-facts))

