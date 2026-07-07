(ns io.github.hglabplh_tech.reflect.clojure.code.generate.json.jsongenerator
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

(defn- write-json [data]
  (json/write-str (json-safe data) :escape-slash false))

(defn class-def-gen-hook [class-data]
  {:type "class-definition"
   :data class-data})

(defn ctor-gen-hook [ctor-data]
  {:type "constructor"
   :data (:ctor ctor-data)})

(defn method-gen-hook [method-data]
  {:type "method"
   :data (:method method-data)})

(defn field-gen-hook [field-data]
  {:type "field"
   :data (:field field-data)})

(defn enum-gen-hook [enum-data]
  {:type "enum"
   :data enum-data})

(defn record-gen-hook [record-data]
  {:type "record"
   :data record-data})

(defn lambda-gen-hook [lambda-data]
  {:type "lambda"
   :data lambda-data})

(defn switch-gen-hook [switch-data]
  {:type "switch"
   :data switch-data})

(defn emit-gen-hook [generated]
  (write-json generated))

(def generator
  {:class-def-gen-hook class-def-gen-hook
   :ctor-gen-hook ctor-gen-hook
   :method-gen-hook method-gen-hook
   :field-gen-hook field-gen-hook
   :enum-gen-hook enum-gen-hook
   :record-gen-hook record-gen-hook
   :lambda-gen-hook lambda-gen-hook
   :switch-gen-hook switch-gen-hook
   :emit-gen-hook emit-gen-hook})
