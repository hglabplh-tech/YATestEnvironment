(ns io.github.hglabplh-tech.reflect.clojure.api.annotations.annot-tool
  (:require [io.github.hglabplh-tech.reflect.clojure.api.reflect-annotation :as refla])
  (:import (io.github.hglabplh_tech.reflect.clojure.api.annotations  AnnotationTool)))

(defn get-annotation-info
  "gets the meta info of a given annotation -- TODO: exper. has to be reworked"
  {
   :added "1.1.0"
   :static true
   }
  [annot]
  (let [ tool  (AnnotationTool. annot)
        annot-attrs (.getAnnotationAttributes ^AnnotationTool tool)]
    annot-attrs
   )
  )

(defn get-annotation-data-vals
  "gets the values defined by an annotation "
  {
   :added "1.1.0"
   :static true
   }
  [annot]
  (let [annot-data  (AnnotationTool/retrieveAnnotationValues annot)]
    annot-data
    ))



(defn- get-vals-from-objects-vect-data
  "gets the annotation values of a vector of methods
  or fields which can have themselves more than one annotation"
  {
   :added "1.1.0"
   :static true
   }
  [annotations]
  (let [annot-as-cloj  (map (fn [annots]
                      (map (fn [annot]
                             (if (nil? annot)
                               'nil
                               (get-annotation-data-vals annot)
                               ))
                           annots)) annotations)]
    annot-as-cloj))

(defn- get-vals-from-obj-vect-data
  "gets the annotation values of a method or field
  or fields which can have themselves more than one annotation"
  {
   :added "1.1.0"
   :static true
   }
  [annotations]
  (let [annot-as-cloj    (map (fn [annot]
                                     (if (nil? annot)
                                       'nil
                                       (get-annotation-data-vals annot)
                                       ))
                                   annotations)]
    annot-as-cloj))



(defn get-annotation-values-from-class-methods
  "gets the annotation values of a vector of methods
   which can have themselves more than one annotation"
  {
   :added "1.1.0"
   :static true
   }
  [all-methods]
  (let [annotations (map refla/get-method-annots all-methods)]
    (get-vals-from-objects-vect-data annotations)))

(defn get-annotation-values-from-class-fields
  "gets the annotation values of a vector of fields
   which can have themselves more than one annotation"
  {
   :added "1.1.0"
   :static true
   }
  [fields]
  (let [annotations (map refla/get-field-annots fields)]
    (get-vals-from-objects-vect-data annotations)))

(defn get-annotation-values-from-method
  "gets the annotation values of a vector of fields
   which can have themselves more than one annotation"
  {
   :added "1.1.0"
   :static true
   }
  [meth]
  (let [annotations (refla/get-method-annots meth)]
    (get-vals-from-obj-vect-data annotations)))

(defn get-annotation-values-from-field
  "gets the annotation values of a field
   which can have more than one annotation"
  {
   :added "1.1.0"
   :static true
   }
  [field]
  (let [annotations (refla/get-field-annots field)]
    (get-vals-from-obj-vect-data annotations)))