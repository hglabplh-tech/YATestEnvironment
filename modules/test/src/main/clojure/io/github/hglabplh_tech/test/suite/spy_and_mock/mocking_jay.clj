(ns io.github.hglabplh_tech.test.suite.spy-and-mock.mocking-jay
  (:require [clojure.pprint :refer :all]
            [clojure.string :as str]
            [clojure.walk :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.fun-reader :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.schema-spec.parse-specs :as pspecs]
            [io.github.hglabplh-tech.test.suite.spy-and-mock.mocking-j-collector :as collector]
            [io.github.hglabplh_tech.test.suite.spy-and-mock.mock-spy-misc-util :refer :all])


  )

(declare collect-recur-data)
(declare collect-flow-statistics)
(declare collect-exception-statistics)
(declare get-spy-flow-stats)
(declare filter-action)


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

(def mock-meta (atom {}))

(def ns-f-meta-atom (atom []))

(def redirected-bindings (atom {}))

(def statistic-control-flow (atom {:flow-stats [] :statistics {}}))

(def recur-data-vect (atom []))

(def recur-data-map (atom {}))

(def control-flow-collection (atom {:language :clojure :funcalls []}))

(def rule-vect (atom []))

(def track-exceptions? (atom true))


(clojure.core/defn- invoke-with-tracking
  "Wraps `invoke-fn` with recursion tracking, return flow statistics, and conditional
   exception tracking in a try/catch/finally block."
  [fun-meta args invoke-fn]
  (let [thread-fun-name (qualified-fun-name-with-thread-prefix fun-meta)]
    (collect-recur-data :enter thread-fun-name)
    (try
      (let [result (invoke-fn)]
        (collect-flow-statistics fun-meta result args)
        result)
      (catch Throwable ex
        (collect-exception-statistics fun-meta ex args)
        (throw ex))
      (finally
        (collect-recur-data :ret-from thread-fun-name)))))

(clojure.core/defn- rule-args-match? [rule args]
  (let [predicates (:param-cond (:when-clauses rule))]
    (and (= (count predicates) (count args))
         (every? true? (map (clojure.core/fn [pred value]
                              (boolean (pred value)))
                            predicates
                            args)))))

(clojure.core/defn- call-rule-action [action]
  (apply (:fun action) (:params action)))


(clojure.core/defn fun-mock-call [fun & args]
  (apply filter-action fun fun args))


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
  "Registers a mock condition rule for `fun` without requiring quoted symbols.
   Supports optional :else clause and multiple rules per function."
  ([fun cond? ret-val-predicate delim args-vect when-action-vect]
   `(register-rule! (var ~fun) ~cond? ~ret-val-predicate ~delim ~args-vect ~when-action-vect nil nil))
  ([fun cond? ret-val-predicate delim args-vect when-action-vect else? else-action-vect]
   `(register-rule! (var ~fun) ~cond? ~ret-val-predicate ~delim ~args-vect ~when-action-vect ~else? ~else-action-vect)))

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



(clojure.core/defn return-val [value]
  (println "Mock ret val called")
  value)

(clojure.core/defn throw-ex [excp]
  (throw excp))

