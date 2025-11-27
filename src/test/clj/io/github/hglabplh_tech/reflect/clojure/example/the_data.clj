(ns ie.harald.g.p.it-cons.test.suite.example.the-data
      (:refer-clojure :exclude [defn fn])
  (:require [active.data.realm :as realm]
            [clojure.pprint :refer :all]
            [active.data.realm.attach :refer :all]
            [active.data.record :as rec]
            [schema.spec.core :refer :all]
            [active.data.realm.internal.record-meta :as act-meta]
            ))

(rec/def-record user [account-no :- realm/uuid
                      gender   :- realm/string
                      lastname :- realm/string
                      nickname :- realm/string
                      address1 :- realm/string
                      address2 :- realm/string
                      county :- realm/string])

(rec/def-record bridge [account-no :- realm/uuid
                        billings :- (realm/set-of realm/integer)
                        ])

(rec/def-record bill [account-no :- realm/uuid
                      billing-no :- realm/integer
                      billing-articles :- (realm/set-of realm/string)
                      total :- realm/number])
