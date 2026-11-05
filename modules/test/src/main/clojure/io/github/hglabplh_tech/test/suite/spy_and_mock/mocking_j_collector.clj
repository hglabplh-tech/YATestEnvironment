(ns io.github.hglabplh-tech.test.suite.spy-and-mock.mocking-j-collector
  (:require [clojure.repl :refer :all]))

(defn get-fun-caller []
  (->> (.getStackTrace (Thread/currentThread))
       (map #(.getClassName %))))

(defn clean-get-fun-caller
  "get the nTh caller of a function or a back-trace
  @param index - which element of the trace is the start element"
  [^Integer index trace?]
  (let [result (map demunge (drop index (get-fun-caller)))]
    (if trace?
      result
      (first  result)
      )))
