(ns io.github.hglabplh-tech.reflect.clojure.api.convert-java-cloj-tests
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-class :as rcl]
            [io.github.hglabplh-tech.reflect.clojure.api.convert-java-cloj :as cjc]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-special-forms :as enrecla]))

(deftest java-cloj.convert.enum.test
  (testing "base test for the different functions to convert from java
            presentation to clojure "
    (let [class-util (rcl/get-class-util
                       "io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec.Category")
          enum-result (enrecla/analyze-enum (rcl/get-the-class class-util))]
          (pprint enum-result)
      )))

(run-tests)
