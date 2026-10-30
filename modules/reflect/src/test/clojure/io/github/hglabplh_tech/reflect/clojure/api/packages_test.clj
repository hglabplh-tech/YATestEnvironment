(ns io.github.hglabplh_tech.reflect.clojure.api.packages-test
  (:require [clojure.pprint :refer :all]
            [clojure.test :refer :all]
            [io.github.hglabplh_tech.reflect.clojure.api.packages :as pkgs]))



(deftest hgp.package.list.test
  (testing "This test checks if the list-pkgs works correctly"
    (let [result (pkgs/list-packages
                   "io.github.hglabplh_tech.reflect.clojure.api"
                   "io.github.hglabplh_tech.reflect.clojure")]
      (pprint (map type (first result) ))
      (pprint result)
    )))

(run-tests)