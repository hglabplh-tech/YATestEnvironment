(ns ie.harald.g.p.it-cons.test.suite.spy-and-mock.stack-funs-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [ie.harald.g.p.it-cons.test.suite.spy-and-mock.real-fun-checkers :refer :all]
            [ie.harald.g.p.it-cons.test.suite.spy-and-mock.mocking-jay :refer :all]
            [ie.harald.g.p.it-cons.test.suite.example.the-funs-ns :refer :all]
            [ie.harald.g.p.it-cons.test.suite.example.the-main :refer :all]
            [ie.harald.g.p.it-cons.test.suite.spy-and-mock.mocking-jay-test :refer :all]))



(defn stck-fun-tester []
  (try
    (add-some-user-entries)
  (catch Exception e (.getStackTrace e ))))

(defn test-stack-fun []
  )
(prolog to-spy stck-fun-tester add-some-user-entries add-bill add-user logit try-it)
  (deftest test.find-in-list
    (testing "This test checks the find ns-entry fun"
          (def test-vect ["apple" "banana" "banana"  "raspberry" "orange" "orange"
                "strawberry" "tomato" "tomato" "honey" "honey"])
          (is  (= (find-in-list test-vect "throttle") (boolean nil)))
          (is  (= (find-in-list test-vect "tomato") (boolean 1)))
          (is  (= (find-in-list test-vect "blueberry") (boolean nil)))
          (is  (= (find-in-list test-vect "honey") (boolean 1)))
      ))

(deftest test.real-life.find-in-list
  (testing "This test checks the find ns-entry fun - with real life conditions"
    (spy hgp.cljito.example.the-main hgp.cljito.example.the-funs-ns)
    (let [stk-trace (stck-fun-tester)]
      (get-processed-stackdata stk-trace (meta (var stck-fun-tester)))
      )
    ))
(run-tests)
