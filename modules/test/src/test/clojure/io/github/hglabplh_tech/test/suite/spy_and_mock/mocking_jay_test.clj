(ns io.github.hglabplh_tech.test.suite.spy-and-mock.mocking-jay-test
  (:refer-clojure :exclude [def defn fn])
  (:require [clojure.test :refer :all]
            [clojure.walk :refer :all]
            [clojure.pprint :refer :all]
            [active.data.realm :as realm]
            [active.data.realm.attach :refer :all]
            [schema.core :as s]
            [io.github.hglabplh_tech.test.suite.spy-and-mock.mocking-jay :refer :all]
            [io.github.hglabplh_tech.test.suite.spy-and-mock.mock-spy-misc-util :refer :all]
            [io.github.hglabplh_tech.test.suite.spy-and-mock.mocking-jay :refer :all]
            [io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns :refer :all]
            [io.github.hglabplh-tech.test.suite.spy-and-mock.mocking-j-collector :as collector])
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

(clojure.core/defn factorial
      ([n] (factorial n 1))
      ([n acc]
       (if (zero? n)
         acc
         (recur (dec n) (*' acc n)))))

(clojure.core/defn factorial-wo-recur
  ([n] (factorial-wo-recur n 1))
  ([n acc]
   (if (zero? n)
     acc
     (factorial-wo-recur (dec n) (*' acc n)))))

(clojure.core/defn format-message
      "Formats a message with positional arguments and optional named parameters."
      [recipient body & {:keys [greeting punctuation uppercase?]
                         :or   {greeting "Hello"
                                punctuation "!"
                                uppercase? false}}]
      (let [msg (str greeting ", " recipient ": " body punctuation)]
        (if uppercase?
          (.toUpperCase msg)
          msg)))

(defn fun-store-being-spyed :- realm/boolean
      [msg :- realm/string a :- realm/number b :- realm/number]
  (let [res (* a b)]
    (println msg res)
    (boolean res)))

(clojure.core/defn try-it [a b c call-me]
 (let [result (+ a b c)]
  (println "Ups I am here" result)
  (if call-me
    (try-it 6 7 5 false)
    [(fun-store-being-spyed "Inner call: " 5 6) result])
  ))

(prolog to-spy  my-enum-test try-it fun-store-being-spyed factorial factorial-wo-recur format-message)
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
    (try-it 8 8 8 true)
    (pprint @control-flow-collection)
    (pprint (get-spy-statistics-by-funs 'try-it 'fun-store-being-spyed))
    (pprint (get-spy-flow-stats))
    (pprint (get-recur-data))
    ))

(deftest test-example-spy
  (testing "A simple test for spy functionality with enums"
    (spy io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns)
    (pprint (my-enum-test  64.5 :two))
    (pprint (get-spy-statistics-by-funs 'my-enum-test))
    (pprint (get-spy-flow-stats))
    ))

(deftest test-fun-with-named-params
  (testing "A test for spy functionality with recursive and with named parameters"
    (spy io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns)
    (factorial 10)
    (factorial-wo-recur 10)
    (format-message "Bob" "Server is down" :uppercase? true :greeting "Alert")
    (println "=============== mock-meta==============================")
    (pprint @mock-meta)
    (println "=============== END mock-meta==============================")
    (pprint (get-spy-statistics-by-funs 'process-items 'factorial-wo-recur 'factorial))
    (pprint (get-spy-flow-stats))
    (pprint (get-recur-data))
    ))