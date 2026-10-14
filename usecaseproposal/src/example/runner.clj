(ns example.runner
  (:require [clojure.test :as test]
            [example.test-generator :as generator]))

(defn -main [& [config-file]]
  (let [result (generator/generate-tests!
                 (generator/reflected-interface)
                 (generator/read-definitions (or config-file "user-definitions.json"))
                 "generated/example/generated_test.clj")]
    (load-file (:test-file result))
    (println "Generated:" (:test-file result))
    (let [summary (test/run-tests 'example.generated-test)]
      (println "Spy output: output/<use-case-id>-spy.json")
      (shutdown-agents)
      (when (pos? (+ (:fail summary) (:error summary))) (System/exit 1)))))
