(ns io.github.hglabplh-tech.test.suite.generator.test-def-reader
  (:require [clojure.edn :as edn]))


(clojure.core/defn read-use-case-def [file-path]
  (let [edn-content (edn/read (.getResourceAsStream file-path))]

    )
  )