(ns io.github.hglabplh-tech.reflect.clojure.fun-reader-test
  (:refer-clojure :exclude [def defn fn])
  (:require [active.data.realm :as realm]
            [active.data.realm.attach :refer :all]
            [active.data.realm.schema :refer :all]
            [active.data.record :as sut]
            [clojure.pprint :refer :all]
            [clojure.test :refer :all]
            [io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.fun-reader :refer :all]))

(sut/def-record the-rec [return-val :- realm/symbol
                         parm-list :- (realm/set-of realm/symbol)
                         line :- (realm/integer-from 7)
                         factor :- (realm/real-range :ex 7.7 100.8 :in)
                         ])

(deftest structured-fun-schema.test
  (testing "The conversion of the meta-schema defn to structured output"
    (analyze-struct io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns my-set-test)
    ))



(deftest structured-def-rec-schema.test (testing "The conversion of the meta-schema records to structured output"

                                          (pprint (get-rec-meta the-rec))
                                          (pprint (decompile-meta
                                                    io.github.hglabplh-tech.reflect.clojure.fun-reader-test
                                                    test-the-rec))
                                          (pprint (decompile-meta
                                                    io.github.hglabplh-tech.reflect.clojure.fun-reader-test
                                                    the-rec))



                                          ))
(run-tests)

