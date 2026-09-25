(ns io.github.hglabplh-tech.reflect.code.general.gen-unified-astlike-code
  (:refer-clojure :exclude [defn fn])
  (:require [active.data.realm :as realm]
            [active.data.record :as rec]

            [clojure.pprint :refer [pprint]])
  )

(def transform-keys
  [:class, :enum, :lambda, :switch-type, :function, :method, :var, :member-var,
   :record, :inner-class :field])

(rec/def-record base-type-def [the-type :- realm/string
                           init-value :- realm/any
                           array-type? :- realm/boolean
                           the-dimension :- (realm/set-of realm/integer)])

(rec/def-record class-type-def  :extends base-type-def [the-name :- realm/string
                                               extends-type :- realm/string
                                               implements-types :- (realm/set-of realm/string)
                                               class-type :- (realm/enum :field :record :enum :class-definition :inner)
                                               class-fields :- realm/any
                                               class-methods :- realm/any]) ;; the any types have to be substituted

(def my-base (class-type-def extends-type "" implements-types []  class-type :field class-fields [] class-methods []  the-type "Integer" init-value 5 array-type? false the-dimension [0]))
(pprint my-base)
