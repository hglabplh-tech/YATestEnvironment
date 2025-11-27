(ns io.github.hglabplh-tech.reflect.clojure.api.convert-java-cloj
  (:require [io.github.hglabplh-tech.reflect.clojure.api.reflect-annotation :as annot]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-class :as rcl]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-field :as fields]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-meths-ctors :as meths]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-special-forms :as er-refl]
            [io.github.hglabplh-tech.reflect.clojure.general.cloj-java-represent :as represent]))

(declare retrieve-class-info)

(defn retrieve-ctor-info
  "retrieve the information about the constructor of a class like
   ctor name parameter types annotations attributes modifiers and so on"
  {:added  "1.1.0"
   :static true}
  [ctor]
  (let [ctor-name (meths/get-ctor-name ctor)
        ctor-general (meths/get-all-ctor-and-type-modifiers ctor)
        ctor-annots (annot/get-ctor-annots ctor)
        ctor-p-annots (annot/get-ctor-param-annots ctor)
        ctor-generic-ex-types (meths/get-ctor-gen-exception-types ctor)
        ctor-declaring-class (meths/get-ctor-declaring-class ctor)
        ]
    {:ctor
     {:obj-name            ctor-name
      :general             ctor-general
      :parm-annot          ctor-p-annots
      :annots              ctor-annots
      :gen-exception-types ctor-generic-ex-types
      :declaring-class     ctor-declaring-class}}
    ))

(defn retrieve-method-info
  "retrieve the information about the method of a class like
  method name parameter types return types annotations attributes modifiers and so on"
  {:added  "1.1.0"
   :static true}
  [method]
  (let [meth-name (meths/get-meth-name method)
        meth-general (meths/get-all-meth-and-type-modifiers method)
        meth-generic-parm-types (meths/get-generic-meth-param-types method)
        meth-p-annots (annot/get-meth-param-annots method)
        meth-annots (annot/get-method-annots method)
        meth-generic-ret-type (represent/type-def-representation
                                (meths/get-generic-method-return-type method))
        meth-generic-ex-types (map represent/type-def-representation
                                (meths/get-meth-gen-exception-types method))
        meth-declaring-class (meths/get-meth-declaring-class method)
        ]
    {:method
     {:obj-name            meth-name
      :general             meth-general
      :gen-parm-types      meth-generic-parm-types
      :parm-annot          meth-p-annots :annots meth-annots
      :gen-return-type     meth-generic-ret-type
      :gen-exception-types meth-generic-ex-types
      :declaring-class     meth-declaring-class}}
    ))

(defn retrieve-field-info
  "retrieve the information about the field of a class like
 field name field type annotations modifiers and generics"
  {:added  "1.1.0"
   :static true}
  [field]
  (let [fld-name (fields/get-field-name field)
        fld-general (fields/get-all-fields-and-type-modifiers field)
        fld-gen-type (fields/get-generic-type field)
        fld-annots (fields/get-all-annots field)
        ]
    {:field
     {:obj-name fld-name
      :general  fld-general
      :gen-type fld-gen-type
      :annots   fld-annots}}
    ))

(defn retrieve-direct-class-parameters
  "retrieve the direct parameters of a constructor or method"
  {:added  "1.1.0"
   :static true}
  [clazz-util]
  (let [class-name (rcl/get-class-name clazz-util)
        attributes (rcl/get-class-attributes (rcl/get-the-class clazz-util))
        interfaces (rcl/get-interfaces clazz-util)
        super-class (rcl/get-super-class clazz-util)
        gen-interfaces (rcl/get-generic-interfaces clazz-util)
        gen-super-class (rcl/get-generic-super-class clazz-util)
        enclosing-class (rcl/get-enclosing-class clazz-util)
        enclosing-ctor (rcl/get-enclosing-constructor clazz-util)
        enclosing-meth (rcl/get-enclosing-method clazz-util)
        enum-specs (er-refl/analyze-enum (rcl/get-the-class clazz-util))
        record-specs (er-refl/analyze-record (rcl/get-the-class clazz-util))]
    {:class
     {:obj-name              class-name
      :attributes            attributes
      :super                 super-class
      :interface             interfaces
      :gen-interface         gen-interfaces
      :gen-super             gen-super-class
      :enclosing-class       enclosing-class
      :enclosing-constructor enclosing-ctor
      :enclosing-method      enclosing-meth
      :enum-specs            enum-specs
      :record-specs          record-specs}
     }
    ))
(defn retrieve-class-body [constructors class-fields class-methods inner-classes]
  (let [ctor-infos (map retrieve-ctor-info constructors)
        field-infos (map retrieve-field-info class-fields)
        method-infos (map retrieve-method-info class-methods)
        class-infos (map (fn [clazz]
                           (retrieve-class-info (rcl/get-class-util-by-class clazz)))
                         inner-classes)]

    {:body {:ctor-infos   ctor-infos
            :field-infos  field-infos
            :method-infos method-infos
            :class-infos  class-infos}}
    ))


(defn retrieve-class-info [clazz-util]
  (let [class-def (retrieve-direct-class-parameters clazz-util)
        class-body (retrieve-class-body (rcl/get-all-ctors clazz-util)
                                        (rcl/get-all-fields clazz-util)
                                        (rcl/get-all-methods clazz-util)
                                        (rcl/get-all-sub-classes clazz-util))]
    {:class-data
     {:definition class-def
      :cl-body
      class-body}})
  )
