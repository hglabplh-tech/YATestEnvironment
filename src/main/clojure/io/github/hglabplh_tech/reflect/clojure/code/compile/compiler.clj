(ns io.github.hglabplh-tech.reflect.clojure.code.compile.compiler
  (:require [io.github.hglabplh-tech.reflect.clojure.api.packages :as pkg]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-class :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.api.convert-java-cloj :as conv]
            [io.github.hglabplh_tech.reflect.clojure.code.generate.generator :as gen]
            [io.github.hglabplh_tech.reflect.clojure.code.generate.yaml.yamlgenerator :as ygen]
            [io.github.hglabplh_tech.reflect.clojure.code.generate.xml.xmlgenerator :as xgen]
            [io.github.hglabplh_tech.reflect.clojure.code.generate.json.jsongenerator :as jgen]
            [io.github.hglabplh_tech.reflect.clojure.code.generate.codegen.jgenerator :as cgen])
  (:import (clojure.lang Symbol)
           (java.lang Class)
           (io.github.hglabplh_tech.reflect.clojure.api.utils ClassUtil)))

(defn- hooks-for-format [out-format]
  (case (keyword out-format)
    :json jgen/generator
    :xml xgen/generator
    :yaml ygen/generator
    :yml ygen/generator
    :java cgen/generator
    :codegen cgen/generator
    :jcode cgen/generator
    (throw (IllegalArgumentException.
            (str "unsupported output format: " out-format)))))

(defn compile-class
  "The class to compile is given by it's canonical class name"
  {:added "1.1.2"
   :static true}
  [^String canonical-name]
  (let [clazz (Class/forName canonical-name)
        clazz-util (ClassUtil. clazz)
        class-info (conv/retrieve-class-info clazz-util)]
    class-info
    ))

(defn compile-class-conv
"The class to compile is given by it's canonical class name after compile to tags it is converted to specified format(s)"
  {:added "1.1.2"
   :static true}
[^String canonical-name
^Symbol out-format]
(let [clazz (Class/forName canonical-name)
      clazz-util (ClassUtil. clazz)
      comp-res (conv/retrieve-class-info clazz-util)
      hooks (hooks-for-format out-format)]
  (gen/register-hooks! hooks)
  (gen/generate comp-res)))


(defn search-compile-class
  "Here the class for compiling the reflection result is searched in the packages path"
  [class-name & pkg-list]
  (let [compile-pkg-list (apply pkg/list-packages pkg-list)
        clazz-util (apply get-class class-name pkg-list)]
    (if (not (nil? clazz-util))
      (conv/retrieve-class-info clazz-util)
      (throw (IllegalArgumentException. "class does not exist")))
    ))
