(ns io.github.hglabplh-tech.reflect.clojure.fun-reader
  (:refer-clojure :exclude [def defn fn])
  (:require [active.data.realm.attach :refer :all]
            [active.data.realm.attach :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.analyze.fun-analyzer :refer :all]
            [io.github.hglabplh_tech.test.suite.spy-and-mock.real-fun-checkers :as fc]
            [io.github.hglabplh_tech.test.suite.static-code.analysis.reflect-code]))

(clojure.core/defn printout_excp [excp]
  (println (.getMessage excp))
  (.printStackTrace excp))

(clojure.core/defn structure-out-meta [meta-data]
  "do it think about a well formed struct "
  (let [schema-data-part (fc/get-structured-meta-data meta-data)]
    schema-data-part
    ))

(clojure.core/defn get-schema-structured [namespace-sym function-sym]
  (let [ns-intern-map (ns-interns namespace-sym)
        fun (get ns-intern-map function-sym)]
    (if-not (or (nil? ns-intern-map)
                (empty? ns-intern-map))
      (let [meta-data (meta fun)]
        (structure-out-meta meta-data)))))

(clojure.core/defn really-get-meta [namespace-sym fun-sym]
  (let [ns-intern-map (ns-interns namespace-sym)
        the-fun (get ns-intern-map fun-sym)]
    (if-not (or (nil? ns-intern-map)
                (empty? ns-intern-map))
      (let [meta-data (meta the-fun)]
        (try
          (let [decompiled (fc/get-structured-meta-data meta-data)]
            decompiled)
          (catch Throwable excp
            (printout_excp excp)
            ))
        ;;(get-rec-meta meta-data)
        ))))

(clojure.core/defn get-decompiled-meta [meta-data]
  (try
    (let [decompiled (fc/get-structured-meta-data meta-data)]
      decompiled)
    (catch Throwable excp
      (printout_excp excp)
      ))
  ;;(get-rec-meta meta-data)
  )


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
                          (require [ns-in :refer :all])
                          (get-schema-structured ns-in obj))
                        objects
                        )]
     (analyze-fun result#)))

(clojure.core/defmacro decompile-meta [ns-in obj]
  `(do ~@(require [ns-in :refer :all])
       ~@(really-get-meta ns-in obj)))