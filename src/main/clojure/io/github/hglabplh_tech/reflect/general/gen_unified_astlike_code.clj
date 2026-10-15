;; Copyright (c) 2026 Harald Glab-Plhak

(ns io.github.hglabplh-tech.reflect.general.gen-unified-astlike-code
  (:require [io.github.hglabplh-tech.reflect.general.unified-struct-def :as defs])
  )

(defn cloj-transform-value [base-data add-on]
  (let [ns-name (get base-data :ns)
        documentation (get base-data :doc)
        line-no (get base-data :line)
        arg-list (get base-data :argument-list)]
    )
  )

(defn clojure-transform [namespace reflect-data]
  (let [content-keys (mapv (fn [value] (first (keys value))) reflect-data)
        content-vals (mapv (clojure.core/fn [value]
                             (first (vals value))) reflect-data)
        transformed (mapv (fn [raw-value]
                            (let [value-of-struct (get raw-value :structured-meta)
                                  base-meta (get value-of-struct :base-data)
                                  add-on (get value-of-struct :add-on)]
                              (cloj-transform-value base-meta add-on))) content-vals)]
    (conj {:language :clojure :namespace-class namespace :function-class-catalog content-keys } transformed)
    ))

(defn transform-to-unified [reflect-input]
  (let [lang (get reflect-input :language)]
    (cond
      (= lang :clojure)
      (clojure-transform (get reflect-input :ns-name) (get reflect-input :reflect-data))
      :else
      (println "Not yet here")
      )))

