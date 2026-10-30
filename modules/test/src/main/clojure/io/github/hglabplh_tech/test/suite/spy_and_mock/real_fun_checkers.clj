;; (c) Harald Glab-Plhak
;; TODO: look for a better place for each function here some functions are really for mock / spy but some are reflective. Refacture this later
(ns io.github.hglabplh_tech.test.suite.spy-and-mock.real-fun-checkers
  (:refer-clojure :exclude [defn fn])
  (:require [clojure.walk :refer :all]
            [clojure.reflect :as refl]
            [clojure.pprint :refer :all]
            [active.data.realm :as realm]
            [active.data.realm.attach :refer :all]
            [active.data.realm.internal.record-meta :as act-meta]

            [active.data.realm.internal.records :as realm-records]
            [active.data.raw-record :as rrec]
            [schema.core :as sc]
            [active.data.record :as sut]

            [clojure.pprint :refer :all]
            [clojure.test :refer :all]
            [schema.spec.core :refer :all]
            ))
(def to-spy :spy-key)
(def to-mock :mock-key)


(defmacro get-fun-meta [funname]
  `(meta (var ~funname)))


(clojure.core/defn get-schema-active-meta [all-meta-data gen-meta-data] ;; FIXME metadata have to be overviewed
  (let [active-fun-meta (get all-meta-data fn-realm-meta-key {})
        schema-meta-data (get all-meta-data :schema)
        result-meta (conj {:schema schema-meta-data fn-realm-meta-key active-fun-meta}  gen-meta-data)]
    (println result-meta)
    result-meta

    )

  )
;;; implement it with that logic slightly changed
;; from active-data and in active data it works the thing
;; with using constantly seems to me a very good idea


(defmacro transfer-fun-meta
  "Macro to set the Meta of the mocked function to a mock"
  [generated-fun orig-fun]
  `(do (alter-meta! (var ~generated-fun)
                    (constantly
                      (assoc (get-schema-active-meta (meta (var ~orig-fun))
                                                     (meta (var ~generated-fun)))
                        :mock-type :fun)))
       (var ~generated-fun)))

(defmacro add-meta-mock
  "Macro to set the Meta of the mocked function to a mock"
  [fun-name]
  `(do (alter-meta! (var ~fun-name)
                    (constantly
                      (assoc (meta (var ~fun-name))
                        to-mock ~fun-name)))
       (var ~fun-name)))

(defmacro add-meta-spy
  "Macro to set the Meta of the mocked function to a spy"
  [fun-name]
  `(do (alter-meta! (var ~fun-name)
                    (constantly
                      (assoc (meta (var ~fun-name))
                        to-spy ~fun-name)))
       (var ~fun-name)))




