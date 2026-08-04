(ns io.github.hglabplh-tech.reflect.clojure.active-data.parse-meta
  (:require [active.data.raw-record :as rrec :refer [is-a?]]
            [active.data.realm.attach :as attach]
            [active.data.realm.inspection :as rinspect]
            [active.data.realm.schema :as schema]
            [active.data.struct.internal.key :refer :all]
            [clojure.pprint :refer :all]))

(declare decompile-active-rec)
(defn active-rec-to-schema [ad-record]
  (let [output (schema/schema ad-record)]
    output))

(clojure.core/defn walk-through [fun the-list]
  (loop [the-rest the-list
         result []]
    (if (empty? the-rest)
      result
      (let [the-result (fun (doall (first the-rest)))]
        (recur (rest the-rest) (conj result the-result)))
      )
    ))

(clojure.core/defn realm-base-schema [value]
  (if (rinspect/realm? value)
    {:description   (rinspect/description value)
     ;;:predicate-fun (rinspect/predicate value)
     :meta-data     (rinspect/metadata value)}
    {}))

(clojure.core/defn record-field-schema [realm-value]
  (let [realm-base (realm-base-schema realm-value)
        field-name-raw (rinspect/record-realm-field-name realm-value)
        field-realm-raw (rinspect/record-realm-field-realm realm-value)
        field-getter-raw (rinspect/record-realm-field-getter realm-value)
        cooked {:field-def [field-name-raw
                            (decompile-active-rec field-realm-raw)
                            field-getter-raw
                            ]}]
    (println "realm field")
    [realm-base cooked]
    )
  )

(clojure.core/defn fun-case-output-input-schema [fun-case]
  (println "function case entered ->")
  (let [position-args-raw (rinspect/function-case-positional-argument-realms fun-case)
        pos-args-def (mapv decompile-active-rec  position-args-raw)
        opt-args-raw (rinspect/function-case-optional-arguments-realm fun-case)
        opt-args-def (if (nil? opt-args-raw)
                       []
                       (mapv decompile-active-rec opt-args-raw))
        ret-val-raw (rinspect/function-case-return-realm fun-case)
        ret-val-def (decompile-active-rec ret-val-raw)
        cooked {:function-case-def {:ret-val-def ret-val-def
                                    :pos-arg-def pos-args-def
                                    :opt-arg-def opt-args-def
                                    }}]

    (println "function case realm")
    [cooked]                                     ;; FIXME have to introduce realm-base again
    ))

