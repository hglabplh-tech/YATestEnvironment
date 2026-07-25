(ns io.github.hglabplh_tech.test.suite.datagen.artifact.storage.postgresql
  (:require [io.github.hglabplh_tech.test.suite.datagen.artifact.storage.protocol :as storage])
  (:import (java.sql DriverManager Timestamp)
           (java.time Instant)))

(def default-config
  {:jdbc-url "jdbc:postgresql://localhost:5432/testdatagen"
   :user "testdatagen"
   :password "testdatagen"
   :schema "testdata"})

(defn- safe-identifier [value]
  (let [identifier (or value "testdata")]
    (when-not (re-matches #"[A-Za-z_][A-Za-z0-9_]*" identifier)
      (throw (ex-info "Unsafe PostgreSQL identifier in artifact storage config."
                      {:identifier identifier})))
    identifier))

(defn- execute! [conn sql]
  (with-open [statement (.createStatement conn)]
    (.execute statement sql)))

(defn- ensure-schema! [conn schema]
  (execute! conn (str "CREATE SCHEMA IF NOT EXISTS " schema))
  (execute! conn
            (str "CREATE TABLE IF NOT EXISTS " schema ".generated_artifacts ("
                 "id TEXT PRIMARY KEY,"
                 "format TEXT NOT NULL,"
                 "content_type TEXT NOT NULL,"
                 "filename TEXT NOT NULL,"
                 "hierarchy TEXT NOT NULL,"
                 "metadata TEXT NOT NULL,"
                 "payload BYTEA,"
                 "row_data TEXT,"
                 "sequence_no BIGINT NOT NULL,"
                 "created_at TIMESTAMPTZ NOT NULL"
                 ")")))

(defn- instant-timestamp [value]
  (Timestamp/from (Instant/parse value)))

(defrecord PostgreSQLStorage [conn schema stored]
  storage/Storage
  (store! [_ artifact]
    (with-open [statement (.prepareStatement conn
                                             (str "INSERT INTO " schema ".generated_artifacts "
                                                  "(id, format, content_type, filename, hierarchy, metadata, payload, row_data, sequence_no, created_at) "
                                                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                                                  "ON CONFLICT (id) DO UPDATE SET "
                                                  "format = EXCLUDED.format, "
                                                  "content_type = EXCLUDED.content_type, "
                                                  "filename = EXCLUDED.filename, "
                                                  "hierarchy = EXCLUDED.hierarchy, "
                                                  "metadata = EXCLUDED.metadata, "
                                                  "payload = EXCLUDED.payload, "
                                                  "row_data = EXCLUDED.row_data, "
                                                  "sequence_no = EXCLUDED.sequence_no, "
                                                  "created_at = EXCLUDED.created_at"))]
      (.setString statement 1 (:id artifact))
      (.setString statement 2 (name (:format artifact)))
      (.setString statement 3 (:content-type artifact))
      (.setString statement 4 (:filename artifact))
      (.setString statement 5 (pr-str (:hierarchy artifact)))
      (.setString statement 6 (pr-str (:metadata artifact)))
      (if-let [payload (:bytes artifact)]
        (.setBytes statement 7 payload)
        (.setObject statement 7 nil))
      (if-let [row (:row artifact)]
        (.setString statement 8 (pr-str row))
        (.setObject statement 8 nil))
      (.setLong statement 9 (long (:sequence-no artifact)))
      (.setTimestamp statement 10 (instant-timestamp (:created-at artifact)))
      (.executeUpdate statement))
    (swap! stored conj (:id artifact))
    {:type :postgresql
     :schema schema
     :id (:id artifact)})

  (summary [_]
    {:type :postgresql
     :schema schema
     :count (count @stored)
     :ids @stored})

  (close! [_]
    (.close conn)
    true))

(defn create-storage [config]
  (let [{:keys [jdbc-url user password schema]} (merge default-config config)
        schema-name (safe-identifier schema)]
    (try
      (Class/forName "org.postgresql.Driver")
      (catch ClassNotFoundException ex
        (throw (ex-info "PostgreSQL artifact storage needs the org.postgresql.Driver JDBC dependency on the classpath."
                        {:dependency '[org.postgresql/postgresql "42.x"]
                         :storage :postgresql}
                        ex))))
    (let [conn (DriverManager/getConnection jdbc-url user password)]
      (ensure-schema! conn schema-name)
      (->PostgreSQLStorage conn schema-name (atom [])))))
