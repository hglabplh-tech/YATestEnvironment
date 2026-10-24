;; Copyright (c) 2026 Harald Glab-Plhak

(ns io.github.hglabplh-tech.reflect.general.gen-unified-astlike-code
  (:require [io.github.hglabplh-tech.reflect.clojure.active-data.parse-meta :as pactd]
            [io.github.hglabplh-tech.reflect.general.cloj-to-unified :as cluni]
            [io.github.hglabplh-tech.reflect.general.java-to-unified :as juni]
            [clojure.pprint :refer :all]))



(defn transform-to-unified [reflect-input]
  (let [lang (get reflect-input :language)]
    (cond
      (= lang :clojure)
      (cluni/clojure-transform (get reflect-input :ns-name) (get reflect-input :reflect-data))
      (= lang :java)
      (juni/java-transform (get reflect-input :class-name) (get reflect-input :reflect-data))
      :else
      (println "Not yet here")
      )))

