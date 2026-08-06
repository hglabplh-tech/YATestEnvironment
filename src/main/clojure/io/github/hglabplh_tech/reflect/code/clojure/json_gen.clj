;;(c) Harald Glab-Plhak, 2026
(ns io.github.hglabplh-tech.reflect.code.clojure.json-gen
  (:require [io.github.hglabplh-tech.reflect.code.json-util :refer :all]) )

(defn to-json [meta-structured]
  (write-json meta-structured)
  )


(defn from-json [json-string]
  )

(defn debug-print [json-str]
  (print-pretty json-str))