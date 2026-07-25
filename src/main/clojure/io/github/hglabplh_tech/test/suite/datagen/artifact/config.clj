(ns io.github.hglabplh_tech.test.suite.datagen.artifact.config
  (:require [clojure.edn :as edn]
            [io.github.hglabplh_tech.test.suite.datagen.artifact.domain :as domain]))

(def storage-types #{:file :memory :postgresql})

(def default-config
  {:defaults {:locale "de-DE"
              :seed 42}
   :storage {:type :file
             :config {:base-dir "target/generated-test-artifacts"}}
   :jobs []})

(defn- ensure-known! [label allowed value context]
  (when-not (contains? allowed value)
    (throw (ex-info (str "Unsupported artifact " label ".")
                    (assoc context
                           :value value
                           :supported allowed)))))

(defn- validate-job! [idx global-storage job]
  (let [format (:format job)
        storage (or (:storage job) (:type global-storage))]
    (ensure-known! "format" domain/data-formats format {:job-index idx})
    (ensure-known! "storage" storage-types storage {:job-index idx})
    (domain/hierarchy-segments (:hierarchy job))
    true))

(defn validate-config! [config]
  (let [cfg (merge default-config config)]
    (ensure-known! "storage" storage-types (get-in cfg [:storage :type]) {:storage (:storage cfg)})
    (doseq [[idx job] (map-indexed vector (:jobs cfg))]
      (validate-job! idx (:storage cfg) job))
    cfg))

(defn read-config [path]
  (validate-config! (edn/read-string (slurp path))))
