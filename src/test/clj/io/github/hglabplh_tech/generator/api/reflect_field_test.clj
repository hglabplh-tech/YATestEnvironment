(ns io.github.hglabplh-tech.reflect.clojure.api.reflect-field-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-class :as rcl]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-field :as rfield]))



(deftest reflect.fields.test
  (testing "Here the reflection of fields in a class is tested"
    (let [spec-clazz-refl (rcl/get-class-util
                            "io.github.hglabplh_tech.reflect.clojure.api.app_exam.Application")
          all-fields (rcl/get-all-fields spec-clazz-refl)]
       (pprint (map rfield/get-all-fields-and-type-modifiers all-fields))
       )))
(run-tests)