(clojure.core/defn decompile-active-rec [st-value]
  (let [value st-value]
    (println "Funtion select-meta-rec ->")
    (pprint value)

    (if (rinspect/realm? value) ;; FIXME normally only have to ask for realm :-(
      (let [realm-base (realm-base-schema value)]
        (cond
          (rinspect/builtin-scalar? value)
          (let [realm-id-raw (rinspect/builtin-scalar-realm-id value)
                cooked {:scalar-realm-id realm-id-raw
                        :scalar-name     (str realm-id-raw)}]
            (println "scalar realm")
            [realm-base cooked])

          (rinspect/from-predicate? value)
          [realm-base]

          (rinspect/integer-from-to? value)
          (let [int-from-realm-raw (rinspect/integer-from-to-realm-from value)
                int-to-realm-raw (rinspect/integer-from-to-realm-to value)
                cooked {:int-from-val (decompile-active-rec int-from-realm-raw)
                        :int-to-val   (decompile-active-rec int-to-realm-raw)}]
            (println "realm integer from to")
            [realm-base cooked]
            )

          (rinspect/real-range? value)
          (let [realm-clusive-left-raw (rinspect/real-range-realm-clusive-left value)
                realm-left-raw (rinspect/real-range-realm-clusive-left value)
                realm-clusive-right (rinspect/real-range-realm-clusive-right value)
                realm-right-raw (rinspect/real-range-realm-right value)
                cooked {:clusive-left  realm-clusive-left-raw
                        :left-limit    realm-left-raw
                        :clusice-right realm-clusive-right
                        :right-limit   realm-right-raw}]
            (println "realm real-range")
            [realm-base cooked]
            )

          (rinspect/union? value)
          (let [the-rec (rinspect/union-realm-realms value)
                cooked {:union-realm-def (walk-through decompile-active-rec the-rec)}]
            (println "realm union")
            [realm-base cooked])

          (rinspect/intersection? value)
          (let [the-rec (rinspect/intersection-realm-realms value)
                cooked {:intersect-realm-def
                        (mapv decompile-active-rec the-rec)}]
            (println "realm intersection")
            [realm-base cooked])

          (rinspect/sequence-of? value)
          (let [raw (rinspect/sequence-of-realm-realm value)
                cooked {:sequence-of-realm
                        (decompile-active-rec raw)}]
            (println "realm sequence of")
            [realm-base cooked]
            )

          (rinspect/set-of? value)
          (let [realm-realm-raw (rinspect/set-of-realm-realm value)
                cooked {:set-of-realm
                        (decompile-active-rec
                          realm-realm-raw)}]
            (println "realm set of")
            [realm-base cooked]
            )

          (rinspect/map-with-keys? value)
          (let [keys-realm-map-raw (rinspect/map-with-keys-realm-map value)
                cooked {:map-key-realm (decompile-active-rec keys-realm-map-raw)}]
            (println "realm map with keys")
            [realm-base cooked]
            )

          (rinspect/map-of? value)
          (let [key-realm-raw (rinspect/map-of-realm-key-realm value)
                value-realm-raw (rinspect/map-of-realm-value-realm value)
                cooked {:map-entry-realms [(decompile-active-rec key-realm-raw)
                                           (decompile-active-rec value-realm-raw)]}]
            ;; look how we have to get  it -> compound ?
            (println "map of realm")
            [realm-base cooked]
            )

          (rinspect/optional? value)
          (let [raw (rinspect/optional-realm-realm value)
                cooked (decompile-active-rec raw)]
            (println "optional realm found: ")
            (pprint cooked)
            [realm-base cooked]
            )

          (rinspect/enum? value)                            ;; FIXME it seems in some place the predicate fun is called accidentally
          (let [raw (rinspect/enum-realm-values value)
                cooked {:enum-def raw}]
            (println "enum realm")
            [realm-base cooked]
            )

          (rinspect/tuple? value)
          (let [tuple-realms-raw (rinspect/tuple-realm-realms value)
                cooked {:tuple-realms (mapv decompile-active-rec tuple-realms-raw)}]
            (println "realm tuple")
            [realm-base cooked]
            )


          (rinspect/record? value)
          (let [name-raw (rinspect/record-realm-name value)
                ctor-raw (rinspect/record-realm-constructor value)
                fields-raw (rinspect/record-realm-fields value)
                cooked {:record-name name-raw
                        :record-ctor ctor-raw
                        :rec-fields
                        (mapv (fn [v] (println "rec-field ->") (pprint v) (record-field-schema v))
                                      fields-raw)}]
            (println "record  realm")
            [realm-base cooked]
            )



          (rinspect/function? value)
          (let [the-cases (rinspect/function-realm-cases value)
                cooked {:function-cases-def (mapv (fn [val]
                                                    (println "fun-case-realm -> ")
                                                    (pprint val)
                                                    (fun-case-output-input-schema val)) the-cases)}]
            (println "here is the function realm")
            [realm-base cooked]
            )

          (rinspect/delayed? value)
          (let [raw (rinspect/delayed-realm-delay value)
                cooked {:delayed-realm-def (decompile-active-rec raw)}]
            (println "delayed realm")
            [realm-base cooked]
            )
          (rinspect/named? value)
          (let [name-raw (rinspect/named-realm-name value)
                realm-raw (rinspect/named-realm-realm value)
                cooked {:named-realm-def
                        {:name       name-raw
                         :name-realm (decompile-active-rec realm-raw)}}]
            (pprint cooked)
            (println "named realm")
            [realm-base cooked])
        :else
        (do
          (println " This case should never happen")        ;; FIXME: here we have a gap
          (pprint value)
          [value]
          ;;(throw (IllegalArgumentException. (str "invalid realm member " value)))
          ) )))))


(clojure.core/defn get-data-active-meta [meta-data]
  (let [active-meta (get meta-data attach/fn-realm-meta-key)
        a-meta (decompile-active-rec active-meta)
        stripped-meta-desc (first a-meta)
        the-value (first (rest (first stripped-meta-desc)))]
    (pprint active-meta)
    the-value
    ))