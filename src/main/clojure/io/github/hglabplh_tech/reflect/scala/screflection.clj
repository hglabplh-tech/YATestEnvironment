(ns io.github.hglabplh-tech.reflect.scala.screflection
  (:require [clojure.string :as str]))

(defn enum->keyword [x]
  (when x
    (-> x
        str
        str/lower-case
        (str/replace "_" "-")
        keyword)))

(defn transform-annotation [annotation]
  {:type
   (str (.tpe annotation))

   :arguments
   (try
     (vec (.arguments annotation))
     (catch Throwable _
       []))})

(defn transform-type [t]
  (when t
    {:display-name
     (str t)

     :type-class
     (-> t class .getName)

     :addon
     {:scala
      {:raw-representation
       (str t)}}}))

(defn transform-type-param [param]
  {:name
   (str (.name param))

   :variance
   (cond
     (try (.isCovariant param)
          (catch Throwable _ false))
     :covariant

     (try (.isContravariant param)
          (catch Throwable _ false))
     :contravariant

     :else
     :invariant)

   :bounds
   {:lower
    (try
      (transform-type (.lowerBound param))
      (catch Throwable _ nil))

    :upper
    (try
      (transform-type (.upperBound param))
      (catch Throwable _ nil))}})

(defn transform-parameter [param]
  {:name
   (str (.name param))

   :type
   (try
     (transform-type (.declaredType param))
     (catch Throwable _
       nil))

   :modifiers
   {:implicit?
    (try (.isImplicit param)
         (catch Throwable _ false))

    :using?
    (try (.isGivenOrUsing param)
         (catch Throwable _ false))

    :repeated?
    (try (.isRepeated param)
         (catch Throwable _ false))

    :by-name?
    (try (.isByName param)
         (catch Throwable _ false))}})

(defn transform-method [method]
  {:name
   (str (.name method))

   :kind
   :method

   :owner
   (try
     (str (.owner method))
     (catch Throwable _
       nil))

   :visibility
   (try
     (enum->keyword (.visibility method))
     (catch Throwable _
       nil))

   :type-parameters
   (try
     (mapv transform-type-param
           (.typeParams method))
     (catch Throwable _
       []))

   :parameter-lists
   (try
     (mapv
       (fn [plist]
         (mapv transform-parameter plist))
       (.paramSymss method))
     (catch Throwable _
       []))

   :return-type
   (try
     (transform-type (.declaredType method))
     (catch Throwable _
       nil))

   :annotations
   (try
     (mapv transform-annotation
           (.annotations method))
     (catch Throwable _
       []))

   :modifiers
   {:abstract?
    (try (.isAbstract method)
         (catch Throwable _ false))

    :final?
    (try (.isFinal method)
         (catch Throwable _ false))

    :private?
    (try (.isPrivate method)
         (catch Throwable _ false))

    :protected?
    (try (.isProtected method)
         (catch Throwable _ false))}

   :addon
   {:scala
    {:inline?
     (try (.isInline method)
          (catch Throwable _ false))

     :transparent?
     (try (.isTransparent method)
          (catch Throwable _ false))

     :extension?
     (try (.isExtensionMethod method)
          (catch Throwable _ false))

     :given?
     (try (.isGiven method)
          (catch Throwable _ false))}}})

(defn transform-field [field]
  {:name
   (str (.name field))

   :kind
   :field

   :type
   (try
     (transform-type (.declaredType field))
     (catch Throwable _
       nil))

   :visibility
   (try
     (enum->keyword (.visibility field))
     (catch Throwable _
       nil))

   :annotations
   (try
     (mapv transform-annotation
           (.annotations field))
     (catch Throwable _
       []))

   :modifiers
   {:mutable?
    (try (.isMutable field)
         (catch Throwable _ false))

    :final?
    (try (.isFinal field)
         (catch Throwable _ false))

    :lazy?
    (try (.isLazy field)
         (catch Throwable _ false))}})

(defn transform-class [cls]
  {:name
   (str (.fullName cls))

   :simple-name
   (str (.name cls))

   :kind
   (cond
     (try (.isTrait cls)
          (catch Throwable _ false))
     :trait

     (try (.isModuleClass cls)
          (catch Throwable _ false))
     :object

     (try (.isEnum cls)
          (catch Throwable _ false))
     :enum

     :else
     :class)

   :owner
   (try
     (str (.owner cls))
     (catch Throwable _
       nil))

   :visibility
   (try
     (enum->keyword (.visibility cls))
     (catch Throwable _
       nil))

   :type-parameters
   (try
     (mapv transform-type-param
           (.typeParams cls))
     (catch Throwable _
       []))

   :parents
   (try
     (mapv transform-type
           (.parents cls))
     (catch Throwable _
       []))

   :annotations
   (try
     (mapv transform-annotation
           (.annotations cls))
     (catch Throwable _
       []))

   :constructors
   (try
     (->> (.declaredMethods cls)
          (filter #(= "<init>" (str (.name %))))
          (mapv transform-method))
     (catch Throwable _
       []))

   :methods
   (try
     (->> (.declaredMethods cls)
          (remove #(= "<init>" (str (.name %))))
          (mapv transform-method))
     (catch Throwable _
       []))

   :fields
   (try
     (mapv transform-field
           (.declaredFields cls))
     (catch Throwable _
       []))

   :modifiers
   {:abstract?
    (try (.isAbstract cls)
         (catch Throwable _ false))

    :final?
    (try (.isFinal cls)
         (catch Throwable _ false))

    :sealed?
    (try (.isSealed cls)
         (catch Throwable _ false))}

   :addon
   {:scala
    {:trait?
     (try (.isTrait cls)
          (catch Throwable _ false))

     :object?
     (try (.isModuleClass cls)
          (catch Throwable _ false))

     :enum?
     (try (.isEnum cls)
          (catch Throwable _ false))

     :case?
     (try (.isCase cls)
          (catch Throwable _ false))

     :sealed?
     (try (.isSealed cls)
          (catch Throwable _ false))}}})
