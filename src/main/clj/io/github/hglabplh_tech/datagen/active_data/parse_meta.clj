(ns io.github.hglabplh-tech.datagen.active-data.parse-meta
  (:require [clojure.string :as str]
    [active.data.raw-record :as rrec]
            [active.data.record :as rec]
            [active.data.struct :as struct]
            [active.data.realm :as realm]
            [active.data.realm.attach :as attach]
            [active.data.realm.internal.records :as int-recs]
            [active.data.struct.internal.key :refer :all]
            [clojure.pprint :refer :all])
  (:import (java.lang String)))



(clojure.core/defn select-meta-rec [st-value]
  (let [value  st-value
             ]

  (if (rrec/is-a? int-recs/realm value)
    (let [realm-base {:description   (int-recs/description value)
                      :predicate-fun (int-recs/predicate value)
                      :meta-data     (int-recs/metadata value)}]
      (cond
        (rrec/is-a? int-recs/builtin-scalar-realm value)
        (let [cooked {:scalar-realm-id (int-recs/builtin-scalar-realm-id value)}]
          (println "scalar realm")
          [realm-base cooked]
          )

        (rrec/is-a? int-recs/integer-from-to-realm value)
        (let [cooked {:int-from-val (int-recs/integer-from-to-realm-from value)
                      :int-to-val   (int-recs/integer-from-to-realm-to value)}]
          (println "realm integer from to")
          [realm-base cooked]
          )

        (rrec/is-a? int-recs/real-range-realm value)
        (let [cooked {:clusive-left  (int-recs/real-range-realm-clusive-left value)
                      :left-limit    (int-recs/real-range-realm-left value)
                      :right-limit   (int-recs/real-range-realm-right value)
                      :clusice-right (int-recs/real-range-realm-clusive-right value)}]
          (println "realm real-range")
          [realm-base cooked]
          )

        (rrec/is-a? int-recs/union-realm value)
        (let [the-rec (int-recs/union-realm-realms value)
              cooked {:union-realm-def (map select-meta-rec the-rec)}]
          (println "realm union")
          [realm-base cooked])

        (rrec/is-a? int-recs/intersection-realm value)
        (let [the-rec (int-recs/intersection-realm-realms value)
              cooked {:intersect-realm-def
                      (map select-meta-rec the-rec)}]
          (println "realm intersection")
          [realm-base cooked])

        (rrec/is-a? int-recs/sequence-of-realm value)
        (let [cooked {:sequence-of-realm
                      (select-meta-rec
                        (int-recs/sequence-of-realm-realm value))}]
          (println "realm sequence of")
          [realm-base cooked]
          )
        (rrec/is-a? int-recs/set-of-realm value)
        (let [cooked {:set-of-realm
                      (select-meta-rec
                        (int-recs/set-of-realm-realm value))}]
          (println "realm set of")
          [realm-base cooked]
          )

        (rrec/is-a? int-recs/map-with-keys-realm value)
        (let [cooked {:map-key-realm (select-meta-rec
                                       (int-recs/map-with-keys-realm-map value))}] ;; look how we have to get  it -> compound ?
          (println "realm map with keys")
          [realm-base cooked]
          )

        (rrec/is-a? int-recs/map-of-realm value)
        (let [cooked {:map-entry-realms [(select-meta-rec
                                           (int-recs/map-of-realm-key-realm value))
                                         (select-meta-rec
                                           (int-recs/map-of-realm-value-realm value))]
                      }]
          ;; look how we have to get  it -> compound ?
           (println "map of realm")
          [realm-base cooked]
          )

        ;; here the rest is implemented

        (rrec/is-a? int-recs/enum-realm value)
        (let [cooked {:enum-def (int-recs/enum-realm-values value)}]
          (println "enum realm")
          [realm-base cooked]
          )

        (rrec/is-a? int-recs/tuple-realm value)
        (let [cooked {:tuple-realms
                      (map select-meta-rec
                           (int-recs/tuple-realm-realms value))}]
          (println "realm tuple")
          [realm-base cooked]
          )

        (rrec/is-a? int-recs/record-realm-field value)
        (let [cooked {:field-def [(int-recs/record-realm-field-name value)
                                  (select-meta-rec
                                    (int-recs/record-realm-field-realm value))
                                  (int-recs/record-realm-field-getter value)
                                  ]}]
          (println "realm field")
          [realm-base cooked]
          )

        (rrec/is-a? int-recs/record-realm value)
        (let [cooked {:record-name
                      (int-recs/record-realm-name value)
                      :record-ctor
                      (int-recs/record-realm-constructor value)
                      :rec-fields
                      (map select-meta-rec
                           (int-recs/record-realm-fields value))}]
          (println "record  realm")
          [realm-base cooked]
          )

        (rrec/is-a? int-recs/function-case value)
        (let [pos-args-def (map select-meta-rec
                                (int-recs/function-case-positional-argument-realms value))
              opt-args-def (if (nil? (int-recs/function-case-optional-arguments-realm value))
                             []
                             (map select-meta-rec
                               (int-recs/function-case-optional-arguments-realm value)))
              ret-val-def (select-meta-rec
                            (int-recs/function-case-return-realm value))
              cooked {:function-case-def {:pos-arg-def pos-args-def
                                          :opt-arg-def opt-args-def
                                          :ret-val-def ret-val-def
                                          }}]

          (println "function case realm")
          [realm-base cooked]
          )


        (rrec/is-a? int-recs/function-realm value)
        (let [the-cases (int-recs/function-realm-cases value)
              cooked {:function-cases-def  (map select-meta-rec the-cases)}]
          (println "function")
          [realm-base cooked]
          )

        (rrec/is-a? int-recs/delayed-realm value)
        (let [cooked {:delayed-realm-def (select-meta-rec
                                           (int-recs/delayed-realm-delay value))}]
          (println "delayed realm")
          [realm-base cooked]
          )
        (rrec/is-a? int-recs/named-realm value)
        (let [cooked {:named-realm-def
                      {:name       (int-recs/named-realm-name value)
                       :name-realm (select-meta-rec
                                     (int-recs/named-realm-realm value))}}]
          (println "named realm")
          [realm-base cooked])
        )
      (do
        (throw (IllegalArgumentException. "no active-data realm"))
        )))))

(clojure.core/defn get-fun-descr-active-meta [meta-data]
  (let [active-meta (get meta-data attach/fn-realm-meta-key)
        a-meta  (map   (clojure.core/fn [val]
                         [val]) active-meta)
        stripped-meta-desc (first a-meta)
        the-value (first (rest (first stripped-meta-desc)))]
    the-value
    ))


