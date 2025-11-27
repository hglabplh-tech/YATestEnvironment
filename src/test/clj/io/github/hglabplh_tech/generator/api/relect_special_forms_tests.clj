(ns io.github.hglabplh-tech.reflect.clojure.api.relect-special-forms-tests
  (:require [clojure.pprint :as pp]
            [clojure.test :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-class :as rcl]
            [io.github.hglabplh-tech.reflect.clojure.api.reflect-special-forms :as sform]))
(def enum-check-map {:enum-spec
                      {"HOST"
                       {:ordinal 0,
                        :class-descr
                        "Lio/github/hglabplh_tech/reflect/clojure/api/app_exam/AppConfigFields;",
                        :enum-descr "EnumDesc[AppConfigFields.HOST]"},
                       "PORT"
                       {:ordinal 1,
                        :class-descr
                        "Lio/github/hglabplh_tech/reflect/clojure/api/app_exam/AppConfigFields;",
                        :enum-descr "EnumDesc[AppConfigFields.PORT]"},
                       "SSL_PORT"
                       {:ordinal 2,
                        :class-descr
                        "Lio/github/hglabplh_tech/reflect/clojure/api/app_exam/AppConfigFields;",
                        :enum-descr "EnumDesc[AppConfigFields.SSL_PORT]"},
                       "URL_SPEC"
                       {:ordinal 3,
                        :class-descr
                        "Lio/github/hglabplh_tech/reflect/clojure/api/app_exam/AppConfigFields;",
                        :enum-descr "EnumDesc[AppConfigFields.URL_SPEC]"},
                       "USER"
                       {:ordinal 4,
                        :class-descr
                        "Lio/github/hglabplh_tech/reflect/clojure/api/app_exam/AppConfigFields;",
                        :enum-descr "EnumDesc[AppConfigFields.USER]"},
                       "IP"
                       {:ordinal 5,
                        :class-descr
                        "Lio/github/hglabplh_tech/reflect/clojure/api/app_exam/AppConfigFields;",
                        :enum-descr "EnumDesc[AppConfigFields.IP]"}}})

(def record-check-map {:record-spec
                       {:test [java.lang.Integer],
                       :first-name [java.lang.String],
                       :last-name [java.lang.String],
                       :ctor-parm-type [java.lang.Integer java.lang.String java.lang.String]}})

(deftest enum.analyze.test
  (testing "Here the enum reflection transform is tested"
    (let [class-util (rcl/get-class-util
                       "io.github.hglabplh_tech.reflect.clojure.api.app_exam.AppConfigFields")
          result (sform/analyze-enum (rcl/get-the-class class-util))]
      (pp/pprint result)
      (is (= enum-check-map result))
      )))

(deftest record.analyze.test
  (testing "Here the record reflection transform is tested"
    (let [class-util (rcl/get-class-util
                       "io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec.MyTestRecord")
          result (sform/analyze-record (rcl/get-the-class class-util))]
      (pp/pprint result)
      (is (= record-check-map result))
      )
    ))

(deftest lambda.analyze.test
  (testing "Here the record reflection transform is tested"
    (let [class-util (rcl/get-class-util
                       "io.github.hglabplh_tech.reflect.clojure.api.example.lambda.LambdaCollect")
          result (sform/analyze-lambda (rcl/get-the-class class-util))]
      (pp/pprint result)                                    ;; TODO: FIXME: Empty result !!!
      ;;(is (= record-check-map result))
      )
    ))

(run-tests)