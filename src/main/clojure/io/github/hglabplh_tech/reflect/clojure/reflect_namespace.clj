(ns io.github.hglabplh-tech.reflect.clojure.reflect-namespace
  (:require [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.fun-reader :as fr]))

(clojure.core/defn create-meta-entry [namespace-sym keyval decompiled?]
  {(keyword (str (key keyval)))
   (if decompiled?
     (fr/get-decompiled-meta (meta (val keyval)))
     (meta (val keyval)))})

(clojure.core/defn get-ns-interns [namespace-sym decompiled?]
  (let [the-namespace (the-ns namespace-sym)
        ns-intern-meta-map (mapv (fn [entry]
                                   (create-meta-entry namespace-sym entry decompiled?))
                                 (ns-interns namespace-sym))]
    (println "Namespace object: ")
    (println namespace-sym)
    (println the-namespace)
    (println "Namespace meta ??: ->")
    (println (ns-aliases the-namespace))
    (println (ns-refers the-namespace))
    (println "NS INTERN")
    {:language :clojure :ns-name (str namespace-sym) :reflect-data ns-intern-meta-map} ))

(clojure.core/defn get-ns-publics [namespace-sym decompiled?]
  (let [the-namespace (the-ns namespace-sym)
        ns-public-meta-map (mapv (fn [entry]
                                   (create-meta-entry namespace-sym entry decompiled?))
                                 (ns-publics the-namespace))
        ]

    (println "Namespace object: ")
    (println namespace-sym)
    (println the-namespace)
    (println "Namespace meta ??: ->")
    (println (ns-aliases the-namespace))
    (println (ns-refers the-namespace))
    (println "NS PUBLICS")
    {:language :clojure :ns-name (str namespace-sym) :reflect-data ns-public-meta-map}))



(clojure.core/defmacro get-intern-defs [namespace-sym decompiled?]
  (require [namespace-sym :refer :all] :use)
  `(do
     ~@(get-ns-interns namespace-sym decompiled?)))

(clojure.core/defmacro get-public-defs [namespace-sym decompiled?]
  (require [namespace-sym :refer :all] :use)
  `(do
     ~@(get-ns-publics namespace-sym decompiled?)))

