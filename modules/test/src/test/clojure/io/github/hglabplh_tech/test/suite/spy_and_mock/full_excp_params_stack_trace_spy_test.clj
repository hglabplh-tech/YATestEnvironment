(ns io.github.hglabplh-tech.test.suite.spy-and-mock.full-excp-params-stack-trace-spy-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [io.github.hglabplh_tech.test.suite.spy-and-mock.mocking-jay :refer :all]
            [io.github.hglabplh_tech.test.suite.spy-and-mock.fun-meta-util :refer :all]))

;; Target functions for spy and mock tests
(defn divide-checked [a b]
  (when (neg? a)
    (throw (ex-info "Negative dividend not allowed" {:code :neg-input :a a})))
  (/ a b))

(defn fetch-account [id]
  {:id id :status :active})

;; Reset atoms and restore Vars around every test using THIS namespace
(use-fixtures :each
              (make-spy-fixture 'io.github.hglabplh-tech.test.suite.spy-and-mock.full-excp-params-stack-trace-spy-test))

(deftest spy-automatic-exception-tracking-test
  (testing "Spy tracks normal returns and automatically tracks thrown exceptions"
    (prolog to-spy divide-checked)
    (spy io.github.hglabplh-tech.test.suite.spy-and-mock.full-excp-params-stack-trace-spy-test)

    ;; 1. Normal call
    (is (= 5 (divide-checked 10 2)))

    ;; 2. Failing call throws and is automatically recorded in statistics
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Negative dividend"
                          (divide-checked -4 2)))

    (let [stats      (first (vals (first (first (get-spy-statistics ["divide-checked"])))))
          ex-entries (get-tracked-exceptions)]
      (is (= 2 (:fun-calls-count stats)))
      (is (= 1 (:exceptions-count stats)))
      (is (= 1 (count ex-entries)))
      (is (= {:code :neg-input :a -4} (get-in (first ex-entries) [:exception :data]))))))

(deftest spy-conditional-exception-tracking-test
  (testing "Conditional predicate filters which exceptions get recorded in statistics"
    (prolog to-spy divide-checked)
    (spy io.github.hglabplh-tech.test.suite.spy-and-mock.full-excp-params-stack-trace-spy-test)

    ;; Activate conditional tracking: ONLY record ExceptionInfo, ignore ArithmeticException
    (with-exception-tracking #(instance? clojure.lang.ExceptionInfo %)
                                ;; Division by zero throws ArithmeticException -> rethrown, NOT recorded
                                (is (thrown? ArithmeticException (divide-checked 10 0)))
                                (is (empty? (get-tracked-exceptions)))

                                ;; Negative input throws ExceptionInfo -> rethrown AND recorded
                                (is (thrown? clojure.lang.ExceptionInfo (divide-checked -1 5)))
                                (is (= 1 (count (get-tracked-exceptions)))))))

(deftest mock-exception-tracking-test
  (testing "Mock tracks exceptions thrown by [throw-ex ...] rules as well as normal stubbed returns"
    (prolog to-mock fetch-account)
    (mock io.github.hglabplh-tech.test.suite.spy-and-mock.full-excp-params-stack-trace-spy-test)

    ;; Register mock rules: throw on negative IDs, return stub map otherwise
    (call-cond-> fetch-account
                    :when :any-map?-key :<-
                    [[neg?]]
                    [throw-ex (ex-info "Account blocked" {:reason :blocked})]
                    :else
                    [return-val {:id 99 :status :mocked}])

    ;; 1. Stubbed return
    (is (= {:id 99 :status :mocked} (fetch-account 99)))

    ;; 2. Mocked exception via throw-ex
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Account blocked"
                          (fetch-account -7)))

    (let [stats      (first (vals (first (first (get-spy-statistics ["fetch-account"])))))
          ex-entries (get-tracked-exceptions)]
      (is (= 2 (:fun-calls-count stats)))
      (is (= 1 (:exceptions-count stats)))
      (is (= "Account blocked" (get-in (first ex-entries) [:exception :message]))))))
