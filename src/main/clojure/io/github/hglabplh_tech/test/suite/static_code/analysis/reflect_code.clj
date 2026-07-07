(ns io.github.hglabplh_tech.test.suite.static-code.analysis.reflect-code
  (:refer-clojure :exclude [defn fn])
  (:require [clojure.walk :refer :all]
            [clojure.reflect :as refl]
            [clojure.pprint :refer :all]
            [active.data.record :as ad-rec]
            [active.data.realm :as realm]
            [active.data.realm.attach :refer :all]
            [active.data.realm.internal.record-meta :as act-meta]
            [schema.spec.core :refer :all]
            [schema.core :as schema]))


(schema/set-fn-validation! (boolean 1))

(ad-rec/def-record ns-reflect [imports :- (realm/map-of realm/symbol realm/any)
                               interns :- (realm/map-of realm/symbol realm/any)
                               refers :- (realm/map-of realm/symbol realm/any)
                               publics :- (realm/map-of realm/symbol realm/any)
                               aliases :- (realm/map-of realm/symbol realm/any)])

(def registered-ns (atom '()))

(clojure.core/defn register-namespaces
      [ns-list]
      (loop [ns-list-temp ns-list]
        (swap! registered-ns conj!  (first ns-list-temp))
        (if (empty? ns-list-temp)
        'done
        (recur (rest ns-list-temp))))
      )


(clojure.core/defn collect-ns-funs [ns-sym]
      (let [imp (clojure.core/ns-imports ns-sym)
            intern (ns-interns ns-sym)
            refs (ns-refers ns-sym)
            pubs (ns-publics ns-sym)
            alias (ns-aliases ns-sym)]
        (ns-reflect imports imp interns intern refers refs publics pubs
                    aliases alias)
        ))

(clojure.core/defn find-funs [in]
      (loop [pubs (publics in)
             funs '()]
        (if (not (nil? (get (meta ~(first pubs)) :arglists)))
          (recur (next pubs) (conj! funs (first pubs)))
          (recur (next pubs) funs))

          ))


