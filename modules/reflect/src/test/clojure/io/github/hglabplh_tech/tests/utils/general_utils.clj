(ns io.github.hglabplh-tech.tests.utils.general-utils
  (:require [clojure.test :refer :all]))

(clojure.core/defn print-all-meta [meta-data]
  (let [base-meta {:base-meta [meta-data]}
        arglists-ext (vec (map meta (first (:arglists meta-data))))
        arglists-ext-map {:arg-list-meta-ext arglists-ext}
        complete-meta (conj base-meta arglists-ext-map)]
    (binding [*print-meta* true]
      (clojure.pprint/pprint complete-meta))
    ))
