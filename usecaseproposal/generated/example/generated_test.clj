(ns
 example.generated-test
 (:require [clojure.test] [example.spy] [example.test-generator] [example.app]))
(clojure.test/deftest
 use-case-UC-001
 (clojure.test/testing
  "Confirm a two-item order with 19 percent tax"
  (let
   [observations
    (example.spy/collector
     {:configVersion "1.0", :language "clojure"}
     {:id "UC-001",
      :headline "Confirm a two-item order with 19 percent tax",
      :requirement
      "Two items at 1000 cents each must produce a confirmed order totaling 2380 cents.",
      :funcalls
      [{:id "create",
        :function-name "example.app/create-order",
        :arguments ["customer-A" [{:product-id "P-100", :quantity 2, :unit-price-cents 1000}]],
        :return
        {:customer-id "customer-A",
         :items [{:product-id "P-100", :quantity 2, :unit-price-cents 1000}]}}
       {:id "subtotal",
        :function-name "example.app/calculate-subtotal",
        :arguments [{:fromStep "create", :path []}],
        :return 2000}
       {:id "tax",
        :function-name "example.app/add-tax",
        :arguments [{:fromStep "subtotal", :path []} 1900],
        :return 2380}
       {:id "confirm",
        :function-name "example.app/confirm-order",
        :arguments [{:fromStep "create", :path []} {:fromStep "tax", :path []}],
        :return {:customer-id "customer-A", :status "confirmed", :total-cents 2380}}]})]
   (try
    (with-redefs-fn
     (example.spy/bindings-for
      observations
      (vector
       #'example.app/create-order
       #'example.app/calculate-subtotal
       #'example.app/add-tax
       #'example.app/confirm-order))
     (fn
      []
      (let
       [result-0
        (binding
         [example.spy/*step-id*
          "create"
          example.spy/*argument-sources*
          ["customer-A" [{:product-id "P-100", :quantity 2, :unit-price-cents 1000}]]]
         (example.app/create-order
          "customer-A"
          [{:product-id "P-100", :quantity 2, :unit-price-cents 1000}]))
        result-1
        (binding
         [example.spy/*step-id*
          "subtotal"
          example.spy/*argument-sources*
          [{:fromStep "create", :path []}]]
         (example.app/calculate-subtotal (example.test-generator/select-result result-0 [])))
        result-2
        (binding
         [example.spy/*step-id*
          "tax"
          example.spy/*argument-sources*
          [{:fromStep "subtotal", :path []} 1900]]
         (example.app/add-tax (example.test-generator/select-result result-1 []) 1900))
        result-3
        (binding
         [example.spy/*step-id*
          "confirm"
          example.spy/*argument-sources*
          [{:fromStep "create", :path []} {:fromStep "tax", :path []}]]
         (example.app/confirm-order
          (example.test-generator/select-result result-0 [])
          (example.test-generator/select-result result-2 [])))]
       (clojure.test/is
        (=
         {:customer-id "customer-A",
          :items [{:product-id "P-100", :quantity 2, :unit-price-cents 1000}]}
         result-0)
        "Expected return for step create")
       (clojure.test/is (= 2000 result-1) "Expected return for step subtotal")
       (clojure.test/is (= 2380 result-2) "Expected return for step tax")
       (clojure.test/is
        (= {:customer-id "customer-A", :status "confirmed", :total-cents 2380} result-3)
        "Expected return for step confirm")
       (let
        [calls (get-in (deref observations) [:useCases 0 :funcalls])]
        (clojure.test/is
         (=
          ["example.app/create-order"
           "example.app/calculate-subtotal"
           "example.app/add-tax"
           "example.app/confirm-order"]
          (mapv :function-name calls))
         "Actual function call order")
        (clojure.test/is
         (= ["create" "subtotal" "tax" "confirm"] (mapv :id calls))
         "Calls linked to configured steps")))))
    (finally (example.spy/write-output! "output/UC-001-spy.json" observations))))))
