(ns io.github.hglabplh_tech.reflect.examples.clojure.the-funs-ns
  (:refer-clojure :exclude [def defn fn])
  (:require [active.data.realm :as realm]
            [active.data.realm.attach :refer :all]
            [active.data.record :as sut]
            [clojure.pprint :refer :all]
            [io.github.hglabplh_tech.reflect.examples.clojure.the-data :as data]
    ;;TODO: look how to load the ns with all ns things required and think about exclude
            [schema.core :as s]))
(s/set-fn-validation! (boolean 1))

(def all-users (atom {}))




(defn log-thing :- realm/boolean
      [the-output :- realm/any]
      (pprint the-output)
      (boolean 1))

(defn logit :- realm/any [to-write :- realm/any]
      (println "Content Logged :")
      (log-thing to-write))


(defn my-easy-test :- realm/number
      [description :- realm/string
       op-class :- realm/symbol
       first-val :- realm/number
       second-val :- realm/number
       ]
      (let [print-text (str description "of: " op-class ": ")
            result (+ first-val second-val)]
        (println (str print-text result))
        result))

(defn my-set-test :- realm/any
      [firstval :- realm/number
       setval :- (realm/set-of realm/number)]
      (let [print-text (str " set " setval)]
        print-text
        ))

(defn my-enum-test :- realm/any
      [firstval :- realm/number
       enumval :- (realm/enum :one :two :three :four :five :six :seven 2 4 6 8 0)]
      (let [print-text (str "found  two params number is: " firstval " enum value is: " enumval)]
        print-text
        ))

(defn operator-fun :- realm/number
      [my-msg :- realm/string
       op1 :- realm/number
       op2 :- realm/number]
      (* op1 op1))

(defn my-complex-test :- realm/any                          ;;(realm/record->record-realm the-rec)
      [firstval :- realm/number
       secondval :- realm/number
       thirdval :- (realm/set-of realm/symbol)
       int-range-param :- (realm/integer-from 5)
       real-range-param :- (realm/real-range :ex 7 10 :in)
       enum-val :- (realm/enum 4 5 6 7)
       operator-fun :- (realm/function
                         realm/string realm/number realm/number
                         -> realm/number)]
      (pprint thirdval)
      (- (+ firstval secondval)
         (+ secondval firstval (operator-fun "mul" 8.78 9.5))))

(defn string->integer-with-contract :- realm/integer
      [s :- realm/string
       base :- (realm/optional [realm/integer])]  [s base])