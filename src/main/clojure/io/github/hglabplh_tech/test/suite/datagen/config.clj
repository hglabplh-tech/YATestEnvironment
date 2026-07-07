(ns io.github.hglabplh_tech.test.suite.datagen.config
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.io File)
           (javax.xml XMLConstants)
           (javax.xml.parsers DocumentBuilderFactory)
           (javax.xml.transform.stream StreamSource)
           (javax.xml.validation SchemaFactory)
           (org.w3c.dom Element Node)))

(def default-config
  {:formats [:json]
   :sample-count 3
   :functions {}})

(defn- element-children [^Element element tag-name]
  (let [nodes (.getChildNodes element)]
    (->> (range (.getLength nodes))
         (map #(.item nodes %))
         (filter #(and (= (.getNodeType %) Node/ELEMENT_NODE)
                       (or (nil? tag-name)
                           (= tag-name (.getNodeName %))))))))

(defn- attr [^Element element name]
  (let [value (.getAttribute element name)]
    (when-not (str/blank? value) value)))

(defn- keywordize-format [value]
  (keyword (str/lower-case value)))

(defn- parse-number [value]
  (when value
    (Double/parseDouble value)))

(defn- split-values [value]
  (when value
    (->> (str/split value #",")
         (map str/trim)
         (remove str/blank?)
         vec)))

(defn validate-config!
  ([xml-path]
   (validate-config! xml-path (io/resource "test-suite-config.xsd")))
  ([xml-path xsd-source]
   (let [schema-factory (SchemaFactory/newInstance XMLConstants/W3C_XML_SCHEMA_NS_URI)
         schema (.newSchema schema-factory
                            (StreamSource.
                              (if (instance? java.net.URL xsd-source)
                                (.openStream xsd-source)
                                (io/input-stream xsd-source))))
         validator (.newValidator schema)]
     (.validate validator (StreamSource. (io/input-stream xml-path)))
     true)))

(defn- parse-dom [xml-path]
  (let [factory (doto (DocumentBuilderFactory/newInstance)
                  (.setNamespaceAware false))
        builder (.newDocumentBuilder factory)]
    (.getDocumentElement (.parse builder (File. (str xml-path))))))

(defn read-config
  "Reads and validates XML rules for generated test data."
  [xml-path]
  (validate-config! xml-path)
  (let [root (parse-dom xml-path)
        defaults-el (first (element-children root "defaults"))
        formats (if defaults-el
                  (let [configured (->> (element-children defaults-el "format")
                                        (map #(.getTextContent %))
                                        (map str/trim)
                                        (remove str/blank?)
                                        (map keywordize-format)
                                        vec)]
                    (if (seq configured) configured (:formats default-config)))
                  (:formats default-config))
        sample-count (if-let [sample-el (and defaults-el
                                             (first (element-children defaults-el "sample-count")))]
                       (Integer/parseInt (str/trim (.getTextContent sample-el)))
                       (:sample-count default-config))
        functions (reduce
                    (fn [acc ^Element function-el]
                      (let [ns-name (attr function-el "namespace")
                            fun-name (attr function-el "name")
                            params (reduce
                                     (fn [pacc ^Element param-el]
                                       (assoc pacc
                                              (attr param-el "name")
                                              {:type (some-> (attr param-el "type") keyword)
                                               :min (parse-number (attr param-el "min"))
                                               :max (parse-number (attr param-el "max"))
                                               :values (split-values (attr param-el "values"))
                                               :format (some-> (attr param-el "format") keywordize-format)}))
                                     {}
                                     (element-children function-el "param"))]
                        (assoc acc [ns-name fun-name] {:params params})))
                    {}
                    (element-children root "function"))]
    {:formats formats
     :sample-count sample-count
     :functions functions}))
