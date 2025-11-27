(ns io.github.hglabplh-tech.reflect.clojure.api.reflect-class-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-field :as rfield]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-class :as rcl]))

(deftest get-class.test
  (testing "getting a class of given packages"
    (let [spec-clazz-refl (rcl/get-class-util
              "io.github.hglabplh_tech.reflect.clojure.api.app_exam.Application")
          reflected-clazz (rcl/get-the-class spec-clazz-refl)
          fields-vect  (rcl/get-all-fields spec-clazz-refl)
          fld-vect-type (apply
                          rfield/get-all-fields-and-type-modifiers
                          fields-vect)
          clazz-attrs (rcl/get-class-attributes reflected-clazz)]
        (pprint fields-vect)
        (pprint fld-vect-type)
        (pprint clazz-attrs)
        )))

(run-tests)
