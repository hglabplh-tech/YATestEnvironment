(ns io.github.hglabplh-tech.reflect.clojure.api.packages
  (:import (java.util Set)
           (java.lang Package)
           (org.reflections Reflections)
           (org.reflections.scanners Scanners)
           (org.reflections.util ConfigurationBuilder FilterBuilder)
           (io.github.hglabplh_tech.reflect.clojure.api.utils GetPackagesAndClasses)))


;; rewrite this using the classpath namespace and
;; build the new logic in class-path
(defn list-packages
  "this function lists the content of a given package by name"
  {:added "0.9.0"
   :static true}
  [& pkg-names]
  (let [pkg-jlist (map GetPackagesAndClasses/findAllClassesUsingClassLoader pkg-names)]
    pkg-jlist
    ))

