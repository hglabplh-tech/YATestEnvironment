(ns io.github.hglabplh-tech.reflect.general.java-to-unified
  (:import (io.github.hglabplh_tech.reflect.general ClojFunctionalUtils)
           (java.lang.reflect AnnotatedType
                              GenericArrayType
                              ParameterizedType
                              Type
                              TypeVariable)))
(defn java-transform-fun-spec [base-data add-on]
  (let [class-def (:classdef base-data)]
    ))
(defn java-transform [namespace reflect-data]
  (let [content-keys (mapv (fn [value] (first (keys value))) reflect-data)
        content-vals (mapv (clojure.core/fn [value]
                             (first (vals value))) reflect-data)
        transformed (mapv (fn [raw-value]
                            (let [value-of-struct (get raw-value :structured-meta)
                                  base-data (get value-of-struct :base-data)
                                  add-on (get value-of-struct :add-on)]
                              (java-transform-fun-spec base-data add-on))) content-vals)]
    {:language :java :namespace-class namespace :function-class-catalog content-keys :fun-specs transformed}
    ))