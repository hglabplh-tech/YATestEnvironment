(ns io.github.hglabplh_tech.reflect.examples.testdata.some-lists-sets
  (:refer-clojure :exclude [defn fn def] )
  (:require [clojure.test :refer :all]
            [active.data.realm :as realm]
            [active.data.realm.attach :refer :all]
            [active.data.record :as sut]
            [clojure.pprint :refer :all]
            [schema.core :as s]
            ))

(s/set-fn-validation! (boolean 0))
(println (my-app * 7 8))
(def funny-list (list 'i-am-a-sym :ups-a-keyword 7 8 9 9.0 (+ 5 6) (list 6 7 8 )
                      ['I 'am 'a 'vector] {:one 1 :two 2 :three 3 :what 'I-am-a-map}
                      (clojure.core/fn [p-one p-two]
                        (+ 9 (* p-one p-two)))))
(def funny-list-types (map type funny-list))
(pprint funny-list)
(pprint funny-list-types)

(defn my-app :- realm/number
      [fun :- (realm/function realm/number realm/number -> realm/number)
       first-num :- realm/number
       second-num :- realm/number
       ]
  (loop [first-num# first-num
         second-num# second-num
         result (fun first-num# second-num#)]
    (if (<= first-num# 0)
      (* result result)
      (recur
        (- first-num# 1)
        (* second-num#
           (+ first-num# 5))
        (+ result (fun first-num# second-num#)))
      )
    )
  )
