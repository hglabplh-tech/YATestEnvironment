(ns io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering
  (:gen-class)
  (:refer-clojure :exclude [defn fn])
  (:require [active.data.realm :as realm]
            [active.data.realm.attach :refer :all]
            [clojure.pprint :refer :all]
            [schema.core :as s]
            ))
(s/set-fn-validation! (boolean 1))

(defn chat-send :- realm/integer
      [msg :- realm/string
       priority :- (realm/enum :low :middle :high :severe :info)
       user-no :- (realm/integer-from-to 1 1024)
       price :- realm/number]

      )

(defn wait-for-atom-match :- realm/integer [the-atom :- realm/any
                           predicate-fn :- (realm/function realm/any -> realm/integer)
                           ]
      (let [p (promise)
            watch-key (keyword (str (java.util.UUID/randomUUID)))]
        ;; 1. Check if the condition is already met before watching
        (if (predicate-fn @the-atom)
          true
          (do
            ;; 2. Register a watcher to look for the change
            (add-watch the-atom watch-key
                       (clojure.core/fn   [the-key reference old-state new-state]
                           (when (predicate-fn new-state)
                             (remove-watch reference the-key)   ; Clean up watcher
                             (deliver p true))))            ; Unblock deref

            ;; 3. Double-check state to prevent a race condition
            ;; (in case it changed right before add-watch registered)
            (if (predicate-fn @the-atom)
              (do (remove-watch the-atom watch-key) true)
              @p)))))

; This blocks the current thread until deliver is called
(def status (atom :pending))

(defn chat-loop :- (realm/enum :startup :running :pending :waiting :ready)
      []
      ;; Start a background thread to wait for the change
      (future
        (println "Background thread: Waiting for status to become :ready...")
        (wait-for-atom-match status (clojure.core/fn  [state ]
                                        (or (= state :ready) (= state :exit)) ))
        (println "Background thread: Success! Atom is now :ready."))
      ;; Simulate work on the main thread
      (Thread/sleep 2000)
      (println "Main thread: Updating status to :ready now.")
      (reset! status :ready))