(clojure.core/defn do-nothing [& _]
  'nothing)

(clojure.core/defn find-meta-for-fun [func-name]
  (first (filter (clojure.core/fn [element]
                   (= func-name (:function-name element)))
                 @mock-meta)))




(clojure.core/defn reset-spy-state!
  "Resets all spy, mock, recursion, and metadata tracking atoms to their initial empty state."
  []
  (reset! mock-meta {})
  (reset! ns-f-meta-atom [])
  (reset! redirected-bindings {})
  (reset! statistic-control-flow {:flow-stats [] :statistics {}})
  (reset! recur-data-vect [])
  (reset! recur-data-map {})
  (reset! control-flow-collection {:language :clojure :funcalls []})
  (reset! rule-vect [])
  :reset-done)


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

(defn restore-real-funs
  [sut]
  (let [sut-publics (vals (ns-publics sut))]
    (doseq [curf sut-publics]
      (when (or (some? (:mock-key (meta curf)))
                (some? (:spy-key (meta curf))))
        (let [fun-name# (qualified-fun-name (meta curf))]
          (when-let [[_ _ orig-fn# _] (get @redirected-bindings fun-name#)]
            (alter-var-root curf (constantly orig-fn#))
            (alter-meta! curf dissoc to-spy to-mock)
            (swap! redirected-bindings dissoc fun-name#)))))))

(clojure.core/defn reset-spy-state!
  "Resets all spy, mock, recursion, exception tracking, and metadata atoms."
  []
  (reset! mock-meta {})
  (reset! ns-f-meta-atom [])
  (reset! redirected-bindings {})
  (reset! statistic-control-flow {:flow-stats [] :statistics {}})
  (reset! recur-data-vect [])
  (reset! recur-data-map {})
  (reset! control-flow-collection {:language :clojure :funcalls []})
  (reset! rule-vect [])
  (reset! track-exceptions? true)
  :reset-done)

(clojure.core/defn make-spy-fixture
  "Returns a clojure.test :each fixture that restores original functions in `namespaces`
   and resets all spy/mock atoms before and after every test."
  [& namespaces]
  (clojure.core/fn [test-fn]
    (reset-spy-state!)
    (try
      (test-fn)
      (finally
        (doseq [ns-sym namespaces]
          (restore-real-funs ns-sym))
        (reset-spy-state!)))))

(defmacro with-exception-tracking
  "Temporarily sets `track-exceptions?` policy (`true`, `false`, or predicate fn) for `body`,
   restoring the previous setting afterwards."
  [policy & body]
  `(let [prev# @track-exceptions?]
     (reset! track-exceptions? ~policy)
     (try
       ~@body
       (finally
         (reset! track-exceptions? prev#)))))

(clojure.core/defn get-tracked-exceptions
  "Returns all flow-stats entries where `:threw?` is true."
  []
  (filterv :threw? (map first (get-spy-flow-stats))))

(clojure.core/defn get-normal-returns
  "Returns all flow-stats entries where `:threw?` is false."
  []
  (filterv (comp not :threw?) (map first (get-spy-flow-stats))))


(clojure.core/defn printerr [& args]
  (binding [*out* *err*]
    (apply println args)))

(clojure.core/defn get-cached-args-descr
  "Retrieves pre-reflected :args-types-list from @mock-meta, populating @mock-meta if absent."
  [fun-meta]
  (let [q-name (qualified-fun-name fun-meta)
        cached (or (find-meta-for-fun q-name)
                   (let [structured (get-structured-meta-data fun-meta)]
                     (swap! mock-meta assoc q-name structured)
                     structured))]
    (get-in cached [:structured-meta :base-data :args-types-list])))

(clojure.core/defn add-upd-spy-statistics
  "Records normal return flow statistics and increments call count."
  [fun-name fun-args-descr call-count ret-val args]
  (let [recursive-call? (> (get @recur-data-map fun-name 0) 1)]
    (swap! statistic-control-flow update :flow-stats conj
           [{:name       fun-name
             :args       args
             :pos-args   (pspecs/format-args fun-args-descr args)
             :args-descr fun-args-descr
             :recursive? recursive-call?
             :threw?     false
             :ret-val    ret-val}])
    (swap! statistic-control-flow update-in [:statistics fun-name]
           (fn [existing]
             (-> (or existing {:fun-calls-count 0 :exceptions-count 0})
                 (assoc :fun-calls-count call-count))))))

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

(clojure.core/defn collect-flow-statistics
  "Records normal return flow statistics using pre-cached reflection metadata."
  [fun-meta ret-value args]
  (let [fun-name#        (qualified-fun-name-with-thread-prefix fun-meta)
        fun-args-descr#  (get-cached-args-descr fun-meta)
        fun-calls#       (get-in @statistic-control-flow [:statistics fun-name# :fun-calls-count] 0)
        funcall-counter# (inc fun-calls#)]
    (add-upd-spy-statistics fun-name# fun-args-descr# funcall-counter# ret-value args)))

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
      :exceptions       exceptions
      :normal-returns   normal-returns
      :recursion        {:call-log  recur-vect
                         :depth-map recur-map}
      :reflected-meta   selected-meta})))


(defn find-meta-for-fun [function-id]
  (get @mock-meta function-id))

(defn store-real-fun [orig-fn meta-ns-name meta-fun-name fun-meta-data]
  (let [q-name (qualified-fun-name fun-meta-data)]
    ;; Only store if not already redirected so double-spying doesn't overwrite the original fn
    (when-not (contains? @redirected-bindings q-name)
      (swap! redirected-bindings assoc
             q-name
             [meta-ns-name meta-fun-name orig-fn fun-meta-data]))))




(defn mock-hook
  "Adds mock rule dispatch with recursion tracking, flow statistics,
   and conditional exception tracking (capturing both mocked and real exceptions)."
  [sut]
  (let [sut-intern (vals (ns-publics sut))]
    (doseq [curf sut-intern]
      (when (some? (:mock-key (meta curf)))
        (let [fun-name# (get (meta curf) :name)
              fun-ns#   (get (meta curf) :ns)
              fun-meta# (meta curf)]
          (store-real-fun @curf fun-ns# fun-name# fun-meta#)
          (alter-var-root
            curf
            (fn [f]
              (fn [& args#]
                (invoke-with-tracking fun-meta# args# #(apply filter-action f fun-meta# args#))))))))))

(defn find-in-list [the-list to-find]

  (loop [the-list-int# the-list]
    (do
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

;; Controls whether/which exceptions are recorded in @statistic-control-flow.
;; Can be `true` (all exceptions), `false` (none), or a predicate `(fn [ex] ...)`.
(def track-exceptions? (atom true))

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



(clojure.core/defn get-tracked-exceptions
  "Returns all flow-stats entries where an exception was thrown and tracked."
  []
  (filterv :threw? (map first (get-spy-flow-stats))))

(clojure.core/defn fun-spy-call
  [fun fun-meta stack-trace-data]
  (let [fun# fun
        stack-trace-data# stack-trace-data]
    ;;(apply collect-meta-active-data fun-name# args#)
    (println (collector/clean-get-fun-caller 3 true))
    (println "I am a spy and I see you: " `~fun#)

    'spyed))

(defn spy-hook
  "Adds spy instrumentation with stack trace capture, recursion tracking,
   and automatic conditional exception tracking."
  [sut]
  (let [sut-publics (ns-publics sut)]
    (doseq [[_ curf] sut-publics]
      (when (some? (:spy-key (meta curf)))
        (let [fun-name# (get (meta curf) :name)
              fun-ns#   (get (meta curf) :ns)
              fun-meta# (meta curf)]
          (store-real-fun @curf fun-ns# fun-name# fun-meta#)
          (alter-var-root
            curf
            (fn [f]
              (fn [& args#]
                (let [stack-trc-data (.getStackTrace (Thread/currentThread))]
                  (fun-spy-call f fun-meta# stack-trc-data)
                  (invoke-with-tracking fun-meta# args# #(apply f args#)))))))))))

(defmacro mock
  "Mocks 1..n namespaces at runtime after `prolog` has tagged target Vars."
  [& namespaces]
  `(do ~@(map (clojure.core/fn [ns-sym] `(mock-hook '~ns-sym)) namespaces)))

(defmacro spy
  "Spies on 1..n namespaces at runtime after `prolog` has tagged target Vars."
  [& name-spaces]
  `(do ~@(map (clojure.core/fn [ns-sym] `(spy-hook '~ns-sym)) name-spaces)))

(defmacro restore-orig-funs
  "Restores original function bindings for 1..n namespaces at runtime."
  [& names]
  `(do ~@(map (clojure.core/fn [ns-sym] `(restore-real-funs '~ns-sym)) names)))


(defn the-prolog-1
  "Expands to code that registers the namespace and pre-caches reflection metadata in @mock-meta."
  [var-name]
  `(let [the-meta#   (meta (var ~var-name))
         the-fun-ns# (ns-name (get the-meta# :ns))]
     (collect-meta-by-ns the-fun-ns#)
     (collect-fun-meta-data the-meta#)))

(defmacro prolog
  [key & names]
  (case key
    to-spy
    `(do
       ~@(map (clojure.core/fn [n] `(add-meta-spy ~n)) names)
       ~@(map the-prolog-1 names))
    to-mock
    `(do
       ~@(map (clojure.core/fn [n] `(add-meta-mock ~n)) names)
       ~@(map the-prolog-1 names))))

