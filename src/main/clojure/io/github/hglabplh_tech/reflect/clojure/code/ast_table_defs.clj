(ns io.github.hglabplh_tech.reflect.clojure.code.ast-table-defs
  (:import (java.lang.reflect Type
                              TypeVariable
                              GenericArrayType
                              ParameterizedType
                              )))


(def assoc-class-definition
  "here the class definition tags for the class header are defined"
  [:enclosing-method
   :interface
   :record-specs
   :super
   :enclosing-class
   :enclosing-constructor
   :gen-super
   :attributes
   :obj-name
   :gen-interface
   :enum-specs])

(def assoc-ctor
  "the description of the constructor definition"
  [:obj-name
  :general
  :parm-annot
  :annots
  :gen-exception-types
  :declaring-class])

(def assoc-class-body
  "here the class definition tags for the class body are defined"
  [:field-infos
   :ctor-infos
   :method-infos])

(def assoc-enumeration-def
  "description of a enum constant that is reflected"
  [:ordinal
   :class-descr
   :enum-descr])
