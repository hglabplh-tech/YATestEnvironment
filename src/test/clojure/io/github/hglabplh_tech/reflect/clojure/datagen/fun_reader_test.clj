(ns io.github.hglabplh_tech.reflect.clojure.datagen.fun-reader-test
  (:refer-clojure :exclude [def defn fn])
  (:require [active.data.realm :as realm]
            [active.data.realm.attach :refer :all]
            [active.data.realm.schema :refer :all]
            [active.data.record :as sut]
            [clojure.pprint :refer :all]
            [clojure.test :refer :all]
            [io.github.hglabplh_tech.reflect.clojure.datagen.fun-reader :refer :all]))

(sut/def-record the-rec [return-val :- realm/symbol
                         parm-list :- (realm/set-of realm/symbol)
                         line :- (realm/integer-from 7)
                         factor :- (realm/real-range :ex 7.7 100.8 :in)
                         ])
(def test-the-rec (the-rec return-val 'k parm-list (set '(p o k f))
                           line 8 factor 56.9))

(defn adder :- realm/number [title :- realm/string
                             parm1 :- realm/number
                             parm2 :- realm/number]
      (println title)
      (+ parm1 parm2))
(defn my-test :- realm/any                                  ;;(realm/record->record-realm the-rec)
      [first-val :- realm/number
       second-val :- realm/number
       third-val :- (realm/set-of realm/symbol)
       ;;int-range-param :- (realm/integer-from 5)
       ;;                       real-range-param :- (realm/real-range :ex 7 10 :in)
       enum-val :- (realm/enum 4 5 6 7)
       fun-val :- (realm/function
                    realm/string realm/number realm/number
                    -> realm/number)]
      (pprint third-val)
      (- (+ first-val second-val)
         (+ second-val first-val (fun-val 8 9)))
      test-the-rec
      )







(deftest structured-fun-schema.test
  (testing "The conversion of the meta-schema defn to structured output"
    (analyze-struct hgp.cljito.datagen.fun-reader-test my-test)
    ))



(deftest structured-def-rec-schema.test (testing "The conversion of the meta-schema records to structured output"

    (pprint (get-rec-meta the-rec))
    (pprint (meta-raw
              hgp.cljito.datagen.fun-reader-test
              test-the-rec))
    (pprint (meta-raw
              hgp.cljito.datagen.fun-reader-test
              the-rec))



    ))
(run-tests)
