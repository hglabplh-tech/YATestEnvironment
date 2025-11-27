(ns ie.harald.g.p.it-cons.test.suite.datagen.analyze.fun-analyzer
  (:require [active.data.realm :as realm]
            [clojure.pprint :refer :all]
            [active.data.raw-record :as raw]
            [schema.core :as sc]
            [active.data.record :as sut]))

(sut/def-record fun-def [return-type :- realm/any
                         the-pos-parameters :- (realm/map-of realm/string realm/any)
                         ])
(defn analyze-fun
  "This is the main entry point to analyze functions"
  [processed-meta]
  (let [recording-part (get processed-meta :recording)
        addon-schema-data (get recording-part :add-on)
        schema-data (first  (get  (first addon-schema-data) :schema))
        return-type (get schema-data :return-type)
        arg-info-map (get schema-data :arg-info-map)
        names-vect (get arg-info-map :names-vect)
        types-vect (get arg-info-map :scheme-types)

        ]

  (pprint processed-meta)
  (println "---------------------------")
  (pprint schema-data)
  (println "---------------------------")
  (println "===============================================================================")
  (println "Type:" (type return-type))
  (pprint return-type)
  (println "===============================================================================")
  (println "Type:" (type arg-info-map))
  (pprint arg-info-map)
  (println "===============================================================================")
  (pprint names-vect)
  (pprint types-vect)
  processed-meta))
