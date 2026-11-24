(ns io.github.hglabplh-tech.test.suite.generator.test-gen
  (:require [clojure.pprint :as pprint]
            [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]
            [schema.core :as sc]
            [io.github.hglabplh-tech.test.suite.spy-and-mock.stats-util :as stats]
            [io.github.hglabplh_tech.test.suite.spy-and-mock.mock-spy-misc-util :as misc]
            ))


;; =============================================================================
;; 1. TYPE / SPEC -> SAMPLE VALUE SYNTHESIZER
;; =============================================================================

(defn synthesize-sample-value
  "Generates a deterministic sample value from a parameter's :spec, :schema, or name hint."
  [{:keys [param spec schema default]}]
  (let [pname (str/lower-case (name (or param "arg")))]
    (cond
      ;; 1. If a clojure.spec predicate/spec is attached, sample from its generator
      (and spec (spec/get-spec spec))
      (gen/generate (spec/gen spec))

      ;; 2. Match common Plumatic Schema / Realm / Type hints
      (or (= schema String) (str/includes? (str spec schema) "string") (str/includes? pname "name") (str/includes? pname "msg"))
      (str "sample-" (name (or param "text")))

      (or (= schema Long) (= schema Integer) (str/includes? (str spec schema) "int") (str/includes? pname "count") (str/includes? pname "id"))
      42

      (or (= schema Double) (str/includes? (str spec schema) "number") (str/includes? pname "amount") (str/includes? pname "price"))
      19.95

      (or (= schema Boolean) (str/includes? (str spec schema) "bool") (str/ends-with? pname "?"))
      true

      ;; 3. Fallback to schema default if present, otherwise keyword placeholder
      (some? default)
      default

      :else
      (keyword (str "sample-" (name (or param "val")))))))

;; =============================================================================
;; 2. BUILD POSITIONAL & NAMED TEST INPUT VECTORS FROM REFLECTION + STATS-CONFIG
;; =============================================================================

(defn generate-call-vectors
  "Generates concrete argument vectors for every arity of `structured-meta`,
   applying any `:fix-val` overrides and `:do-stats` predicates from `stats-conf`."
  [structured-meta stats-conf]
  (let [arities     (get-in structured-meta [:structured-meta :base-data :args-types-list] [])
        filter-cfg  (get-in stats-conf [:config :param-filter] {:mode :none})
        fixed-vals  (if (= :fix-val (:mode filter-cfg)) (:values filter-cfg {}) {})
        preds       (if (= :do-stats (:mode filter-cfg)) (:predicates filter-cfg {}) {})]
    (mapcat
      (fn [arity]
        (let [pos-defs   (:positional-parameters arity [])
              named-defs (:named-parameters arity [])
              ;; Build positional arguments (preferring :fix-val overrides)
              pos-args   (mapv (fn [p]
                                 (let [idx   (:position p)
                                       pname (:param p)]
                                   (cond
                                     (contains? fixed-vals idx)            (get fixed-vals idx)
                                     (contains? fixed-vals pname)          (get fixed-vals pname)
                                     (contains? fixed-vals (keyword pname)) (get fixed-vals (keyword pname))
                                     :else                                 (synthesize-sample-value p))))
                               pos-defs)
              ;; Build named arguments as flat keyword-value pairs
              named-kv   (mapcat (fn [np]
                                   (let [k   (keyword (name (or (:key np) (:param np))))
                                         val (if (contains? fixed-vals k)
                                               (get fixed-vals k)
                                               (synthesize-sample-value np))]
                                     [k val]))
                                 named-defs)
              base-call  (vec (concat pos-args named-kv))
              ;; Verify generated call satisfies :do-stats predicates if configured
              valid?     (every? (fn [[k pred-fn]]
                                   (if (number? k)
                                     (and (< k (count pos-args)) (pred-fn (nth pos-args k)))
                                     true))
                                 preds)]
          (if valid?
            (if (seq named-defs)
              ;; Emit two variants when named params exist: positional-only (defaults) & full named args
              [pos-args base-call]
              [pos-args])
            [])))
      arities)))

;; =============================================================================
;; 3. EMIT READY-TO-RUN clojure.test FORMS
;; =============================================================================

(defn emit-deftest-form
  "Generates a `clojure.test/deftest` form for `fun-sym` using reflected metadata
   and optional live spy observations from `mocking-jay`."
  [fun-sym structured-meta]
  (let [fun-meta     {:name (symbol (name fun-sym))
                      :ns   (some-> (namespace fun-sym) symbol find-ns)}
        stats-conf   (stats/resolve-stats-conf fun-meta)
        synthetic    (generate-call-vectors structured-meta stats-conf)
        ;; Also pull any real recorded calls from @statistic-control-flow
        recorded     (->> (get @misc/statistic-control-flow :flow-stats)
                          (map first)
                          (filter #(str/includes? (str (:name %)) (name fun-sym))))
        test-name    (symbol (str (name fun-sym) "-generated-test"))
        unqual-fn    (symbol (name fun-sym))]
    `(clojure.test/deftest ~test-name
       ;; 1. Assertions generated from live recorded spy runs
       ~@(for [{:keys [args ret-val threw? exception]} recorded]
           (if threw?
             `(clojure.test/is (~'thrown? ~(symbol (:class exception))
                                 (~unqual-fn ~@args)))
             `(clojure.test/is (= ~ret-val (~unqual-fn ~@args)))))

       ;; 2. Smoke / contract assertions generated purely from static reflection
       ~@(when (empty? recorded)
           (for [call-args synthetic]
             `(clojure.test/testing ~(str "Generated call with args: " (pr-str call-args))
                (clojure.test/is (some? (~unqual-fn ~@call-args)))))))))

(defn print-generated-tests-for-ns
  "Generates and pretty-prints `deftest` code for all functions cached in `@mj/mock-meta`."
  []
  (doseq [[q-name meta-entry] @misc/mock-meta]
    (pprint/pprint (emit-deftest-form (symbol q-name) meta-entry))
    (println)))

