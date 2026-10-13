(ns io.github.hglabplh-tech.reflect.clojure.reflect-namespace-test
  (:refer-clojure :exclude [def defn fn])
  (:require [clojure.pprint :refer :all]
            [clojure.test :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.reflect-namespace :as reflns]
            [io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering :refer :all]))

;;(reflns/get-intern-defs io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns true)

(deftest simple-public-ns-reflect
  (testing "Reflect a namespace without decompile"
    (let [result (reflns/get-ns-publics 'io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering false)  the-keys (keys result)
          the-vals (vals result)
          content-keys (mapv (clojure.core/fn [value]
                               (first (keys value))) (first the-vals))
          content-vals (mapv (clojure.core/fn [value]
                               (first (vals value))) (first the-vals))]
      (is (first the-keys) 'io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering)
      (is content-keys [:chat-send :status :wait-for-atom-match :chat-loop])
      (pprint content-keys)
      (pprint content-vals))
    ))

(deftest simple-intern-ns-reflect
  (testing "Reflect a namespace without decompile"
    (let [result (reflns/get-ns-interns 'io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering false)
          the-keys (keys result)
          the-vals (vals result)
          content-keys (mapv (clojure.core/fn [value]
                               (first (keys value))) (first the-vals))
          content-vals (mapv (clojure.core/fn [value]
                               (first (vals value))) (first the-vals))]
      (is (first the-keys) 'io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering)
      (is content-keys [:chat-send :status :wait-for-atom-match :chat-loop])
      (pprint content-keys)
      (pprint content-vals))
    ))

(deftest decomp-public-ns-reflect
  (testing "Reflect a namespace without decompile"
    (let [result (reflns/get-ns-publics 'io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering true)
          the-keys (keys result)
          the-vals (vals result)
          val-content-keys (mapv (clojure.core/fn [value]
                                   (first (keys value))) (first the-vals))
          val-content-vals (mapv (clojure.core/fn [value]
                                   (first (vals value))) (first the-vals))
          ]
      (is (first the-keys) 'io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering)
      (is val-content-keys [:chat-send :status :wait-for-atom-match :chat-loop])
      (pprint val-content-keys)
      (pprint val-content-vals))
    ))

(deftest decomp-intern-ns-reflect
  (testing "Reflect a namespace without decompile"
    (let [result (reflns/get-ns-interns 'io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering true)
          the-keys (keys result)
          the-vals (vals result)
          val-content-keys (mapv (clojure.core/fn [value]
                                   (first (keys value))) (first the-vals))
          val-content-vals (mapv (clojure.core/fn [value]
                                   (first (vals value))) (first the-vals))
          ]
      (is (first the-keys) 'io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering)
      (is val-content-keys [:chat-send :status :wait-for-atom-match :chat-loop])
      (pprint val-content-keys)
      (pprint val-content-vals))
    ))


(run-tests)