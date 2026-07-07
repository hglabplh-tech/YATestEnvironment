;; Copyright (c) 2026 Harald Glab-Plhak

(ns io.github.hglabplh-tech.reflect.clojure.api.reflect-special-forms
  (:require [io.github.hglabplh-tech.reflect.clojure.api.reflect-types :as types])
  (:import (io.github.hglabplh_tech.reflect.clojure.api.utils
             LambdaUtil
             SpecialFormsUtil
             SwitchUtil)
           (java.lang.reflect  AnnotatedType)           ))

(declare handle-record
         handle-enum)

(defn analyze-enum
  "Analyze enum type"
  {:added "1.1.0"
   :static true}
  [clazz]
  (let [enum-spec
        (SpecialFormsUtil/getEnumSpec clazz)]
    {:enum-spec enum-spec}
    ))

(defn analyze-record
  "Analyze record type"
  {:added "1.1.0"
   :static true}
  [clazz]
  (let [rec-spec
        (if (types/is-record clazz)
          (SpecialFormsUtil/getRecordSpec clazz)
          {})
        result (if (= rec-spec {})
                 rec-spec
                 {:record-spec rec-spec})]
    result))

(defn analyze-lambda
  "Analyze lambda expressions :: experimental phase // will be enhanced later"
  {:added "1.1.0"
   :static true}
  [clazz]
  (let [lambda-spec
        (LambdaUtil/getLambdaSpec clazz)]
    lambda-spec)
  )

(defn analyze-switch
  "Analyze switch statements and switch expressions"
  {:added "1.1.0"
   :static true}
  [clazz]
  (let [switch-spec
        (SwitchUtil/getSwitchSpec clazz)]
    switch-spec)
  )
