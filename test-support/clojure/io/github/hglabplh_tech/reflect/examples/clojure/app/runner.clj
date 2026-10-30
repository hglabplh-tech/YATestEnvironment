(ns io.github.hglabplh-tech.reflect.examples.clojure.app.runner
  (:require [clojure.test :refer :all]
            [io.github.hglabplh-tech.reflect.examples.clojure.app.little-answering :as answer] ))

(clojure.core/defn -main [& args]
  (answer/chat-loop)
  )