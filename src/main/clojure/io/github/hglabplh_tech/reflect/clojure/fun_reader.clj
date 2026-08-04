(ns io.github.hglabplh-tech.reflect.clojure.fun-reader
  (:refer-clojure :exclude [def defn fn])
  (:require [clojure.contrib.fcase :refer [fcase]]
            [io.github.hglabplh_tech.test.suite.static-code.analysis.reflect-code]
            [schema.core :as schema]
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
            [active.data.realm.internal.record-meta :as rec-meta]
            [active.data.realm.attach :as attach]
            [io.github.hglabplh_tech.test.suite.spy-and-mock.real-fun-checkers :as fc]
            [io.github.hglabplh-tech.reflect.clojure.analyze.fun-analyzer :refer :all]))

(clojure.core/defn printout_excp [excp]
  (println (.getMessage excp))
  (.printStackTrace excp))



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

(clojure.core/defn really-get-meta [namespace-sym record-sym]
  (let [ns-intern-map (ns-interns namespace-sym)
        rec (get ns-intern-map record-sym)]
    (if-not (or (nil? ns-intern-map)
                (empty? ns-intern-map))
      (let [meta-data (meta rec)]
        (try
          (let [
                decompiled (fc/get-decompiled-active-data meta-data)])
           (fc/get-decompiled-active-data meta-data)
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

(defmacro decompile-meta [ns-in obj]
  `(do ~@(really-get-meta ns-in obj)))