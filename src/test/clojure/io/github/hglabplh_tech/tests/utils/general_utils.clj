;; (c) Harald Glab-Plhak, 2026
(ns io.github.hglabplh-tech.tests.utils.general-utils
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [clojure.string :as str]
            [schema.core :as s]))

(declare get-stacktrace-data)
(declare test-exception)

(defn test-exception
  "build the test exception"
  {:added "1.4.0"
   :static true}
  [excp]
  (ex-info  "exception information map: " (conj {:ex-msg (.getMessage excp)}
                 (get-stacktrace-data excp)) excp))

(defn get-stacktrace-data
  "get the stack trace data"
  {:added "1.4.0"
   :static true}
  [excp]
  (let [stack-trace (.getStackTrace excp)
        first-element (aget stack-trace 0)]
    {:class-name (.getClassName first-element)
     :file-name (.getFileName first-element)
     :method-name (.getMethodName first-element)
     :line-number (.getLineNumber first-element)
     }
    ))


(defn call-with-schema-check
  "call with schema check true the function parameters and
  return values are checked by active-data/schema lib"
  {:added "1.4.0"
   :static true}
  [fun & params]
  (try
  (s/set-fn-validation! (boolean 1))
  (let [result (apply fun params)]
    result)
  (catch Throwable excp
    (let [excp-to-throw  (test-exception excp)]
    (pprint excp-to-throw)
    (throw excp-to-throw)))
  (finally s/set-fn-validation! (boolean 0)))
  )



(defn pprint-ex-info
  "Formats and prints a clojure.lang.ExceptionInfo (ex-info) object
   into a human-readable, structured layout."
  {:added "1.4.0"
   :static true}
  [^Throwable ex]
  (let [msg        (.getMessage ex)
        data       (ex-data ex)
        cause      (.getCause ex)
        stack      (.getStackTrace ex)
        top-frame  (when (pos? (count stack)) (aget stack 0))
        class-name (.getName (class ex))]

    (println (str/join "" (repeat 60 "=")))
    (println "🔥 EXCEPTION TYPE :" class-name)
    (println "📝 MESSAGE        :" msg)

    (when top-frame
      (println "📍 LOCATION       :"
               (format "%s.%s (%s:%d)"
                       (.getClassName top-frame)
                       (.getMethodName top-frame)
                       (.getFileName top-frame)
                       (.getLineNumber top-frame))))

    (when data
      (println "📊 EX-DATA MAP    :")
      ;; Pretty-prints the custom map data structurally
      (binding [*print-right-margin* 80]
        (pprint data)))

    (when cause
      (println "🔗 CAUSED BY      :" (.getName (class cause)) "-" (.getMessage cause)))

    (println (str/join "" (repeat 60 "=")))))


;;(call-with-schema-check (fn [x y z] (println  (+ x y z))) 7 81 9)
