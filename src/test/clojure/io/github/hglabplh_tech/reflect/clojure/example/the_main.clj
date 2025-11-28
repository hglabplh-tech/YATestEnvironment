(ns io.github.hglabplh_tech.reflect.clojure.example.the-main
  (:refer-clojure :exclude [defn fn])
  (:require [clojure.pprint :refer :all]
            [active.data.realm :as realm]
            [active.data.realm.attach :refer :all]
            [schema.spec.core :refer :all]
            [active.data.realm.internal.record-meta :as act-meta]
            [hgp.cljito.example.the-data :as data]
            [io.github.hglabplh_tech.reflect.clojure.example.the-funs-ns :as fun]))

(defn add-some-user-entries :- realm/any []
      (let [user-id (random-uuid)

            ]

        (fun/add-user
          user-id
          "male"
          "Glab-Plhak"
          "Harry"
          "Lackagh Annyalla"
          "Castleblayney"
          "Monagahn")
        (throw (java.io.IOException. "Hello I am cool"))
        (fun/add-bill user-id 8757
                  (set (list "dishwasher" "pump-gun" "plate"))
                  678.96)))



(clojure.core/defn -main []
  (println "I am main")
  (add-some-user-entries)
  )

