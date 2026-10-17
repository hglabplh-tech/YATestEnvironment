(ns io.github.hglabplh-tech.productive.special-meta.contracts
  (:require [active.data.realm :as realm]
            [active.data.struct.internal.key :refer :all]
            [clojure.pprint :refer :all]
            )
  )

(def hex-regexp #"\p{XDigit}+")
(def email-regexp #"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$")
(def http-url-regexp #"^(https?)://[A-Za-z0-9.-]+(:[0-9]{1,5})?(/[A-Za-z0-9._~:/?#\[\]@!$&'()*+,;=%-]*)?$")
(def ger-zip-code #"^\d{5}$")
;; Strict IPv4 regex: 4 octets from 0-255 without unneeded leading zeros
(def ipv4-regexp
  #"^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")

;; Strict IPv6 regex: Handles all standard hex layouts, omitted zeros, and shorthand colons
(def ipv6-regexp
  #"^(?:(?:[0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|(?:[0-9a-fA-F]{1,4}:){1,7}:|(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}|(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}|(?:[0-9a-fA-F]{1,4}:){1,3}(?::[0-9a-fA-F]{1,4}){1,4}|(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:(?:(?::[0-9a-fA-F]{1,4}){1,6})|:(?:(?::[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(?::[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(?:ffff(?::0{1,4}){0,1}:){0,1}(?:(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3}(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])|(?:[0-9a-fA-F]{1,4}:){1,4}:(?:(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3}(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$")

;; this is how I thought it will be the best variant to define such things - needs changes in active-data

;;(def hexadecimal-realm (realm/regex
;;                       hex-regexp))
;;
;;(def email-realm (realm/regex
;;                 email-regexp))
;;
;;(def http-url-realm (realm/regex
;;                      http-url-regexp))
;;
;; this is the variant I do not prefer but how it is designed up to now no changes in active-data
;;
(clojure.core/defn build-regex-realm [pred-key reg-exp description]
  (let [regexp-bind reg-exp
        descr-bind description
        ]
    (realm/restricted pred-key
                      realm/string
                      (fn [to-check] (re-matches regexp-bind to-check))
                      descr-bind)))

(def hex-check (build-regex-realm :hexstring hex-regexp "hexstring - check for hexadecimal number"))
(def email-check (build-regex-realm :emailstring email-regexp "emailstring - check for a correct email string"))
(def http-url-check (build-regex-realm :urlstring http-url-regexp "urlstring - check for a correct url http / https"))
(def ger-zip-code-check (build-regex-realm :ger-zip-code ger-zip-code "check for the german zipcode format"))

(def ipv4-format-check (build-regex-realm :ipv4string ipv4-regexp "ipv4string - check for a valid ipv4 address string"))
(def ipv6-format-check (build-regex-realm :ipv6string ipv6-regexp "ipv6string - check for a valid ipv6 address string"))
(def ip-format-check (realm/union ipv4-format-check ipv6-format-check))

(clojure.core/defn build-java-type-realm [pred-key java-type]
  (let [java-type-bind java-type
        descr-bind (str "Java class to check for: " java-type)
        ]
    (realm/restricted pred-key realm/any
                      (fn [to-check]
                        (if (instance? java.lang.Object to-check)
                          (instance? java-type-bind to-check)
                          false))
                      descr-bind)))

(defn check-for-block-size-int [block-div]
  (let [division block-div]
    (fn [val]
      (zero? (mod val division)))))

(defn build-integer-restrictor
  [pred-key pred-gen-fun description & criterion]
  (let [the-fun (apply pred-gen-fun criterion)]
    (realm/restricted pred-key realm/integer the-fun description)
    ))
(defn block-size-checker
  [block-size-div]
  (build-integer-restrictor :block-size-mult-check check-for-block-size-int
                            (str "Check for BlockSize is multiple of: " block-size-div)
                                                                block-size-div))
