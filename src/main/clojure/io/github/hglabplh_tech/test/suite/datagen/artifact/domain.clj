(ns io.github.hglabplh_tech.test.suite.datagen.artifact.domain
  (:require [clojure.string :as str])
  (:import (java.time Instant)
           (java.util UUID)))

(def extensions
  {:pdf "pdf"
   :relational "edn"
   :txt "txt"
   :docx "docx"
   :jpeg "jpg"
   :tiff "tiff"})

(def content-types
  {:pdf "application/pdf"
   :relational "application/edn"
   :txt "text/plain; charset=utf-8"
   :docx "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
   :jpeg "image/jpeg"
   :tiff "image/tiff"})

(def data-formats (set (keys extensions)))

(defn safe-segment [value]
  (let [segment (-> (or value "unknown")
                    str
                    str/lower-case
                    (str/replace #"[^a-z0-9._-]+" "-")
                    (str/replace #"(^-+|-+$)" ""))]
    (if (str/blank? segment) "unknown" segment)))

(defn hierarchy-segments [hierarchy]
  (let [{project :project
         feature :feature
         subfeature :subfeature
         class-name :class
         method :method} hierarchy]
    (cond
      (and project feature subfeature)
      [(safe-segment project) "features" (safe-segment feature) (safe-segment subfeature)]

      (and project class-name method)
      [(safe-segment project) "classes" (safe-segment class-name) (safe-segment method)]

      :else
      (throw (ex-info "Artifact hierarchy needs either project/feature/subfeature or project/class/method."
                      {:hierarchy hierarchy})))))

(defn artifact-name [artifact-format sequence-no]
  (clojure.core/format "sample-%04d.%s"
                       (long sequence-no)
                       (get extensions artifact-format "dat")))

(defn base-artifact [job sequence-no]
  (let [{:keys [format storage hierarchy metadata]} job]
    {:id (str (UUID/randomUUID))
     :created-at (str (Instant/now))
     :format format
     :storage storage
     :hierarchy hierarchy
     :metadata (or metadata {})
     :sequence-no sequence-no
     :filename (artifact-name format sequence-no)
     :content-type (get content-types format "application/octet-stream")}))
