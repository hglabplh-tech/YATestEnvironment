(ns io.github.hglabplh-tech.reflect.clojure.api.reflect-annotation-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-class :as rcl]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-annotation :as annot])

  )

(def spec-clazz-refl  (rcl/get-class-util
                        "io.github.hglabplh_tech.reflect.clojure.api.app_exam.Application"))
(deftest annot.class.test
  (testing "class annotation test"
    (let [the-class   (.getTheClass spec-clazz-refl)]
      (pprint (annot/get-class-annots the-class))
    )))

(deftest annot.ctor.test
  (testing "constructor annotation test"
    (let [the-ctor-arr   (rcl/get-all-ctors spec-clazz-refl)
          ]
      (pprint (map annot/get-ctor-annots the-ctor-arr))
      (pprint (map annot/get-ctor-param-annots the-ctor-arr))
      )))

(deftest annot.meth.test
  (testing "method annotation test"
    (let [the-meth-arr   (rcl/get-all-methods spec-clazz-refl)
          ]
      (pprint (map annot/get-method-annots the-meth-arr))
      (pprint (map annot/get-meth-param-annots the-meth-arr))
      (pprint (map annot/get-annot-return-type the-meth-arr))
      )))

(deftest annot.field.test
  (testing "field annotation test"
    (let [the-field-arr   (rcl/get-all-fields spec-clazz-refl)
          ]
      (pprint (map annot/get-field-annots the-field-arr))
      )))



(run-tests)