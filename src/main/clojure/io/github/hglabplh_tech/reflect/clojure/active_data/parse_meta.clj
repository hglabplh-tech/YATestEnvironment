(ns io.github.hglabplh-tech.reflect.clojure.active-data.parse-meta
  (:require [active.data.realm.inspection :as rinspect]
            [active.data.struct.internal.key :refer :all]
            [clojure.pprint :refer :all]))

(declare decompile-active-rec)

(clojure.core/defn realm-base-schema
  "read and store the base fields common in each realm
  @param : the realm"
  {:added  "1.4.0"
   :static true}
  [realm-value]
  (if (rinspect/realm? realm-value)
    {:description (rinspect/description realm-value)
     :meta-data   (rinspect/metadata realm-value)}
    {}))

(clojure.core/defn make-base-predicate-fun [type-id]
  (println type-id)
  (cond
    (= type-id :integer)
    integer?
    (= type-id :rational)
    rational?
    (= type-id :number)
    number?
    (= type-id :keyword)
    keyword?
    (= type-id :string)
    string?
    (= type-id :uuid)
    uuid?
    (= type-id :char)
    char?
    (= type-id :symbol)
    symbol?
    (= type-id :boolean)
    boolean?
    (= type-id :any)
    any?
    :else
    any?
    ))
(defn- real?
  "Returns true if n is a real number."
  [n]
  (number? n))

(clojure.core/defn real-range-pred [left right clusive-left clusive-right]
  (cond
    (and left right)
    (case clusive-left
      (:in) (case clusive-right
              (:in)
              (fn [n]
                (and (real? n)
                     (<= left n right)))
              (:ex)
              (fn [n]
                (and (real? n)
                     (<= left n)
                     (< n right))))
      (:ex) (case clusive-right
              (:in)
              (fn [n]
                (and (real? n)
                     (< left n)
                     (<= n right)))
              (:ex)
              (fn [n]
                (and (real? n)
                     (< left n right)))))

    left
    (case clusive-left
      (:in)
      (fn [n]
        (and (real? n)
             (<= left n)))
      (:ex)
      (fn [n]
        (and (real? n)
             (< left n))))

    right
    (case clusive-right
      (:in)
      (fn [n]
        (and (real? n)
             (<= n right)))
      (:ex)
      (fn [n]
        (and (real? n)
             (< n right))))

    :else
    real?)
  )

(clojure.core/defn record-field-schema
  "read and store the values and the realm for a record field
   @param : the definition for a record field"
  {:added  "1.4.0"
   :static true}
  [realm-value]
  (let [realm-base (realm-base-schema realm-value)
        field-name-raw (rinspect/record-realm-field-name realm-value)
        field-realm-raw (rinspect/record-realm-field-realm realm-value)
        field-getter-raw (rinspect/record-realm-field-getter realm-value)
        cooked (conj realm-base {:field-def [field-name-raw
                                             (decompile-active-rec field-realm-raw)
                                             field-getter-raw
                                             ]})]
    (println "realm field")
    cooked
    )
  )

(clojure.core/defn fun-case-output-input-schema
  "read and store the definitions of a function case
   @param : the definition for a function case"
  {:added  "1.4.0"
   :static true}
  [fun-case]
  (println "function case entered ->")
  (let [realm-base (realm-base-schema fun-case)
        position-args-raw (rinspect/function-case-positional-argument-realms fun-case)
        pos-args-def (mapv decompile-active-rec position-args-raw)
        opt-args-raw (rinspect/function-case-optional-arguments-realm fun-case)
        opt-args-def (if (nil? opt-args-raw)
                       []
                       (mapv decompile-active-rec opt-args-raw))
        ret-val-raw (rinspect/function-case-return-realm fun-case)
        ret-val-def (decompile-active-rec ret-val-raw)
        cooked (conj realm-base {:predicate-fun     fn?
                                 :function-case-def {:ret-val-def ret-val-def
                                                     :pos-arg-def pos-args-def
                                                     :opt-arg-def opt-args-def
                                                     }})]

    (println "function case realm")
    cooked                                                  ;; FIXME have to introduce realm-base again
    ))

