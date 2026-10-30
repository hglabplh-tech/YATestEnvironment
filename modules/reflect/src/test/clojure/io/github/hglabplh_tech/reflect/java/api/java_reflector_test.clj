(ns io.github.hglabplh-tech.reflect.java.api.java-reflector-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.java.api.java-reflector :as jrefl]))


(deftest simple-reflector-test (testing "A simple java reflection of a small class"
                                 (println (System/getProperty "java.class.path"))
                                 (let [result (jrefl/get-structured-meta-data "io.github.hglabplh_tech.reflect.java.api.app_exam.Application")]
                                   (pprint result))))