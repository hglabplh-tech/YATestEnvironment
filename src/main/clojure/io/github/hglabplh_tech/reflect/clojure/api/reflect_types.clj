(ns io.github.hglabplh_tech.reflect.clojure.api.reflect-types
  (:import (io.github.hglabplh_tech.reflect.clojure.api.utils ClassUtil FieldsUtil MethsCtorsUtil)
           (java.lang.reflect Constructor Method Modifier AnnotatedType)))


(defn is-annotation
  "Is class a annotation
   @param : the class "
  {:added  "0.9.0"
   :static true}
  [^Class class-to-check]
  (ClassUtil/isClassAnnotation class-to-check))


(defn is-anonymous
  "Is class a anonymous class
   @param : the class "
  {:added  "0.9.0"
   :static true}
  [^Class class-to-check]
  (ClassUtil/isClassAnonymous class-to-check))

(defn is-array
  "Is class a array
   @param : the class "
  {:added  "0.9.0"
   :static true}
  [^Class class-to-check]
  (ClassUtil/isClassArray class-to-check))

(defn is-enum
  "Is class a enum
   @param : the class "
  {:added  "0.9.0"
   :static true}
  [^Class class-to-check]
  (ClassUtil/isClassEnum class-to-check))

(defn is-interface
  "Is class a interface
   @param : the class "
  {:added  "0.9.0"
   :static true}
  [^Class class-to-check]
  (ClassUtil/isClassInterface class-to-check))

(defn is-local
  "Is class a local class
   @param : the class "
  {:added  "0.9.0"
   :static true}
  [^Class class-to-check]
  (ClassUtil/isClassLocal class-to-check))


(defn is-member
  "Is class a member class
   @param : the class "
  {:added  "0.9.0"
   :static true}
  [^Class class-to-check]
  (ClassUtil/isClassMember class-to-check))

(defn is-primitive
  "Is class a primitive class
   @param : the class "
  {:added  "0.9.0"
   :static true}
  [^Class class-to-check]
  (ClassUtil/isClassPrimitive class-to-check))

(defn is-sealed
  "Is class a sealed class
   @param : the class "
  {:added  "0.9.0"
   :static true}
  [^Class class-to-check]
  (ClassUtil/isClassSealed class-to-check))

(defn is-synthetic
  "Is class a synthetic class
   @param : the class "
  {:added  "0.9.0"
   :static true}
  [^Class class-to-check]
  (ClassUtil/isClassSynthetic class-to-check))

(defn is-record
  "Is the class a record
 @param : the class"
  {:added  "0.9.0"
   :static true}
  [class]
  (ClassUtil/isClassRecord class))

(defn is-synthetic-ctor
  "Is constructor synthetic
   @param : the constructor "
  {:added  "0.9.0"
   :static true}
  [^Constructor ctor-to-check]
  (MethsCtorsUtil/isSyntheticCtor ctor-to-check))

(defn is-varargs-ctor
  "Has constructor varargs
  @param : the constructor "
  {:added  "0.9.0"
   :static true}
  [^Constructor ctor-to-check]
  (MethsCtorsUtil/isVarArgsCtor ctor-to-check))

(defn is-synthetic-meth
  "Is method synthetic
  @param : the method "
  {:added  "0.9.0"
   :static true}
  [^Method meth-to-check]
  (MethsCtorsUtil/isSyntheticMethod meth-to-check))

(defn is-varargs-meth
  "Has method varargs
  @param : the method "
  {:added  "0.9.0"
   :static true}
  [^Method meth-to-check]
  (MethsCtorsUtil/isVarArgsMethod meth-to-check))

(defn is-default-meth
  "Is method a default method
  @param : the method "
  {:added  "0.9.0"
   :static true}
  [^Method meth-to-check]
  (MethsCtorsUtil/isDefaultMethod meth-to-check))

(defn is-bridge-meth
  "Is method a bridge method
  @param : the method "
  {:added  "0.9.0"
   :static true}
  [^Method meth-to-check]
  (MethsCtorsUtil/isBridgeMethod meth-to-check))

(defn is-enum-constant [field]
  "Is field a enum constant
  @param : the field "
  {:added  "0.9.0"
   :static true}
  (FieldsUtil/isEnumConst field))

(defn is-synthetic-field
  "Is field a enum constant
 @param : the field "
  {:added  "0.9.0"
   :static true}
  [field]
  (FieldsUtil/isSynthetic field))



(defn get-type-readable [type-object]
  (ClassUtil/getValuesOfInterface type-object))

