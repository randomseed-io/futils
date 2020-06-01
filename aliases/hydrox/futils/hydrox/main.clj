(ns futils.hydrox.main
  (:require [hydrox.core                     :refer :all]
            [clojure.string                  :as     str]))

(defn -main
  [& args]
  (dive)
  (generate-docs))
