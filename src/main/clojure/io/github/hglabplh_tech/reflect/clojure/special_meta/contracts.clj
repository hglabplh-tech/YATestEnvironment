(ns io.github.hglabplh-tech.reflect.clojure.special-meta.contracts
  (:refer-clojure :exclude [def defn fn])
  (:require [active.data.struct.internal.key :refer :all]
            [active.data.realm.attach :refer :all]
            [active.data.realm :as realm]
            [active.data.record :as sut]
            [clojure.pprint :refer :all]))

(def hex-regexp #"\p{XDigit}+")
(def email-regexp #"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$")
(def http-url-regexp #"^(https?)://[A-Za-z0-9.-]+(:[0-9]{1,5})?(/[A-Za-z0-9._~:/?#\[\]@!$&'()*+,;=%-]*)?$")

(def hexadecimal-realm (realm/regex
                         hex-regexp))

(def email-realm (realm/regex
                  email-regexp))

(def http-url-realm (realm/regex
                   http-url-regexp))



