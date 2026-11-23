(ns io.github.hglabplh_tech.test.suite.spy-and-mock.mocking-jay
  (:require [clojure.pprint :refer :all]
            [clojure.string :as str]
            [clojure.walk :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.fun-reader :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.schema-spec.parse-specs :as pspecs]
            [io.github.hglabplh-tech.test.suite.spy-and-mock.mocking-j-collector :as collector]
            [io.github.hglabplh-tech.test.suite.spy-and-mock.stats-util :refer :all]
            [io.github.hglabplh_tech.test.suite.spy-and-mock.mock-spy-misc-util :refer :all])


  )

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

(clojure.core/defn fun-mock-call [fun & args]
  (apply filter-action fun fun args))



(clojure.core/defn return-val [value]
  (println "Mock ret val called")
  value)

(clojure.core/defn throw-ex [excp]
  (throw excp))

(clojure.core/defn do-nothing [& _]
  'nothing)

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

(defn add-stats-conf[fun-sym config]
  (swap! stats-config update fun-sym assoc :config config)
  )

(defn the-prolog-1
  "Expands to code that attaches stats config, registers the namespace,
   and pre-caches reflection metadata in @mock-meta."
  [var-name]
  `(let [target-var#     (var ~var-name)
         the-meta#       (meta target-var#)
         the-fun-ns#     (ns-name (:ns the-meta#))
         the-stats-conf# (resolve-stats-conf the-meta#)]
     (alter-meta! target-var# assoc :stats-config the-stats-conf#)
     (collect-meta-by-ns the-fun-ns#)
     (collect-fun-meta-data (meta target-var#))))

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

