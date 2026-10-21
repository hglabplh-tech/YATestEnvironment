(ns io.github.hglabplh-tech.reflect.examples.clojure.complex.mixed-defs
  (:require [active.data.realm :as realm]
            [schema.core :as s]
            [clojure.spec.alpha :as spec] ))

;; 1. Register specs for individual keys
(spec/def ::tags (spec/coll-of string?))
(spec/def ::visibility #{:public :private})
(spec/def ::max-members int?)

;; 2. Spec uses spec/cat for positionals, then matches the map argument
(spec/fdef create-project
        :args (spec/cat :id int?
                     :owner string?
                     :title string?
                     :opts (spec/keys :req-un [::tags]
                                   :opt-un [::visibility ::max-members]))
        :ret map?)

;; 3. Implement the mixed function
(defn create-project [id owner title {:keys [tags visibility max-members]
                                      :or {visibility :public max-members 5}}]
  {:project/id           id
   :project/owner        owner
   :project/title        title
   :project/tags         tags
   :project/visibility   visibility
   :project/max-members  max-members})

(defn valid-zip? ^Boolean [^String z] (and (string? z) (re-matches #"^\d{5}$" z)))

(defn calc-plus ^Boolean [^String msg ^long a ^long b ] (println msg (str "result: " (+ a b))))

(defn ^clojure.lang.IPersistentMap process-user
  ;; 3 Positional Parameters
  [^Long id ^String owner ^Boolean active?
   ;; 3 Named Parameters via map destructuring with inline hints AND default values
   {:keys [^String title ^Long max-members ^clojure.lang.Keyword visibility]
    :or {title "Untitled" max-members 10 visibility :public}}]

  {:status "success" :id id})
;; Varargs are also supported in the higher-order function argument specifications.
(s/defn write-person-3 [format-f :- (s/fn String
                                      [String Integer]
                                      [String realm/string String  [String]])
                        person :- {:name String
                                   :age realm/integer
                                   :gender (realm/optional realm/char)}
                        address :- {:street realm/string
                                    :hno s/Int
                                    :zipcode (s/pred valid-zip?)
                                    :city s/Str
                                    :country realm/string}]
  (format-f (:name person) (:age person) "..."))

