(ns io.github.hglabplh-tech.reflect.clojure.schema-spec.parse-specs
  (:require [clojure.pprint :refer :all]
    [schema.core :as s]
            [clojure.spec.alpha :as spec])) 
(defn reflect-mixed-params
  "Takes the metadata of a function and
   reflects its complete runtime composition as a structured map."
  [fun-meta]
  (let [
        func-name (symbol (str (:ns fun-meta) "/" (:name fun-meta)))
        raw-args (first (:arglists fun-meta))

        ;; 1. Split positional args from the trailing destructured map
        positional-arg-symbols (vec (remove map? raw-args))
        destructured-map (first (filter map? raw-args))

        ;; 2. Retrieve spec details
        spec-form (when-let [sp (spec/get-spec func-name)]
                    (spec/describe sp))
        spec-vect-flat (rest spec-form )
        spec-args-flat (rest (first (rest (rest spec-form ))))

        ;; Extract spec/keys blocks anywhere inside the spec tree
        keys-spec-form (->> spec-form :args (tree-seq coll? seq)
                            (filter #(and (coll? %)
                                          (= 'clojure.spec.alpha/keys (first %))))
                            first)
        keys-map (when keys-spec-form (apply hash-map (rest keys-spec-form)))
        req-keys (:req-un keys-map)
        opt-keys (:opt-un keys-map)

        ;; Map spec labels to spec definitions for positional parameters
        pos-spec-lookup (into {} (mapv vec (partition 2 spec-args-flat)))
        flat-spec-lookup  (apply hash-map spec-vect-flat)
        ]



    {:function-name func-name

     ;; Parse positional variables
     :positional-parameters
     (mapv (fn [sym]
             {:param sym
              :spec  (get pos-spec-lookup (keyword sym))})
           positional-arg-symbols)

     ;; Parse named variables if a destructured map exists
     :named-parameters
     (when destructured-map
       (let [named-keys (:keys destructured-map)
             defaults   (:or destructured-map)]
         (into {} (map (fn [k]
                         [k {:required? (boolean (some #{k} req-keys))
                             :optional? (boolean (some #{k} opt-keys))
                             :default   (get defaults k :none)}])
                       named-keys))))

     :return-spec (:ret flat-spec-lookup)}))
