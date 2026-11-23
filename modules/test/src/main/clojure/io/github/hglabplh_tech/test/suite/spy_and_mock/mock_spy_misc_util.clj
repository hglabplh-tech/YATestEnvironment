;; (c) Harald Glab-Plhak
(ns io.github.hglabplh_tech.test.suite.spy-and-mock.mock-spy-misc-util
  (:refer-clojure :exclude [defn])
  (:require [clojure.walk :refer :all]
            [clojure.pprint :refer :all]
            [active.data.realm.attach :refer :all]
            [clojure.pprint :refer :all]
            [clojure.test :refer :all]
            [clojure.string :as str]
            [schema.spec.core :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.fun-reader :refer :all]
            [io.github.hglabplh-tech.test.suite.spy-and-mock.stats-util :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.schema-spec.parse-specs :as pspecs]
            )
  (:import (io.github.hglabplh_tech.tests.utils Utilities)))
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
(clojure.core/defn any-object? [value] (= (type value) java.lang.Object))
(clojure.core/defn any-set? [value] (set? value))
(clojure.core/defn any-set-of? [value pred]
  (and (set? value) (every? pred value)))
(clojure.core/defn any-short? [value] (= (type value) java.lang.Short))
(clojure.core/defn any-string? [value] (string? value))
(clojure.core/defn any-vararg? [value] (sequential? value))
(clojure.core/defn set-of? [pred]
  (clojure.core/fn [value]
    (and [(set? value) (map pred value)])
    ))
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
   :set-of?-key         set-of?
   })

;; Function Mocking
(defrecord ActiveDataCustom [var-names var-types var-optional?])
(defrecord Mocker [function-name])
(defrecord ArgCond [fun params])
(defrecord WhenClauses [ret-val-type-pred param-cond])
(defrecord Rule [function-name
                 when-clauses
                 when-action
                 else-action])



(def to-spy :spy-key)
(def to-mock :mock-key)

(def mock-meta (atom {}))

(def ns-f-meta-atom (atom []))

(def redirected-bindings (atom {}))

(def statistic-control-flow (atom {:flow-stats [] :statistics {}}))

(def recur-data-vect (atom []))

(def recur-data-map (atom {}))

(def control-flow-collection (atom {:language :clojure :funcalls []}))

(def rule-vect (atom []))

(def track-exceptions? (atom true))

(clojure.core/defn get-type-pred-by-key [key]
  (get type-predicate-map key))


(clojure.core/defn qualified-fun-name [fun-meta]
  (str (ns-name (:ns fun-meta)) "/" (:name fun-meta)))

(clojure.core/defn qualified-fun-name-with-thread-prefix [fun-meta]
  (str (Utilities/getThreadIdentifierPrefix) (ns-name (:ns fun-meta)) "/" (:name fun-meta)))

(clojure.core/defn find-meta-for-fun [func-name]
  (first (filter (clojure.core/fn [element]
                   (= func-name (:function-name element)))
                 @mock-meta)))

(clojure.core/defn register-rule!
  [fun-var cond? ret-val-predicate delim args-vect when-action-vect else? else-action-vect]
  (when-not (and (= cond? :when) (= delim :<-))
    (throw (java.lang.IllegalArgumentException.
             "Invalid call-cond-> syntax! Expected :when <ret-pred> :<- <args-vect> <when-action> [:else <else-action>]")))
  (let [fun-meta  (meta fun-var)
        qual-sym  (symbol (qualified-fun-name fun-meta))
        args-list (mapv
                    (clojure.core/fn [arg-element]
                      (let [arg-pred-key (first arg-element)
                            arg-pred     (if (keyword? arg-pred-key)
                                           (get-type-pred-by-key arg-pred-key)
                                           arg-pred-key)
                            parameters   (rest arg-element)]
                        (clojure.core/fn [value]
                          (if (and (= :$ (first parameters))
                                   (= (count parameters) 2))
                            (apply arg-pred (list value (second parameters)))
                            (arg-pred value)))))
                    args-vect)
        when-action (ArgCond. (first when-action-vect) (rest when-action-vect))
        else-action (when (= else? :else)
                      (ArgCond. (first else-action-vect) (rest else-action-vect)))]
    (swap! rule-vect conj
           (Rule. qual-sym
                  (WhenClauses. ret-val-predicate args-list)
                  when-action
                  else-action))))


