(ns io.github.hglabplh_tech.reflect.clojure.example.the-funs-ns
  (:refer-clojure :exclude [defn fn def ])
  (:require [active.data.realm :as realm]
            [clojure.pprint :refer :all]
            [active.data.realm.attach :refer :all]
            [active.data.record :as rec]
            [schema.spec.core :as spec]
            [schema.core :as s]
            [active.data.realm.internal.record-meta :as act-meta]
            [io.github.hglabplh_tech.reflect.clojure.example.the-data :as data]))
(s/set-fn-validation! (boolean 1))

(def all-users (atom {}))

(def all-bills (atom {}))


(defn log-thing :- realm/boolean
      [the-output :- realm/any]
      (pprint the-output)
      (boolean 1))

(defn logit :- realm/any [to-write :- realm/any]
      (println "Content Logged :")
      (log-thing to-write))

(defn add-user :- (realm/record->record-realm data/user)
      [acc-no :- realm/uuid
       gen   :- realm/string
       lastn :- realm/string
       nickn :- realm/string
       addr1 :- realm/string
       addr2 :- realm/string
       co :- realm/string]
      (let [the-user (data/user data/account-no acc-no
                 data/gender  gen
                 data/lastname lastn
                 data/nickname nickn
                 data/address1 addr1
                 data/address2 addr2
                 data/county co)]
        (logit the-user)
        (swap! all-users conj {acc-no the-user})
        the-user))



(defn add-bill :- (realm/record->record-realm data/bill)
      [user-id :- realm/uuid
       bill-no :- realm/integer
       article-list :- (realm/set-of realm/string)
       total-price :- realm/number]
      (let [the-bill (data/bill data/account-no user-id  data/billing-no bill-no
                                data/billing-articles article-list
                                data/total total-price )]
        (logit the-bill)
        (swap! all-bills conj {bill-no the-bill})
        the-bill))