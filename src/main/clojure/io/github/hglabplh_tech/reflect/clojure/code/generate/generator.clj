(ns io.github.hglabplh_tech.reflect.clojure.code.generate.generator)

(declare class-def-gen-hook
         class-body-gen-hook
         generate
         ctor-gen-hook
         method-gen-hook
         field-gen-hook
         enum-gen-hook
         record-gen-hook
         lambda-gen-hook
         switch-gen-hook)

(def registry
  "Output generator hooks. The compiler initializes this with a concrete
  implementation such as JSON, XML, YAML, or Java code generation."
  (atom {}))

(defn register-hooks!
  [hooks]
  (reset! registry hooks))

(defn replace-fun
  [fun-key data]
  (if-let [user-fun (get @registry fun-key)]
    (user-fun data)
    data))

(defn ctor-gen-hook [ctor-data]
  (replace-fun :ctor-gen-hook ctor-data))

(defn method-gen-hook [method-data]
  (replace-fun :method-gen-hook method-data))

(defn field-gen-hook [field-data]
  (replace-fun :field-gen-hook field-data))

(defn enum-gen-hook [enum-data]
  (replace-fun :enum-gen-hook enum-data))

(defn record-gen-hook [record-data]
  (replace-fun :record-gen-hook record-data))

(defn lambda-gen-hook [lambda-data]
  (replace-fun :lambda-gen-hook lambda-data))

(defn switch-gen-hook [switch-data]
  (replace-fun :switch-gen-hook switch-data))

(defn- present-data? [value]
  (and (some? value)
       (not= {} value)
       (not= [] value)
       (not= '() value)))

(defn class-def-gen-hook [definition]
  (let [class-data (:class definition)
        class-result (replace-fun :class-def-gen-hook class-data)
        enum-result (when (present-data? (:enum-specs class-data))
                      (enum-gen-hook (:enum-specs class-data)))
        record-result (when (present-data? (:record-specs class-data))
                        (record-gen-hook (:record-specs class-data)))
        lambda-result (when (present-data? (:lambda-specs class-data))
                        (lambda-gen-hook (:lambda-specs class-data)))
        switch-result (when (present-data? (:switch-specs class-data))
                        (switch-gen-hook (:switch-specs class-data)))]
    {:class class-result
     :enum enum-result
     :record record-result
     :lambda lambda-result
     :switch switch-result}))

(defn class-body-gen-hook [class-body]
  (let [real-body (:body class-body)]
    {:constructors (vec (map ctor-gen-hook (:ctor-infos real-body)))
     :fields (vec (map field-gen-hook (:field-infos real-body)))
     :methods (vec (map method-gen-hook (:method-infos real-body)))
     :classes (vec (map generate (:class-infos real-body)))}))

(defn generate
  [comp-data]
  (let [data-body (get comp-data :class-data)
        definition (get data-body :definition)
        class-body (get data-body :cl-body)]
    (let [def-result (class-def-gen-hook definition)
          body-result (class-body-gen-hook class-body)
          generated {:definition def-result
                     :body body-result}]
      (replace-fun :emit-gen-hook generated))))
