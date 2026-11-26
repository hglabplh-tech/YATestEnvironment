(ns io.github.hglabplh-tech.reflect.clojure.schema-spec.parse-specs
  (:require [clojure.pprint :refer [pprint]]
            [io.github.hglabplh-tech.reflect.util.misc-utils :as mu]
            [clojure.spec.alpha :as spec]))

(defn- with-type-hint [parameter binding-form named?]
  (if-let [hint
           (:tag (meta binding-form))]
    (assoc parameter :type-hint hint)
    parameter))

(clojure.core/defn printerr [& args]
  (binding [*out* *err*]
    (apply println args)))
(defn resolve-leaf-spec
  "Recursively unwraps a spec keyword or spec form into its underlying predicate/description."
  ([spec-or-kw]
   (resolve-leaf-spec spec-or-kw #{}))
  ([spec-or-kw seen]
   (cond
     ;; Prevent cycles if specs reference themselves
     (contains? seen spec-or-kw)
     spec-or-kw

     ;; Registered keyword spec (e.g., :my-app/recipient) -> look up and describe
     (keyword? spec-or-kw)
     (if-let [sub-spec (spec/get-spec spec-or-kw)]
       (resolve-leaf-spec (spec/describe sub-spec) (conj seen spec-or-kw))
       spec-or-kw)

     ;; Already a raw Spec object -> describe it first
     (spec/spec? spec-or-kw)
     (resolve-leaf-spec (spec/describe spec-or-kw) (conj seen spec-or-kw))

     ;; Predicate symbol or form (e.g., string?, int?, (and int? pos?))
     :else
     spec-or-kw)))

(defn- select-param-list [fun-args-descr arg-count]
  (let [valid-arities (filter #(<= (count (:positional-parameters %)) arg-count)
                              fun-args-descr)
        candidates    (if (seq valid-arities) valid-arities fun-args-descr)
        min-diff-vect (mapv (fn [element]
                              (- arg-count (count (:positional-parameters element))))
                            candidates)
        min-val       (apply min min-diff-vect)
        index-of-min  (first (keep-indexed (fn [idx item]
                                             (when (= item min-val) idx))
                                           min-diff-vect))]
    (nth candidates index-of-min)))
(clojure.core/defn reflect-mixed-params
  "Reflect each arity's positional, named, and rest parameters.
   Preserves specs, resolved leaf predicates (:spec-type), and defaults;
   includes :type-hint only when declared."
  [fun-meta]
  (loop [result-list   []
         raw-args-list (:arglists fun-meta)]
    (if (empty? raw-args-list)
      result-list
      (let [func-name        (symbol (str (:ns fun-meta) "/" (:name fun-meta)))
            raw-args         (first raw-args-list)
            fixed-args       (take-while #(not= '& %) raw-args)
            rest-arg         (second (drop-while #(not= '& %) raw-args))
            destructured-map (first (filter map? (concat fixed-args [rest-arg])))
            spec-form        (when-let [sp (spec/get-spec func-name)]
                               (spec/describe sp))
            flat-spec-lookup (when spec-form
                               (apply hash-map (rest spec-form)))
            args-spec        (:args flat-spec-lookup)
            pos-specs        (when (and (seq? args-spec)
                                        (contains? #{'cat 'clojure.spec.alpha/cat}
                                                   (first args-spec)))
                               (mapv second (partition 2 (rest args-spec))))
            ;; FIX 1: Include 'keys* and 'clojure.spec.alpha/keys* alongside 'keys
            keys-spec-form   (->> args-spec
                                  (tree-seq coll? seq)
                                  (filter #(and (seq? %)
                                                (contains? #{'keys 'clojure.spec.alpha/keys
                                                             'keys* 'clojure.spec.alpha/keys*}
                                                           (first %))))
                                  first)
            keys-map         (when keys-spec-form
                               (apply hash-map (rest keys-spec-form)))
            ;; Collect all registered spec keywords from :req, :req-un, :opt, :opt-un
            all-req-specs    (concat (:req keys-map) (:req-un keys-map))
            all-opt-specs    (concat (:opt keys-map) (:opt-un keys-map))
            ;; Map local symbol (e.g. 'greeting) -> qualified spec keyword (e.g. :my-app/greeting)
            named-spec-by-sym (into {}
                                    (map (fn [spec-kw] [(symbol (name spec-kw)) spec-kw]))
                                    (concat all-req-specs all-opt-specs))
            req-keys         (set (map (comp symbol name) all-req-specs))
            opt-keys         (set (map (comp symbol name) all-opt-specs))
            return-hint      (or (:tag fun-meta) (:tag (meta raw-args)))
            ret-spec-raw     (:ret flat-spec-lookup)
            result
            {:function-name func-name
             :positional-parameters
             (->> fixed-args
                  (map-indexed
                    (fn [position binding-form]
                      (when (symbol? binding-form)
                        (let [p-spec (get pos-specs position)]
                          (with-type-hint
                            {:param     binding-form
                             :position  position
                             :spec      p-spec
                             ;; FIX 2: Resolve the registered spec keyword/form, not the local symbol
                             :spec-type (when p-spec (resolve-leaf-spec p-spec))}
                            binding-form false)))))
                  (remove nil?)
                  vec)
             :named-parameters
             (when destructured-map
               (let [defaults (:or destructured-map)]
                 (into {}
                       (map (fn [k]
                              (let [spec-kw (get named-spec-by-sym k)]
                                [k (with-type-hint
                                     {:required? (contains? req-keys k)
                                      :optional? (contains? opt-keys k)
                                      :req-keys  (if (contains? req-keys k) req-keys :none)
                                      :opt-keys  (if (contains? opt-keys k) opt-keys :none)
                                      :spec      spec-kw
                                      ;; FIX 3: Resolve the qualified spec keyword (e.g. :my-app/greeting -> string?)
                                      :spec-type (when spec-kw (resolve-leaf-spec spec-kw))
                                      :default   (get defaults k :none)}
                                     k true)])))
                       (:keys destructured-map))))
             :return-spec      ret-spec-raw
             :return-spec-type (when ret-spec-raw (resolve-leaf-spec ret-spec-raw))}]
        (recur (conj result-list
                     (cond-> result
                             return-hint (assoc :return-type-hint return-hint)
                             (symbol? rest-arg)
                             (assoc :rest-parameter
                                    (with-type-hint {:param rest-arg :variadic? true} rest-arg false))))
               (rest raw-args-list))))))


(defn- format-pos-args
  [fun-args-descr arguments]
  (let [pos-params (:positional-parameters (select-param-list fun-args-descr (count arguments)))]
    (loop [pos-param-args  []
           pos-params-rest pos-params
           pos-args-rest   arguments]
      (if (empty? pos-params-rest)
        pos-param-args
        (let [arg-map (assoc (first pos-params-rest) :value (first pos-args-rest))]
          (recur (conj pos-param-args arg-map)
                 (rest pos-params-rest)
                 (rest pos-args-rest)))))))

(defn- format-named-args
  [fun-args-descr arguments]
  (let [selected-arity   (select-param-list fun-args-descr (count arguments))
        param-descr      (if (seq (:named-parameters selected-arity))
                           selected-arity
                           (some #(when (seq (:named-parameters %)) %) fun-args-descr))
        named-params-map (:named-parameters param-descr)
        size-pos-params  (count (:positional-parameters selected-arity))
        tail-args        (drop size-pos-params arguments)
        named-args-map   (cond
                           (empty? tail-args) {}
                           (and (= 1 (count tail-args)) (map? (first tail-args))) (first tail-args)
                           :else (into {} (map vec (partition 2 tail-args))))]
    (loop [named-param-args     []
           named-param-keys     (vec (keys named-params-map))
           named-param-keys-kwd (mapv #(keyword (name %)) named-param-keys)
           named-param-values   (vec (vals named-params-map))]
      (if (empty? named-param-keys)
        named-param-args
        (let [act-key     (first named-param-keys)
              act-key-kwd (first named-param-keys-kwd)
              param-def   (first named-param-values)
              default-val (:default param-def)
              arg-value   (if (contains? named-args-map act-key-kwd)
                            (get named-args-map act-key-kwd)
                            default-val)
              result      (assoc param-def
                            :param act-key
                            :arg-value arg-value)]
          (recur (conj named-param-args result)
                 (rest named-param-keys)
                 (rest named-param-keys-kwd)
                 (rest named-param-values)))))))




(clojure.core/defn format-args [fun-args-descr arguments]
  {:pos-args (format-pos-args fun-args-descr arguments) :named-args (format-named-args fun-args-descr arguments)}
  )

