(ns io.github.hglabplh-tech.test.suite.spy-and-mock.mocking-j-collector-test
  (:require [clojure.test :refer :all]
            [io.github.hglabplh-tech.test.suite.spy-and-mock.mocking-j-collector :as collector]))

(defn the-caller []
  (collector/clean-get-fun-caller 8 false))

(defn the-top-caller []
  (println (the-caller))
  (collector/clean-get-fun-caller 8 true))

(deftest get-caller-test (testing "getting the caller fun of a function"
                           (let [result (the-top-caller)]
                             (println result))
                           ))