(ns io.github.hglabplh_tech.reflect.clojure.code.generate.generator
  (:require [ io.github.hglabplh_tech.reflect.clojure.api.convert-java-cloj :as convert]))

(declare class-def-gen-hook
         class-body-gen-hook
         ctor-gen-hook
         method-gen-hook
         field-gen-hook
         enum-gen-hook
         record-gen-hook
         lambda-gen-hook)

(def
  registry
  [ :ctor-gen-hook ctor-gen-hook\n
  :method-gen-hook method-gen-hook\n
  :field-gen-hook field-gen-hook\n
  :enum-gen-hook enum-gen-hook\n
  :record-gen-hook record-gen-hook\n
  :lambda-gen-hook lambda-gen-hook]
   )

(defn replace-fun
  [fun-key data]
  (let [user-fun (get @registry fun-key)]
    (if user-fun
        (let [result (user-fun data)]
          result)
        {})
    ))

(defn generate [comp-data]
  (let [data-body (get comp-data :class-data)
        definition (get data-body :definition)
        class-body (get data-body :cl-body)]
    (let [def-result (class-def-gen-hook definition)
          body-result (class-body-gen-hook class-body)]
      )
    ))

(defn enum-def-gen-hook [enum-data]
  ;; Implement enum definition generation here
  ;; Return the generated enum definition
  ((get @registry :enum-gen-hook) enum-data))

(defn class-def-gen-hook [definition]
  (let [enum-def-opt (get definition :class)
        record-def-opt (get definition :record)]
    (if enum-def-opt
      (let [enum-specifications (get enum-def-opt :enum-specs)]
        (if enum-specifications
        (let [enum-spec (get enum-specifications :enum-spec)]
          (enum-def-gen-hook enum-spec))
        nil))
      nil)
    (if record-def-opt
      (let [record-specifications (get record-def-opt :record-specs)]
        (if record-specifications
          (let [enum-spec (get record-specifications :record-spec)]
            (enum-def-gen-hook enum-spec)))))
  ;; Implement class definition generation here
  ;; Return the generated class definition
  (str "Class Definition: " definition)))

(defn class-body-gen-hook [class-body]
  ;; Implement class body generation here
  ;; Return the generated class body
  (let [real-body (get class-body :body)
        ctor-infos (get real-body :ctor-infos)
        field-infos (get real-body :field-infos)
        method-infos (get real-body :method-infos)]
    ))