(defn- get-modifiers-readable
  "Get a readable format with modifiers integer as
  set of keywods with values
 @param : the modifiers integer "
  {:added  "0.9.0"
   :static true}
  [mod-int]
  (let [mod-map {:abstract     (Modifier/isAbstract mod-int)
                 :final        (Modifier/isFinal mod-int)
                 :interface    (Modifier/isInterface mod-int)
                 :native       (Modifier/isNative mod-int)
                 :transient    (Modifier/isTransient mod-int)
                 :strict       (Modifier/isStrict mod-int)
                 :synchronized (Modifier/isSynchronized mod-int)
                 :static       (Modifier/isStatic mod-int)
                 :volatile     (Modifier/isVolatile mod-int)
                 :public       (Modifier/isPublic mod-int)
                 :private      (Modifier/isPrivate mod-int)
                 :protected    (Modifier/isProtected mod-int)}]
    mod-map))

(defn- get-class-attributes-readable
  "Get a readable format with class attributes as
  set of keywods with values
  @param : the class "
  {:added  "0.9.0"
   :static true}
  [class-to-check]
  (let [class-attr-map {:attr-annotation (is-annotation class-to-check)
                        :attr-anonymous  (is-anonymous class-to-check)
                        :attr-array      (is-array class-to-check)
                        :attr-enum       (is-enum class-to-check)
                        :attr-interface  (is-interface class-to-check)
                        :attr-local      (is-local class-to-check)
                        :attr-member     (is-member class-to-check)
                        :attr-primitive  (is-primitive class-to-check)
                        :attr-sealed     (is-sealed class-to-check)
                        :attr-synthetic  (is-synthetic class-to-check)
                        :attr-record     (is-record class-to-check)}]
    class-attr-map))

(defn- get-ctor-attributes-readable
  "Get a readable format with constructor attributes
  set of keywods with values
  @param : constructor "
  {:added  "0.9.0"
   :static true}
  [ctor-to-check]
  (let [ctor-attr-map {:attr-varargs   (is-varargs-ctor ctor-to-check)
                       :attr-synthetic (is-synthetic-ctor ctor-to-check)
                       }]
    ctor-attr-map))

(defn- get-meth-attributes-readable [meth-to-check]
  "Get a readable format with method attributes
 set of keywods with values
 @param : the method "
  {:added  "0.9.0"
   :static true}
  (let [meth-attr-map {:attr-varargs   (is-varargs-meth meth-to-check)
                       :attr-synthetic (is-synthetic-meth meth-to-check)
                       :attr-default   (is-default-meth meth-to-check)
                       :attr-bridge    (is-bridge-meth meth-to-check)
                       }]
    meth-attr-map))

(defn- get-field-attributes-readable
  "Get a readable format with field attributes
  set of keywods with values
  @param : the field "
  {:added  "0.9.0"
   :static true}
  [field]
  (let [ctor-attr-map {:attr-enum-const (is-enum-constant field)
                       :attr-synthetic  (is-synthetic-field field)
                       }]
    ctor-attr-map))



(defn attributes-bool-filter
  "Takes a map {key, bool} returns a vector with all keys there the value is true "
  {:added  "0.9.0"
   :static true}
  [the-map-to-filter]
  (loop [modifier-keys (keys the-map-to-filter)
         result []]
    (if (empty? modifier-keys)
      result
      (let [act-key (first modifier-keys)
            value (get the-map-to-filter act-key)]
        (recur (rest modifier-keys) (if value
                                      (conj result act-key)
                                      result))
        ))
    )
  )



(defn get-item-modifiers
  "get a better readable and processable format of modifiers "
  {:added  "0.9.0"
   :static true}
  [mod-int]
  (attributes-bool-filter (get-modifiers-readable mod-int)))

(defn get-class-attributes
  "get a better readable and processable format of class attributes"
  {:added  "0.9.0"
   :static true}
  [class-to-check]
  (attributes-bool-filter (get-class-attributes-readable class-to-check)))

(defn get-ctor-attributes
  "get a better readable and processable format of constructor attributes"
  {:added  "0.9.0"
   :static true}
  [ctor-to-check]
  (attributes-bool-filter (get-ctor-attributes-readable ctor-to-check)))

(defn get-meth-attributes
  "get a better readable and processable format of method attributes"
  {:added  "0.9.0"
   :static true}
  [meth-to-check]
  (attributes-bool-filter (get-meth-attributes-readable meth-to-check)))

(defn get-field-attributes
  "get a better readable and processable format of field attributes"
  {:added  "0.9.0"
   :static true}
  [field]
  (attributes-bool-filter (get-field-attributes-readable field)))

