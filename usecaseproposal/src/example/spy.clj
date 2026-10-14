(ns example.spy
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]))

;; An executable adapter for the proposed mocking-jay output contract.
;; It does not load or modify the external project's current spy implementation.
(def ^:dynamic *step-id* nil)
(def ^:dynamic *argument-sources* nil)

(defn collector [config use-case]
  (atom {:configVersion (:configVersion config)
         :language (:language config)
         :useCases [(assoc use-case :funcalls [])]}))

(defn- record! [observations function-id args outcome]
  (swap! observations update-in [:useCases 0 :funcalls]
         (fn [calls]
           (conj calls
                 (merge {:id *step-id*
                         :call-id (inc (count calls))
                         :function-name function-id
                         :argument-sources *argument-sources*
                         :arguments (vec args)}
                        outcome)))))

(defn bindings-for [observations vars]
  ;; Capture actual function values before installing any wrappers.
  (into {}
        (for [v vars
              :let [original @v
                    metadata (meta v)
                    function-id (str (ns-name (:ns metadata)) "/" (:name metadata))]]
          [v (fn [& args]
               (let [result (try
                              (apply original args)
                              (catch Throwable error
                                (record! observations function-id args
                                         {:outcome "threw"
                                          :exception {:type (.getName (class error))
                                                      :message (.getMessage error)}})
                                (throw error)))]
                 (record! observations function-id args
                          {:outcome "returned" :return result})
                 result))])))

(defn write-output! [filename observations]
  (io/make-parents filename)
  (spit filename (json/write-str @observations :indent true)))
