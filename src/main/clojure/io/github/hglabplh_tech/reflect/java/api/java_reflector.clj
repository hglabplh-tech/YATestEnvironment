;; Copyright (c) 2026 Harald Glab-Plhak

(ns io.github.hglabplh-tech.reflect.java.api.java-reflector
  (:require [io.github.hglabplh-tech.reflect.java.api.reflect-class :as rcl]
            ))

(clojure.core/defn get-structured-meta-data [^String class-name]
(let [class-util (rcl/get-class-util class-name)
      struct-data {:structured-class
                    (rcl/get-all-class-attributes-and-values class-util)
                    }
      ]
  {:language :java :class-name (str class-name) :reflect-data struct-data}

  ))