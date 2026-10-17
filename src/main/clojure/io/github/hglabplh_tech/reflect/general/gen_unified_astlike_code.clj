;; Copyright (c) 2026 Harald Glab-Plhak

(ns io.github.hglabplh-tech.reflect.general.gen-unified-astlike-code
  )

(defn extract-schema-info [sch]
  "Helper to unpack Prismatic Schemas and Active Data realm configurations."
  (cond
    ;; 1. Check if the schema is an explicit Prismatic inline predicate (s/pred)
    (instance? schema.core.Predicate sch)
    {:datatype    :any
     :constraints {:predicate-fn (:pred-name sch)}
     :add-on      nil}

    ;; 2. Check if it's an Active Data realm map containing constraints
    (and (map? sch) (contains? sch :active.data.realm/type))
    {:datatype    (:active.data.realm/type sch)
     :constraints (:active.data.realm/constraints sch)
     :add-on      (if-let [warn-fn (get-in sch [:active.data.realm/constraints :warning-fn])]
                    {:has-warning-hook true :warning-extractor warn-fn}
                    nil)}

    ;; 3. Handle standard Prismatic/Clojure classes (e.g., java.lang.String, s/Str)
    (or (class? sch) (keyword? sch))
    {:datatype    sch
     :constraints nil
     :add-on      nil}

    ;; 4. Fallback default for untyped or unhandled schema definitions
    :else
    {:datatype    :any
     :constraints nil
     :add-on      nil}))


(defn parse-function-params [func-var]
  (let [v-meta             (meta func-var)
        arglist            (first (:arglists v-meta))
        positional-args    (take-while #(not= % '&) arglist)
        named-destruc      (first (rest (drop-while #(not= % '&) arglist)))
        named-keys         (:keys named-destruc)
        defaults           (:or named-destruc)

        schema-inputs      (take-while #(not= % '=>) (first (:schema v-meta)))
        positional-schemas (take (count positional-args) schema-inputs)
        named-map-schema   (last schema-inputs)]

    (concat
      ;; Build Positional Maps
      (map-indexed
        (fn [idx arg-sym]
          (let [p-schema (nth positional-schemas idx nil)
                extracted (extract-schema-info p-schema)]
            {:name        arg-sym
             :position    (inc idx)
             :type        :positional
             :datatype    (:datatype extracted)
             :attributes  nil
             :constraints (:constraints extracted)
             :add-on      nil}))
        positional-args)

      ;; Build Named Maps
      (map
        (fn [arg-sym]
          (let [has-default? (contains? defaults arg-sym)
                kw-schema    (or (get named-map-schema arg-sym)
                                 (get named-map-schema (s/optional-key arg-sym)))
                extracted    (extract-schema-info kw-schema)]
            {:name        arg-sym
             :position    :named
             :type        :named
             :datatype    (:datatype extracted)
             :attributes  (if has-default? {:has-default true} nil)
             :constraints (:constraints extracted)
             :add-on      (if has-default? {:default-value (get defaults arg-sym)} nil)}))
        named-keys))))

(defn parse-argslist [args-list]
 (let [arglist            (first args-list)
       positional-args    (take-while #(not= % '&) arglist)
       named-destruc      (first (rest (drop-while #(not= % '&) arglist)))
       named-keys         (get named-destruc :keys)
       defaults           (get named-destruc :or )
       schema-inputs      (take-while #(not= % '=>)
                                      (first ))
       positional-schemas (take (count positional-args) schema-inputs)
       named-map-schema   (last schema-inputs)]

   (concat
     ;; Build Positional Maps
     (map-indexed
       (fn [idx arg-sym]
         (let [p-schema (nth positional-schemas idx nil)
               extracted (extract-schema-info p-schema)]
           {:name        arg-sym
            :position    (inc idx)
            :type        :positional
            :datatype    (:datatype extracted)
            :attributes  nil
            :constraints (:constraints extracted)
            :add-on      nil}))
       positional-args)

     ;; Build Named Maps
     (map
       (fn [arg-sym]
         (let [has-default? (contains? defaults arg-sym)
               kw-schema    (or (get named-map-schema arg-sym)
                                (get named-map-schema (s/optional-key arg-sym)))
               extracted    (extract-schema-info kw-schema)]
           {:name        arg-sym
            :position    :named
            :type        :named
            :datatype    (:datatype extracted)
            :attributes  (if has-default? {:has-default true} nil)
            :constraints (:constraints extracted)
            :add-on      (if has-default? {:default-value (get defaults arg-sym)} nil)}))
       named-keys))))


))

(defn cloj-transform-value [base-data add-on]
  (let [documentation (get base-data :doc)
        line-no (get base-data :line)
        arg-list (get base-data :argument-list)]

    )
  )

(defn clojure-transform [namespace reflect-data]
  (let [content-keys (mapv (fn [value] (first (keys value))) reflect-data)
        content-vals (mapv (clojure.core/fn [value]
                             (first (vals value))) reflect-data)
        transformed (mapv (fn [raw-value]
                            (let [value-of-struct (get raw-value :structured-meta)
                                  base-meta (get value-of-struct :base-data)
                                  add-on (get value-of-struct :add-on)]
                              (cloj-transform-value base-meta add-on))) content-vals)]
    (conj {:language :clojure :namespace-class namespace :function-class-catalog content-keys} transformed)
    ))

(defn transform-to-unified [reflect-input]
  (let [lang (get reflect-input :language)]
    (cond
      (= lang :clojure)
      (clojure-transform (get reflect-input :ns-name) (get reflect-input :reflect-data))
      :else
      (println "Not yet here")
      )))

