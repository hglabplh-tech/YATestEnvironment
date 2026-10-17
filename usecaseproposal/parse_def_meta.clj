(ns parse-def-meta)

(require '[schema.core :as s])

(s/defn process-order
        "Processes a customer order and returns a shipping manifest summary."
        [product  :- s/Str
         count    :- s/Int
         price    :- s/Num
         & {:keys [name surname street zip-code city county country]
            :or   {county "N/A" country "Germany"}} :- {(s/optional-key :name) s/Str
                                                        (s/optional-key :surname) s/Str
                                                        (s/optional-key :street) s/Str
                                                        (s/optional-key :zip-code) s/Str
                                                        (s/optional-key :city) s/Str
                                                        (s/optional-key :county) s/Str
                                                        (s/optional-key :country) s/Str}]
        {:product product :total (* count price) :ship-to surname})

(defn process-order-2
      "Processes a customer order and returns a shipping manifest summary."
      [^String product ^Long count ^Double price & {:keys [name surname street zip-code city county country]
                                                    :or   {county "N/A" country "Germany"}}]
      {:product product :total (* count price) :ship-to surname})

(def assoc-param-def
  "the definition of a parameter"
  [:name
   :position
   :type
   :datatype
   :attributes
   :constraints
   :add-on])

(defn parse-function-params [func-var]
      (let [;; 1. Fetch the first argument vector signature from metadata
            arglist (first (:arglists (meta func-var)))

            ;; 2. Split the list into positional arguments and the map destructuring rest
            positional-args (take-while #(not= % '&) arglist)
            named-destruc   (first (rest (drop-while #(not= % '&) arglist)))

            ;; 3. Extract the named keys and their default options map (:or)
            named-keys      (:keys named-destruc)
            defaults        (:or named-destruc)]

           (concat
             ;; Build positional parameter maps
             (map-indexed
               (fn [idx arg-sym]
                   ;; Extract type-hint metadata directly from the parameter symbol
                   (let [hint (:tag (meta arg-sym))]
                        {:name        arg-sym
                         :position    (inc idx)
                         :type        :positional
                         :datatype    (if hint hint :any)
                         :attributes  nil
                         :constraints nil
                         :add-on      nil}))
               positional-args)

             ;; Build named parameter maps
             (map
               (fn [arg-sym]
                   (let [has-default? (contains? defaults arg-sym)]
                        {:name        arg-sym
                         :position    :named
                         :type        :named
                         :datatype    :any ;; Named params in keys-destructuring don't hold individual inline hints easily
                         :attributes  (if has-default? {:has-default true} nil)
                         :constraints nil
                         :add-on      (if has-default? {:default-value (get defaults arg-sym)} nil)}))
               named-keys))))
(defn process-order
      "Processes a customer order and returns a shipping manifest summary."
      [^String product ^Long count ^Double price & {:keys [name surname street zip-code city county country]
                                                    :or   {county "N/A" country "Germany"}}]
      {:product product :total (* count price) :ship-to surname})
({:name product,  :position 1,      :type :positional, :datatype String, :attributes nil, :constraints nil, :add-on nil}
 {:name count,    :position 2,      :type :positional, :datatype Long,   :attributes nil, :constraints nil, :add-on nil}
 {:name price,    :position 3,      :type :positional, :datatype Double, :attributes nil, :constraints nil, :add-on nil}
 {:name name,     :position :named, :type :named,      :datatype :any,   :attributes nil, :constraints nil, :add-on nil}
 {:name surname,  :position :named, :type :named,      :datatype :any,   :attributes nil, :constraints nil, :add-on nil}
 {:name street,   :position :named, :type :named,      :datatype :any,   :attributes nil, :constraints nil, :add-on nil}
 {:name zip-code, :position :named, :type :named,      :datatype :any,   :attributes nil, :constraints nil, :add-on nil}
 {:name city,     :position :named, :type :named,      :datatype :any,   :attributes nil, :constraints nil, :add-on nil}
 {:name county,   :position :named, :type :named,      :datatype :any,   :attributes {:has-default true}, :constraints nil, :add-on {:default-value "N/A"}}
 {:name country,  :position :named, :type :named,      :datatype :any,   :attributes {:has-default true}, :constraints nil, :add-on {:default-value "Germany"}})

(def assoc-param-def
  "the definition of a parameter"
  [:name
   :position
   :type
   :datatype
   :attributes
   :constraints
   :add-on])

(defn parse-function-params [func-var]
      (let [v-meta          (meta func-var)
            ;; 1. Fall back to standard arglists for structural breakdown
            arglist         (first (:arglists v-meta))
            positional-args (take-while #(not= % '&) arglist)
            named-destruc   (first (rest (drop-while #(not= % '&) arglist)))
            named-keys      (:keys named-destruc)
            defaults        (:or named-destruc)

            ;; 2. Extract Prismatic Schema inputs from the :schema metadata
            ;; Prismatic stores schemas as a vector [In1 In2 ... => Out]
            schema-inputs   (take-while #(not= % '=>) (first (:schema v-meta)))

            ;; The positional schemas align sequentially with our positional args
            positional-schemas (take (count positional-args) schema-inputs)
            ;; The last element in the schema inputs represents our keyword argument map schema
            named-map-schema   (last schema-inputs)]

           (concat
             ;; Build positional parameter maps using Prismatic metadata
             (map-indexed
               (fn [idx arg-sym]
                   (let [p-schema (nth positional-schemas idx nil)]
                        {:name        arg-sym
                         :position    (inc idx)
                         :type        :positional
                         :datatype    (if p-schema p-schema :any)
                         :attributes  nil
                         :constraints nil
                         :add-on      nil}))
               positional-args)

             ;; Build named parameter maps using Prismatic map schema lookups
             (map
               (fn [arg-sym]
                   (let [has-default? (contains? defaults arg-sym)
                         ;; Look up the schema for this keyword via standard or optional schema key
                         kw-schema    (or (get named-map-schema arg-sym)
                                          (get named-map-schema (s/optional-key arg-sym)))]
                        {:name        arg-sym
                         :position    :named
                         :type        :named
                         :datatype    (if kw-schema kw-schema :any)
                         :attributes  (if has-default? {:has-default true} nil)
                         :constraints nil
                         :add-on      (if has-default? {:default-value (get defaults arg-sym)} nil)}))
               named-keys))))
({:name product,  :position 1,      :type :positional, :datatype java.lang.String, :attributes nil, :constraints nil, :add-on nil}
 {:name count,    :position 2,      :type :positional, :datatype java.lang.Long,   :attributes nil, :constraints nil, :add-on nil}
 {:name price,    :position 3,      :type :positional, :datatype java.lang.Number, :attributes nil, :constraints nil, :add-on nil}
 {:name name,     :position :named, :type :named,      :datatype java.lang.String, :attributes nil, :constraints nil, :add-on nil}
 {:name surname,  :position :named, :type :named,      :datatype java.lang.String, :attributes nil, :constraints nil, :add-on nil}
 {:name street,   :position :named, :type :named,      :datatype java.lang.String, :attributes nil, :constraints nil, :add-on nil}
 {:name zip-code, :position :named, :type :named,      :datatype java.lang.String, :attributes nil, :constraints nil, :add-on nil}
 {:name city,     :position :named, :type :named,      :datatype java.lang.String, :attributes nil, :constraints nil, :add-on nil}
 {:name county,   :position :named, :type :named,      :datatype java.lang.String, :attributes {:has-default true}, :constraints nil, :add-on {:default-value "N/A"}}
 {:name country,  :position :named, :type :named,      :datatype java.lang.String, :attributes {:has-default true}, :constraints nil, :add-on {:default-value "Germany"}})

(require '[schema.core :as s]
         '[active.data.realm :as realm])

;; Suppose we have a custom domain predicate for identifying European/German zip-codes
(defn valid-zip? [z] (and (string? z) (re-matches #"^\d{5}$" z)))

(s/defn process-order
        "Processes a customer order and returns a shipping manifest summary."
        [product  :- s/Str
         count    :- (s/pred pos?)               ;; Prismatic constraint predicate
         price    :- realm/number                ;; Active Data realm type
         & {:keys [name surname street zip-code city county country]
            :or   {county "N/A" country "Germany"}} :- {(s/optional-key :name)     realm/string
                                                        (s/optional-key :surname)  realm/string
                                                        (s/optional-key :street)   realm/string
                                                        (s/optional-key :zip-code) (s/pred valid-zip?)
                                                        (s/optional-key :city)     realm/string
                                                        (s/optional-key :county)   realm/string
                                                        (s/optional-key :country)  realm/string}]
        {:product product :total (* count price) :ship-to surname})

(def assoc-param-def
  "the definition of a parameter"
  [:name :position :type :datatype :attributes :constraints :add-on])

(defn extract-schema-info [sch]
      "Helper to unpack Prismatic Schemas and Active Data constraints."
      (cond
        ;; Check if the schema is an explicit Prismatic inline predicate (s/pred)
        (instance? schema.core.Predicate sch)
        {:datatype :any :constraints {:predicate-fn (:pred-name sch)}}

        ;; Check if the schema is part of an Active Data realm or specific predicate
        (and (map? sch) (contains? sch :active.data.realm/type))
        {:datatype (:active.data.realm/type sch) :constraints (:active.data.realm/constraints sch)}

        ;; Fallback for default primitive wrappers
        sch
        {:datatype sch :constraints nil}

        :else
        {:datatype :any :constraints nil}))

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
({:name product,  :position 1,      :type :positional, :datatype java.lang.String, :attributes nil, :constraints nil, :add-on nil}
 {:name count,    :position 2,      :type :positional, :datatype :any,             :attributes nil, :constraints {:predicate-fn pos?}, :add-on nil}
 {:name price,    :position 3,      :type :positional, :datatype :active.data/number, :attributes nil, :constraints nil, :add-on nil}
 {:name name,     :position :named, :type :named,      :datatype :active.data/string, :attributes nil, :constraints nil, :add-on nil}
 {:name surname,  :position :named, :type :named,      :datatype :active.data/string, :attributes nil, :constraints nil, :add-on nil}
 {:name street,   :position :named, :type :named,      :datatype :active.data/string, :attributes nil, :constraints nil, :add-on nil}
 {:name zip-code, :position :named, :type :named,      :datatype :any,             :attributes nil, :constraints {:predicate-fn valid-zip?}, :add-on nil}
 {:name city,     :position :named, :type :named,      :datatype :active.data/string, :attributes nil, :constraints nil, :add-on nil}
 {:name county,   :position :named, :type :named,      :datatype :active.data/string, :attributes {:has-default true}, :constraints nil, :add-on {:default-value "N/A"}}
 {:name country,  :position :named, :type :named,      :datatype :active.data/string, :attributes {:has-default true}, :constraints nil, :add-on {:default-value "Germany"}})