(clojure.core/defn decompile-active-rec
  "read and decompile a realm in our case the root realm of a function/record (stored in metadata)
   @param : the realm stored in the function or record metadata"
  {:added  "1.4.0"
   :static true}
  [st-value]
  (let [value st-value]
    (println "Funtion decompile-active-rec ->")
    (if (rinspect/realm? value)                             ;; FIXME normally only have to ask for realm :-(
      (let [realm-base (realm-base-schema value)]
        (cond
          (rinspect/builtin-scalar? value)
          (let [realm-id (rinspect/builtin-scalar-realm-id value)
                cooked (conj realm-base {:predicate-fun   (make-base-predicate-fun realm-id)
                                         :scalar-realm-id realm-id
                                         :scalar-name     (str realm-id)})]
            (println "scalar realm")
            (pprint cooked)
            cooked)


          (rinspect/integer-from-to? value)
          (let [from (rinspect/integer-from-to-realm-from value)
                to (rinspect/integer-from-to-realm-to value)
                cooked (conj realm-base {:predicate-fun (fn [x]
                                                          (and (integer? x)
                                                               (<= from x to)))
                                         :int-from-val  from
                                         :int-to-val    to})]
            (println "realm integer from to")
            cooked
            )

          (rinspect/real-range? value)
          (let [realm-clusive-left (rinspect/real-range-realm-clusive-left value)
                realm-left (rinspect/real-range-realm-left value)
                realm-clusive-right (rinspect/real-range-realm-clusive-right value)
                realm-right (rinspect/real-range-realm-right value)
                cooked (conj realm-base {:predicate-fun (real-range-pred realm-left
                                                                         realm-right
                                                                         realm-clusive-left
                                                                         realm-clusive-right)
                                         :clusive-left  realm-clusive-left
                                         :left-limit    realm-left
                                         :clusice-right realm-clusive-right
                                         :right-limit   realm-right})]
            (println "realm real-range")
            cooked
            )

          (rinspect/union? value)
          (let [realms (rinspect/union-realm-realms value)
                the-rec-compiled (mapv decompile-active-rec realms)
                cooked (conj realm-base {:predicate-fun   (let [predicates (map rinspect/predicate realms)]
                                                            (fn [x]
                                                              (clojure.core/boolean (some #(% x) predicates))))
                                         :union-realm-def the-rec-compiled})]
            (println "realm union")
            cooked)

          (rinspect/intersection? value)
          (let [realms (rinspect/intersection-realm-realms value)
                cooked (conj realm-base {:predicate-fun (let [predicates (map rinspect/predicate realms)]
                                                          (fn [x]
                                                            (every? #(% x) predicates)))
                                         :intersect-realm-def
                                         (mapv decompile-active-rec realms)})]
            (println "realm intersection")
            cooked)

          (rinspect/sequence-of? value)
          (let [raw (rinspect/sequence-of-realm-realm value)
                cooked (conj {:predicate-fun sequential?
                              :sequence-of-realm
                              (decompile-active-rec raw)})]
            (println "realm sequence of")
            cooked
            )

          (rinspect/set-of? value)
          (let [realm-realm-raw (rinspect/set-of-realm-realm value)
                cooked (conj realm-base {:predicate-fun set?
                                         :set-of-realm
                                         (decompile-active-rec
                                           realm-realm-raw)})]
            (println "realm set of")
            cooked
            )

          (rinspect/map-with-keys? value)
          (let [keys-realm-map-raw (rinspect/map-with-keys-realm-map value)
                cooked (conj realm-base {:predicate-fun map?
                                         :map-key-realm (decompile-active-rec keys-realm-map-raw)})]
            (println "realm map with keys")
            cooked
            )

          (rinspect/map-of? value)
          (let [key-realm-raw (rinspect/map-of-realm-key-realm value)
                value-realm-raw (rinspect/map-of-realm-value-realm value)
                cooked (conj realm-base {:predicate-fun    map?
                                         :map-entry-realms [(decompile-active-rec key-realm-raw)
                                                            (decompile-active-rec value-realm-raw)]})]
            ;; look how we have to get  it -> compound ?
            (println "map of realm")
            cooked
            )

          (rinspect/optional? value)
          (let [opt-realm (rinspect/optional-realm-realm value)
                cooked (conj realm-base {:predicate-fun (let [inner-predicate (rinspect/predicate opt-realm)]
                                                          (fn [x]
                                                            (or (nil? x)
                                                                (inner-predicate x))))
                                         :optional      (decompile-active-rec opt-realm)})]
            (println "optional realm found: ")
            cooked
            )

          (rinspect/enum? value)                            ;; FIXME it seems in some place the predicate fun is called accidentally
          (let [values-set (rinspect/enum-realm-values value)
                cooked (conj realm-base {:predicate-fun (fn [x]
                                                          (clojure.core/contains? values-set x))
                                         :enum-def      values-set})]
            (println "enum realm")
            cooked)

          (rinspect/tuple? value)
          (let [tuple-realms (rinspect/tuple-realm-realms value)
                cooked (conj realm-base {:predicate-fun (let [size (count tuple-realms)]
                                                          (fn [x]
                                                            (and (sequential? x)
                                                                 (= (count x) size))))
                                         :tuple-realms  (mapv decompile-active-rec tuple-realms)})]
            (println "realm tuple")
            cooked
            )


          (rinspect/record? value)
          (let [name-raw (rinspect/record-realm-name value)
                ctor-raw (rinspect/record-realm-constructor value)
                fields-raw (rinspect/record-realm-fields value)
                cooked (conj realm-base {:record-name name-raw
                                         :record-ctor ctor-raw
                                         :rec-fields
                                         (mapv (fn [v] (println "rec-field ->") (record-field-schema v))
                                               fields-raw)})]
            (println "record  realm")
            [realm-base cooked]
            )



          (rinspect/function? value)
          (let [the-cases (rinspect/function-realm-cases value)
                cooked (conj realm-base {:function-cases-def (mapv (fn [val]
                                                                     (println "fun-case-realm -> ")
                                                                     (fun-case-output-input-schema val)) the-cases)})]
            (println "here is the function realm")
            (pprint cooked)
            cooked
            )

          (rinspect/delayed? value)
          (let [obj (rinspect/delayed-realm-delay value)
                cooked (conj realm-base {:predicate-fun     (fn [x#]
                                                              ((rinspect/predicate (force obj)) x#))
                                         :delayed-realm-def (decompile-active-rec obj)})]
            (println "delayed realm")
            cooked
            )

          (rinspect/named? value)
          (let [name-raw (rinspect/named-realm-name value)
                realm-raw (rinspect/named-realm-realm value)
                cooked (conj realm-base {:predicate-fun (rinspect/predicate value)
                                         :named-realm-def
                                         {:name       name-raw
                                          :name-realm (decompile-active-rec realm-raw)}})]
            (println "named realm")
            cooked)
          :else
          (do
            (println " This case should never happen")      ;; FIXME: here we have a gap
            [value]
            ;;(throw (IllegalArgumentException. (str "invalid realm member " value)))
            ))))))