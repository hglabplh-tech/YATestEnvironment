(ns io.github.hglabplh-tech.reflect.scala.tasty-context-holder
   (:require [clojure.contrib.classpath :as cp]
             [clojure.string :as str])
   (:import
     [java.nio.file Path Paths FileSystems]
     [java.net URI]
     [scala.jdk.javaapi CollectionConverters]
     [tastyquery.jdk ClasspathLoaders]
     [tastyquery Contexts$Context]))

  (defn- application-classpath-strings []
   (str/split
     (System/getProperty "java.class.path")
     (re-pattern
       (java.util.regex.Pattern/quote
         java.io.File/pathSeparator))))

;; use this later on to keep code shorter and more compact
 (defn- java-list->scala-list
   [^java.util.List xs]
   (-> (CollectionConverters/asScala xs)
       (.toList)))


 (defn create-tasty-context []
   (Contexts$Context/initialize
     (ClasspathLoaders/read
       (-> (CollectionConverters/asScala
             ^java.util.List
             (mapv
               #(Paths/get
                  ^String %
                  (make-array String 0))
               (application-classpath-strings)))
           (.toList)))))

 (defn- find-class-symbol
   [^String fully-qualified-name]
   (let [ctx (create-tasty-context)]
     (.findTopLevelClass ctx fully-qualified-name)))

 (defn find-class-symbol!
   [^String fully-qualified-name]

   (let [ctx (create-tasty-context)
         sym (.findTopLevelClass ctx fully-qualified-name)]

     (when-not sym
       (throw
         (ex-info
           (str "Scala class not found: " fully-qualified-name)
           {:class-name fully-qualified-name})))

     sym))

 (defn get-class-symbol [class-name]
   (find-class-symbol!
     class-name))