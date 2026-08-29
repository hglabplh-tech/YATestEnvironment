(ns io.github.hglabplh-tech.reflect.kotlin.kreflection
  (:import (kotlin.metadata Attributes KmClass KmClassifier$Class KmClassifier$TypeAlias
                            KmClassifier$TypeParameter KmConstructor KmFunction KmProperty KmType KmValueParameter)
           (kotlin.metadata.jvm KotlinClassMetadata KotlinClassMetadata$Class KotlinClassMetadata$FileFacade)))

(declare transform-km-class)

;; 1. Helper to resolve types cleanly to a string representation
(defn- resolve-type-name [^KmType km-type]
  (if (nil? km-type)
    "unknown"
    (let [classifier (.getClassifier km-type)]
      (cond
        (instance? KmClassifier$Class classifier)       (.getName ^KmClassifier$Class classifier)
        (instance? KmClassifier$TypeParameter classifier) (str "T_" (.getId ^KmClassifier$TypeParameter classifier))
        (instance? KmClassifier$TypeAlias classifier)     (.getName ^KmClassifier$TypeAlias classifier)
        :else "unknown"))))

(defn- transform-value-parameter [^KmValueParameter param]
  {:name        (.getName param)
   :type        (resolve-type-name (.getType param))
   :is-nullable (and (.getType param) (Attributes/isNullable (.getType param)))})

(defn- enum->keyword [x]
  (when x
    (-> x
        .name
        clojure.string/lower-case
        keyword)))

;; 2. Specific transformers for each internal component
(defn- transform-property
  [^KmProperty prop]

  (let [^KmType return-type (.getReturnType prop)]

    {:name
     (.getName prop)

     :visibility
     (some-> (Attributes/getVisibility prop)
             str)

     :modality
     (some-> (Attributes/getModality prop)
             str)

     :is-var
     (Attributes/isVar prop)

     :is-const
     (Attributes/isConst prop)

     :is-lateinit
     (Attributes/isLateinit prop)

     :is-delegated
     (Attributes/isDelegated prop)

     :return-type
     (resolve-type-name return-type)

     :is-nullable
     (Attributes/isNullable return-type)}))


(defn- transform-function [^KmFunction func]
  {:name        (.getName func)
   :return-type (resolve-type-name (.getReturnType func))
   :parameters  (mapv transform-value-parameter (.getValueParameters func))})


(defn- transform-constructor [^KmConstructor ctor]
  {
   :parameters (mapv transform-value-parameter (.getValueParameters ctor))})

;; 3. Core Class Transformer
(defn- transform-km-class [^KmClass km-class]
  {:name         (.getName km-class)
   :constructors (mapv transform-constructor (.getConstructors km-class))
   :properties   (mapv transform-property (.getProperties km-class))
   :functions    (mapv transform-function (.getFunctions km-class))
   :supertypes   (mapv resolve-type-name (.getSupertypes km-class))})

;; 4. Main Entry Point Function
(defn parse-kotlin-metadata [^Class clazz]
  (if-let [metadata-anno (.getAnnotation clazz kotlin.Metadata)]
    (let [metadata (KotlinClassMetadata/readLenient metadata-anno)]
      (cond
        (instance? KotlinClassMetadata$Class metadata)
        {:metadata-type :class
         :class-details (transform-km-class (.getKmClass ^KotlinClassMetadata$Class metadata))}

        (instance? KotlinClassMetadata$FileFacade metadata)
        {:metadata-type :file-facade
         :details "Top-level Kotlin file properties/functions (Package-level scope)"}

        :else
        {:metadata-type :unknown
         :class-name (.getName (class metadata))}))
    {:error "Not a Kotlin class or no metadata found"}))

(defn parse-kotlin-meta-by-name [ ^String class-name]
  (let [clazz (Class/forName class-name)]
    (parse-kotlin-metadata clazz)
    ))