(ns io.github.hglabplh-tech.reflect.general.unified-struct-def
  (:import (java.lang.reflect Type
                              TypeVariable
                              GenericArrayType
                              ParameterizedType
                              )))

(def languages [:clojure :java :kotlin :scala :python])
(def assoc-file-definition
  "The definition of the file either class definition or namespace functions classes in compound"
  [:namespace-class
   :function-class-catalog
   :language
   ])

(def assoc-func-class-catalog
    [:class
     :enum
     :lambda
     :object
     :function
     :var
     :member-var
     :record
     ])
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

(def assoc-param-def
  "the definition of a parameter"
  [:name
   :position
   :type
   :attributes
   :constraints
   :add-on])

(def assoc-param-addon-def
  "the definition of a parameter"
  :cloj-predicate
  :java-annot
  )
(def assoc-meth-fun-def
"here the definition param keys for functions and methods"
[:parameter-count
 :attributes
 :parameter-type                                            ;; positional / named
 :parameter-definitions                                     ;; a sequence of parameter-def
 ])

(def assoc-enumeration-def
  "description of a enum constant that is reflected"
  [:ordinal
   :class-descr
   :enum-descr])
