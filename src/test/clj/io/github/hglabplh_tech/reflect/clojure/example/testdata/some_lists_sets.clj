(ns ie.harald.g.p.it-cons.test.suite.example.testdata.some-lists-sets
  (:require [clojure.test :refer :all]))
(println (my-app * 7 8))
(def funny-list (list 'i-am-a-sym :ups-a-keyword 7 8 9 9.0 (+ 5 6) (list 6 7 8 )
                      ['I 'am 'a 'vector] {:one 1 :two 2 :three 3 :what 'I-am-a-map}
                      (clojure.core/fn [p-one p-two]
                        (+ 9 (* p-one p-two)))))
(def funny-list-types (map type funny-list))
(pprint funny-list)
(pprint funny-list-types)

(clojure.core/defn my-app [fun first-num second-num]
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
