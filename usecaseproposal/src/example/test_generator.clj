(ns example.test-generator
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.test :as test]
            [example.app]
            [example.spy :as spy]))

(def callable-vars
  [#'example.app/create-order #'example.app/calculate-subtotal
   #'example.app/add-tax #'example.app/confirm-order])

(defn reflected-interface
  ([] (reflected-interface callable-vars))
  ([vars]
  ;; Reflection input: actual loaded Vars and their function metadata.
  ;; This example obtains layout/arity; production realm/type validation goes here.
  {:language "clojure"
   :functions
   (into {}
         (for [v vars
               :let [m (meta v)
                     id (str (ns-name (:ns m)) "/" (:name m))]]
           [id {:var v :arglists (:arglists m) :doc (:doc m)}]))}))

(defn read-definitions [filename]
  (json/read-str (slurp filename) :key-fn keyword))

(defn select-result [result path]
  (get-in result (mapv #(if (string? %) (keyword %) %) path)))

(defn- argument-form [argument prior-bindings]
  (if (and (map? argument) (contains? argument :fromStep))
    (let [result-symbol (get prior-bindings (:fromStep argument))]
      (when-not result-symbol
        (throw (ex-info "Result reference must point to an earlier step" {:argument argument})))
      (when-not (vector? (:path argument))
        (throw (ex-info "Result reference requires a path array" {:argument argument})))
      (list 'example.test-generator/select-result result-symbol (:path argument)))
    argument))

(defn generate-test-form [reflection use-case]
  (when-not (seq (:funcalls use-case))
    (throw (ex-info "Use case needs at least one call" {})))
  (when-not (re-matches #"[A-Za-z0-9_-]+" (:id use-case))
    (throw (ex-info "Invalid use-case ID" {:id (:id use-case)})))
  (let [steps (:funcalls use-case)
        compiled
        (reduce
          (fn [{:keys [prior bindings checks]} step]
            (let [id (:id step)
                  function-id (:function-name step)
                  descriptor (get-in reflection [:functions function-id])
                  result-symbol (symbol (str "result-" (count prior)))
                  arguments (:arguments step)]
              (when (or (not (string? id)) (contains? prior id))
                (throw (ex-info "Step IDs must be unique strings" {:step step})))
              (when-not descriptor
                (throw (ex-info "Callable absent from reflection" {:function function-id})))
              (when-not (and (vector? arguments)
                            (some #(= (count %) (count arguments)) (:arglists descriptor)))
                (throw (ex-info "Arguments do not match reflected arity" {:step step})))
              (when-not (contains? step :return)
                (throw (ex-info "This example requires an expected return" {:step step})))
              {:prior (assoc prior id result-symbol)
               :bindings
               (into bindings
                     [result-symbol
                      (list 'binding
                            ['example.spy/*step-id* id
                             'example.spy/*argument-sources* arguments]
                            (cons (symbol function-id)
                                  (mapv #(argument-form % prior) arguments)))])
               :checks (conj checks
                             (list 'clojure.test/is
                                   (list '= (:return step) result-symbol)
                                   (str "Expected return for step " id)))}))
          {:prior {} :bindings [] :checks []}
          steps)
        expected-order (mapv :function-name steps)
        expected-ids (mapv :id steps)]
    (list 'clojure.test/deftest (symbol (str "use-case-" (:id use-case)))
          (list 'clojure.test/testing (:headline use-case)
                (list 'let ['observations
                            (list 'example.spy/collector
                                  {:configVersion "1.0" :language (:language reflection)} use-case)]
                      (list 'try
                            (list 'with-redefs-fn
                                  (list 'example.spy/bindings-for 'observations
                                        (cons 'vector
                                              (mapv #(list 'var (symbol %))
                                                    (distinct expected-order))))
                                  (list 'fn []
                                        (apply list 'let (:bindings compiled)
                                               (concat (:checks compiled)
                                                       [(list 'let ['calls (list 'get-in (list 'deref 'observations) [:useCases 0 :funcalls])]
                                                              (list 'clojure.test/is (list '= expected-order (list 'mapv :function-name 'calls)) "Actual function call order")
                                                              (list 'clojure.test/is (list '= expected-ids (list 'mapv :id 'calls)) "Calls linked to configured steps"))]))))
                            (list 'finally
                                  (list 'example.spy/write-output!
                                        (str "output/" (:id use-case) "-spy.json") 'observations))))))))

(defn generate-tests!
  "First input: reflected interface. Second input: parsed user JSON.
   Write executable deftests without executing application functions."
  [reflection definitions output-file]
  (when-not (= "clojure" (:language reflection) (:language definitions))
    (throw (ex-info "Reflection and user definitions must both describe Clojure" {})))
  (let [use-cases (:useCases definitions)]
    (when (or (empty? use-cases)
              (not= (count use-cases) (count (set (map :id use-cases)))))
      (throw (ex-info "Use-case IDs must be unique, with at least one use case" {})))
    (let [forms (mapv #(generate-test-form reflection %) use-cases)
          namespaces (->> use-cases
                          (mapcat :funcalls)
                          (map #(symbol (namespace (symbol (:function-name %)))))
                          distinct
                          (sort-by str))
          ns-form (list 'ns 'example.generated-test
                        (cons :require
                              (mapv vector (distinct (concat '[clojure.test example.spy example.test-generator]
                                                            namespaces)))))]
      (io/make-parents output-file)
      (with-open [writer (io/writer output-file)]
        (binding [*out* writer
                  pprint/*print-miser-width* nil
                  pprint/*print-right-margin* 100]
          (pprint/pprint ns-form)
          (doseq [form forms] (pprint/pprint form))))
      {:test-file output-file :test-count (count forms)})))

(defn -main [& [config-file output-file]]
  (let [result (generate-tests!
                 (reflected-interface)
                 (read-definitions (or config-file "user-definitions.json"))
                 (or output-file "generated/example/generated_test.clj"))]
    (println "Generated" (:test-count result) "test(s):" (:test-file result))))
