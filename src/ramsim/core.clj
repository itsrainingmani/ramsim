(ns ramsim.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Welcome to the RAM SIMulator"))

;; Represent the state of the circuit
(def ex-state-v0 {:charge-map {:a 1 :b 2 :c 0}
                  :nand-gates [{:ins  [:a :b]
                                :out :c}]})



