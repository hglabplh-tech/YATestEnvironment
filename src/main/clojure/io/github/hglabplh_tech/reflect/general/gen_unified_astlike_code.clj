;; Copyright (c) 2026 Harald Glab-Plhak

(ns io.github.hglabplh-tech.reflect.general.gen-unified-astlike-code
  (:require [io.github.hglabplh-tech.reflect.clojure.active-data.parse-meta :as pactd]
            [clojure.pprint :refer :all]))
(clojure.core/defn decompile-if [act-data]
  (if (pactd/active-data-realm? act-data)
    (pactd/decompile-active-rec act-data)
    act-data
    ))

(defn get-active-ret-and-param [active-data]
  (let [cases-def (:function-cases-def active-data)
        case-def (:function-case-def (first cases-def))]

  case-def
  ))
(defn clj-parse-fun-specs [args-lists pr-schema act-data]
  (let [
        return-type (get pr-schema :return-type)
        arg-infos (:arg-infos args-lists)
        case-def (get-active-ret-and-param act-data)
        positional-params (:positional-parameters args-lists)
        named-params (:positional-parameters args-lists)
        return-def (:ret-val-def case-def)
        pos-arg-def (:pos-arg-def case-def)
        opt-arg-def (:opt-arg-def case-def)]

    (conj
      {:return-type {:base (:return-type-hint args-lists) :prschema return-type :cloj-specs (:return-spec args-lists) :active-data return-def}}

      ;; Build Positional Maps
      {:parameter-types {:base (conj  {:positional-params positional-params}
                                      {:named-params named-params})
                         :cloj-add-on {:prschema    (mapv (clojure.core/fn [param-element]
                                                            (let [s-type (get param-element :schema-type)
                                                                  is-map? (map? s-type)
                                                                  type (if is-map? :mapped :positional)
                                                                  datatype (if is-map? (update-vals s-type decompile-if) s-type)
                                                                  p-name (get param-element :name)
                                                                  optional? (get param-element :optional?)]
                                                              {:name      p-name
                                                               :type      type
                                                               :datatype  datatype ;; has to be parsed before in fun_reader
                                                               :optional? optional?
                                                               :add-on    nil}))
                                                          arg-infos)
                                       :active-data { :pos-arg-def pos-arg-def
                                                      :opt-arg-def opt-arg-def}
                                       :cloj-specs args-lists}}})
    ))

(defn cloj-transform-fun-spec [base-data add-on]
  (let [documentation (get base-data :doc)
        fun-name (get base-data :name)
        line-no (get base-data :line)
        col-no (get base-data :column)
        args-types-list (get base-data :args-types-list )
        pr-schema (get add-on :schema)
        act-data (get add-on :active-data)]
    ;;cloj-specs (get add-on :cloj-specs)]

    {:fun-name            fun-name
     :function-definition (clj-parse-fun-specs args-types-list pr-schema act-data)
     :cloj-addon
     {:line      line-no
      :column    col-no
      :doc       documentation}
     })
  )

(defn clojure-transform [namespace reflect-data]
  (let [content-keys (mapv (fn [value] (first (keys value))) reflect-data)
        content-vals (mapv (clojure.core/fn [value]
                             (first (vals value))) reflect-data)
        transformed (mapv (fn [raw-value]
                            (let [value-of-struct (get raw-value :structured-meta)
                                  base-data (get value-of-struct :base-data)
                                  add-on (get value-of-struct :add-on)]
                              (cloj-transform-fun-spec base-data add-on))) content-vals)]
    {:language :clojure :namespace-class namespace :function-class-catalog content-keys :fun-specs transformed}
    ))

(defn transform-to-unified [reflect-input]
  (let [lang (get reflect-input :language)]
    (cond
      (= lang :clojure)
      (clojure-transform (get reflect-input :ns-name) (get reflect-input :reflect-data))
      :else
      (println "Not yet here")
      )))

