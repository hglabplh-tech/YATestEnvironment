(ns io.github.hglabplh-tech.reflect.java.api.misc-tests
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.java.api.java-reflector :as jrefl]
            ))

(deftest bad-designed-rec (testing "a bad designed record with misc inner classes"
                            (println (System/getProperty "java.class.path"))
                            (let [result (jrefl/get-structured-meta-data
                                           "io.github.hglabplh_tech.reflect.java.api.example.annot_rec.PoorRecordDesign")]
                              (pprint result)
                            )))

(deftest bad-designed-enum (testing "a bad designed enum with non-private interfaces or inner classes"
                            (println (System/getProperty "java.class.path"))
                            (let [result (jrefl/get-structured-meta-data
                                           "io.github.hglabplh_tech.reflect.java.api.example.annot_rec.OrderStatus")]
                              (pprint result)
                              )))

(deftest bad-designed-annot (testing "a bad designed annotation with methods containing logic or inner classes containing logic"
                             (println (System/getProperty "java.class.path"))
                             (let [result (jrefl/get-structured-meta-data
                                            "io.github.hglabplh_tech.reflect.java.api.example.annot_rec.MethodDescriberBad")]
                               (pprint result)
                               )))