(defmacro call-cond->
  "Registers a mock condition rule for `fun`.
   Accepts either an unquoted symbol (`my-fn`) or a quoted symbol (`'my-fn`)."
  ([fun cond? ret-val-predicate delim args-vect when-action-vect]
   (let [target-sym (if (and (seq? fun) (= 'quote (first fun)))
                      (second fun)
                      fun)]
     `(register-rule! (var ~target-sym) ~cond? ~ret-val-predicate ~delim ~args-vect ~when-action-vect nil nil)))
  ([fun cond? ret-val-predicate delim args-vect when-action-vect else? else-action-vect]
   (let [target-sym (if (and (seq? fun) (= 'quote (first fun)))
                      (second fun)
                      fun)]
     `(register-rule! (var ~target-sym) ~cond? ~ret-val-predicate ~delim ~args-vect ~when-action-vect ~else? ~else-action-vect))))

(clojure.core/defn find-action-rules-for-fun
  "Returns all rules registered for the function described by `fun-meta`."
  [fun-meta]
  (let [unqual-sym (:name fun-meta)
        qual-sym   (symbol (qualified-fun-name fun-meta))]
    (filter (clojure.core/fn [rule]
              (let [target (:function-name rule)]
                (or (= target qual-sym)
                    (= target unqual-sym))))
            @rule-vect)))




(clojure.core/defn get-cached-args-descr
  "Retrieves pre-reflected :args-types-list from @mock-meta, populating @mock-meta if absent."
  [fun-meta]
  (let [q-name (qualified-fun-name fun-meta)
        cached (or (find-meta-for-fun q-name)
                   (let [structured (get-structured-meta-data fun-meta)]
                     (swap! mock-meta assoc q-name structured)
                     structured))]
    (get-in cached [:structured-meta :base-data :args-types-list])))

(clojure.core/defn get-fun-meta [fun-sym]
  (meta (resolve fun-sym)))


(clojure.core/defn get-schema-active-meta

  [all-meta-data gen-meta-data] ;; FIXME metadata have to be overviewed
  (let [active-fun-meta (get all-meta-data fn-realm-meta-key {})
        schema-meta-data (get all-meta-data :schema)
        result-meta (conj {:schema schema-meta-data fn-realm-meta-key active-fun-meta}  all-meta-data)]
    result-meta

    )

  )
;;; implement it with that logic slightly changed
;; from active-data and in active data it works the thing
;; with using constantly seems to me a very good idea

(clojure.core/defn collect-meta-by-ns [the-fun-ns]
      (swap! ns-f-meta-atom conj (name the-fun-ns)
             ))

(defmacro transfer-fun-meta
  "Macro to set the Meta of the mocked function to a mock"
  [generated-fun orig-fun]
  `(do (alter-meta! (var ~generated-fun)
                    (constantly
                      (assoc (get-schema-active-meta (meta (var ~orig-fun))
                                                     (meta (var ~generated-fun)))
                        :mock-type :fun)))
       (var ~generated-fun)))


(defmacro add-meta-stats-conf
  "Macro to set the Meta of the mocked function to a mock"
  [fun-name config]
  `(do (alter-meta! (var ~fun-name)
                    (constantly
                      (assoc (meta (var ~fun-name))
                        :stats-conf config
                        )))
       (var ~fun-name)))

(defmacro add-meta-mock
  "Macro to set the Meta of the mocked function to a mock"
  [fun-name]
  `(do (alter-meta! (var ~fun-name)
                    (constantly
                      (assoc (meta (var ~fun-name))
                        to-mock ~fun-name)))
       (var ~fun-name)))

(defmacro add-meta-spy
  "Macro to set the Meta of the mocked function to a spy"
  [fun-name]
  `(do (alter-meta! (var ~fun-name)
                    (constantly
                      (assoc (meta (var ~fun-name))
                        to-spy ~fun-name)))
       (var ~fun-name)))
(clojure.core/defn collect-fun-meta-data
  "Populates @mock-meta once per qualified function name if not already cached."
  [fun-meta]
  (let [q-name (qualified-fun-name fun-meta)]
    (or (find-meta-for-fun q-name)
        (get (swap! mock-meta
                    (fn [m]
                      (if (contains? m q-name)
                        m
                        (assoc m q-name (get-structured-meta-data fun-meta)))))
             q-name))))

(clojure.core/defn collect-recur-data [trigger-key fun-name]
  (if (get @recur-data-map fun-name)
    (do (swap! recur-data-vect conj {trigger-key
                                     fun-name
                                     :recursive?
                                     (> (get @recur-data-map fun-name) 1)})
        (if (= trigger-key :enter)
          (swap! recur-data-map update fun-name inc)
          (swap! recur-data-map update fun-name dec)))

    (do (swap! recur-data-map conj {fun-name 1})
        (swap! recur-data-vect conj {trigger-key fun-name :recursive? false}))))

(clojure.core/defn get-recur-data []
  {:fun-vector @recur-data-vect :fun-call-map @recur-data-map}
  )

(clojure.core/defn add-upd-spy-statistics
  "Records normal return flow statistics (respecting `:param-filter`) and increments call count."
  [fun-meta fun-name fun-args-descr call-count ret-val args]
  (let [{:keys [record? filtered-args]} (apply-param-filter fun-meta fun-args-descr args)]
    (when record?
      (let [recursive-call-depth (get @recur-data-map fun-name 0)
            recursive-call?      (> recursive-call-depth 1)]
        (swap! statistic-control-flow update :flow-stats conj
               [{:name                 fun-name
                 :args                 filtered-args
                 :pos-named--args      (pspecs/format-args fun-args-descr filtered-args)
                 :args-descr           fun-args-descr
                 :recursive-call-depth recursive-call-depth
                 :recursive?           recursive-call?
                 :threw?               false
                 :ret-val              ret-val}])
        (swap! statistic-control-flow update-in [:statistics fun-name]
               (fn [existing]
                   (-> (or existing {:fun-calls-count 0 :exceptions-count 0})
                       (assoc :fun-calls-count call-count))))))))

(clojure.core/defn collect-flow-statistics
  "Records normal return flow statistics using pre-cached reflection metadata."
  [fun-meta ret-value args]
  (let [fun-name#        (qualified-fun-name-with-thread-prefix fun-meta)
        fun-args-descr#  (get-cached-args-descr fun-meta)
        fun-calls#       (get-in @statistic-control-flow [:statistics fun-name# :fun-calls-count] 0)
        funcall-counter# (inc fun-calls#)]
    (add-upd-spy-statistics fun-meta fun-name# fun-args-descr# funcall-counter# ret-value args)))
(def track-exceptions? (atom true))

;; Controls whether/which exceptions are recorded in @statistic-control-flow.
;; Can be `true` (all exceptions), `false` (none), or a predicate `(fn [ex] ...)`.
(clojure.core/defn should-track-exception? [ex]
  (let [policy @track-exceptions?]
    (cond
      (fn? policy) (boolean (policy ex))
      :else        (boolean policy))))

(clojure.core/defn collect-exception-statistics
  "Records thrown exception details into @statistic-control-flow using pre-cached reflection metadata."
  [fun-meta ex args]
  (when (should-track-exception? ex)
    (let [fun-name#        (qualified-fun-name-with-thread-prefix fun-meta)
          fun-args-descr#  (get-cached-args-descr fun-meta)
          recursive-call?  (> (get @recur-data-map fun-name# 0) 1)
          ex-details#      {:class   (.getName (class ex))
                            :message (.getMessage ex)
                            :data    (ex-data ex)}]
      (swap! statistic-control-flow update :flow-stats conj
             [{:name       fun-name#
               :args       args
               :pos-args   (pspecs/format-args fun-args-descr# args)
               :args-descr fun-args-descr#
               :recursive? recursive-call?
               :threw?     true
               :exception  ex-details#}])
      (swap! statistic-control-flow update-in [:statistics fun-name#]
             (fn [existing]
               (-> (or existing {:fun-calls-count 0 :exceptions-count 0})
                   (update :fun-calls-count inc)
                   (update :exceptions-count inc)))))))

(defmacro with-exception-tracking
  "Activates or configures exception tracking (`true`, `false`, or predicate fn) for `body`,
   restoring the previous setting afterwards."
  [policy & body]
  `(let [prev# @track-exceptions?]
     (reset! track-exceptions? ~policy)
     (try
       ~@body
       (finally
         (reset! track-exceptions? prev#)))))

(clojure.core/defn get-spy-statistics                       ;; TODO: make the function more flexible -> see below logic
  "Macro to set the Meta of the mocked function to a spy"
  [fun-parts]                                               ;; has to be reworked >!! do it tomorrow
  (let [raw-stats (:statistics @statistic-control-flow)
        keys-list (keys raw-stats)
        stats (mapv (clojure.core/fn [fun-part]
                      (let [fun-names (filter (clojure.core/fn [str-to-check]
                                                (str/includes?
                                                  str-to-check
                                                  fun-part))
                                              keys-list)]
                        (mapv (clojure.core/fn [fun-name]
                                {fun-name (get raw-stats fun-name)})
                              fun-names)))
                    fun-parts)]

    stats))

(clojure.core/defn get-spy-flow-stats                       ;; TODO: make the function more flexible -> see below logic
  "Macro to set the Meta of the mocked function to a spy"
  []                                                        ;; has to be reworked >!! do it tomorrow
  (let [raw-stats (:flow-stats @statistic-control-flow)]
    raw-stats))
(clojure.core/defn analyze-call-determinism
      "Iterates over `:flow-stats` using `loop` and groups calls by `[fun-name args]`.
       Returns a map of `{[fun-name args-vec] {:call-count n, :return-values [...], :consistent-result? bool}}`."
      [flow-stats]
      (loop [entries flow-stats
             acc     {}]
        (if (empty? entries)
          ;; Final pass: compute :consistent-result? (true if all calls with same args yielded the exact same outcome)
          (into {}
                (map (fn [[k stats]]
                       [k (assoc stats :consistent-result? (= 1 (count (set (:outcomes stats)))))]))
                acc)
          (let [entry   (first entries)
                ;; Unwrap [{:name ...}] if wrapped in a single-element vector
                record  (if (vector? entry) (first entry) entry)
                call-key [(:name record) (vec (:args record))]
                outcome  (if (:threw? record)
                           {:threw (:exception record)}
                           (:ret-val record))]
            (recur (rest entries)
                   (-> acc
                       (update-in [call-key :call-count] (fnil inc 0))
                       (update-in [call-key :outcomes] (fnil conj []) outcome)))))))

(clojure.core/defn find-meta-for-fun [function-id]
      (get @mock-meta function-id))


(clojure.core/defn get-spy-statistics-by-funs
  "Returns a structured report of statistics, flow entries, exceptions,
   recursion tracking, and cached reflection metadata.
   - When called with no arguments: returns data for ALL mocked and spied functions.
   - When called with `fun-syms`: filters results to matching function names."
  ([]
   (get-spy-statistics-by-funs nil))
  ([& fun-syms]
   (let [fun-parts      (->> fun-syms (remove nil?) (mapv str))
         matches-fun?   (fn [entry-name]
                          (or (empty? fun-parts)
                              (some #(str/includes? (str entry-name) %) fun-parts)))
         raw-stats      (:statistics @statistic-control-flow)
         selected-stats (into {}
                              (filter (fn [[k _]] (matches-fun? k)))
                              raw-stats)
         all-flows      (mapv first (:flow-stats @statistic-control-flow))
         determinism-analysis (analyze-call-determinism all-flows)
         selected-flows (filterv #(matches-fun? (:name %)) all-flows)
         exceptions     (filterv :threw? selected-flows)
         normal-returns (filterv (comp not :threw?) selected-flows)
         recur-vect     (filterv (fn [entry]
                                   (some matches-fun? (vals entry)))
                                 @recur-data-vect)
         recur-map      (into {}
                              (filter (fn [[k _]] (matches-fun? k)))
                              @recur-data-map)
         selected-meta  (into {}
                              (filter (fn [[k _]] (matches-fun? k)))
                              @mock-meta)
         hooked-funs    (->> (keys @redirected-bindings)
                             (filter matches-fun?)
                             vec)]
     {:hooked-functions hooked-funs
      :selected-stats   selected-stats
      :summary          {:total-calls      (reduce + 0 (map :fun-calls-count (vals selected-stats)))
                         :total-exceptions (reduce + 0 (map #(get % :exceptions-count 0) (vals selected-stats)))}
      :flow-stats       selected-flows
      :determinism-analysis determinism-analysis
      :exceptions       exceptions
      :normal-returns   normal-returns
      :recursion        {:call-log  recur-vect
                         :depth-map recur-map}
      :reflected-meta   selected-meta})))

(clojure.core/defn- rule-args-match? [rule args]
  (let [predicates (:param-cond (:when-clauses rule))]
    (and (= (count predicates) (count args))
         (every? true? (map (clojure.core/fn [pred value]
                              (boolean (pred value)))
                            predicates
                            args)))))

(clojure.core/defn- call-rule-action [action]
  (apply (:fun action) (:params action)))

(clojure.core/defn filter-action
  "Evaluates all rules for `fun-meta` in registration order.
   Executes the first matching :when-action, otherwise the first :else-action,
   or delegates to the original `func`."
  [func fun-meta & args]
  (let [rules (find-action-rules-for-fun fun-meta)]
    (if (seq rules)
      (if-let [matched-rule (first (filter #(rule-args-match? % args) rules))]
        (call-rule-action (:when-action matched-rule))
        (if-let [fallback-action (some :else-action rules)]
          (call-rule-action fallback-action)
          (apply func args)))
      (apply func args))))

(clojure.core/defn get-tracked-exceptions
  "Returns all flow-stats entries where an exception was thrown and tracked."
  []
  (filterv :threw? (map first (get-spy-flow-stats))))
