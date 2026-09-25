(ns io.github.hglabplh-tech.reflect.scala.screflection
  (:import
    [io.github.hglabplh_tech.reflect.reflscala.bridge ScalaTastyBridge]
    [java.util Map List ArrayList]))

(declare java->clj)

(defn- java-map->clj
  "Convert a java.util.Map recursively into a Clojure map.
   String keys are converted to keywords."
  [^Map m]
  (into {}
        (map (fn [[k v]]
               [(if (string? k) (keyword k) k)
                (java->clj v)]))
        m))

(defn java->clj
  "Recursively converts Java collections returned by the Scala bridge
   into ordinary Clojure data structures."
  [x]
  (cond
    (instance? java.util.Map x)
    (java-map->clj x)

    (instance? java.util.List x)
    (mapv java->clj x)

    :else
    x))

(defn inspect-tasty-file
  "Inspect one Scala 3 .tasty file.
   Returns normalized Clojure data."
  [filename]
  (->> (ScalaTastyBridge/inspectTastyFile (str filename))
       (mapv java->clj)))

(defn inspect-tasty-files
  "Inspect several Scala 3 .tasty files."
  [files]
  (let [paths (ArrayList.)]
    (doseq [f files]
      (.add paths (str f)))
    (->> (ScalaTastyBridge/inspectTastyFiles paths)
         (mapv java->clj))))

(defn normalize-scala-element
  "Normalize Scala-specific reflection entries into the unified model."
  [m]
  (case (:kind m)
    "top-level-function"
    (assoc m
      :kind :function
      :function-kind :top-level
      :language :scala)

    "method"
    (assoc m
      :kind :function
      :function-kind :method
      :language :scala)

    "lambda"
    (assoc m
      :kind :function
      :function-kind :lambda
      :language :scala)

    "class"
    (assoc m
      :kind :type
      :type-kind :class
      :language :scala)

    "trait"
    (assoc m
      :kind :type
      :type-kind :trait
      :language :scala)

    "enum"
    (assoc m
      :kind :type
      :type-kind :enum
      :language :scala)

    "val"
    (assoc m
      :kind :variable
      :variable-kind :val
      :language :scala)

    "var"
    (assoc m
      :kind :variable
      :variable-kind :var
      :language :scala)

    (assoc m :language :scala)))

(defn inspect-scala-file
  "Inspect one TASTy file and normalize all returned elements."
  [filename]
  (mapv normalize-scala-element
        (inspect-tasty-file filename)))

(defn inspect-scala-files
  "Inspect several TASTy files and normalize all returned elements."
  [files]
  (mapv normalize-scala-element
        (inspect-tasty-files files)))

(defn only-functions
  "Return all methods, top-level functions and lambdas."
  [elements]
  (filterv #(= :function (:kind %)) elements))

(defn only-types
  "Return reflected classes, traits and enums."
  [elements]
  (filterv #(= :type (:kind %)) elements))

(defn only-lambdas
  "Return only lambda expressions."
  [elements]
  (filterv #(= :lambda (:function-kind %)) elements))

(defn only-top-level-functions
  "Return only Scala top-level functions."
  [elements]
  (filterv #(= :top-level (:function-kind %)) elements))
