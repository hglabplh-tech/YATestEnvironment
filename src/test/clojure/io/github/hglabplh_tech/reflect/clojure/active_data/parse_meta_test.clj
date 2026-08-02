(ns  io.github.hglabplh-tech.reflect.clojure.active-data.parse-meta-test
  (:refer-clojure :exclude [def defn fn])
  (:require [active.data.realm :as realm]
    [clojure.pprint :refer [pprint]]
            [clojure.test :refer :all]
            [io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns :refer :all]
            [active.data.realm.attach :refer :all]
            [active.data.realm.schema :refer :all]
            [active.data.record :as sut]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.fun-reader :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.active-data.parse-meta :refer :all]
            )
  )


(deftest function_easy_compile_ad_schema (testing "The conversion of the active data - meta-schema records to structured output / easy test"
                                           (try
                                              (let [m-data (meta-raw io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns my-easy-test)]
                                                (println "test the fun call ->")
                                                (my-easy-test "Hallo " (gensym "any") 68 98)
                                                (println "================================ the decompile result======================")
                                                (pprint m-data)))
                                              (catch Exception e
                                                (.printStackTrace e)
                                                )))
(run-tests)
