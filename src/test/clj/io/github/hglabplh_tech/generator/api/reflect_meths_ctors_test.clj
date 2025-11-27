(ns io.github.hglabplh-tech.reflect.clojure.api.reflect-meths-ctors-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-class :as rcl]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-meths-ctors :as mctor]))



(deftest reflect.methods.test
  (testing "Here the reflection of fields in a class is tested"
    (let [spec-clazz-refl (rcl/get-class-util
                            "io.github.hglabplh_tech.reflect.clojure.api.app_exam.Application")
          all-meths (rcl/get-all-methods spec-clazz-refl)]
      (pprint (map mctor/get-all-meth-and-type-modifiers all-meths))
      (pprint (map mctor/get-meth-param-types all-meths))
      (pprint (map mctor/get-method-return-type all-meths))
      )))

(deftest reflect.ctor.test
  (testing "Here the reflection of fields in a class is tested"
    (let [spec-clazz-refl (rcl/get-class-util
                            "io.github.hglabplh_tech.reflect.clojure.api.app_exam.Application"
                            )
          all-ctors (rcl/get-all-ctors spec-clazz-refl)]
      (println "Here are the ctor results")
      (pprint (map mctor/get-all-ctor-and-type-modifiers all-ctors))
      (pprint (map mctor/get-ctor-param-types all-ctors))
      )))


(run-tests)