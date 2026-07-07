(ns io.github.hglabplh_tech.test.suite.datagen.sqlite
  (:require [clojure.java.io :as io])
  (:import (java.sql DriverManager PreparedStatement Statement)))

(def schema-statements
  ["create table if not exists analyzed_functions (
      id integer primary key autoincrement,
      namespace text not null,
      name text not null,
      file text,
      line integer,
      return_type text,
      analysis text not null,
      created_at text default current_timestamp
    )"
   "create table if not exists analyzed_parameters (
      id integer primary key autoincrement,
      function_id integer not null,
      position integer not null,
      name text not null,
      type text,
      generated_values text,
      foreign key(function_id) references analyzed_functions(id)
    )"
   "create table if not exists generated_test_data (
      id integer primary key autoincrement,
      function_id integer not null,
      case_index integer not null,
      format text not null,
      payload text not null,
      foreign key(function_id) references analyzed_functions(id)
    )"])

(defn connection [db-path]
  (Class/forName "org.sqlite.JDBC")
  (io/make-parents db-path)
  (DriverManager/getConnection (str "jdbc:sqlite:" db-path)))

(defn init-db! [db-path]
  (with-open [conn (connection db-path)
              stmt (.createStatement conn)]
    (doseq [sql schema-statements]
      (.execute stmt sql)))
  db-path)

(defn- set-params! [^PreparedStatement stmt values]
  (doseq [[idx value] (map-indexed vector values)]
    (.setObject stmt (inc idx) value))
  stmt)

(defn execute-insert! [conn sql values]
  (with-open [stmt (.prepareStatement conn sql Statement/RETURN_GENERATED_KEYS)]
    (set-params! stmt values)
    (.executeUpdate stmt)
    (with-open [keys (.getGeneratedKeys stmt)]
      (when (.next keys)
        (.getLong keys 1)))))

(defn store-analysis! [db-path analyses]
  (init-db! db-path)
  (with-open [conn (connection db-path)]
    (doseq [analysis analyses]
      (let [function-id (execute-insert!
                         conn
                         "insert into analyzed_functions(namespace, name, file, line, return_type, analysis)
                          values (?, ?, ?, ?, ?, ?)"
                         [(:namespace analysis)
                          (:name analysis)
                          (:file analysis)
                          (:line analysis)
                          (:return-type analysis)
                          (pr-str analysis)])]
        (doseq [param (:parameters analysis)]
          (execute-insert!
           conn
           "insert into analyzed_parameters(function_id, position, name, type, generated_values)
            values (?, ?, ?, ?, ?)"
           [function-id
            (:position param)
            (:name param)
            (:type param)
            (pr-str (:values param))]))
        (doseq [case (:cases analysis)
                [format payload] (:payloads case)]
          (execute-insert!
           conn
           "insert into generated_test_data(function_id, case_index, format, payload)
            values (?, ?, ?, ?)"
           [function-id (:case-index case) (name format) payload])))))
  db-path)
