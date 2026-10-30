(ns io.github.hglabplh_tech.test.suite.datagen.artifact.player-test
  (:require [clojure.test :refer :all]
            [io.github.hglabplh_tech.test.suite.datagen.artifact.generator :as generator]
            [io.github.hglabplh_tech.test.suite.datagen.artifact.player :as player]))

(def sample-hierarchy
  {:project "Atlas"
   :feature "Document Import"
   :subfeature "PDF Validation"})

(defn- starts-with-bytes? [^bytes payload prefix]
  (let [prefix-bytes (.getBytes prefix "UTF-8")]
    (and (<= (alength prefix-bytes) (alength payload))
         (every? true?
                 (map-indexed
                  (fn [idx value]
                    (= value (aget payload idx)))
                  prefix-bytes)))))

(deftest player-generates-memory-artifacts
  (let [result (player/run! {:defaults {:seed 17}
                             :storage {:type :memory}
                             :jobs [{:format :txt
                                     :count 2
                                     :hierarchy sample-hierarchy}
                                    {:format :relational
                                     :count 1
                                     :hierarchy {:project "Customer Service"
                                                 :class "CustomerRepository"
                                                 :method "findActiveCustomers"}}]})]
    (is (= 3 (:count result)))
    (is (= [:txt :txt :relational] (mapv :format (:generated result))))
    (is (= 1 (count (:storage-summaries result))))
    (is (= 3 (:count (first (:storage-summaries result)))))))

(deftest generator-produces-portable-document-formats
  (let [job {:format :pdf
             :storage :memory
             :hierarchy sample-hierarchy}
        pdf (generator/generate-artifact job 1 42)
        docx (generator/generate-artifact (assoc job :format :docx) 1 42)
        txt (generator/generate-artifact (assoc job :format :txt) 1 42)]
    (is (starts-with-bytes? (:bytes pdf) "%PDF-1.4"))
    (is (starts-with-bytes? (:bytes docx) "PK"))
    (is (re-find #"Synthetic test data" (String. ^bytes (:bytes txt) "UTF-8")))))
