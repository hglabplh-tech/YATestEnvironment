;;(c) Harald Glab-Plhak, 2026
(ns io.github.hglabplh-tech.reflect.code.json-util
  (:require [clojure.data.json :as json]))

(declare json-safe)
(defn- key-safe [value]
  (cond
    (keyword? value) (name value)
    (symbol? value) (str value)
    :else (str value)))

(defn- json-safe-map [data]
  (into {}
        (map (fn [[k v]]
               [(key-safe k) (json-safe v)])
             data)))

(defn json-safe [data]
  (cond
    (nil? data) nil
    (or (string? data) (number? data) (true? data) (false? data)) data
    (keyword? data) (name data)
    (symbol? data) (str data)
    (class? data) (.getName ^Class data)
    (map? data) (json-safe-map data)
    (or (sequential? data) (set? data)) (vec (map json-safe data))
    (.isArray (class data)) (vec (map json-safe (seq data)))
    :else (str data)))

(defn write-json [data]
  (json/write-str (json-safe data) :escape-slash false))

(defn print-pretty [json-str]
  (json/write-str json-str :indent true))