(ns io.github.hglabplh-tech.reflect.clojure.detailed-cloj-reflect
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.general.gen-unified-astlike-code :as uni]
            [io.github.hglabplh-tech.reflect.clojure.reflect-namespace :as reflns]
            [io.github.hglabplh-tech.reflect.clojure.fun-reader :as fr]
            [io.github.hglabplh-tech.reflect.examples.clojure.complex.mixed-defs :refer :all]))


(deftest type-hints-fun (testing "detailed test of a function with type hints and optional param something like (defn.... [parm & rest])"
                           (let [fun-meta (meta (var io.github.hglabplh-tech.reflect.examples.clojure.complex.mixed-defs/typehint-more))
                                 result (fr/really-get-meta 'io.github.hglabplh-tech.reflect.examples.clojure.complex.mixed-defs
                                                            'typehint-more)]
                             (pprint fun-meta)
                             (pprint result)
                             )))
(deftest namespace-trace-with-deep-check (testing "detailed test of a function with type hints and optional param something like (defn.... [parm & rest])"
                          (let [result (reflns/get-ns-publics 'io.github.hglabplh-tech.reflect.examples.clojure.complex.mixed-defs)]
                            (pprint result)
                            (println "======== final =======")
                            (pprint (uni/transform-to-unified result))
                            )))

(deftest cloj-spec-fun (testing "detailed test of a function with clojure.spec"

           ))