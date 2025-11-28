(ns io.github.hglabplh_tech.reflect.clojure.api.reflect-class
  (:require [io.github.hglabplh_tech.reflect.clojure.api.packages :as pkg]
            [io.github.hglabplh_tech.reflect.clojure.api.reflect-types :as rtypes])
  (:import (java.lang.annotation Annotation)
           (io.github.hglabplh_tech.reflect.clojure.api.utils ClassUtil)))

(defn get-class-util
  "get the class util by the canonical class-name with Class/forName"
  {:added "1.1.0"}
  [class-name]
  (ClassUtil. (Class/forName class-name)))

(defn get-class-util-by-class
  "get the class util by a given class"
  {:added "1.1.0"}
  [^Class the-class]
  (ClassUtil. the-class))

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
                           result))  (apply pkg/list-packages pkg-names)))))]
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
  (.getGenericSuperClass util))

(defn get-super-class
  "Get the super-class  of a class
  @param : ClassUtil instance with the class we analyze"
  {:added "0.9.0"}
  [^ClassUtil util]
  (.getSuperClass util))

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
