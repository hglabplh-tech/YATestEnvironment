;; Copyright (c) 2026 Harald Glab-Plhak

(ns io.github.hglabplh-tech.reflect.java.api.reflect-class
  (:require [io.github.hglabplh-tech.reflect.java.api.annotations.annot-tool :as atool]
            [io.github.hglabplh-tech.reflect.java.api.packages :as pkg]
            [io.github.hglabplh-tech.reflect.java.api.reflect-field :as rfld]
            [io.github.hglabplh-tech.reflect.java.api.reflect-meths-ctors :as rmct]
            [io.github.hglabplh-tech.reflect.java.api.reflect-special-forms :as er-refl]
            [io.github.hglabplh-tech.reflect.java.api.reflect-types :as rtypes])
  (:import (io.github.hglabplh_tech.reflect.general ClojFunctionalUtils)
           (io.github.hglabplh_tech.reflect.java.api.utils AnnotationsUtil ClassUtil)
           ))
(declare get-all-class-attributes-and-values-internal)

(def classes-defs (atom []))

(defn add-class-reflected [class-name]
  (swap! classes-defs conj class-name))


(defn class-reflected? [class-name]
  (boolean (some #{class-name} @classes-defs)))


(defn get-class-util
  "get the class util by the canonical class-name with Class/forName"
  {:added "1.1.0"}
  [class-name]
  (ClassUtil. (Class/forName class-name)))

(defn get-class-util-by-class
  "get the class util by a given class"
  {:added "1.1.0"}
  [^Class the-class]
  (if (nil? the-class)
    nil
    (ClassUtil. the-class)))

(defn get-class
  "Get a class out of the package definitions by the canonical name
   @param clazz-name : the canonical name of the class
   @param pkg-names : the package names at least one is mandatory"
  {:added "0.9.0"}
  [clazz-name & pkg-names]
  (let [the-clazz (ClassUtil. (first (first (map (fn [pkg-list]
                                                   (let [result (filter
                                                                  (fn [member]
                                                                    (.equals (.getCanonicalName member)
                                                                             clazz-name))
                                                                  pkg-list)]
                                                     result)) (apply pkg/list-packages pkg-names)))))]
    the-clazz))


(defn get-all-classes-of-pkg
  "get all classes at the top level of a defined package"
  {:added "0.9.0"}
  [& pkg-names]
  (let [the-clazz-vect (map
                         (fn [element]
                           (ClassUtil. (first (first element))))
                         (apply pkg/list-packages pkg-names))]
    the-clazz-vect))

(defn get-all-ctors
  "Get all constructors declared in the class if they are visible or not
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getAllConstructors util))

(defn get-public-methods
  "Get all public methods declared in the class which are visible
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getPublicMethods util))


(defn get-class-annots
  "Get te annotations of a class definition"
  {:added  "0.9.0"
   :static true}
  [^Class clazz]
  (AnnotationsUtil/getClassAnnots clazz))

(defn get-all-fields
  "Get all fields declared in the class if they are visible or not
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil class-util]
  (.getAllFields class-util))

(defn get-all-methods
  "Get all methods declared in the class if they are visible or not
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getAllMethods util))

(defn get-method-by-name
  "Get a method by its name declared in the class if it is visible or not
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util ^String methodName]
  (.getMethodByName util methodName))

(defn get-all-sub-classes
  [^ClassUtil util]
  "Get all subclasses declared in the class if they are visible or not
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  (.getAllSubClasses util))

(defn get-class-name
  "get the class name"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getClassName util))

(defn get-public-ctors
  "Get all public constructors declared in the class which are visible
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getConstructors util))

(defn get-annots-by-type
  "Get all annotations by the given type
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util ^Class annotClass]
  (.getDeclAnnotsByType util annotClass))


(defn get-generic-interfaces
  "Get the generic interfaces of a class
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getGenericInterfaces util))

(defn get-interfaces
  "Get the interfaces of a class
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getInterfaces util))

(defn get-generic-super-class
  "Get the generic super-class of a class
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (let [result (.getGenericSuperClass util)]
    (if (nil? result)
      "N/A"
      (ClojFunctionalUtils/readParametrizedType result))
    ))

(defn get-super-class
  "Get the super-class  of a class
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (let [the-clazz (.getSuperClass util)
        the-clazz-name (if (nil? the-clazz) "UnknownClass" (.getCanonicalName the-clazz))]
    (if (or (= the-clazz-name "UnknownClass") (class-reflected? the-clazz-name))
      {:class-already-reflected the-clazz-name}
      (get-all-class-attributes-and-values-internal (get-class-util-by-class the-clazz)))
    ))

