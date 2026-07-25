(ns io.github.hglabplh_tech.test.suite.datagen.active-data.parse-meta
  (:require [active.data.raw-record :as rrec :refer [is-a?]]
            [active.data.realm.attach :as attach]
            [active.data.realm.inspection :as ad-inspect]
            [active.data.struct.internal.key :refer :all]
            [clojure.pprint :refer :all]))



(clojure.core/defn select-meta-rec [st-value]
  (let [value st-value]

    (if (ad-inspect/realm? value)
      (let [realm-base {:description   (ad-inspect/description value)
                        :predicate-fun (ad-inspect/predicate value)
                        :meta-data     (ad-inspect/metadata value)}]
        (cond
          (ad-inspect/builtin-scalar? value)
          (let [cooked {:scalar-realm-id (ad-inspect/builtin-scalar-realm-id value)
                        :scalar-realm    (ad-inspect/builtin-scalar-realm value)}]
            (println "scalar realm")
            [realm-base cooked]
            )

          (ad-inspect/integer-from-to value)
          (let [cooked {:int-from-val (ad-inspect/integer-from-to-realm-from value)
                        :int-to-val   (ad-inspect/integer-from-to-realm-to value)}]
            (println "realm integer from to")
            [realm-base cooked]
            )

          (ad-inspect/real-range? value)
          (let [cooked {:clusive-left  (ad-inspect/real-range-realm-clusive-left value)
                        :left-limit    (ad-inspect/real-range-realm-left value)
                        :right-limit   (ad-inspect/real-range-realm-right value)
                        :clusice-right (ad-inspect/real-range-realm-clusive-right value)}]
            (println "realm real-range")
            [realm-base cooked]
            )

          (ad-inspect/union? value)
          (let [the-rec (ad-inspect/union-realm-realms value)
                cooked {:union-realm-def (map select-meta-rec the-rec)}]
            (println "realm union")
            [realm-base cooked])

          (ad-inspect/intersection? value)
          (let [the-rec (ad-inspect/intersection-realm-realms value)
                cooked {:intersect-realm-def
                        (map select-meta-rec the-rec)}]
            (println "realm intersection")
            [realm-base cooked])

          (ad-inspect/sequence-of? value)
          (let [cooked {:sequence-of-realm
                        (select-meta-rec
                          (ad-inspect/sequence-of-realm-realm value))}]
            (println "realm sequence of")
            [realm-base cooked]
            )
          (ad-inspect/set-of? value)
          (let [cooked {:set-of-realm
                        (select-meta-rec
                          (ad-inspect/set-of-realm-realm value))}]
            (println "realm set of")
            [realm-base cooked]
            )

          (ad-inspect/map-with-keys? value)
          (let [cooked {:map-key-realm (select-meta-rec
                                         (ad-inspect/map-with-keys-realm-map value))}] ;; look how we have to get  it -> compound ?
            (println "realm map with keys")
            [realm-base cooked]
            )

          (ad-inspect/map-of? value)
          (let [cooked {:map-entry-realms [(select-meta-rec
                                             (ad-inspect/map-of-realm-key-realm value))
                                           (select-meta-rec
                                             (ad-inspect/map-of-realm-value-realm value))]
                        }]
            ;; look how we have to get  it -> compound ?
            (println "map of realm")
            [realm-base cooked]
            )

          ;; here the rest is implemented

          (ad-inspect/enum? value)
          (let [cooked {:enum-def (ad-inspect/enum-realm-values value)}]
            (println "enum realm")
            [realm-base cooked]
            )

          (ad-inspect/tuple? value)
          (let [cooked {:tuple-realms
                        (map select-meta-rec
                             (ad-inspect/tuple-realm-realms value))}]
            (println "realm tuple")
            [realm-base cooked]
            )

          (record? value)
          (let [the-record {:record (ad-inspect/record-realm value)}
                the-rec-meta {:rec-name   (ad-inspect/record-realm-name (get the-record :record))
                              :rec-ctor   (ad-inspect/record-realm-constructor (get the-record :record))
                              :rec-fileds (ad-inspect/record-realm-fields (get the-record :record))}]
            (get the-rec-meta :rec-fileds)
            (loop [rec-meta the-rec-meta result {}]
              (let [field {:field-name   (ad-inspect/record-realm-field-name (first rec-meta))
                           :field-realm  (map select-meta-rec (ad-inspect/record-realm-field-realm (first rec-meta)) )
                           :field-getter (ad-inspect/record-realm-field-getter (first rec-meta))
                           }]
                (if (nil? rec-meta)
                  result)
                (recur (rest rec-meta) (conj result field)))))



          (println "realm field")
          [realm-base cooked]
          )

        (is-a?  ad-inspect/function-realm-cases value)
        (let [pos-args-def (map select-meta-rec
                                (ad-inspect/function-case-positional-argument-realms value))
              opt-args-def (if (nil? (ad-inspect/function-case-optional-arguments-realm value))
                             []
                             (map select-meta-rec
                                  (ad-inspect/function-case-optional-arguments-realm value)))
              ret-val-def (select-meta-rec
                            (ad-inspect/function-case-return-realm value))
              cooked {:function-case-def {:pos-arg-def pos-args-def
                                          :opt-arg-def opt-args-def
                                          :ret-val-def ret-val-def
                                          }}]

          (println "function case realm")
          [realm-base cooked]
          )


        (rrec/is-a? int-recs/function-realm value)
        (let [the-cases (ad-inspect/function-realm-cases value)
              cooked {:function-cases-def (map select-meta-rec the-cases)}]
          (println "function")
          [realm-base cooked]
          )

        (ad-inspect/delayed? value)
        (let [cooked {:delayed-realm-def (select-meta-rec
                                           (ad-inspect/delayed-realm-delay value))}]
          (println "delayed realm")
          [realm-base cooked]
          )

        (ad-inspect/named? value)
        (let [cooked {:named-realm-def
                      {:name       (ad-inspect/named-realm-name value)
                       :name-realm (select-meta-rec
                                     (ad-inspect/named-realm-realm value))}}]
          (println "named realm")
          [realm-base cooked])
        )
      (do
        (throw (IllegalArgumentException. "no active-data realm"))
        )))) )

;;??????????????? TODO: mke ready
(clojure.core/defn get-fun-descr-active-meta [meta-data]
  (let [active-meta (get meta-data attach/fn-realm-meta-key)
        a-meta (map (clojure.core/fn [val]
                      [val]) active-meta)
        stripped-meta-desc (first a-meta)
        the-value (first (rest (first stripped-meta-desc)))]
    the-value
    ))


