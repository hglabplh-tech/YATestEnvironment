(ns io.github.hglabplh-tech.reflect.general.compiler
  (:require [clojure.edn :as edn]
            [clojure.pprint :refer :all]
            [clojure.java.io :as io])
  (:import (java.io PushbackReader)))

(defn load-edn-from-classpath [resource-path]
  (let [raw-spec (with-open [r (PushbackReader. (io/reader (io/resource resource-path)))]
                   (edn/read r))
        catalog  (:mapping/catalog raw-spec)
          by-id    (into {} (map (juxt :mapping/id identity) catalog))]
      (assoc raw-spec :compiled/catalog by-id)))


;; Example usage (note: omit "resources/" from the path):
;; (def settings (load-edn-from-classpath "config/settings.edn"))

(defn load-edn-file [file-path]
  (with-open [r (io/reader file-path)]
    (edn/read (java.io.PushbackReader. r))))

;; Example usage:
;; (def config (load-edn-file "resources/config.edn"))


(def compiler-config (load-edn-from-classpath "java-to-unified-form.edn"))
(pprint compiler-config)