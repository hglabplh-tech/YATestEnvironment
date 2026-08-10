(ns io.github.hglabplh-tech.reflect.clojure.reflect-namespace
  (:require  [clojure.pprint :refer :all]))

(clojure.core/defn get-ns-interns [namespace-sym]

  (let [search-space (find-ns namespace-sym)
        ns-intern-map (ns-interns search-space)
        ]
    (pprint ns-intern-map)))

(clojure.core/defmacro get-all-defs [namespace-sym]
  `(do  ~@(require [namespace-sym :refer :all])
     ~@(get-ns-interns namespace-sym)))

(get-all-defs io.github.hglabplh-tech.reflect.clojure.fun-reader)