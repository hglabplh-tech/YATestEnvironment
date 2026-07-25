(ns io.github.hglabplh_tech.test.suite.datagen.artifact.storage.file
  (:require [clojure.java.io :as io]
            [io.github.hglabplh_tech.test.suite.datagen.artifact.domain :as domain]
            [io.github.hglabplh_tech.test.suite.datagen.artifact.storage.protocol :as storage]))

(defn- target-file [base-dir artifact]
  (apply io/file base-dir
         (concat (domain/hierarchy-segments (:hierarchy artifact))
                 [(:filename artifact)])))

(defrecord FileStorage [base-dir stored]
  storage/Storage
  (store! [_ artifact]
    (let [file (target-file base-dir artifact)]
      (io/make-parents file)
      (if-let [payload (:bytes artifact)]
        (with-open [out (io/output-stream file)]
          (.write out ^bytes payload))
        (spit file (pr-str (:row artifact)) :encoding "UTF-8"))
      (swap! stored conj (.getPath file))
      {:type :file
       :path (.getPath file)
       :content-type (:content-type artifact)}))

  (summary [_]
    {:type :file
     :base-dir base-dir
     :count (count @stored)
     :paths @stored})

  (close! [_]
    true))

(defn create-storage [{:keys [base-dir]}]
  (->FileStorage (or base-dir "target/generated-test-artifacts") (atom [])))
