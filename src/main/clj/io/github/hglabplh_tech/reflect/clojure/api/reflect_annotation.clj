(ns io.github.hglabplh-tech.reflect.clojure.api.reflect-annotation
  (:import (java.lang Class)
           (java.lang.reflect Constructor Field Method)
           (io.github.hglabplh_tech.reflect.clojure.api.utils AnnotationsUtil)))

(defn get-class-annots
  "Get te annotations of a class definition"
  {:added "0.9.0"
   :static true}
  [^Class clazz]
  (AnnotationsUtil/getClassAnnots clazz))

(defn get-ctor-annots
  "Get te annotations of a constructor definition"
  {:added "0.9.0"
   :static true}
  [^Constructor ctor]
  (let [annotations (AnnotationsUtil/getCtorAnnots ctor)]
   annotations))


(defn get-ctor-param-annots
  "Get te annotations of a constructor parameters definition"
  {:added "0.9.0"
   :static true}
  [^Constructor ctor]
  (AnnotationsUtil/getCtorParamAnnots ctor))

(defn get-field-annots
  "Get te annotations of a field definition"
  {:added "0.9.0"
   :static true}
  [^Field field]
  (AnnotationsUtil/getFieldAnnots field))

(defn get-method-annots
  "Get te annotations of a method definition"
  {:added "0.9.0"
   :static true}
  [^Method meth]
  (AnnotationsUtil/getMethodAnnots meth))

(defn get-meth-param-annots
  "Get te annotations of a method parameters definition"
  {:added "0.9.0"
   :static true}
  [^Method meth]
  (AnnotationsUtil/getMethParamAnnots meth))

(defn get-annot-return-type
  "Get te annotations of a method return type definition"
  {:added "0.9.0"
   :static true}
  [^Method meth]
  (AnnotationsUtil/getAnnotReturnType meth))