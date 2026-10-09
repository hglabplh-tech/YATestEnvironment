(ns io.github.hglabplh-tech.reflect.clojure.special-meta.contracts-tests
  (:refer-clojure :exclude [def defn fn])
  (:require [clojure.pprint :refer [pprint]]
            [clojure.test :refer :all]
            [active.data.realm.attach :refer :all]
            [active.data.realm :as realm]
            [active.data.realm.schema :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.special-meta.contracts :as contract]
            [io.github.hglabplh-tech.tests.utils.general-utils :as utils]
            [schema.core :as s])
  (:import  (io.github.hglabplh_tech.reflect.java.api.utils ClassUtil)))

(defn test-fun-hex :- realm/boolean
  [hex-str :- contract/hex-check]
  (println hex-str)
  true)

(defn test-fun-urls :- realm/boolean
  [param :- contract/email-check
   param2 :- contract/http-url-check]
  (println param)
  (println param2)
  true)

(defn test-fun-clutil :- realm/boolean
      [param :- (contract/build-java-type-realm :class-util ClassUtil)]
      (pprint param)
      true)

(deftest simple-regex-test (testing "the hex regex / the email regex and the url regex")
                           (is (some? (s/validate (schema contract/hex-check)  "ACDEFbcda567832")))
                           (is (thrown? Exception (s/validate (schema contract/hex-check)  "ACDEFbcda567832ZK")))
                           (test-fun-hex "AABBeeff0768D")
                           (test-fun-urls "hglabplhak@gmail.com" "https://www.ibm.com")
                           (test-fun-urls "hglabplhak@icloud.com" "http://www.ibm.com")
                           )
(deftest simple-java-type-test (testing "UUID instance? realm")
                               (is (s/validate (schema (contract/build-java-type-realm :uuid java.util.UUID))
                                                           (java.util.UUID/randomUUID)), true)
                               (is (thrown? Exception (s/validate (schema (contract/build-java-type-realm :uuid java.util.UUID))  "test")))
                               (is (thrown? Exception (utils/call-with-schema-check test-fun-clutil java.util.Date)))
                               (is (utils/call-with-schema-check test-fun-clutil (ClassUtil. java.util.UUID)), true)
                               )
(run-tests)
