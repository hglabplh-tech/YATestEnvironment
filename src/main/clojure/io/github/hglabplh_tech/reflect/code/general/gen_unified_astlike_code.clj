;; Copyright (c) 2026 Harald Glab-Plhak

(ns io.github.hglabplh-tech.reflect.code.general.gen-unified-astlike-code
  (:refer-clojure :exclude [defn fn])
  (:require [active.data.realm :as realm]
            [active.data.record :as rec]
            [clojure.pprint :refer [pprint]])
  )

(def transform-keys
  [:class, :enum, :lambda, :switch-type, :function, :method, :var, :member-var,
   :record, :inner-class :field])

(rec/def-record syntax-root [prog-name :- realm/string
                             name-space :- realm/string
                             prog-content :- realm/record])

(rec/def-record base-type-def [the-name :- realm/string
                               the-type :- (realm/enum :string :byte :integer :short :float :double :long :class :object :function)
                               static? :- realm/boolean
                               immutable? :- realm/boolean?
                               final? :- realm/boolean      ;;classes or definitions like java.lang.String which cannot be overrided
                               init-value :- realm/any])

(rec/def-record array-type [base-type-of :- realm/record
                            the-dimension :- (realm/set-of realm/integer)])

(rec/def-record class-type-def :extends base-type-def [extends-type :- realm/string1
                                                       abstract? :- realm/boolean
                                                       access-def :- (realm/enum :public :protected :private)
                                                       implements-types :- (realm/set-of realm/string)
                                                       class-type :- (realm/enum :field :record :enum :class-definition :inner)
                                                       class-fields :- (realm/set-of realm/record)
                                                       class-methods :- (realm/set-of realm/record)]) ;; the any types have to be substituted

(rec/def-record var-type-def :extends base-type-def [scope :- (realm/enum :global :local :field)
                                                       access-def :- (realm/enum :public :protected :private)])

(rec/def-record parameters-def :extends base-def [named? :- realm/boolean
                                                  vararg? :- realm/boolean])

(rec/def-record meth-function :extends base-type-def [parameters :- (realm/sequence-of realm/record)
                                                      scope :- (realm/enum :global :local :method)
                                                      access-def :- (realm/enum :global :namespace :public :protected :private)])

(def my-base (class-type-def extends-type "" implements-types [] abstract? false class-type
                             :record static? false access-def
                             :public class-fields [] class-methods []
                             the-type :integer init-value 5  immutable? true final? false))
(pprint my-base)
