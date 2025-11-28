(ns io.github.hglabplh_tech.reflect.clojure.api.reflect-meths-ctors
  (:require [io.github.hglabplh_tech.reflect.clojure.api.reflect-types :as rtypes]
            [io.github.hglabplh_tech.reflect.clojure.general.cloj-java-represent :as represent])
  (:import (io.github.hglabplh_tech.reflect.clojure.api.utils MethsCtorsUtil)))

(defn get-ctor-name
  "Get the constructors name
  @param : the ctor we look for"
  {:added  "0.9.0"
   :static true}
  [ctor]
  (MethsCtorsUtil/getCtorName ctor))

(defn get-ctor-param-count
  "Get the constructors parameter count
  @param : the ctor we look for"
  {:added  "0.9.0"
   :static true}
  [ctor]
  (MethsCtorsUtil/getCtorParamCount ctor))

(defn get-ctor-exception-types
  "Get the constructors exception types
  @param : the ctor we look for"
  {:added  "0.9.0"
   :static true}
  [ctor]
  (MethsCtorsUtil/getCtorExceptionTypes ctor))

(defn get-ctor-gen-exception-types
  "Get the constructors generic exception types
  @param : the ctor we look for"
  {:added  "0.9.0"
   :static true}
  [ctor]
  (represent/type-def-representation
    (MethsCtorsUtil/getCtorGenericExTypes ctor)))

(defn get-ctor-param-types
  "Get the constructors parameter types
  @param : the ctor we look for"
  {:added  "0.9.0"
   :static true}
  [ctor]
  (MethsCtorsUtil/getCtorParamTypes ctor))

"Get the constructors modifiers
  @param : the ctor we look for"
{:added  "0.9.0"
 :static true}
(defn get-ctor-modifier [ctor]
  (MethsCtorsUtil/getCtorModifiers ctor))
4
(defn get-all-ctor-and-type-modifiers
  "Get all the constructors modifiers
  types attributes these values are translated partly in keywods
  especially for modifiers and attributes
  @param : the ctors list"
  {:added  "0.9.0"
   :static true}
  [& ctors]
  (map (fn [ctor]
         {:ctor-name  (get-ctor-name ctor)
          :parm-types (get-ctor-param-types ctor)
          :modifiers  (rtypes/get-item-modifiers
                        (get-ctor-modifier ctor))
          :attribs    (rtypes/get-ctor-attributes ctor)
          :exception-types (get-ctor-exception-types ctor)})
       ctors))

(defn get-ctor-declaring-class
  "Get the declaring class of the constructor
  @param : the constructor we look for"
  {:added  "1.1.0"
   :static true}
  [ctor]
  (MethsCtorsUtil/getCtorDeclaringClass ctor))


(defn get-meth-name
  "Get the methods name
  @param : the method we look for"
  {:added  "0.9.0"
   :static true}
  [meth]
  (MethsCtorsUtil/getMethodName meth))

(defn get-meth-declaring-class
  "Get the declaring class of method
  @param : the method we look for"
  {:added  "1.1.0"
   :static true}
  [meth]
  (MethsCtorsUtil/getMethDeclaringClass meth))

(defn get-meth-param-count
  "Get the methods parameter count
   @param : the method we look for"
  {:added  "0.9.0"
   :static true}
  [meth]
  (MethsCtorsUtil/getMethodParamCount meth))

(defn get-meth-param-types
  "Get the methods parameter types
 @param : the method we look for"
  {:added  "0.9.0"
   :static true}
  [meth]
  (MethsCtorsUtil/getMethodParamTypes meth))


(defn get-generic-meth-param-types
  "Get the methods parameter types
 @param : the method we look for"
  {:added  "0.9.0"
   :static true}
  [meth]
  (map represent/type-def-representation (MethsCtorsUtil/getMethGenericParmTypes meth)))

(defn get-method-return-type
  "Get the methods return type
@param : the method we look for"
  {:added  "0.9.0"
   :static true}
  [meth]
  (MethsCtorsUtil/getMethodReturnType meth))

(defn get-generic-method-return-type
  "Get the methods generic return type
@param : the method we look for"
  {:added  "0.9.0"
   :static true}
  [meth]
  (represent/type-def-representation
    (MethsCtorsUtil/getMethGenericReturnType meth)))

(defn get-method-modifier
  "Get the methods modifier
@param : the method we look for"
  {:added  "0.9.0"
   :static true}
  [meth]
  (MethsCtorsUtil/getMethodModifiers meth))

(defn get-meth-exception-types
  "Get the methods exception types
  @param : the ctor we look for"
  {:added  "0.9.0"
   :static true}
  [meth]
  (MethsCtorsUtil/getMethExceptionTypes meth))

(defn get-all-meth-and-type-modifiers
  "Get all the methods modifiers
 types attributes these values are translated partly in keywods
 especially for modifiers and attributes
 @param : the list of methods"
  [& meths]
  (map (fn [meth]
         {:meth-name  (get-meth-name meth)
          :parm-types (get-meth-param-types meth)
          :modifiers  (rtypes/get-item-modifiers
                        (get-method-modifier meth))
          :attribs    (rtypes/get-meth-attributes meth)
          :exception-types (get-meth-exception-types meth)
          :return-type (get-method-return-type meth)}    ;; ???????????????
         )
       meths))



(defn get-meth-gen-exception-types
  "Get the methods generic exception types
  @param : the ctor we look for"
  {:added  "0.9.0"
   :static true}
  [meth]
  (represent/type-def-representation
    (MethsCtorsUtil/getMethGenericExTypes meth)))