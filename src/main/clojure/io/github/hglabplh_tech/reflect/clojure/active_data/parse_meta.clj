(ns io.github.hglabplh-tech.reflect.clojure.active-data.parse-meta
  (:require [active.data.raw-record :as rrec :refer [is-a?]]
            [active.data.realm.attach :as attach]
            [active.data.realm.inspection :as rinspect]
            [active.data.struct.internal.key :refer :all]
            [clojure.pprint :refer :all]))

(clojure.core/defn walk-through [fun the-list]
  (loop [the-rest the-list
         result []]
    (if (empty? the-rest)
      result
      (let [the-result  (fun (doall (first the-rest)))]
        (recur (rest the-rest) (conj result the-result)))
      )
    ))

(clojure.core/defn select-meta-rec [st-value]
  (let [value st-value]
    (println "Funtion select-meta-rec ->")
    (pprint value)

    (if (or (rinspect/realm? value) (is-a? rinspect/function-case value))
      (let [realm-base (if (rinspect/realm? value)
                         {:description   (rinspect/description value)
                        :predicate-fun (rinspect/predicate value)
                        :meta-data     (rinspect/metadata value)}
                         {})]
        (cond
          (rinspect/builtin-scalar? value)
          (let [cooked {:scalar-realm-id (rinspect/builtin-scalar-realm-id value)}]
            (println "scalar realm")
            [realm-base cooked]
            )

          (rinspect/from-predicate? value)
          (let [result {:from-predicate (rinspect/from-predicate value)}])

          (rinspect/integer-from-to? value)
          (let [int-from-realm-raw (rinspect/integer-from-to-realm-from value)
                int-to-realm-raw (rinspect/integer-from-to-realm-to value)
                cooked {:int-from-val (select-meta-rec int-from-realm-raw)
                        :int-to-val    (select-meta-rec int-to-realm-raw)}]
            (println "realm integer from to")
            [realm-base cooked]
            )

          (rinspect/real-range? value)
          (let [cooked {:clusive-left  (rinspect/real-range-realm-clusive-left value)
                        :left-limit    (rinspect/real-range-realm-left value)
                        :right-limit   (rinspect/real-range-realm-right value)
                        :clusice-right (rinspect/real-range-realm-clusive-right value)}]
            (println "realm real-range")
            [realm-base cooked]
            )

          (rinspect/union? value)
          (let [the-rec (rinspect/union-realm-realms value)
                cooked {:union-realm-def (map select-meta-rec the-rec)}]
            (println "realm union")
            [realm-base cooked])

          (rinspect/intersection? value)
          (let [the-rec (rinspect/intersection-realm-realms value)
                cooked {:intersect-realm-def
                        (map select-meta-rec the-rec)}]
            (println "realm intersection")
            [realm-base cooked])

          (rinspect/sequence-of? value)
          (let [raw (rinspect/sequence-of-realm-realm value)
                cooked {:sequence-of-realm
                        (select-meta-rec raw)}]
            (println "realm sequence of")
            [realm-base cooked]
            )

          (rinspect/set-of? value)
          (let [realm-realm-raw (rinspect/set-of-realm-realm value)
                cooked {:set-of-realm
                        (select-meta-rec
                          realm-realm-raw)}]
            (println "realm set of")
            [realm-base cooked]
            )

          (rinspect/map-with-keys? value)
          (let [cooked {:map-key-realm (select-meta-rec
                                         (rinspect/map-with-keys-realm-map value))}] ;; look how we have to get  it -> compound ?
            (println "realm map with keys")
            [realm-base cooked]
            )

          (rinspect/map-of? value)
          (let [cooked {:map-entry-realms [(select-meta-rec
                                             (rinspect/map-of-realm-key-realm value))
                                           (select-meta-rec
                                             (rinspect/map-of-realm-value-realm value))]
                        }]
            ;; look how we have to get  it -> compound ?
            (println "map of realm")
            [realm-base cooked]
            )

          ;; here the rest is implemented

          (rinspect/enum? value)
          (let [cooked {:enum-def (rinspect/enum-realm-values value)}]
            (println "enum realm")
            [realm-base cooked]
            )

          (rinspect/tuple? value)
          (let [cooked {:tuple-realms
                        (walk-through select-meta-rec
                             (rinspect/tuple-realm-realms value))}]
            (println "realm tuple")
            [realm-base cooked]
            )

          (rrec/is-a? rinspect/record-realm-field value)
          (let [cooked {:field-def [(rinspect/record-realm-field-name value)
                                      (rinspect/record-realm-field-realm value)
                                    (rinspect/record-realm-field-getter value)
                                    ]}]
            (println "realm field")
            [realm-base cooked]
            )

          (rinspect/record? value)
          (let [cooked {:record-name
                        (rinspect/record-realm-name value)
                        :record-ctor
                        (rinspect/record-realm-constructor value)
                        :rec-fields
                        (walk-through (fn [v] (println "rec-field ->") (pprint v) (select-meta-rec v))
                             (rinspect/record-realm-fields value))}]
            (println "record  realm")
            [realm-base cooked]
            )


          (is-a? rinspect/function-case value)
          (let [dummy []]
            (println "function case entered ->")
            (let [position-args-raw (rinspect/function-case-positional-argument-realms value)
                pos-args-def (walk-through (fn [v]
                                                    (println "pos arguments:")
                                                    (pprint v)
                                                    (select-meta-rec v)) position-args-raw)
                opt-args-raw (rinspect/function-case-optional-arguments-realm value)
                opt-args-def (if (nil? opt-args-raw)
                               []
                               (select-meta-rec opt-args-raw))
                ret-val-raw   (rinspect/function-case-return-realm value)
                ret-val-def  (select-meta-rec ret-val-raw)
                cooked {:function-case-def {:pos-arg-def pos-args-def
                                            :opt-arg-def opt-args-def
                                            :ret-val-def ret-val-def
                                            }}]

            (println "function case realm")
            [realm-base cooked]
            ))

          (rinspect/function? value)
          (let [the-cases (rinspect/function-realm-cases value)
                cooked {:function-cases-def (walk-through (fn [val]
                                                            (println "fun-case-realm -> ")
                                                            (pprint val)
                                                            (select-meta-rec val)) the-cases)}]
            (println "here is the function realm")
            [realm-base cooked]
            )

          (rinspect/delayed? value)
          (let [cooked {:delayed-realm-def (select-meta-rec
                                             (rinspect/delayed-realm-delay value))}]
            (println "delayed realm")
            [realm-base cooked]
            )
          (rinspect/named? value)
          (let [cooked {:named-realm-def
                        {:name       (rinspect/named-realm-name value)
                         :name-realm (select-meta-rec
                                       (rinspect/named-realm-realm value))}}]
            (pprint cooked)
            (println "named realm")
            [realm-base cooked])
          )
        (do
          (throw (IllegalArgumentException. (str "invalid realm member " value)))
          )))))


(clojure.core/defn get-data-active-meta [meta-data]
  (let [active-meta (get meta-data attach/fn-realm-meta-key)
        a-meta (map select-meta-rec active-meta)
        stripped-meta-desc (first a-meta)
        the-value (first (rest (first stripped-meta-desc)))]
    (pprint active-meta)
    the-value
    ))


