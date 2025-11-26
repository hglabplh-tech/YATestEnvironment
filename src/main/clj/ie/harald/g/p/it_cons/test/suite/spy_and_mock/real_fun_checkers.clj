(ns ie.harald.g.p.it-cons.test.suite.spy-and-mock.real-fun-checkers
  (:refer-clojure :exclude [defn fn])
  (:require [clojure.walk :refer :all]
            [clojure.reflect :as refl]
            [clojure.pprint :refer :all]
            [active.data.realm :as realm]
            [active.data.realm.attach :refer :all]
            [active.data.realm.internal.record-meta :as act-meta]

            [active.data.realm.internal.records :as realm-records]
            [active.data.raw-record :as rrec]
            [schema.core :as sc]
            [active.data.record :as sut]
            [clojure.pprint :refer :all]
            [clojure.test :refer :all]
            [schema.spec.core :refer :all]
            ))
(def to-spy :spy-key)
(def to-mock :mock-key)


(defmacro get-fun-meta [funname]
  `(meta (var ~funname)))

(clojure.core/defn get-active-meta [all-meta-data gen-meta-data]
  (let [record-meta-data (get all-meta-data act-meta/record-realm-meta-key)
        fields-meta-data (get all-meta-data act-meta/fields-realm-map-meta-key)
        schema-meta-data (get all-meta-data :schema)
        result-meta (conj {:schema                            schema-meta-data act-meta/record-realm-meta-key record-meta-data
                           act-meta/fields-realm-map-meta-key fields-meta-data} gen-meta-data)]
    (println result-meta)
    result-meta

    )

  )
;;; implement it with that logic slightly changed
;; from active-data and in active data it works the thing
;; with using constantly seems to me a very good idea


(defmacro transfer-fun-meta
  "Macro to set the Meta of the mocked function to a mock"
  [generated-fun orig-fun]
  `(do (alter-meta! (var ~generated-fun)
                    (constantly
                      (assoc (get-active-meta (meta (var ~orig-fun))
                                              (meta (var ~generated-fun)))
                        :mock-type :fun)))
       (var ~generated-fun)))

(defmacro add-meta-mock
  "Macro to set the Meta of the mocked function to a mock"
  [fun-name]
  `(do (alter-meta! (var ~fun-name)
                    (constantly
                      (assoc (meta (var ~fun-name))
                        to-mock ~fun-name)))
       (var ~fun-name)))

(defmacro add-meta-spy
  "Macro to set the Meta of the mocked function to a mock"
  [fun-name]
  `(do (alter-meta! (var ~fun-name)
                    (constantly
                      (assoc (meta (var ~fun-name))
                        to-spy ~fun-name)))
       (var ~fun-name)))


(defmacro get-fun-meta-val-by-key [fun-name the-tag]
  `(get (meta (var ~fun-name))
        ~the-tag))

(defmacro get-fun-meta-args [the-fun-name]
  `(get-fun-meta-val-by-key ~the-fun-name
                            :arglists))

(defmacro get-fun-meta-args-count [the-fun-name]
  `(count (first (get-fun-meta-val-by-key ~the-fun-name
                                          :arglists))))

(defmacro get-fun-meta-ns [the-fun-name]
  `(get-fun-meta-val-by-key ~the-fun-name
                            :ns))

(defmacro get-fun-meta-ns-sym [the-fun-name]
  `(symbol (.toString (get-fun-meta-val-by-key ~the-fun-name
                                               :ns))))

(defmacro get-fun-meta-name [the-fun-name]
  `(get-fun-meta-val-by-key ~the-fun-name
                            :name))

(defmacro get-fun-meta-line [the-fun-name]
  `(get-fun-meta-val-by-key ~the-fun-name
                            :line))

(defmacro get-fun-meta-col [the-fun-name]
  `(get-fun-meta-val-by-key ~the-fun-name
                            :column))

(clojure.core/defn parse-base-schema [input]
  (let [ret-val (first input)
        params (first (rest input))]
    [ret-val params]
    ))

