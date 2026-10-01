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
                                              (let [m-data (really-get-meta 'io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns 'my-easy-test)]
                                                (println "================================ the decompile result======================")
                                                (pprint m-data))
                                              (catch Exception e
                                                (.printStackTrace e)
                                                ))))

(deftest function_my_set_compile_ad_schema (testing "The conversion of the active data - meta-schema records to structured output / set realm"
                                           (try
                                             (let [m-data (really-get-meta 'io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns 'my-set-test)]
                                               (println "================================ the decompile result======================")
                                               (pprint m-data)))
                                           (catch Exception e
                                             (.printStackTrace e)
                                             )))

(deftest function_my_enum_compile_ad_schema (testing "The conversion of the active data - meta-schema records to structured output / enum realm"
                                             (try
                                               (let [m-data (really-get-meta 'io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns 'my-enum-test)]
                                                 (println "================================ the decompile result======================")
                                                 (pprint m-data)
                                                 ))
                                             (catch Exception e
                                               (.printStackTrace e)
                                               )))

(deftest function_my_complex_compile_ad_schema (testing "The conversion of the active data - meta-schema records to structured output / complex  realms"
                                              (try
                                                (let [m-data (really-get-meta 'io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns 'my-complex-test)]
                                                  (println "================================ the decompile result======================")
                                                  (pprint m-data)
                                                  ))
                                              (catch Exception e
                                                (.printStackTrace e)
                                                )))
(run-tests)