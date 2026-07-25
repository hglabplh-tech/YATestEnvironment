(ns io.github.hglabplh_tech.test.suite.datagen.artifact.storage.protocol)

(defprotocol Storage
  (store! [this artifact])
  (summary [this])
  (close! [this]))
