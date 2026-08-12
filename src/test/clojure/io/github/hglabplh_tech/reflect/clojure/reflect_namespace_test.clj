(ns io.github.hglabplh-tech.reflect.clojure.reflect-namespace-test
  (:refer-clojure :exclude [def defn fn])
  (:require [clojure.test :refer :all]
    [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.reflect-namespace :as reflns]))

;;(reflns/get-intern-defs io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns true)

(deftest simple-public-ns-reflect
  (testing "Reflect a namespace without decompile"
    (reflns/get-public-defs io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns false)))


(deftest simple-intern-ns-reflect
  (testing "Reflect a namespace without decompile"
    ;; (reflns/get-intern-defs io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns false)
    ))

(deftest decomp-public-ns-reflect
  (testing "Reflect a namespace without decompile"
    ;;(reflns/get-public-defs io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns true)
    ))


(deftest decomp-intern-ns-reflect
  (testing "Reflect a namespace without decompile"
    ;;(reflns/get-intern-defs io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns true)
    ))


(run-tests)