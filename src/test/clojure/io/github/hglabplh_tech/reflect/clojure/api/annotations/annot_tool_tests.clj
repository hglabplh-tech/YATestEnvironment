(ns io.github.hglabplh_tech.reflect.clojure.api.annotations.annot-tool-tests
  (:require [clojure.test :refer :all]
            [clojure.pprint :as pp]
            [clojure.pprint :refer :all]
            [io.github.hglabplh_tech.reflect.clojure.api.reflect-class :as cls]
            [io.github.hglabplh_tech.reflect.clojure.api.reflect-annotation :as refla]
            [io.github.hglabplh_tech.reflect.clojure.api.annotations.annot-tool :as tool])
  (:import (java.lang Deprecated)))

(def smoke-result {:annotation-type "java.lang.Deprecated",
                   :fields-values {},
                   :methods-values
                   {:meth-since "v1.78", :meth-for-removal true}})



(deftest smoke.test.get-vals
  (testing "the retrieve functionality for annotation values"
    (let [cls-util (cls/get-class-util
                  "io.github.hglabplh_tech.reflect.clojure.api.app_exam.Application")
          ctors (cls/get-public-ctors cls-util)
          annotations (refla/get-ctor-annots (first ctors))
          annot (get annotations 0)
          annot-as-cloj  (tool/get-annotation-data-vals annot)]
    (pp/pprint annot-as-cloj)

    (is (= annot-as-cloj smoke-result))
    )
    ))

(deftest expanded.test.get-vals
  (testing "the expanded retrieve functionality for annotation values"
    (let [cls-util (cls/get-class-util
                     "io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec.AnnotTestClass")
          all-methods (cls/get-all-methods cls-util)
          all-fields (cls/get-all-fields cls-util)
          meth-annots-as-cloj (tool/get-annotation-values-from-method (first all-methods))
          methods-annots-as-cloj   (tool/get-annotation-values-from-class-methods all-methods)
          fld-annots-as-cloj (tool/get-annotation-values-from-field (first all-fields))
          fields-annots-as-cloj   (tool/get-annotation-values-from-class-fields all-fields)
          ]
      (pp/pprint meth-annots-as-cloj)
      (pp/pprint methods-annots-as-cloj)
      (pp/pprint fld-annots-as-cloj)
      (pp/pprint fields-annots-as-cloj)
      ;;(is (= annot-as-cloj expanded-res))
      )
    ))

(deftest annotation.annot.reflect
  (testing "reflect the annotation directly"

    (let [cls-util (cls/get-the-class (cls/get-class-util
                     "io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec.MethodDescriber"))
          annot-res (tool/get-annotation-info cls-util)
          ]
      (pprint annot-res)

    )))

(run-tests)