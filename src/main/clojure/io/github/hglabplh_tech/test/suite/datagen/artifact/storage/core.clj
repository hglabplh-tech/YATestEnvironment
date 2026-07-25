(ns io.github.hglabplh_tech.test.suite.datagen.artifact.storage.core
  (:require [io.github.hglabplh_tech.test.suite.datagen.artifact.storage.file :as file]
            [io.github.hglabplh_tech.test.suite.datagen.artifact.storage.memory :as memory]
            [io.github.hglabplh_tech.test.suite.datagen.artifact.storage.postgresql :as postgresql]))

(defn create-storage [type config]
  (case type
    :file (file/create-storage config)
    :memory (memory/create-storage config)
    :postgresql (postgresql/create-storage config)
    (throw (ex-info "Unsupported artifact storage."
                    {:storage type
                     :supported #{:file :memory :postgresql}}))))
