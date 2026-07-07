(ns io.github.hglabplh_tech.reflect.clojure.code.generate.xml.xmlgenerator
  (:require [clojure.string :as str]))

(declare value->xml)

(defn- tag-name [value]
  (cond
    (keyword? value) (name value)
    (symbol? value) (str value)
    :else (str value)))

(defn- xml-name [value]
  (-> (tag-name value)
      (str/replace #"[^A-Za-z0-9_.-]" "-")))

(defn- escape-xml [value]
  (-> (str value)
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")
      (str/replace "\"" "&quot;")
      (str/replace "'" "&apos;")))

(defn- map-entry->xml [[k v]]
  (str "<" (xml-name k) ">" (value->xml v) "</" (xml-name k) ">"))

(defn- seq-entry->xml [value]
  (str "<item>" (value->xml value) "</item>"))

(defn value->xml [value]
  (cond
    (nil? value) ""
    (map? value) (apply str (map map-entry->xml value))
    (sequential? value) (apply str (map seq-entry->xml value))
    (set? value) (apply str (map seq-entry->xml value))
    :else (escape-xml value)))

(defn- tagged [tag data]
  (str "<" tag ">" (value->xml data) "</" tag ">"))

(defn class-def-gen-hook [class-data]
  {:class-definition class-data})

(defn ctor-gen-hook [ctor-data]
  {:constructor (:ctor ctor-data)})

(defn method-gen-hook [method-data]
  {:method (:method method-data)})

(defn field-gen-hook [field-data]
  {:field (:field field-data)})

(defn enum-gen-hook [enum-data]
  {:enum enum-data})

(defn record-gen-hook [record-data]
  {:record record-data})

(defn lambda-gen-hook [lambda-data]
  {:lambda lambda-data})

(defn switch-gen-hook [switch-data]
  {:switch switch-data})

(defn emit-gen-hook [generated]
  (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
       (tagged "generation" generated)))

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
