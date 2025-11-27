(ns io.github.hglabplh-tech.reflect.clojure.general.cloj-java-represent-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-class :as rcl]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-meths-ctors :as mctor]
            [io.github.hglabplh-tech.reflect.clojure.general.cloj-java-represent :as present]
            )
  (:import (java.util Optional)))

(defn is-opt-present [^Optional opt]
  (.isPresent opt))

(defn get-opt [^Optional opt]
  (.get opt))
(deftest jtype-presentation-test
  (testing "Java Reflect Type Clojure representation test"
    (let [reflected-cls (rcl/get-class-util
                          "io.github.hglabplh_tech.reflect.clojure.api.example.MyTuple")
          found-meth (rcl/get-method-by-name reflected-cls "value")
          param-represent (if (is-opt-present found-meth)
                       (mctor/get-generic-method-return-type
                         (get-opt found-meth))
                       [])]
      (is (is-opt-present found-meth) "method not found")
      (pprint (get-opt found-meth))
      (pprint param-represent)
      )))
