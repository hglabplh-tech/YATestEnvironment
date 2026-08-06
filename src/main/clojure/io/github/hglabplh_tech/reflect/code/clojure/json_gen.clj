(ns io.github.hglabplh-tech.reflect.code.clojure.json-gen
  (:require [io.github.hglabplh-tech.reflect.code.json-util :refer :all]) )

(defn to-json [meta-structured]
  (write-json meta-structured)
  )

(defn from-json [json-string]
  )
