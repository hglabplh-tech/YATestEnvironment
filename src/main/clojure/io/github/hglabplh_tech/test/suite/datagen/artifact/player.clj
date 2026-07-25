(ns io.github.hglabplh_tech.test.suite.datagen.artifact.player
  (:refer-clojure :exclude [run!])
  (:require [io.github.hglabplh_tech.test.suite.datagen.artifact.config :as config]
            [io.github.hglabplh_tech.test.suite.datagen.artifact.generator :as generator]
            [io.github.hglabplh_tech.test.suite.datagen.artifact.storage.core :as storage-core]
            [io.github.hglabplh_tech.test.suite.datagen.artifact.storage.protocol :as storage]))

(defprotocol Player
  (play! [this generation-config]))

(defn- storage-key [type storage-config]
  [type storage-config])

(defn- storage-for! [storages type storage-config]
  (let [key (storage-key type storage-config)]
    (or (get @storages key)
        (let [created (storage-core/create-storage type storage-config)]
          (swap! storages assoc key created)
          created))))

(defn- generate-job! [storages defaults global-storage job]
  (let [storage-type (or (:storage job) (:type global-storage))
        storage-config (merge (:config global-storage) (:storage-config job))
        storage (storage-for! storages storage-type storage-config)
        count (long (or (:count job) 1))
        seed (long (or (:seed job) (:seed defaults) 42))]
    (for [sequence-no (range 1 (inc count))
          :let [artifact (generator/generate-artifact (assoc job
                                                             :storage storage-type
                                                             :seed seed)
                                                      sequence-no
                                                      seed)
                reference (storage/store! storage artifact)]]
      {:artifact-id (:id artifact)
       :format (:format artifact)
       :storage storage-type
       :sequence-no sequence-no
       :reference reference})))

(defrecord TestDataPlayer []
  Player
  (play! [_ generation-config]
    (let [cfg (config/validate-config! generation-config)
          storages (atom {})]
      (try
        (let [generated (vec (mapcat #(generate-job! storages
                                                     (:defaults cfg)
                                                     (:storage cfg)
                                                     %)
                                     (:jobs cfg)))]
          {:generated generated
           :count (count generated)
           :storage-summaries (mapv storage/summary (vals @storages))})
        (finally
          (doseq [store (vals @storages)]
            (storage/close! store)))))))

(def default-player (->TestDataPlayer))

(defn run! [generation-config]
  (play! default-player generation-config))
