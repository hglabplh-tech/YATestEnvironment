(ns io.github.hglabplh-tech.reflect.clojure.special-meta.contracts-tests
  (:refer-clojure :exclude [def defn fn])
  (:require [clojure.pprint :refer [pprint]]
            [clojure.test :refer :all]
            [active.data.realm.attach :refer :all]
            [active.data.realm :as realm]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.special-meta.contracts :as contract])
  (:import  (io.github.hglabplh_tech.reflect.java.api.utils ClassUtil)))
(defn test-fun-hex :- realm/any
  [hex-str :- contract/hexadecimal-realm]
  (println hex-str)
  true)

(defn test-fun-urls :- realm/any
  [param :- contract/email-realm
   param2 :- contract/http-url-realm]
  (println param)
  (println param2)
  true)

(defn test-fun-clutil :- realm/any
      [param :- (realm/java-type-realm ClassUtil)]
      (pprint param)
      true)

(deftest simple-regex-test (testing "the hex regex / the email regex and the url regex")

                           (test-fun-hex "AABBeeff0768D")
                           (test-fun-urls "hglabplhak@gmail.com" "https://www.ibm.com")
                           (test-fun-urls "hglabplhak@icloud.com" "http://www.ibm.com")
                           )
(deftest simple-java-type-test (testing "UUID instance? realm")
                               (test-fun-clutil ClassUtil)
                               (test-fun-clutil (ClassUtil. java.util.UUID))
                               )
(run-tests)
