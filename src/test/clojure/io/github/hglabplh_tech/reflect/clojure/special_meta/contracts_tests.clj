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

(defn test-fun-ip :- realm/boolean
      [hex-str :- contract/ip-format-check]
      (println hex-str)
      true)

(defn test-fun-urls :- realm/boolean
  [param :- contract/http-url-check]
  (println param)
  true)

(defn test-fun-email :- realm/boolean
      [param :- contract/email-check]
      (println param)
      true)

(defn test-fun-clutil :- realm/boolean
      [param :- (contract/build-java-type-realm :class-util ClassUtil)]
      (pprint param)
      true)

(defn test-fun-blocksize-mult :- realm/boolean
      [param :- (contract/block-size-checker 1024)]
      (pprint param)
      true)

(deftest simple-regex-test (testing "the hex regex / the email regex and the url regex"
                           (is (some? (s/validate (schema contract/hex-check)  "ACDEFbcda567832")))
                           (is (thrown? Exception (s/validate (schema contract/hex-check)  "ACDEFbcda567832ZK")))
                           (is (utils/call-with-schema-check test-fun-hex "AABBeeff0768D") true)
                           (is (thrown? Exception (utils/call-with-schema-check test-fun-hex "ACDEFbcda567832ZK")))
                           (is (utils/call-with-schema-check test-fun-urls  "https://www.ibm.com") true)
                           (is (thrown? Exception (utils/call-with-schema-check test-fun-urls  "htt?~ps://www.ibm.com")))
                           (is (utils/call-with-schema-check test-fun-email "hglabplhak@gmail.com") true)
                           (is (thrown? Exception (utils/call-with-schema-check test-fun-email "hglabplha??=k@gmail.com!!" )))
                           (is (utils/call-with-schema-check test-fun-ip "127.0.0.1") true)
                           (is (thrown? Exception (utils/call-with-schema-check test-fun-ip "259.0.0.1")))
                           (is (utils/call-with-schema-check test-fun-ip "2a00:1450:4001:831::2004") true)
                           (is (thrown? Exception (utils/call-with-schema-check test-fun-ip "2h00:145T:4001:831::2004"))))
                           )
(deftest simple-java-type-test (testing "Class type instance? realm"
                               (is (s/validate (schema (contract/build-java-type-realm :uuid java.util.UUID))
                                                           (java.util.UUID/randomUUID)) true)
                               (is (thrown? Exception (s/validate (schema (contract/build-java-type-realm :uuid java.util.UUID))  "test")))
                               (is (thrown? Exception (utils/call-with-schema-check test-fun-clutil java.util.Date)))
                               (is (utils/call-with-schema-check test-fun-clutil (ClassUtil. java.util.UUID)) true)
                               ))

(deftest simple-miscelaneous-realm-test (testing "Test of realms like block size is a multiple of and more"
                                          (is (s/validate (schema (contract/block-size-checker 1024))
                                                          4096) true)
                                          (is (thrown? Exception  (s/validate (schema (contract/block-size-checker 1024))
                                                                              4099) true))
                                          (is (thrown? Exception (utils/call-with-schema-check test-fun-blocksize-mult 4099)))
                                          (is (utils/call-with-schema-check test-fun-blocksize-mult 8192 ) true)

                                          ))
(run-tests)
