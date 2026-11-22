(ns io.github.hglabplh-tech.reflect.util.collection-path-test
  (:require [clojure.pprint :refer [pprint]]
            [clojure.test :refer :all]
            [com.rpl.specter :as spect]
            [io.github.hglabplh-tech.reflect.util.collection-path :as sut]))

;; =============================================================================
;; TEST FIXTURES (Heterogeneous AST / Reflection Trees)
;; =============================================================================

(def sample-ir
  {:ast/id "java:com.example.OrderService"
   :ast/tag :type-definition
   :ast/attributes {:element/name "OrderService"
                    :element/visibility :public}
   :ast/children
   [{:ast/tag :callable
     :ast/attributes {:element/name "processOrder"
                      :element/visibility :public
                      :callable/kind :method}
     :ast/children [{:ast/tag :parameter
                     :ast/attributes {:element/name "orderId" :type "String"}}
                    {:ast/tag :parameter
                     :ast/attributes {:element/name "amount" :type "int"}}]}
    {:ast/tag :callable
     :ast/attributes {:element/name "auditOrder"
                      :element/visibility :private
                      :callable/kind :method}
     :ast/children [{:ast/tag :parameter
                     :ast/attributes {:element/name "secretAuditKey" :type "String"}}]}
    {:ast/tag :callable
     :ast/attributes {:element/name "verifyToken"
                      :element/visibility :protected
                      :callable/kind :method}}
    {:ast/tag :member
     :ast/attributes {:element/name "maxRetries"
                      :element/visibility :private}}
    ;; Secondary unknown schema shape inside the same tree
    {:kind :clojure-fn
     :meta {:access :private
            :name "internal-helper"
            :tags #{:pure :audited}}}]})

;; =============================================================================
;; 1. UNIT TESTS FOR LOW-LEVEL PREDICATES (has-value-at? / has-value-in?)
;; =============================================================================

(deftest has-value-at-test
  (let [node {:ast/tag :callable
              :ast/attributes {:element/visibility :public}}]
    (testing "matches single top-level keyword path"
      (is (true? (sut/has-value-at? node :ast/tag :callable)))
      (is (false? (sut/has-value-at? node :ast/tag :member))))

    (testing "matches deeply nested vector path"
      (is (true? (sut/has-value-at? node [:ast/attributes :element/visibility] :public)))
      (is (false? (sut/has-value-at? node [:ast/attributes :element/visibility] :private))))

    (testing "safely returns false on non-map inputs or missing keys"
      (is (false? (sut/has-value-at? nil :ast/tag :callable)))
      (is (false? (sut/has-value-at? [:not-a-map] :ast/tag :callable)))
      (is (false? (sut/has-value-at? "string" :ast/tag :callable)))
      (is (false? (sut/has-value-at? node [:non :existent :path] :callable))))))

(deftest has-value-in-test
  (let [node {:ast/tag :callable
              :ast/attributes {:element/visibility :protected}}]
    (testing "accepts sets, vectors, lists, and single scalar values"
      (is (true? (sut/has-value-in? node [:ast/attributes :element/visibility] #{:private :protected})))
      (is (true? (sut/has-value-in? node [:ast/attributes :element/visibility] [:private :protected])))
      (is (true? (sut/has-value-in? node [:ast/attributes :element/visibility] '(:protected))))
      (is (true? (sut/has-value-in? node [:ast/attributes :element/visibility] :protected)))
      (is (false? (sut/has-value-in? node [:ast/attributes :element/visibility] #{:public :package-private}))))

    (testing "handles nil values safely without false positives"
      (is (false? (sut/has-value-in? node :missing-key #{:public :private})))
      (is (true? (sut/has-value-in? {:a nil} :a #{nil :other}))))))

;; =============================================================================
;; 2. UNIT TESTS FOR MULTI-CRITERIA MATCHER (matches-criteria?)
;; =============================================================================

(deftest matches-criteria-test
  (let [node {:ast/tag :callable
              :ast/attributes {:element/name "auditOrder"
                               :element/visibility :private}}]
    (testing ":and mode (default) requires all criteria to match"
      (is (true? (sut/matches-criteria?
                   node
                   {:ast/tag :callable
                    [:ast/attributes :element/visibility] #{:private :protected}})))
      (is (false? (sut/matches-criteria?
                    node
                    {:ast/tag :callable
                     [:ast/attributes :element/visibility] :public}))))

    (testing ":or mode succeeds if at least one criterion matches"
      (is (true? (sut/matches-criteria?
                   node
                   {:ast/tag :member
                    [:ast/attributes :element/visibility] :private}
                   :or)))
      (is (false? (sut/matches-criteria?
                    node
                    {:ast/tag :member
                     [:ast/attributes :element/visibility] :public}
                    :or))))

    (testing "accepts sequence of [path expected] pairs as well as maps"
      (is (true? (sut/matches-criteria?
                   node
                   [[:ast/tag :callable]
                    [[:ast/attributes :element/name] "auditOrder"]]
                   :and))))

    (testing "throws ex-info on invalid mode"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"Invalid match mode"
                            (sut/matches-criteria? node {:ast/tag :callable} :xor))))

    (testing "returns false for non-map nodes"
      (is (false? (sut/matches-criteria? [1 2 3] {:ast/tag :callable}))))))

;; =============================================================================
;; 3. DIRECT NAVIGATOR TESTS (ALL-NODES & WALK-NODES-WITH-PRUNE)
;; =============================================================================

(deftest recursive-navigators-test
  (testing "ALL-NODES traverses every nested node without stopping when an ancestor matches"
    (let [public-nodes (spect/select
                         [sut/ALL-NODES
                          #(sut/has-value-at? % [:ast/attributes :element/visibility] :public)
                          :ast/attributes
                          :element/name]
                         sample-ir)]
      ;; Matches BOTH the parent type-definition AND the child processOrder callable
      (is (= ["OrderService" "processOrder"] public-nodes))))

  (testing "WALK-NODES-WITH-PRUNE halts descent when prune-pred returns true"
    (let [private-branch? (fn [node]
                            (sut/has-value-at? node [:ast/attributes :element/visibility] :private))
          visible-params  (spect/select
                            [(sut/WALK-NODES-WITH-PRUNE private-branch?)
                             #(sut/has-value-at? % :ast/tag :parameter)
                             :ast/attributes
                             :element/name]
                            sample-ir)]
      ;; 'secretAuditKey' inside private 'auditOrder' is pruned; only public params remain
      (is (= ["orderId" "amount"] visible-params))))

  (testing "WALK-NODES-WITH-PRUNE stops at root if root itself matches prune-pred"
    (let [prune-root (fn [node] (sut/has-value-at? node :ast/tag :type-definition))
          res        (spect/select [(sut/WALK-NODES-WITH-PRUNE prune-root) map?] sample-ir)]
      (is (= [] res)))))

;; =============================================================================
;; 4. TESTS FOR smart-walk-through (Map, Vector, Fn Specs & :prune-pred)
;; =============================================================================

(deftest smart-walk-through-test
  (testing "supports map spec (:values & :select-path), vector shorthand, and raw fn"
    (let [result (sut/smart-walk-through
                   sample-ir
                   ;; 1. Map spec with :values and :select-path
                   {:id          :param-names
                    :pred-fun    sut/has-value-at?
                    :values      [:ast/tag :parameter]
                    :select-path [:ast/attributes :element/name]}

                   ;; 2. Vector shorthand: [pred-fun path allowed-values]
                   [sut/has-value-in? [:ast/attributes :element/visibility] #{:protected}]

                   ;; 3. Raw 1-arg predicate function
                   (fn [n] (and (map? n) (= :clojure-fn (:kind n)))))]

      ;; Keyed by explicit :id (includes all 3 parameters across public and private methods)
      (is (= ["orderId" "amount" "secretAuditKey"] (:param-names result)))

      ;; Keyed by positional index 1 (vector shorthand)
      (is (= 1 (count (get result 1))))
      (is (= "verifyToken"
             (get-in (first (get result 1)) [:ast/attributes :element/name])))

      ;; Keyed by positional index 2 (raw fn)
      (is (= ["internal-helper"]
             (mapv #(get-in % [:meta :name]) (get result 2))))))

  (testing "supports :prune-pred inside smart-walk-through map spec"
    (let [result (sut/smart-walk-through
                   sample-ir
                   {:id          :public-only-params
                    :pred-fun    sut/has-value-at?
                    :values      [:ast/tag :parameter]
                    :prune-pred  #(sut/has-value-at? % [:ast/attributes :element/visibility] :private)
                    :select-path [:ast/attributes :element/name]})]
      (is (= ["orderId" "amount"] (:public-only-params result))))))

(run-tests)
