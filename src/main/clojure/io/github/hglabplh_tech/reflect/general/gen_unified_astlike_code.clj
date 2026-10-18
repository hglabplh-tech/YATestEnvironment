;; Copyright (c) 2026 Harald Glab-Plhak

(ns io.github.hglabplh-tech.reflect.general.gen-unified-astlike-code)

(defn clj-parse-fun-specs [args-list pr-schema act-data]
  (let [arglist (first args-list)
        positional-args (take-while #(not= % '&) arglist)
        named-destruc (first (rest (drop-while #(not= % '&) arglist)))
        named-keys (get named-destruc :keys)
        defaults (get named-destruc :or)
        return-type (get pr-schema :return-type)
        arg-infos (get pr-schema :arg-infos)]

    (conj
      {:return-type return-type}
      ;; Build Positional Maps
      {:parameter-types (mapv (clojure.core/fn [param-element]
                                (let [s-type (get param-element :schema-type)
                                      p-name (get param-element :name)
                                      optional? (get param-element :optional?)]
                                  {:name      p-name
                                   :type      (if (map? s-type) :mapped :standalone)
                                   :datatype  s-type        ;; has to be parsed before in fun_reader
                                   :optional? optional?
                                   :add-on    nil}))
                              arg-infos)})
    ))

(defn cloj-transform-fun-spec [base-data add-on]
  (let [documentation (get base-data :doc)
        fun-name (get base-data :name)
        line-no (get base-data :line)
        col-no (get base-data :column)
        args-list (get base-data :argument-list)
        pr-schema (get add-on :schema)
        act-data (get add-on :active-data)]
    {:fun-name   fun-name
     :param-defs (clj-parse-fun-specs args-list pr-schema act-data)
     :cloj-addon
     {:line   line-no
      :column col-no
      :doc    documentation}
     })
  )

(defn clojure-transform [namespace reflect-data]
  (let [content-keys (mapv (fn [value] (first (keys value))) reflect-data)
        content-vals (mapv (clojure.core/fn [value]
                             (first (vals value))) reflect-data)
        transformed (mapv (fn [raw-value]
                            (let [value-of-struct (get raw-value :structured-meta)
                                  base-meta (get value-of-struct :base-data)
                                  add-on (get value-of-struct :add-on)]
                              (cloj-transform-fun-spec base-meta add-on))) content-vals)]
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

