(ns io.github.hglabplh-tech.reflect.examples.clojure.complex.mixed-defs
  (:require [active.data.realm :as realm]
            [schema.core :as s]))

(defn valid-zip? [z] (and (string? z) (re-matches #"^\d{5}$" z)))


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

