(ns io.github.hglabplh-tech.reflect.clojure.schema-spec.parse-specs
  (:require [clojure.spec.alpha :as spec]))

(defn- with-type-hint [parameter binding-form]
  (if-let [hint (:tag (meta binding-form))]
    (assoc parameter :type-hint hint)
    parameter))

(defn reflect-mixed-params
  "Reflect the first arity's positional, named and rest parameters.
   Preserve specs and defaults; include :type-hint only when declared."
  [fun-meta]
  (let [func-name (symbol (str (:ns fun-meta) "/" (:name fun-meta)))
        raw-args (first (:arglists fun-meta))
        fixed-args (take-while #(not= '& %) raw-args)
        rest-arg (second (drop-while #(not= '& %) raw-args))
        destructured-map (first (filter map? (concat fixed-args [rest-arg])))
        spec-form (when-let [sp (spec/get-spec func-name)]
                    (spec/describe sp))
        flat-spec-lookup (apply hash-map (rest spec-form))
        args-spec (:args flat-spec-lookup)
        pos-specs (when (and (seq? args-spec)
                            (contains? #{'cat 'clojure.spec.alpha/cat}
                                       (first args-spec)))
                   (mapv second (partition 2 (rest args-spec))))
        keys-spec-form (->> args-spec
                            (tree-seq coll? seq)
                            (filter #(and (seq? %)
                                          (contains? #{'keys 'clojure.spec.alpha/keys}
                                                     (first %))))
                            first)
        keys-map (when keys-spec-form
                   (apply hash-map (rest keys-spec-form)))
        req-keys (set (map (comp symbol name) (:req-un keys-map)))
        opt-keys (set (map (comp symbol name) (:opt-un keys-map)))
        return-hint (or (:tag fun-meta) (:tag (meta raw-args)))
        result
        {:function-name func-name
         :positional-parameters
         (->> fixed-args
              (map-indexed
                (fn [position binding-form]
                  (when (symbol? binding-form)
                    (with-type-hint
                      {:param binding-form
                       :position position
                       :spec (get pos-specs position)}
                      binding-form))))
              (remove nil?)
              vec)
         :named-parameters
         (when destructured-map
           (let [defaults (:or destructured-map)]
             (into {}
                   (map (fn [k]
                          [k (with-type-hint
                               {:required? (contains? req-keys k)
                                :optional? (contains? opt-keys k)
                                :default (get defaults k :none)}
                               k)]))
                   (:keys destructured-map))))
         :return-spec (:ret flat-spec-lookup)}]
    (cond-> result
      return-hint (assoc :return-type-hint return-hint)
      (symbol? rest-arg)
      (assoc :rest-parameter
             (with-type-hint {:param rest-arg :variadic? true} rest-arg)))))
