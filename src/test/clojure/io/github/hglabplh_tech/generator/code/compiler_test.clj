(ns io.github.hglabplh_tech.reflect.clojure.code.compiler-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh_tech.reflect.clojure.code.ast-table-defs :as tdefs]
            [io.github.hglabplh_tech.reflect.clojure.code.compile.compiler :as comp]
            [io.github.hglabplh_tech.reflect.clojure.code.comp-test-data :as data]
            [io.github.hglabplh_tech.reflect.clojure.api.reflect-field :as rfield]
            [io.github.hglabplh_tech.reflect.clojure.api.reflect-class :as rcl] ))

(deftest simple-class-compile
  (testing "This simple test is to smoke test / check if compile works"
    (let  [comp-result (comp/compile-class
      "io.github.hglabplh_tech.reflect.clojure.api.example.InterfaceImplBaseAbstr")]
      (pprint comp-result)
    )))

(deftest more-cmplx-class-compile
  (testing "This more complex test is to smoke test / check if compile works"
    (let  [comp-result (comp/compile-class
                         "io.github.hglabplh_tech.reflect.clojure.api.example.InterfaceImplBase"
                         )]
      (pprint comp-result)
      )))

(deftest enum-class-compile
  (testing "This is a test for enum classes / check if compile works"
    (let [comp-result
          (comp/compile-class
            "io.github.hglabplh_tech.reflect.clojure.api.app_exam.AppConfigFields")]

      (pprint comp-result)
      ;;      (is (= class-frag (get (get (get data/enum-class-header-data :compilation)
      ;;                      :class      ) :enum-specs)))
      )))

(deftest record-class-compile
  (testing "This is a test for record classes / check if compile works"
    (let  [comp-result
           (comp/compile-class
             "io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec.MyTestRecord"
             )]
      (pprint comp-result)
      )))


(deftest lambda-class-compile
  (testing "This is a test for lambda classes / check if compile works"
    (let  [comp-result
           (comp/compile-class
             "io.github.hglabplh_tech.reflect.clojure.api.example.lambda.LambdaCollect"
             )]
      (pprint comp-result)
      )))

(deftest synthetic-class-compile
  (testing "This is a test for lambda classes / check if compile works"
    (let  [comp-result
           (comp/compile-class
             "io.github.hglabplh_tech.reflect.clojure.api.example.special.CaseInner"
             )]
      (pprint comp-result)
      )))

(run-tests)
