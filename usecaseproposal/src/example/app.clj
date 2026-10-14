(ns example.app)

(defn create-order
  "Create an order from a customer ID and item maps."
  [customer-id items]
  {:customer-id customer-id :items items})

(defn calculate-subtotal
  "Calculate the subtotal in integer cents."
  [order]
  (reduce + (map #(* (:quantity %) (:unit-price-cents %)) (:items order))))

(defn add-tax
  "Add tax in basis points; 1900 basis points means 19 percent. Round half up."
  [subtotal-cents tax-basis-points]
  (+ subtotal-cents (quot (+ (* subtotal-cents tax-basis-points) 5000) 10000)))

(defn confirm-order
  "Return the confirmed order summary."
  [order total-cents]
  {:customer-id (:customer-id order)
   :status "confirmed"
   :total-cents total-cents})
