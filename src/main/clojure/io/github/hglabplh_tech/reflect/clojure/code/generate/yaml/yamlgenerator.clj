(ns io.github.hglabplh_tech.reflect.clojure.code.generate.yaml.yamlgenerator
  (:require [clojure.string :as str]))

(declare value->yaml)

(defn- scalar [value]
  (cond
    (nil? value) "null"
    (string? value) (pr-str value)
    (keyword? value) (name value)
    (symbol? value) (str value)
    :else (str value)))

(defn- indent [level]
  (apply str (repeat level "  ")))

(defn- key-name [value]
  (cond
    (keyword? value) (name value)
    (symbol? value) (str value)
    :else (str value)))

(defn- map->yaml [m level]
  (str/join
   "\n"
   (map (fn [[k v]]
          (if (coll? v)
            (str (indent level) (key-name k) ":\n" (value->yaml v (inc level)))
            (str (indent level) (key-name k) ": " (scalar v))))
        m)))

(defn- seq->yaml [items level]
  (str/join
   "\n"
   (map (fn [value]
          (if (coll? value)
            (str (indent level) "-\n" (value->yaml value (inc level)))
            (str (indent level) "- " (scalar value))))
        items)))

(defn value->yaml
  ([value]
   (value->yaml value 0))
  ([value level]
   (cond
     (map? value) (map->yaml value level)
     (or (sequential? value) (set? value)) (seq->yaml value level)
     :else (str (indent level) (scalar value)))))

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
  (str "---\n" (value->yaml generated) "\n"))

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
