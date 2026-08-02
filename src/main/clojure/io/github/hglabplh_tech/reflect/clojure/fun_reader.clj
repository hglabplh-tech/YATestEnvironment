(ns io.github.hglabplh-tech.reflect.clojure.fun-reader
  (:refer-clojure :exclude [def defn fn])
(:require [io.github.hglabplh_tech.test.suite.static-code.analysis.reflect-code]  [schema.core :as schema]
          [active.data.realm.attach :refer :all]
          [active.data.realm.inspection :as realm-inspection]
          [active.data.struct.internal.closed-struct-map :as struct-map]
          [active.data.struct :as struct]
          [active.data.raw-record :as record]
          [active.data.realm.internal.record-meta :as recm]
          [clojure.pprint :refer :all]
          [active.data.realm :as realm]
          [schema.core :as schema]
          [active.data.realm.attach :refer :all]
          [active.data.record :as sut]
          [active.data.realm.attach :as attach]
          [io.github.hglabplh_tech.test.suite.spy-and-mock.real-fun-checkers :as fc]
          [io.github.hglabplh-tech.reflect.clojure.active-data.parse-meta :as pactd]
          [io.github.hglabplh-tech.reflect.clojure.analyze.fun-analyzer :refer :all]))

(clojure.core/defn printout_excp [excp]
  (println (.getMessage excp))
  (.printStackTrace excp))

(clojure.core/defn get-rec-meta [raw-meta]
  (let [dummy (do (println "the meta: " raw-meta )
                  (println "the type: " (type raw-meta)
                           "is record: " (active.data.raw-record/record? raw-meta)))
        active-meta (get raw-meta attach/fn-realm-meta-key)
        ;;parsed-meta  (pactd/active-rec-to-schema  active-meta) ;; was select.... before / from parse_meta.clj
        parsed-meta  (pactd/decompile-active-rec active-meta)
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
        (try
           (get-rec-meta meta-data)
           (catch Throwable excp
             (printout_excp excp)
             ))
        ;;(get-rec-meta meta-data)
        ))))


(clojure.core/defn get-meta-full [namespace-sym record-sym]
  (let [ns-intern-map (ns-interns namespace-sym)
        rec (get ns-intern-map record-sym)]
    (if-not (or (nil? ns-intern-map)
                (empty? ns-intern-map))
      (let [meta-data (meta rec)]
       meta-data
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
  (let [result `(do ~@(get-meta-raw ns-in obj)) ]
      result
      ))