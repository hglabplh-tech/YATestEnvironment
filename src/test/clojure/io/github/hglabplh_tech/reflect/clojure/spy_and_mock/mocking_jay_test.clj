(ns io.github.hglabplh_tech.reflect.clojure.spy-and-mock.mocking-jay-test
  (:refer-clojure :exclude [def defn fn])
  (:require [clojure.test :refer :all]
            [clojure.walk :refer :all]
            [clojure.pprint :refer :all]
            [active.data.realm :as realm]
            [active.data.realm.attach :refer :all]
            [schema.core :as s]
            [io.github.hglabplh_tech.reflect.clojure.example.the-funs-ns :refer :all]
            [io.github.hglabplh_tech.reflect.clojure.spy-and-mock.real-fun-checkers :refer :all]
            [io.github.hglabplh_tech.reflect.clojure.spy-and-mock.mocking-jay :refer :all])
  (:import (clojure.lang Symbol Namespace)))

(s/set-fn-validation! (boolean 1))

(defn i-am-a-fake-fun :- realm/boolean
      [msg :- realm/string a :- realm/number b :- realm/number]
      (let [res (* a b)]
        (println msg res)
        (boolean res)))

(clojure.core/defn i-am-a-fake-fun-store [msg a b]
  (let [res (* a b)]
    (println msg res)
    (boolean res)))

(defn fun-store-being-spyed :- realm/boolean
      [msg :- realm/string a :- realm/number b :- realm/number]
  (let [res (* a b)]
    (println msg res)
    (boolean res)))
(clojure.core/defn try-it [a b c]
  (println "Ups I am here" (+ a b c)))

(prolog to-spy add-user add-bill log-thing try-it fun-store-being-spyed)
(prolog to-mock  i-am-a-fake-fun )
(deftest test-extended-mock
  (testing "the extended mock"


    (call-cond-> 'i-am-a-fake-fun-store
                 :when
                 :any-boolean?-key :<-
                 [[:any-string?-key :$] [:any-int?-key :$] [:any-int?-key :$]
                   ]
                 [return-val 5]
                 :else
                 [return-val "real-fun"])

    (mock io.github.hglabplh_tech.test.suite.spy-and-mock.mocking-jay-test)
    (i-am-a-fake-fun "hello" 8 9)))

(deftest test-extended.mock-act-data
  (testing "the extended mock"


    (call-cond-> 'i-am-a-fake-fun
                 :when
                 :any-boolean?-key :<-
                 [[:any-string?-key :$] [:any-int?-key :$] [:any-int?-key :$]
                  ]
                 [return-val 5]
                 :else
                 [return-val "real-fun"])

    (mock  io.github.hglabplh_tech.test.suite.spy-and-mock.mocking-jay-test)
    (println  "get it" (i-am-a-fake-fun "Hi" 8 9 ))
    ))

(deftest test-simple-spy
  (testing "A simple test for spy functionality"
    (spy io.github.hglabplh_tech.test.suite.spy-and-mock.mocking-jay-test)
    ;;(println (get-fun-meta fun-store-being-spyed))
    (fun-store-being-spyed "Here I am I am real: " 8 8)
    ;;(restore-orig-funs hgp.cljito.spy-and-mock.mocking-jay-test)
    (try-it 8 8 8)
    ))

(deftest test-example-spy
  (testing "A simple test for spy functionality"
    (spy io.github.hglabplh_tech.test.suite.example.the-funs-ns)
    (pprint (add-bill (random-uuid) 8757
                            (set (list "dishwasher" "pump-gun" "plate"))
                            678.96))

    ))
(run-tests)


