(ns io.github.hglabplh_tech.spy-and-mock.mocking-jay

  (:require [clojure.pprint :refer :all]
            [clojure.string :as str]
            [clojure.walk :refer :all]
            [io.github.hglabplh_tech.spy-and-mock.real-fun-checkers :refer :all])

  )

(clojure.core/defn any-boolean? [value] (boolean? value))
(clojure.core/defn any-byte? [value] (= (type value) java.lang.Byte))
(clojure.core/defn any-char? [value] (char? value))
(clojure.core/defn any-collection? [value] (= (type value) clojure.lang.ISeq))
(clojure.core/defn any-double? [value] (double? value))
(clojure.core/defn any-float? [value] (float? value))
(clojure.core/defn any-int? [value] (int? value))
(clojure.core/defn any-list? [value] (list? value))
(clojure.core/defn any-long? [value] (= (type value) java.lang.Long))
(clojure.core/defn any-map? [value] (map? value))

(clojure.core/defn get-set-of? [pred]
  (clojure.core/fn [value]
    (and [(set? value) (map pred value)])
    ))
((get-set-of? integer?) '(3 4 5 67 8))
(clojure.core/defn any-object? [value] (= (type value) java.lang.Object))
(clojure.core/defn any-set? [value] (set? value))
(clojure.core/defn any-set-of? [value pred] (set?))
(clojure.core/defn any-short? [value] (= (type value) java.lang.Short))
(clojure.core/defn any-string? [value] (string? value))
(clojure.core/defn any-vararg? [value] ())

;; create mapping for functions
;; Meta data
(def type-predicate-map
  {:any-boolean?-key    any-boolean?
   :any-byte?-key       any-byte?
   :any-char?-key       any-char?
   :any-collection?-key any-collection?
   :any-double?-key     any-double?
   :any-float?-key      any-float?
   :any-int?-key        any-int?
   :any-list?-key       any-list?
   :any-long?-key       any-long?
   :any-map?-key        any-map?
   :any-object?-key     any-object?
   :any-set?-key        any-set?
   :any-set-of?-key     any-set-of?
   :any-short?-key      any-short?
   :any-string?-key     any-string?
   :any-vararg?-key     any-vararg?
   })



(clojure.core/defn get-type-pred-by-key [key]
  (get type-predicate-map key))

(defrecord CustomMeta [c-stat-key c-stat-val])
(defrecord FunMetata [function-name
                      return-type
                      argument-types
                      custom-meta])

(defrecord NSFunMeta [ns-name fun-name fun-meta])

;; Flow of control runtime

(defrecord FunCalls [function-name
                     argument-values
                     return-value])

;; Function Mocking
(defrecord ActiveDataCustom [var-names var-types var-optional?])
(defrecord Mocker [function-name])
(defrecord ArgCond [fun params])
(defrecord WhenClauses [ret-val-type-pred param-cond])
(defrecord Rule [function-name
                 when-clauses
                 when-action
                 else-action])



(defmacro make-fn [m]
  `(clojure.core/fn [& args#]
     (eval
       (cons '~m args#))))

(def mock-meta (atom []))

(def ns-f-meta-atom (atom []))
(def mock-control-flow (atom []))

(def rule-vect (atom []))

(def redirected-bindings (atom {}))


;;(call-cond-> i-am-a-fake-fun-store
;                 :when
;                 :any-boolean?-key :<-
;                 [[:any-int?-key :$] [:any-int?-key :$] [:any-int?-key :$]
;                   [:any-set-of?-key :$ integer?]]
;                 [return-val 5]
;                 :else
;                 [do-nothing])
(defn call-cond-> [fun
                   cond?
                   ret-val-predicate
                   delim
                   args-vect
                   when-action-vect
                   else?
                   else-action-vect]
  (let [ret-val-pred# ret-val-predicate
        delim?-ok# (= delim :<-)
        args-list# (map
                     (clojure.core/fn [arg-element]
                       (let [arg-pred (first arg-element)
                             parameters (rest arg-element)]
                         (clojure.core/fn [value]
                           ;; has to be rewritten because also
                           ;; zero parms are possible (should work !!!)
                           (if (not= (count parameters) 0)
                             (if ((= :$ (get parameters 0))
                                  (= (count parameters) 2))
                               (apply arg-pred
                                      (list value
                                            (get parameters 1)))
                               (arg-pred value))
                             (arg-pred))
                           ))) args-vect)
        when-action (ArgCond. (first when-action-vect)
                              (rest when-action-vect))
        else-action (if else?
                      (ArgCond. (first else-action-vect)
                                (rest else-action-vect))
                      nil)]

    (if delim?-ok#
      (swap! rule-vect conj
             (Rule. fun
                    (WhenClauses. ret-val-pred# args-list#)
                    when-action
                    else-action
                    ))
      (throw (java.lang.IllegalArgumentException.
               "macro syntax !!! "
               )))))


(clojure.core/defn return-val [value]
  (println "Mock ret val called")
  value)

(clojure.core/defn throw-ex [excp]
  (throw excp))

(clojure.core/defn do-nothing [dummy]
  'nothing)

(clojure.core/defn find-action-rule-for-fun [func-name]
  (first (filter #(clojure.core/fn
                    [element]
                    (= % (:function-name element))) @rule-vect)))

(clojure.core/defn find-meta-for-fun [func-name]
  (first (filter #(clojure.core/fn
                    [element]
                    (= % (:function-name element)))
                 @mock-meta)))

(clojure.core/defn find-ns-data []
  (first (filter #(clojure.core/fn
                    [element]
                    (= % (:function-name element)))
                 @mock-meta)))

(clojure.core/defn find-mocked-fun [func-name]
  (first (filter (clojure.core/fn
                   [element]
                   (= func-name (:function-name element)))
                 @redirected-bindings)))



(clojure.core/defn collect-meta-active-data [func-name func-meta]
  (if (= (find-meta-for-fun func-name) nil)
    (let [schema-val# (get func-meta
                           :schema)
          schema-val-map# (if (not (seq? schema-val#))
                            (parse-meta-to-map (parse-base-schema
                                                 schema-val#))
                            [])]

      (swap! mock-meta conj (FunMetata. func-name
                                        (get (first schema-val-map#)
                                             :return-type)
                                        (get (get (second schema-val-map#)
                                                  :arg-info-map)
                                             :scheme-types)
                                        (ActiveDataCustom.
                                          (get (get (second schema-val-map#)
                                                    :arg-info-map)
                                               :names-vect)
                                          (get (get (second schema-val-map#)
                                                    :arg-info-map)
                                               :types-vect)
                                          (get (get (second schema-val-map#)
                                                    :arg-info-map)
                                               :optional?-vect))))
      )))



(clojure.core/defn collect-flow-calls [func-name ret-value & args]
  (do
    (swap! mock-control-flow conj (FunCalls. func-name
                                             args
                                             ret-value))
    ))


;; rewrite this fun does not fit to be called from a macro :-(
(clojure.core/defn filter-action [func func-name & args]
  (let [rule (find-action-rule-for-fun func-name)
        fun-meta-rec (find-meta-for-fun func-name)]
    (if (not (nil? rule))
      (let [the-return-type-pred (:ret-val-type-pred
                                   (:when-clauses rule))]
        (loop [the-clauses (:args-list (:when-clauses rule))
               condition (the-return-type-pred (:return-type fun-meta-rec))]
          (if (not (empty? the-clauses))
            (do
              (println (first the-clauses))
              (let [res (and condition (apply (make-fn and)
                                              (map (get-type-pred-by-key
                                                     (first the-clauses))
                                                   args)))]
                (recur (rest the-clauses) res)))
            (if condition
              (let [the-action (:when-action rule)
                    result ((:fun the-action)
                            (:params the-action))]
                (println "when-action called with ret: "
                         result)
                result)
              (if (not (nil? (:else-action rule)))
                (let [the-action (:else-action rule)
                      result (apply (:fun the-action)
                                    (:params the-action))]
                  (println "else-action called with ret: "
                           result)
                  result)
                (do
                  (println "real called")
                  'no-condition-fits
                  ))
              ))
          ))
      (do
        (println "fallback")
        'no-mock-logic
        ))))


(defn store-real-fun [fun-name meta-ns-name meta-fun-name fun-meta-data]
  (swap! redirected-bindings assoc
         (str/join "$" [meta-ns-name meta-fun-name])
         [meta-ns-name meta-fun-name fun-name fun-meta-data])
  )


(defn restore-real-fun
  [sut]
  (let [sut-intern (vals (ns-publics sut))]
    (println sut-intern)
    (doseq [curf sut-intern]
      (when (or (some? (:mock-key (meta curf)))
                (some? (:spy-key (meta curf))))
        (let [fun-name# (get (meta curf) :name)
              the-orig-fun# (get @redirected-bindings fun-name#)]
          (alter-var-root
            curf
            (fn [f]
              (fn [args#]
                (f args#))))

          (swap! redirected-bindings dissoc fun-name#)

          )))))



(defn mock-hook
  "Add some basic instrumentation to each var in a given namespace `sut`.
  A poor man's profiler, this simply prints out the name of each
  fn (var) when run."
  [sut]
  (let [sut-intern (vals (ns-publics sut))]
    (println sut-intern)
    (doseq [curf sut-intern]
      (when (some? (:mock-key (meta curf)))
        (let [fun-name# (get (meta curf) :name)
              fun-ns# (get (meta curf) :ns)
              fun-meta# (meta curf)]
          (store-real-fun curf fun-ns# fun-name# fun-meta#) ; ensure a fn
          (alter-var-root
            curf
            (fn [f]
              (fn [& args#]
                (let [result# (filter-action f fun-name# args#)]
                  (apply collect-flow-calls f result# args#)
                  ))))

          )))))

(defmacro mock
  "mocks 1..n functions take care and only mock if the functions call things which you do not want to have
  in your UNIT - Test AND which are extern to your project otherwise if this is neccessary
  you may have to do a redesign of this part for in a well designed project this should NEVER
  be needed if itfis your own code"
  [& namespaces]
  `(do ~@(map mock-hook namespaces)))

(defn find-in-list [the-list to-find]
  (pprint to-find)
  (loop [the-list-int# the-list]
    ;; (println "the value type: " (type to-find))
    ;;(println "the list element type: " (type (first the-list-int#)))
      (do
        ;;(println "false")
        ;;(pprint (first the-list-int#))
        (if (empty? the-list-int#)
          (boolean nil)
          (if (= (first the-list-int#) to-find)
            (boolean 1)
            (recur (rest the-list-int#))))
        )))


(defn collect-meta-by-ns [the-fun-ns]
  (swap! ns-f-meta-atom conj (name the-fun-ns)
         ))

(defn filter-stack-data [real-ns
                         base-fun
                         the-rest
                         fname-st-element
                         lineno-st-element
                         invocation-type
                         ns-sym-vect
                         fun-meta]
  (let [fun-sym (symbol (str/join "$" [real-ns base-fun]))
        fun-ns (ns-name (get fun-meta :ns))
        fun-name (get fun-meta :name)
        fun-fname (get fun-meta :file)
        fun-line-no (get fun-meta :line)
        fun-column (get fun-meta :column)]

    (if (= base-fun "spy_hook")
      (do
        [:stck-line-result
         fun-ns fun-name fun-fname lineno-st-element
         fun-line-no fun-column
         invocation-type])
      (do
        (if (find-in-list
              @ns-f-meta-atom
              real-ns)
          (do
            (println "The stack entry data")
            (println real-ns base-fun the-rest fname-st-element
                     lineno-st-element invocation-type ns-sym-vect)
            [:stck-line-result
             real-ns base-fun the-rest fname-st-element
             lineno-st-element invocation-type]
            )
          [:no-stck-line-result]
          )
        )
      )
    ))


(defn get-processed-stackdata [stack-trace fun-meta]
  (let [stack-trace-vec (seq stack-trace)
        processed-stack-data (loop [stack-trace-temp# stack-trace-vec
                                    result '()]
                               (if (empty? stack-trace-temp#)
                                 result
                                 (let [value (first stack-trace-temp#)
                                       invocation-type (.getMethodName value)
                                       fname-st-element (.getFileName value)
                                       lineno-st-element (.getLineNumber value)
                                       ns-st-ele-str (.getClassName value)
                                       ns-element-vect (str/split ns-st-ele-str #"\$")
                                       real-ns (first ns-element-vect)
                                       base-fun (second ns-element-vect)
                                       the-rest (str/join "$" (rest (rest ns-element-vect)))
                                       ]
                                   (recur (rest stack-trace-temp#)
                                          (conj result (filter-stack-data
                                                         real-ns
                                                         base-fun
                                                         the-rest
                                                         fname-st-element
                                                         lineno-st-element
                                                         invocation-type
                                                         ns-element-vect
                                                         fun-meta)))
                                   )
                                 ))]
    (pprint processed-stack-data)
    processed-stack-data))

;;; The spy functionality
(defn get-it-spyed [fun fun-meta stack-trace-data]
  (let [fun# `~fun
        stack-trace-data# (get-processed-stackdata stack-trace-data fun-meta)
        ]
    (let [fun-meta# fun-meta
          struct-meta (structure-schema fun-meta#)]
      (println "I am a spy and I see you: " `~fun#)
      (pprint struct-meta)
      struct-meta
      )
    ))

(clojure.core/defn fun-spy-call
  [fun fun-meta stack-trace-data]
  (let [fun# fun
        stack-trace-data# stack-trace-data]
    ;;(apply collect-meta-active-data fun-name# args#)
    (get-it-spyed fun# fun-meta stack-trace-data#)
    'spyed))

(defn spy-hook
  "This hook adds spy functionality to get data for profiling and
  test data generation and more"
  [sut]
  (let [sut-intern (vals (ns-publics sut))]
    (println sut-intern)
    (doseq [curf sut-intern]
      (when (some? (:spy-key (meta curf)))
        (let [fun-name# (get (meta curf) :name)
              fun-ns# (get (meta curf) :ns)
              fun-meta# (meta curf)]
          (do (store-real-fun curf fun-ns# fun-name# fun-meta#) ; ensure a fn
              (alter-var-root
                curf
                (fn [f]
                  (fn [& args#]
                    (let [throwable (java.lang.Throwable. "dummy")
                          stack-trc-data (.getStackTrace throwable)]
                      (fun-spy-call f fun-meta# stack-trc-data)
                      (let [result# (apply f args#)]
                        (apply collect-flow-calls f result# args#)
                        result#)))
                  )))

          )))))

(defmacro spy
  [& name-spaces]
  `(do ~@(map spy-hook name-spaces)))

(defn the-prolog-1
  [var-name]
  `(let [the-meta# (meta (var ~var-name))]
     (constantly
       (let [the-fun-name# (get the-meta# :name)
             the-fun-ns# (ns-name (get the-meta# :ns))]
         (collect-meta-by-ns the-fun-ns#)
         (collect-meta-active-data
           the-fun-name# the-meta#
           )))))

(defmacro prolog
  [key & names]
  (case key
    to-spy
    `(do
       ~@(map (make-fn add-meta-spy) names)
       ~@(map the-prolog-1 names))
    to-mock
    `(do
       ~@(map (make-fn add-meta-mock) names)
       ~@(map the-prolog-1 names))))

(defmacro restore-orig-funs
  "restore the functions to original references"
  [& names]
  `(do
     ~@(map restore-real-fun names)))