(defmacro get-fun-meta-schema [the-fun-name]
  `(let [result# (get-fun-meta-val-by-key ~the-fun-name
                                          :schema)
         cooked-result# (parse-base-schema result#)
         ]
     cooked-result#
     ))

;;{:schema (=> Bool Num Num), :ns #object[clojure.lang.Namespace 0x78504ce9 hgp.cljito.datagen.fun-reader-test], :name my-test, :active.data.realm.attach/realm #active.data.realm.internal.records/function-realm{description function (number, number) -> boolean, predicate #object[clojure.core$fn_QMARK_ 0x26d8908e clojure.core$fn_QMARK_@26d8908e], metadata {}, function-realm-cases (#active.data.realm.internal.records/function-case{function-case-positional-argument-realms [#active.data.realm.internal.records/builtin-scalar-realm{description number, predicate #object[clojure.core$number_QMARK_ 0x30e2016a clojure.core$number_QMARK_@30e2016a], metadata {}, builtin-scalar-realm-id :number} #active.data.realm.internal.records/builtin-scalar-realm{description number, predicate #object[clojure.core$number_QMARK_ 0x30e2016a clojure.core$number_QMARK_@30e2016a], metadata {}, builtin-scalar-realm-id :number}], function-case-optional-arguments-realm nil, function-case-return-realm #active.data.realm.internal.records/builtin-scalar-realm{description boolean, predicate #object[clojure.core$boolean_QMARK_ 0x649f2009 clojure.core$boolean_QMARK_@649f2009], metadata {}, builtin-scalar-realm-id :boolean}})}, :file hgp/cljito/datagen/fun_reader_test.clj, :column 1, :raw-arglists ([first :- (active.data.realm.schema/schema (active.data.realm/compile realm/number)) second :- (active.data.realm.schema/schema (active.data.realm/compile realm/number))]), :line 8, :arglists ([first second]), :doc Inputs: [first :- (active.data.realm.schema/schema (active.data.realm/compile realm/number)) second :- (active.data.realm.schema/schema (active.data.realm/compile realm/number))]
;; Returns: (active.data.realm.schema/schema (active.data.realm/compile realm/boolean))}
(clojure.core/defn parse-arg-types [arg-type-defs]
  (let [descriptors (first (second (first arg-type-defs)))
        arg-type-seq (vec (map (clojure.core/fn [val]
                                 (get val :schema))
                               descriptors))
        arg-opt?-seq (vec (map (clojure.core/fn [val]
                                 (get val :optional?))
                               descriptors))
        arg-types-vect (vec (map (clojure.core/fn [val]
                                   (get val :types-vect))
                                 descriptors))
        arg-name-seq  (vec  (map (clojure.core/fn [val]
                             (name  (get val :name)))
                                   descriptors) )]               ;; correct it

    (let [fun-result {:names-vect     arg-name-seq
                      :scheme-types   arg-type-seq
                      :types-vect     arg-types-vect
                      :optional?-vect arg-opt?-seq}]
      fun-result)
    ))

(clojure.core/defn parse-meta-to-map [schema-val]
  (let [return-type (second (first schema-val))
        arg-info-map (parse-arg-types (rest schema-val))]
    [{:return-type return-type
      :arg-info-map arg-info-map}]
    ))


(clojure.core/defn structure-schema [fun-meta]
  (let [the-schema (get fun-meta :schema)
        the-ns (get fun-meta :ns)
        the-name (get fun-meta :name)
        the-line (get fun-meta :line)
        the-column (get fun-meta :column)
        argument-list (get fun-meta :argument-list)
       ]
    (let [schema-val (parse-base-schema the-schema)]
      (pprint fun-meta)
      {:recording {:base-data {:ns            the-ns
                               :name          the-name
                               :line          the-line
                               :column        the-column
                               :argument-list argument-list
                               }
       :add-on    [{:schema (parse-meta-to-map schema-val)}]}}

      )))