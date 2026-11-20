(ns io.github.hglabplh-tech.reflect.clojure.schema-spec.parse-specs-test
  (:require [clojure.pprint :refer :all]
            [clojure.test :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.schema-spec.parse-specs :as pspecs]))

(defn very-simple [n m]
  (* n m))

(defn fact
  ([n] (fact n 1))
  ([n acc]
   (if (zero? n)
     acc
     (recur (dec n) (*' acc n)))))

(defn fact-extended
  ([n] (fact-extended n 1 {:sense true :no-sense false :question :what}))
  ([n acc {:keys [sense no-sense question]
           :or   {sense true no-sense false question :what}}]
   (if (zero? n)
     acc
     (recur (dec n) (*' acc n) {sense true}))))

(deftest parse-multiple-arity-fun (testing "functions with multiple arity definitions mata-data parsing"
                                    (let [meta-vsimple (meta #'very-simple)
                                          meta-data-fact (meta #'fact)
                                          meta-data-fact-ext (meta #'fact-extended)
                                          param-def-vsimple (pspecs/reflect-mixed-params meta-vsimple)
                                          param-def-fact (pspecs/reflect-mixed-params meta-data-fact)
                                          param-def-fact-ext (pspecs/reflect-mixed-params meta-data-fact-ext)]
                                      (pprint meta-vsimple)
                                      (pprint meta-data-fact)
                                      (pprint meta-data-fact-ext)
                                      (pprint param-def-vsimple)
                                      (pprint param-def-fact)
                                      (pprint param-def-fact-ext)
                                      )))

(deftest test-parm-arg-coll-build (testing "test param arguments as collection build"
           (let [meta-vsimple (meta #'very-simple)
                 meta-data-fact (meta #'fact)
                 meta-data-fact-ext (meta #'fact-extended)
                 param-def-vsimple (pspecs/reflect-mixed-params meta-vsimple)
                 param-def-fact (pspecs/reflect-mixed-params meta-data-fact)
                 param-def-fact-ext (pspecs/reflect-mixed-params meta-data-fact-ext)
                 parm-arg-vs (pspecs/format-args param-def-vsimple [6 7] )
                 parm-arg-fact (pspecs/format-args param-def-fact [16])
                 parm-arg-fact-ext (pspecs/format-args param-def-fact-ext [14 1 :sense true])]
           (pprint parm-arg-vs)
           (pprint parm-arg-fact)
           (pprint parm-arg-fact-ext)

           )))
