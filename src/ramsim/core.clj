(ns ramsim.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Welcome to the RAM SIMulator"))

;; Represent the state of the circuit
(def ex-state-v0 {:charge-map {:a 1 :b 2 :c 0}
                  :nand-gates [{:ins [:a :b]
                                :out :c}]})

; update state v0
; ---------------

(def empty-state {:charge-map {} :nand-gates []})

(defn charge [state wire]
  (get-in state [:charge-map wire]))

(defn charges [state wires]
  (map (partial charge state) wires))

(defn set-charge [state wire charge]
  (assoc-in state [:charge-map wire] charge))

(defn wire-nand-gate [state a b o]
  (update state :nand-gates conj {:ins [a b] :out o}))

;; Model the NAND gate behavior
(defn nand-output
  [a b]
  (if (= a b 1) 0 1))
;; => #'ramsim.core/nand-output


;; (nand-output 0 1)
;; => 1

;; (nand-output 1 1)
;; => 0

(defn dependent-nand-gates
  "Function to find all the NAND gates connected to a specific wire"
  [state wire]
  (filter
    (fn [{:keys [ins]}] (some #{wire} ins))
    (:nand-gates state)))

(dependent-nand-gates (wire-nand-gate empty-state :a :b :c) :a)
; => ({:ins [:a :b], :out :c})

(declare trigger-nand-gate)
(defn trigger
  ([state wire new-v]
   (let [old-charge (charge state wire)
         state' (set-charge state wire new-v)
         new-charge (charge state' wire)]
     (if (= old-charge new-charge)
       state'
       (reduce (fn [acc-state-out] (trigger-nand-gate acc-state-out))
               state'
               (dependent-nand-gates state' wire))))))

;; This follows exactly the model we described:

;;     Update the charge of the wire that was triggered
;;     Find all the NAND gates that the wire was connected too
;;     Trigger those NAND gates if needed.

(defn trigger-nand-gate
  "Calculates the new charge of a NAND gate, and triggers the output"
  [state {:keys [ins out]}]
  (let [new-charge (apply nand-output (charges state ins))]
    (trigger state out new-charge)))

(defn trigger-many
  "Helper function to trigger many wires"
  [state wires charges]
  (reduce
    (fn [acc-state [wire charge]]
      (trigger acc-state wire charge))
    state
    (map vector wires charges)))

(defn wire-not-gate
  ([state a o]
   (wire-nand-gate state a a o)))