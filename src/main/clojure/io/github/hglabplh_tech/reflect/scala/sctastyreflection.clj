(ns io.github.hglabplh-tech.reflect.scala.sctastyreflection
  (:require [io.github.hglabplh-tech.reflect.scala.tasty-context-holder :as tasty-ctx]))

(defn safe-call
  [f default]
  (try
    (f)
    (catch Throwable _
      default)))


(defn scala-name
  [x]
  (when x
    (str x)))


(defn bool-call
  [f]
  (boolean
    (safe-call f false)))

(defn transform-type
  [t]
  (when t
    {:name
     (scala-name t)

     :jvm-class
     (-> t class .getName)

     :addon
     {:scala
      {:raw-type
       (str t)}}}))

(defn transform-type-parameter
  [tp]

  {:name
   (safe-call
     #(str (.name tp))
     nil)

   :variance
   (cond
     (bool-call #(.isCovariant tp))
     :covariant

     (bool-call #(.isContravariant tp))
     :contravariant

     :else
     :invariant)

   :bounds
   {:lower
    (safe-call
      #(transform-type (.lowerBound tp))
      nil)

    :upper
    (safe-call
      #(transform-type (.upperBound tp))
      nil)}})

(defn transform-parameter
  [p]

  {:name
   (safe-call
     #(str (.name p))
     nil)

   :type
   (safe-call
     #(transform-type (.declaredType p))
     nil)

   :modifiers
   {:implicit?
    (bool-call #(.isImplicit p))

    :using?
    (bool-call #(.isGivenOrUsing p))

    :repeated?
    (bool-call #(.isRepeated p))

    :by-name?
    (bool-call #(.isByName p))}})

(defn transform-annotation
  [annotation]

  {:type
   (safe-call
     #(str (.tpe annotation))
     nil)

   :arguments
   (safe-call
     #(vec (.arguments annotation))
     [])})

(defn transform-method
  [m]

  {:name
   (safe-call
     #(str (.name m))
     nil)

   :kind
   :method

   :owner
   (safe-call
     #(str (.owner m))
     nil)

   :signature
   (safe-call
     #(str (.signature m))
     nil)

   :type-parameters
   (safe-call
     #(mapv transform-type-parameter
            (.typeParams m))
     [])

   :parameter-lists
   (safe-call
     #(mapv
        (fn [plist]
          (mapv transform-parameter plist))
        (.paramSymss m))
     [])

   :return-type
   (safe-call
     #(transform-type (.resultType m))
     nil)

   :annotations
   (safe-call
     #(mapv transform-annotation
            (.annotations m))
     [])

   :modifiers
   {:private?
    (bool-call #(.isPrivate m))

    :protected?
    (bool-call #(.isProtected m))

    :final?
    (bool-call #(.isFinal m))

    :abstract?
    (bool-call #(.isAbstract m))

    :static?
    (bool-call #(.isStatic m))}

   :addon
   {:scala
    {:inline?
     (bool-call #(.isInline m))

     :transparent?
     (bool-call #(.isTransparent m))

     :extension?
     (bool-call #(.isExtensionMethod m))

     :given?
     (bool-call #(.isGiven m))

     :implicit?
     (bool-call #(.isImplicit m))}}})

(defn transform-field
  [f]

  {:name
   (safe-call
     #(str (.name f))
     nil)

   :kind
   :field

   :type
   (safe-call
     #(transform-type (.declaredType f))
     nil)

   :annotations
   (safe-call
     #(mapv transform-annotation
            (.annotations f))
     [])

   :modifiers
   {:mutable?
    (bool-call #(.isMutable f))

    :lazy?
    (bool-call #(.isLazy f))

    :final?
    (bool-call #(.isFinal f))

    :private?
    (bool-call #(.isPrivate f))

    :protected?
    (bool-call #(.isProtected f))}})

(defn transform-tree
  [tree]

  {:tree-class
   (-> tree class .getName)

   :show
   (str tree)

   :position
   (safe-call
     #(str (.sourcePos tree))
     nil)

   :type
   (safe-call
     #(transform-type (.tpe tree))
     nil)})


(defn constructor? [m]
  (= "<init>"
     (safe-call #(str (.name m)) "")))


(defn transform-constructor [ctor]
  (assoc
    (transform-method ctor)
    :kind :constructor))

(defn reflect-methods [class-symbol]
  (safe-call
    #(->> (.declaredMethods class-symbol)
          (remove constructor?)
          (mapv transform-method))
    []))


(defn reflect-constructors [class-symbol]
  (safe-call
    #(->> (.declaredMethods class-symbol)
          (filter constructor?)
          (mapv transform-constructor))
    []))


(defn reflect-fields [class-symbol]
  (safe-call
    #(->> (.declaredFields class-symbol)
          (mapv transform-field))
    []))


(defn reflect-scala-class [class-symbol]
  {:name
   (safe-call #(str (.fullName class-symbol)) "##none##")

   :simple-name
   (safe-call #(str (.name class-symbol)) "##none##")

   :kind
   (cond
     (boolean
       (safe-call #(.isTrait class-symbol) false))
     :trait

     (boolean
       (safe-call #(.isModuleClass class-symbol) false))
     :object

     (boolean
       (safe-call #(.isEnum class-symbol) false))
     :enum

     :else
     :class)

   :type-parameters
   (safe-call
     #(mapv
        (fn [tp]
          {:name (str (.name tp))
           :type (transform-type
                   (.declaredType tp))})
        (.typeParams class-symbol))
     [])

   :parents
   (safe-call
     #(mapv transform-type
            (.parentTypes class-symbol))
     [])

   :annotations
   (safe-call
     #(mapv str (.annotations class-symbol))
     [])

   ;; -------------------------
   ;; delegated reflection
   ;; -------------------------

   :constructors
   (reflect-constructors class-symbol)

   :fields
   (reflect-fields class-symbol)

   :methods
   (reflect-methods class-symbol)

   ;; Scala-specific metadata
   :addon
   {:scala
    {:tasty true

     :trait?
     (boolean
       (safe-call #(.isTrait class-symbol) false))

     :object?
     (boolean
       (safe-call #(.isModuleClass class-symbol) false))

     :enum?
     (boolean
       (safe-call #(.isEnum class-symbol) false))

     :case?
     (boolean
       (safe-call #(.isCase class-symbol) false))

     :sealed?
     (boolean
       (safe-call #(.isSealed class-symbol) false))

     :abstract?
     (boolean
       (safe-call #(.isAbstract class-symbol) false))

     :final?
     (boolean
       (safe-call #(.isFinal class-symbol) false))}}})

(defn reflect-scala-class-by-name
  [^String class-name]
  (let [class-symbol (tasty-ctx/get-class-symbol class-name)]

    (when-not class-symbol
      (throw
        (ex-info
          (str "Scala class not found: " class-name)
          {:class-name class-name})))

    (reflect-scala-class class-symbol)))