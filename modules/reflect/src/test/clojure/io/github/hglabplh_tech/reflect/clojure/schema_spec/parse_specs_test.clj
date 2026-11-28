(ns io.github.hglabplh-tech.reflect.clojure.schema-spec.parse-specs-test
  (:require [clojure.pprint :refer :all]
            [clojure.test :refer :all]
            [io.github.hglabplh-tech.tests.utils.general-utils :as utils]
            [clojure.spec.alpha :as s]
            [io.github.hglabplh-tech.reflect.clojure.fun-reader :as fr]
            [io.github.hglabplh-tech.reflect.examples.clojure.complex.mixed-defs :refer :all]
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
(s/def ::recipient string?)
(s/def ::body string?)
(s/def ::greeting string?)
(s/def ::punctuation string?)
(s/def ::uppercase? boolean?)

(s/fdef format-message
        :args (s/cat :recipient string?
                     :body      string?
                     :opts      (s/keys :req-un [::greeting ] :opt-un [::punctuation ::uppercase?]))
        :ret  string?
        :fn   (fn [{:keys [args ret]}]
                ;; Optional behavioral invariant: if :uppercase? was true, output must be uppercase
                (if (get-in args [:opts :uppercase?])
                  (= ret (.toUpperCase ^String ret))
                  true)))

(clojure.core/defn format-message
  "Formats a message with positional arguments and optional named parameters."
  [recipient body & {:keys [greeting punctuation uppercase?]
                     :or   {greeting "Hello"
                            punctuation "!"
                            uppercase? false}}]
  (let [msg (str greeting ", " recipient ": " body punctuation)]
    (if uppercase?
      (.toUpperCase msg)
      msg)))

(clojure.core/defn format-message-typed
  "Formats a message with positional arguments and optional named parameters."
  [^String recipient ^String body & {:keys [^String greeting ^String punctuation ^Boolean uppercase?]
                                     :or   {greeting "Hello"
                                            punctuation "!"
                                            uppercase? false}}]
  (let [msg (str greeting ", " recipient ": " body punctuation)]
    (if uppercase?
      (.toUpperCase msg)
      msg)))

(deftest parse-multiple-arity-fun (testing "functions with multiple arity definitions mata-data parsing"
                                    (let [meta-vsimple (meta #'very-simple)
                                          meta-data-fact (meta #'fact)
                                          meta-data-fact-ext (meta #'fact-extended)
                                          meta-data-format (meta #'format-message)
                                          meta-data-format-typed (meta #'format-message-typed)
                                          param-def-vsimple (fr/reflect-mixed-params meta-vsimple)
                                          param-def-fact (fr/reflect-mixed-params meta-data-fact)
                                          param-def-fact-ext (fr/reflect-mixed-params meta-data-fact-ext)
                                          param-def-format (fr/reflect-mixed-params meta-data-format)
                                          param-def-format-typed (fr/reflect-mixed-params meta-data-format-typed)]
                                      (utils/print-all-meta meta-vsimple)
                                      (utils/print-all-meta meta-data-fact)
                                      (utils/print-all-meta meta-data-fact-ext)
                                      (utils/print-all-meta meta-data-format)
                                      (utils/print-all-meta meta-data-format-typed)
                                      (pprint (fr/get-structured-meta-data meta-data-format))
                                      (pprint param-def-vsimple)
                                      (pprint param-def-fact)
                                      (pprint param-def-fact-ext)
                                      (pprint param-def-format)
                                      (pprint param-def-format-typed)
                                      )))

(deftest test-parm-arg-coll-build (testing "test param arguments as collection build"
           (let [meta-vsimple (meta #'very-simple)
                 meta-data-fact (meta #'fact)
                 meta-data-fact-ext (meta #'fact-extended)
                 param-def-vsimple (fr/reflect-mixed-params meta-vsimple)
                 param-def-fact (fr/reflect-mixed-params meta-data-fact)
                 param-def-fact-ext (fr/reflect-mixed-params meta-data-fact-ext)
                 parm-arg-vs (pspecs/format-args param-def-vsimple [6 7] )
                 parm-arg-fact (pspecs/format-args param-def-fact [16])
                 parm-arg-fact-ext (pspecs/format-args param-def-fact-ext [14 1 :sense true])]
           (pprint parm-arg-vs)
           (pprint parm-arg-fact)
           (pprint parm-arg-fact-ext)

           )))