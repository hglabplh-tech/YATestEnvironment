(ns io.github.hglabplh_tech.test.suite.datagen.pipeline
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [io.github.hglabplh-tech.reflect.clojure.api.convert-java-cloj :as java-convert]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-class :as reflect-class]
            [io.github.hglabplh_tech.test.suite.datagen.config :as config]
            [io.github.hglabplh_tech.test.suite.datagen.sqlite :as sqlite]
            [io.github.hglabplh_tech.test.suite.spy-and-mock.real-fun-checkers :as fc]
            [io.github.hglabplh_tech.test.suite.spy-and-mock.mocking-jay :as spy])
  (:import (java.lang.reflect Method Modifier)))

(defn- ns-form-from-file [file]
  (with-open [reader (java.io.PushbackReader. (io/reader file))]
    (read reader)))

(defn namespace-symbol-from-file [file]
  (let [form (ns-form-from-file file)]
    (when (= 'ns (first form))
      (second form))))

(defn load-analysis-files!
  "Loads one or more Clojure files and returns their declared namespace symbols."
  [files]
  (vec
   (for [file files
         :let [ns-sym (namespace-symbol-from-file file)]]
     (do
       (load-file (str file))
       ns-sym))))

(defn- type-name [schema-type]
  (cond
    (class? schema-type) (.getName ^Class schema-type)
    (symbol? schema-type) (name schema-type)
    (keyword? schema-type) (name schema-type)
    (seq? schema-type) (pr-str schema-type)
    :else (str schema-type)))

(defn- normalized-type [schema-type]
  (let [value (str/lower-case (type-name schema-type))]
    (cond
      (str/includes? value "bool") :boolean
      (str/includes? value "string") :string
      (str/includes? value "str") :string
      (str/includes? value "integer") :integer
      (str/includes? value "int") :integer
      (str/includes? value "long") :integer
      (str/includes? value "number") :number
      (str/includes? value "num") :number
      (str/includes? value "uuid") :uuid
      (str/includes? value "symbol") :symbol
      (str/includes? value "set") :set
      (str/includes? value "map") :map
      (str/includes? value "function") :function
      :else :any)))

(defn- parse-schema-meta [metadata]
  (when-let [schema (:schema metadata)]
    (try
      (let [schema-map (first (fc/parse-meta-to-map (fc/parse-base-schema schema)))]
        {:return-type (:return-type schema-map)
         :args (:arg-info-map schema-map)})
      (catch Exception _
        nil))))

(defn- public-functions [ns-sym]
  (->> (ns-publics ns-sym)
       (filter (fn [[_ var-ref]]
                 (and (fn? @var-ref)
                      (seq (:arglists (meta var-ref))))))
       (sort-by (comp str key))))

(defn- configured-param-rule [cfg ns-name fun-name param-name]
  (get-in cfg [:functions [ns-name fun-name] :params param-name]))

(defn- number-values [rule integer? sample-count]
  (let [min-value (long (or (:min rule) -1))
        max-value (long (or (:max rule) 1))
        middle (long (Math/floor (/ (+ min-value max-value) 2.0)))
        values (distinct [min-value middle max-value])]
    (vec (take sample-count (if integer? values (map double values))))))

(defn- coerce-config-value [type-key value]
  (case type-key
    :boolean (Boolean/parseBoolean value)
    :integer (Long/parseLong value)
    :number (Double/parseDouble value)
    :symbol (symbol value)
    :uuid (java.util.UUID/fromString value)
    value))

(defn- default-values [type-key rule sample-count]
  (if-let [values (seq (:values rule))]
    (vec (map #(coerce-config-value type-key %) values))
    (case type-key
      :boolean [true false]
      :integer (number-values rule true sample-count)
      :number (number-values rule false sample-count)
      :string ["" "sample" "sample-long-value"]
      :symbol ['alpha 'beta 'gamma]
      :uuid [(java.util.UUID/fromString "00000000-0000-0000-0000-000000000001")
             (java.util.UUID/fromString "00000000-0000-0000-0000-000000000002")]
      :set [#{} #{"sample"} #{"sample" "other"}]
      :map [{} {:sample true}]
      :function [(fn [& _] nil)]
      :any [nil true "sample" 0])))

(defn- analyze-var [cfg ns-sym fun-name var-ref]
  (let [metadata (meta var-ref)
        schema-info (parse-schema-meta metadata)
        arg-names (or (seq (get-in schema-info [:args :names-vect]))
                      (map name (first (:arglists metadata))))
        arg-types (or (seq (get-in schema-info [:args :scheme-types]))
                      (repeat (count arg-names) :any))
        ns-name (str ns-sym)
        fn-name (str fun-name)
        sample-count (:sample-count cfg)]
    {:namespace ns-name
     :name fn-name
     :file (:file metadata)
     :line (:line metadata)
     :column (:column metadata)
     :return-type (type-name (:return-type schema-info))
     :parameters
     (vec
      (map-indexed
       (fn [idx [param-name schema-type]]
         (let [name-str (str param-name)
               rule (configured-param-rule cfg ns-name fn-name name-str)
               type-key (or (:type rule) (normalized-type schema-type))
               values (default-values type-key rule sample-count)]
           {:position idx
            :name name-str
            :type (name type-key)
            :schema-type (type-name schema-type)
            :values values}))
       (map vector arg-names arg-types)))}))

(defn analyze-namespaces
  "Analyzes public functions in namespace symbols using function metadata."
  ([namespaces]
   (analyze-namespaces namespaces config/default-config))
  ([namespaces cfg]
   (vec
    (mapcat
     (fn [ns-sym]
       (require ns-sym)
       (for [[fun-name var-ref] (public-functions ns-sym)]
         (analyze-var cfg ns-sym fun-name var-ref)))
     namespaces))))

(defn- class-symbol [class-ref]
  (cond
    (class? class-ref) (.getName ^Class class-ref)
    (symbol? class-ref) (name class-ref)
    (string? class-ref) class-ref
    :else (str class-ref)))

(defn- method-param-name [idx]
  (str "arg" idx))

(defn- public-or-declared-methods [class-util public-only?]
  (if public-only?
    (reflect-class/get-public-methods class-util)
    (reflect-class/get-all-methods class-util)))

(defn- analyze-java-method [cfg class-name ^Method method reflect-method-info]
  (let [method-name (.getName method)
        param-types (vec (.getGenericParameterTypes method))
        sample-count (:sample-count cfg)]
    {:kind "java-method"
     :namespace class-name
     :class-name class-name
     :name method-name
     :jvm-signature (str method-name
                         "("
                         (str/join "," (map type-name param-types))
                         ")")
     :modifiers (Modifier/toString (.getModifiers method))
     :return-type (type-name (.getGenericReturnType method))
     :reflect-info reflect-method-info
     :parameters
     (vec
      (map-indexed
       (fn [idx param-type]
         (let [param-name (method-param-name idx)
               rule (configured-param-rule cfg class-name method-name param-name)
               type-key (or (:type rule) (normalized-type param-type))
               values (default-values type-key rule sample-count)]
           {:position idx
            :name param-name
            :type (name type-key)
            :schema-type (type-name param-type)
            :values values}))
       param-types))}))

(defn analyze-java-classes
  "Analyzes Java classes with clojure.new.api.reflect and normalizes methods into generated-test-data entries."
  ([java-classes]
   (analyze-java-classes java-classes config/default-config))
  ([java-classes cfg]
   (vec
    (mapcat
     (fn [class-ref]
       (let [class-name (class-symbol class-ref)
             class-util (reflect-class/get-class-util class-name)
             methods (public-or-declared-methods class-util (:java-public-only? cfg))]
         ;; Keep the full class reflection available to callers via the method-level reflect-info.
         (java-convert/retrive-class-info class-util)
         (for [method methods
               :let [method-info (java-convert/retrieve-method-info method)]]
           (analyze-java-method cfg class-name method method-info))))
     java-classes))))

(defn- value->json [value]
  (cond
    (nil? value) "null"
    (string? value) (str "\"" (str/escape value {\" "\\\"" \\ "\\\\"}) "\"")
    (keyword? value) (value->json (name value))
    (symbol? value) (value->json (name value))
    (uuid? value) (value->json (str value))
    (map? value) (str "{" (str/join "," (map (fn [[k v]]
                                                (str (value->json (name k)) ":" (value->json v)))
                                              value)) "}")
    (coll? value) (str "[" (str/join "," (map value->json value)) "]")
    :else (str value)))

(defn- payload-json [analysis case-index params]
  (str "{\"function\":" (value->json (:name analysis))
       ",\"namespace\":" (value->json (:namespace analysis))
       ",\"caseIndex\":" case-index
       ",\"parameters\":" (value->json params)
       "}"))

(defn- payload-xml [analysis case-index params]
  (str "<test-case namespace=\"" (:namespace analysis)
       "\" function=\"" (:name analysis)
       "\" index=\"" case-index "\">"
       (apply str (map (fn [[name value]]
                         (str "<param name=\"" name "\">"
                              (value->json value)
                              "</param>"))
                       params))
       "</test-case>"))

(defn- payload-csv [analysis case-index params]
  (str "namespace,function,case,param,value\n"
       (str/join "\n"
                 (map (fn [[name value]]
                        (str (:namespace analysis) "," (:name analysis) ","
                             case-index "," name "," (pr-str value)))
                      params))))

(defn- payload-text [analysis case-index params]
  (str (:namespace analysis) "/" (:name analysis)
       " case " case-index " " (pr-str params)))

(defn- payload-pdf [analysis case-index params]
  (let [text (payload-text analysis case-index params)]
    (str "%PDF-1.4\n"
         "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n"
         "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n"
         "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 300 144] /Contents 4 0 R >> endobj\n"
         "4 0 obj << /Length " (+ 42 (count text)) " >> stream\n"
         "BT /F1 10 Tf 20 100 Td (" text ") Tj ET\n"
         "endstream endobj\n"
         "trailer << /Root 1 0 R >>\n%%EOF")))

(defn- render-payload [format analysis case-index params]
  (case format
    :json (payload-json analysis case-index params)
    :xml (payload-xml analysis case-index params)
    :csv (payload-csv analysis case-index params)
    :pdf (payload-pdf analysis case-index params)
    :string (pr-str params)
    :text (payload-text analysis case-index params)
    (payload-text analysis case-index params)))

(defn- case-values [analysis case-index]
  (into {}
        (map (fn [param]
               [(:name param)
                (nth (:values param)
                     (mod case-index (count (:values param))))])
             (:parameters analysis))))

(defn generate-test-data
  "Augments analyses with generated test cases and formatted payloads."
  ([analyses]
   (generate-test-data analyses config/default-config))
  ([analyses cfg]
   (let [formats (:formats cfg)
         sample-count (:sample-count cfg)]
     (vec
      (for [analysis analyses]
        (assoc analysis
               :cases
               (vec
                (for [case-index (range sample-count)
                      :let [params (case-values analysis case-index)]]
                  {:case-index case-index
                   :parameters params
                   :payloads (into {}
                                   (map (fn [format]
                                          [format (render-payload format analysis case-index params)])
                                        formats))}))))))))

(defn run-spy-case!
  "Runs a function with generated arguments and records the call through the existing spy flow atom."
  [analysis case-data]
  (when (= "java-method" (:kind analysis))
    (throw (UnsupportedOperationException.
            "run-spy-case! supports Clojure functions. Java method invocation needs an instance/static target.")))
  (let [ns-sym (symbol (:namespace analysis))
        fun-sym (symbol (:name analysis))
        fun-var (ns-resolve ns-sym fun-sym)
        args (mapv (fn [param] (get (:parameters case-data) (:name param)))
                   (:parameters analysis))
        result (apply @fun-var args)]
    (apply spy/collect-flow-calls fun-sym result args)
    result))

(defn analyze-files
  "Loads Clojure files and/or Java classes, analyzes them, generates test data, and stores it."
  [{:keys [files namespaces java-classes config-xml db-path]
    :or {db-path "target/test-data/generated-test-data.sqlite"}}]
  (let [cfg (if config-xml
              (merge config/default-config (config/read-config config-xml))
              config/default-config)
        file-namespaces (when (seq files) (load-analysis-files! files))
        all-namespaces (vec (distinct (concat namespaces file-namespaces)))
        clj-analyses (analyze-namespaces all-namespaces cfg)
        java-analyses (analyze-java-classes java-classes cfg)
        analyses (generate-test-data (vec (concat clj-analyses java-analyses)) cfg)]
    (sqlite/store-analysis! db-path analyses)
    {:db-path db-path
     :namespaces all-namespaces
     :java-classes (vec (map class-symbol java-classes))
     :analyses analyses}))
