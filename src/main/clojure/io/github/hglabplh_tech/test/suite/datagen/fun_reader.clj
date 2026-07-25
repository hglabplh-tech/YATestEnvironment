(ns io.github.hglabplh_tech.test.suite.datagen.fun-reader
  (:refer-clojure :exclude [def defn fn])
(:require [io.github.hglabplh_tech.test.suite.static-code.analysis.reflect-code]  [schema.core :as schema]
          [active.data.realm.attach :refer :all]
          [active.data.realm.inspection :as ad-inspection]
          [clojure.pprint :refer :all]
          [schema.core :as schema]
          [active.data.record :as sut]
          [io.github.hglabplh_tech.test.suite.spy-and-mock.real-fun-checkers :as fc]
          [io.github.hglabplh_tech.test.suite.datagen.active-data.parse-meta :as pactd]
          [io.github.hglabplh_tech.test.suite.datagen.analyze.fun-analyzer :refer :all]))



(clojure.core/defn get-rec-meta [raw-meta]
  
  (let [meta-datum (do (println "the meta: " raw-meta )
                  (println "the type: " (type raw-meta)
                           "is realm: " (ad-inspection/realm? raw-meta)))
        parsed-meta  (pactd/select-meta-rec meta-datum)
        ]
    parsed-meta
    ))

(clojure.core/defn structure-out-meta [meta-data]
   "do it think about a well formed struct "
  (let [schema-data-part (fc/structure-schema meta-data)]
    schema-data-part
    ))

(clojure.core/defn get-schema-structured [namespace-sym function-sym]
  (let [ns-intern-map (ns-interns namespace-sym)
        fun (get ns-intern-map function-sym)]
    (if-not (or (nil? ns-intern-map)
                 (empty? ns-intern-map))
      (let [meta-data (meta fun)]
        (structure-out-meta meta-data)))))

(clojure.core/defn get-meta-raw [namespace-sym record-sym]
  (let [ns-intern-map (ns-interns namespace-sym)
        rec (get ns-intern-map record-sym)]
    (if-not (or (nil? ns-intern-map)
                (empty? ns-intern-map))
      (let [meta-data (meta rec)]
        (get-rec-meta meta-data)
        ))))

(defmacro schema-struct [ns-in & objects]
  `(do ~@(map (clojure.core/fn [obj]
               (get-schema-structured ns-in obj))
             objects
             ))
      )

(defmacro analyze-struct [ns-in & objects]
  `(let [result# ~@(map (clojure.core/fn [obj]
                         (get-schema-structured ns-in obj))
                       objects
                       )]
     (analyze-fun result#)))

(defmacro meta-raw [ns-in obj]
  `(do
      ~@(get-meta-raw ns-in obj)
      ))