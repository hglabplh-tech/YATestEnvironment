(require '[example.test-generator :as runner]
         '[example.spy :as spy]
         '[example.app :as app]
         '[clojure.test :as test]
         '[clojure.java.io :as io])
(def definitions (runner/read-definitions "user-definitions.json"))
(def use-case (first (:useCases definitions)))
(def reflection (runner/reflected-interface))
(def originals (mapv deref runner/callable-vars))
(load-file "generated/example/generated_test.clj")
(def normal-summary
  (binding [test/*test-out* (java.io.StringWriter.)]
    (test/run-tests 'example.generated-test)))
(assert (= {:test 1 :pass 6 :fail 0 :error 0} (select-keys normal-summary [:test :pass :fail :error])))
(assert (every? true? (map identical? originals (map deref runner/callable-vars))))
(def negative-case (-> use-case (assoc :id "UC-NEGATIVE") (assoc-in [:funcalls 2 :return] 999)))
(eval (runner/generate-test-form reflection negative-case))
(def negative-summary
  (binding [test/*test-out* (java.io.StringWriter.)]
    (test/run-tests 'user)))
(assert (= 1 (:fail negative-summary)))
(assert (zero? (:error negative-summary)))
(defn rejected? [case-data]
  (try (runner/generate-test-form reflection case-data) false
       (catch clojure.lang.ExceptionInfo _ true)))
(assert (rejected? (assoc-in use-case [:funcalls 0 :arguments] [])))
(assert (rejected? (assoc-in use-case [:funcalls 0 :arguments 0] {:fromStep "tax" :path []})))
(assert (rejected? (assoc-in use-case [:funcalls 0 :function-name] "missing/function")))
(def observations (spy/collector definitions use-case))
(try
  (with-redefs-fn (spy/bindings-for observations [#'app/add-tax])
    #(binding [spy/*step-id* "tax"] (app/add-tax "bad" 1900)))
  (catch Throwable _))
(assert (= "threw" (get-in @observations [:useCases 0 :funcalls 0 :outcome])))
(assert (every? true? (map identical? originals (map deref runner/callable-vars))))
(io/delete-file "output/UC-NEGATIVE-spy.json")
(println "Verified: generated test passes; wrong expectation fails; invalid references/arity/callable rejected; exceptions recorded; originals restored.")
