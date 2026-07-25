(ns io.github.hglabplh_tech.test.suite.datagen.artifact.storage.memory
  (:require [io.github.hglabplh_tech.test.suite.datagen.artifact.storage.protocol :as storage]))

(defrecord MemoryStorage [state]
  storage/Storage
  (store! [_ artifact]
    (swap! state assoc (:id artifact) artifact)
    {:type :memory
     :id (:id artifact)})

  (summary [_]
    {:type :memory
     :count (count @state)
     :ids (vec (keys @state))})

  (close! [_]
    true))

(defn create-storage
  ([] (create-storage {}))
  ([_config]
   (->MemoryStorage (atom {}))))
