(ns io.github.hglabplh_tech.reflect.clojure.code.generate.codegen.jgenerator
  (:require [clojure.string :as str]))

(defn- class-name [class-data]
  (or (:obj-name class-data) "GeneratedClass"))

(defn- simple-name [fqcn]
  (last (str/split (str fqcn) #"\.")))

(defn- modifiers [general]
  (let [mods (:modifiers general)]
    (cond
      (string? mods) mods
      (sequential? mods) (str/join " " mods)
      :else "public")))

(defn class-def-gen-hook [class-data]
  (str (modifiers (:attributes class-data))
       " class "
       (simple-name (class-name class-data))
       " {"))

(defn ctor-gen-hook [ctor-data]
  (let [ctor (:ctor ctor-data)]
    (str "  // constructor " (:obj-name ctor))))

(defn method-gen-hook [method-data]
  (let [method (:method method-data)]
    (str "  // method " (:obj-name method)
         " returns " (:gen-return-type method))))

(defn field-gen-hook [field-data]
  (let [field (:field field-data)]
    (str "  // field " (:obj-name field)
         " : " (:gen-type field))))

(defn enum-gen-hook [enum-data]
  (str "  // enum spec " (pr-str enum-data)))

(defn record-gen-hook [record-data]
  (str "  // record spec " (pr-str record-data)))

(defn lambda-gen-hook [lambda-data]
  (str "  // lambda spec " (pr-str lambda-data)))

(defn switch-gen-hook [switch-data]
  (str "  // switch spec " (pr-str switch-data)))

(defn emit-gen-hook [generated]
  (let [definition (:definition generated)
        body (:body generated)
        special-lines (remove nil? [(:enum definition)
                                    (:record definition)
                                    (:lambda definition)
                                    (:switch definition)])
        body-lines (concat special-lines
                           (:fields body)
                           (:constructors body)
                           (:methods body)
                           (:classes body))]
    (str (:class definition)
         "\n"
         (str/join "\n" body-lines)
         "\n}")))

(def generator
  {:class-def-gen-hook class-def-gen-hook
   :ctor-gen-hook ctor-gen-hook
   :method-gen-hook method-gen-hook
   :field-gen-hook field-gen-hook
   :enum-gen-hook enum-gen-hook
   :record-gen-hook record-gen-hook
   :lambda-gen-hook lambda-gen-hook
   :switch-gen-hook switch-gen-hook
   :emit-gen-hook emit-gen-hook})