(defn get-public-annotations
  "Get all public annotations in the class
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getPublicAnnotations util))

(defn get-public-fields
  "Get all public fields in the class
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getPublicFields util))

(defn get-public-methods
  "Get all public methods in the class
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getPublicMethods util))

(defn get-public-sub-classes
  "Get all public sub-classes in the class
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getPublicSubClasses util))


(defn get-the-class
  "Get the class defined by the given ClassUtil
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getTheClass util))

(defn get-class-attributes
  "Get the attributes of a class
  @param : Class instance"
  {:added "0.9.0"}
  [clazz]
  (rtypes/get-class-attributes clazz))

(defn get-enclosing-constructor
  "Get the enclosing constructor of a subclass
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getEnclosingConstructor util))

(defn get-inner-classes
  "Get the top level subclasses
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getPublicSubClasses util))

(defn get-generics
  "Get the enclosing class of a subclass
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getGenerics util))


(defn get-enclosing-class
  "Get the enclosing class of a subclass
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getEnclosingClass util))

(defn get-enclosing-method
  "Get the enclosing method of a subclass
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getEnclosingMethod util))

(defn get-all-class-attributes-and-values
  "External for getting the fields modifiers and types and
 attributes of a class including interfaces sub-/superclasses
 @param : the class we look for"
  {:added  "1.4.0"
   :static true}
  [^ClassUtil util]
  (reset! classes-defs [])
  (get-all-class-attributes-and-values-internal util))


(defn- interface-attrs-in-map [interface-clazz]
  "Function used inside mapv for reflecting interfaces to call the class reflection or tell
  it is already reflected or nil"
  (if (nil? interface-clazz)
    nil
    (if (class-reflected? (.getCanonicalName interface-clazz))
      {:class-already-reflected (.getCanonicalName interface-clazz)}
      (get-all-class-attributes-and-values-internal
        (get-class-util-by-class interface-clazz)))))

(defn inner-class-representation [inner-classes]
  (mapv (fn [inner-clazz]
          (let [inner-util (get-class-util-by-class inner-clazz)]
            (get-all-class-attributes-and-values-internal inner-util)))
        inner-classes))

(defn- get-all-class-attributes-and-values-internal
  "Internal for getting the fields modifiers and types and
  attributes of a class including interfaces sub-/superclasses
  @param : the class we look for"
  {:added  "1.1.0"
   :static true}
  [^ClassUtil util]

  (if (nil? util)
    {}
    (let [refl-clazz (get-the-class util)
          refl-clazz-name (.getCanonicalName refl-clazz)]
      (if (not (class-reflected? refl-clazz-name))
        (do (add-class-reflected refl-clazz-name)
            (cond
              (rtypes/is-enum refl-clazz)
              {:enum-class {:pure-record (er-refl/analyze-enum refl-clazz)
                            :inner-classes  (inner-class-representation (get-all-sub-classes util))
                            } }
              (rtypes/is-record refl-clazz)
              {:record-class  {:pure-record (er-refl/analyze-record refl-clazz)
                               :inner-classes  (inner-class-representation (get-all-sub-classes util))
                               } }
              (rtypes/is-annotation refl-clazz)
              {:annot-class (atool/get-annotation-info refl-clazz)}
              :else
              {:normal-class
               {:class-name      (get-class-name util)
                :super-class     (get-super-class util)
                :gen-super       (get-generic-super-class util)
                :generics        (get-generics util)
                :pinner-classes  (inner-class-representation (get-all-sub-classes util))
                :annotations     (atool/get-annotations-information (get-class-annots refl-clazz)) ;; TODO: do this also for fields, methods and constructors
                ;  :enum-specs      (er-refl/analyze-enum refl-clazz)
                ;:record-specs    (er-refl/analyze-record refl-clazz)
                :interfaces      (mapv interface-attrs-in-map  (get-interfaces util))
                :gen-interfaces  (get-generic-interfaces util)
                :enclosing-class (get-enclosing-class util)
                :constructors    (rmct/get-all-ctor-and-type-modifiers (get-all-ctors util))
                :methods         (rmct/get-all-meth-and-type-modifiers (get-all-methods util))
                :fields          (rfld/get-all-fields-and-type-modifiers (get-all-fields util))
                :attributes      (get-class-attributes refl-clazz)}}
              )
            )))
    ))