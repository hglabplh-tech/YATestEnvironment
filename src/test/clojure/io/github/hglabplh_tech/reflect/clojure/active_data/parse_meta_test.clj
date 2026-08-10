(ns  io.github.hglabplh-tech.reflect.clojure.active-data.parse-meta-test
  (:refer-clojure :exclude [def defn fn])
  (:require [active.data.realm :as realm]
    [clojure.pprint :refer [pprint]]
            [clojure.test :refer :all]
            [active.data.realm.attach :refer :all]
            [active.data.realm.schema :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.code.clojure.json-gen :as json]
            [io.github.hglabplh-tech.reflect.clojure.fun-reader :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.active-data.parse-meta :refer :all]
            )
  )


(deftest function_easy_compile_ad_schema (testing "The conversion of the active data - meta-schema records to structured output / easy test"
                                           (try
                                              (let [m-data (decompile-meta io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns my-easy-test)]
                                                (println "test the fun call ->")
                                                (my-easy-test "Hallo " (gensym "any") 68 98)
                                                (println "================================ the decompile result======================")
                                                (println (json/to-json m-data))))
                                              (catch Exception e
                                                (.printStackTrace e)
                                                )))

(deftest function_my_set_compile_ad_schema (testing "The conversion of the active data - meta-schema records to structured output / set realm"
                                           (try
                                             (let [m-data (decompile-meta io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns my-set-test)]
                                               (println "test the fun call ->")
                                               (println (my-set-test 22 #{1 2 3}))
                                               (println "================================ the decompile result======================")
                                               (pprint m-data)))
                                           (catch Exception e
                                             (.printStackTrace e)
                                             )))

(deftest function_my_enum_compile_ad_schema (testing "The conversion of the active data - meta-schema records to structured output / enum realm"
                                             (try
                                               (let [m-data (decompile-meta io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns my-enum-test)]
                                                 (println "test the fun call ->")
                                                 (println (my-enum-test 22 :five))
                                                 (println "================================ the decompile result======================")
                                                 (pprint m-data)))
                                             (catch Exception e
                                               (.printStackTrace e)
                                               )))

(deftest function_my_complex_compile_ad_schema (testing "The conversion of the active data - meta-schema records to structured output / complex enum
                                                       set of and function and scalar"
                                              (try
                                                (let [m-data (decompile-meta io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns
                                                                             my-complex-test)]
                                                  (println "================================ the decompile result======================")
                                                  (println (json/to-json m-data))))
                                              (catch Exception e
                                                (.printStackTrace e)
                                                )))

(deftest function_my_optional_compile_ad_schema (testing "The conversion of the active data - meta-schema records to structured output / optional argument"
                                                 (try
                                                   (let [m-data (decompile-meta io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns
                                                                                string->integer-with-contract)]
                                                     (println "================================ the decompile result======================")
                                                     (println (json/to-json m-data))))
                                                 (catch Exception e
                                                   (.printStackTrace e)
                                                   )))

(run-tests)
