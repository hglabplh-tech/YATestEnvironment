(ns io.github.hglabplh-tech.reflect.clojure.reflect-namespace-test
  (:refer-clojure :exclude [def defn fn])
  (:require [clojure.pprint :refer :all]
            [clojure.test :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.reflect-namespace :as reflns]
            [io.github.hglabplh-tech.reflect.general.gen-unified-astlike-code :as uni]
            [io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering :refer :all]
            [io.github.hglabplh-tech.reflect.examples.clojure.complex.mixed-defs :refer :all]))

;;(reflns/get-intern-defs io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns true)

(deftest simple-public-ns-reflect
  (testing "Reflect a namespace without decompile"
    (let [result (reflns/get-ns-publics 'io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering false)  the-keys (keys result)
          the-reflect-data (get result :reflect-data )
          content-keys (mapv (clojure.core/fn [value]
                               (first (keys value))) the-reflect-data)
          content-vals (mapv (clojure.core/fn [value]
                               (first (vals value))) the-reflect-data)]
      (is (get result :language) :clojure)
      (is (get result :ns-name) "io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering")
      (is content-keys [:chat-send :status :wait-for-atom-match :chat-loop])
      (pprint content-keys)
      (pprint content-vals))
    ))

(deftest simple-intern-ns-reflect
  (testing "Reflect a namespace without decompile"
    (let [result (reflns/get-ns-interns 'io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering false)
          the-reflect-data (get result :reflect-data )
          content-keys (mapv (clojure.core/fn [value]
                               (first (keys value))) the-reflect-data)
          content-vals (mapv (clojure.core/fn [value]
                               (first (vals value))) the-reflect-data)]
      (is (get result :language) :clojure)
      (is (get result :ns-name) "io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering")
      (is content-keys [:chat-send :status :wait-for-atom-match :chat-loop])
      (pprint content-keys)
      (pprint content-vals))
    ))

(deftest decomp-public-ns-reflect
  (testing "Reflect a namespace with decompile"
    (let [result (reflns/get-ns-publics 'io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering true)
          the-reflect-data (get result :reflect-data )
          content-keys (mapv (clojure.core/fn [value]
                               (first (keys value))) the-reflect-data)
          content-vals (mapv (clojure.core/fn [value]
                               (first (vals value))) the-reflect-data)]
      (is (get result :language) :clojure)
      (is (get result :ns-name) "io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering")
      (is content-keys [:chat-send :status :wait-for-atom-match :chat-loop])
      (pprint content-keys)
      (pprint content-vals))
    ))

(deftest decomp-intern-ns-reflect
  (testing "Reflect a namespace with decompile"
    (let [result (reflns/get-ns-interns 'io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering true)
          the-reflect-data (get result :reflect-data )
          content-keys (mapv (clojure.core/fn [value]
                               (first (keys value))) the-reflect-data)
          content-vals (mapv (clojure.core/fn [value]
                               (first (vals value))) the-reflect-data)]
      (is (get result :language) :clojure)
      (is (get result :ns-name) "io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering")
      (is content-keys [:chat-send :status :wait-for-atom-match :chat-loop])
      (pprint content-keys)
      (pprint content-vals))
    ))

(deftest decomp-mixed-ns-reflect
  (testing "Reflect a namespace with decompile - prismatic schema / active-data"
    (let [result (reflns/get-ns-interns 'io.github.hglabplh-tech.reflect.examples.clojure.complex.mixed-defs true)
          the-reflect-data (get result :reflect-data )
          content-keys (mapv (clojure.core/fn [value]
                               (first (keys value))) the-reflect-data)
          content-vals (mapv (clojure.core/fn [value]
                               (first (vals value))) the-reflect-data)]
      (is (get result :language) :clojure)
      (is (get result :ns-name) "io.github.hglabplh-tech.reflect.examples.clojure.complex.mixed-defs")
      (is content-keys [:chat-send :status :wait-for-atom-match :chat-loop])
      (pprint content-keys)
      (pprint content-vals)
      (pprint (uni/transform-to-unified result)))
    ))


(run-tests)