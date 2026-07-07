;; Copyright (c) 2026 Harald Glab-Plhak

(ns io.github.hglabplh-tech.reflect.clojure.code.generator-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [io.github.hglabplh_tech.reflect.clojure.code.generate.generator :as gen]
            [io.github.hglabplh_tech.reflect.clojure.code.generate.json.jsongenerator :as jgen]
            [io.github.hglabplh_tech.reflect.clojure.code.generate.xml.xmlgenerator :as xgen]
            [io.github.hglabplh_tech.reflect.clojure.code.generate.yaml.yamlgenerator :as ygen]
            [io.github.hglabplh_tech.reflect.clojure.code.generate.codegen.jgenerator :as cgen]))

(def generated-class-data
  {:class-data
   {:definition
    {:class
     {:obj-name "example.Generated"
      :attributes ["public"]
      :enum-specs {:enum-spec {"ONE" {:ordinal-val 0}}}
      :record-specs {}
      :lambda-specs {}
      :switch-specs {}}}
    :cl-body
    {:body
     {:ctor-infos [{:ctor {:obj-name "Generated"}}]
      :field-infos [{:field {:obj-name "name" :gen-type "java.lang.String"}}]
      :method-infos [{:method {:obj-name "getName" :gen-return-type "java.lang.String"}}]
      :class-infos []}}}})

(deftest json-generator-walks-class-data
  (gen/register-hooks! jgen/generator)
  (let [result (gen/generate generated-class-data)
        parsed (json/read-str result)]
    (is (= "class-definition" (get-in parsed ["definition" "class" "type"])))
    (is (= "constructor" (get-in parsed ["body" "constructors" 0 "type"])))
    (is (= "field" (get-in parsed ["body" "fields" 0 "type"])))
    (is (= "method" (get-in parsed ["body" "methods" 0 "type"])))
    (is (= "enum" (get-in parsed ["definition" "enum" "type"])))))

(deftest text-generators-produce-output
  (doseq [hooks [xgen/generator ygen/generator cgen/generator]]
    (gen/register-hooks! hooks)
    (is (seq (gen/generate generated-class-data)))))
