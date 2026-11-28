(ns io.github.hglabplh-tech.test.suite.spy-and-mock.stats-util)

(def stats-config (atom {}))

;; =============================================================================
;; STATS CONFIGURATION & PARAMETER FILTERING (Positional + Named via args-descr)
;; =============================================================================

(def default-stats-config
  {:config {:param-filter {:mode :none}}})

(defn add-stats-conf
  "Registers a parameter filter config for `fun-sym` (qualified or unqualified)."
  [fun-sym config]
  (let [normalized (if (contains? config :config)
                     config
                     {:config config})]
    (swap! stats-config assoc fun-sym normalized)))

(defn resolve-stats-conf
  "Looks up config in @stats-config by qualified symbol first, then unqualified :name."
  [fun-meta]
  (let [unqual-sym (:name fun-meta)
        qual-sym   (when (:ns fun-meta)
                     (symbol (str (ns-name (:ns fun-meta))) (str unqual-sym)))]
    (or (get @stats-config qual-sym)
        (get @stats-config unqual-sym)
        default-stats-config)))

(defn- match-arity-descr
  "Selects the arity descriptor from `args-descr` matching the runtime `args` count."
  [args-descr args]
  (let [n (count args)]
    (or (first (filter (fn [arity]
                         (let [pos-cnt (count (:positional-parameters arity))]
                           (if (seq (:named-parameters arity))
                             (>= n pos-cnt)
                             (= n pos-cnt))))
                       args-descr))
        (first args-descr))))

(defn- param-matches?
  "Checks if positional index `idx` or parameter name `param-name` is in `target-set`."
  [target-set idx param-name]
  (let [targets (set target-set)]
    (or (and (some? idx) (contains? targets idx))
        (and (some? param-name)
             (let [n (name param-name)]
               (or (contains? targets param-name)
                   (contains? targets (keyword n))
                   (contains? targets (symbol n))))))))

(defn- lookup-by-param
  "Finds `[found? value]` in `lookup-map` by index `idx`, symbol, or keyword."
  [lookup-map idx param-name]
  (cond
    (and (some? idx) (contains? lookup-map idx))
    [true (get lookup-map idx)]

    (some? param-name)
    (let [n  (name param-name)
          kw (keyword n)
          sy (symbol n)]
      (cond
        (contains? lookup-map kw) [true (get lookup-map kw)]
        (contains? lookup-map sy) [true (get lookup-map sy)]
        (contains? lookup-map n)  [true (get lookup-map n)]
        :else                     [false nil]))

    :else [false nil]))

(defn- split-pos-and-named-args
  "Splits `args` into `[pos-args named-part]` using `:positional-parameters` count."
  [arity-descr args]
  (let [pos-defs   (:positional-parameters arity-descr [])
        has-named? (seq (:named-parameters arity-descr))
        pos-count  (count pos-defs)
        args-vec   (vec args)]
    (if (and has-named? (> (count args-vec) pos-count))
      (let [pos-part  (subvec args-vec 0 pos-count)
            tail-part (subvec args-vec pos-count)]
        (if (and (= 1 (count tail-part)) (map? (first tail-part)))
          [pos-part {:style :map :data (first tail-part)}]
          [pos-part {:style :kv :pairs (partition-all 2 tail-part)}]))
      [args-vec nil])))

(defn- reassemble-args
  [filtered-pos named-part]
  (case (:style named-part)
    :map (conj (vec filtered-pos) (:data named-part))
    :kv  (into (vec filtered-pos) (mapcat identity (:pairs named-part)))
    (vec filtered-pos)))

(defn- transform-named-part
  [named-part value-fn]
  (case (:style named-part)
    :map (update named-part :data
                 (fn [m]
                   (into (empty m) (map (fn [[k v]] [k (value-fn k v)])) m)))
    :kv  (update named-part :pairs
                 (fn [pairs]
                   (mapv (fn [pair]
                           (if (= 2 (count pair))
                             [(first pair) (value-fn (first pair) (second pair))]
                             pair))
                         pairs)))
    nil))

(defn- effective-named-map
  "Merges schema defaults from `:named-parameters` with runtime named args for `:do-stats` checks."
  [arity-descr named-part]
  (let [schema-defaults (into {}
                              (keep (fn [np]
                                      (when (contains? np :default)
                                        [(keyword (name (or (:key np) (:param np))))
                                         (:default np)])))
                              (:named-parameters arity-descr))
        runtime-map     (case (:style named-part)
                          :map (:data named-part)
                          :kv  (into {} (filter #(= 2 (count %)) (:pairs named-part)))
                          {})]
    (merge schema-defaults runtime-map)))

(defn apply-param-filter
  "Uses `args-descr` (from `reflect-mixed-params`) to filter/transform
   positional and named `args` according to `:stats-config` in `fun-meta`.
   Returns `{:record? bool :filtered-args [...]}`."
  [fun-meta args-descr args]
  (let [filter-cfg            (get-in fun-meta [:stats-config :config :param-filter] {:mode :none})
        mode                  (:mode filter-cfg :none)
        arity-descr           (match-arity-descr args-descr args)
        pos-defs              (:positional-parameters arity-descr [])
        [pos-args named-part] (split-pos-and-named-args arity-descr args)]
    (case mode
      :none
      {:record? true :filtered-args (vec args)}

      :exclude
      (let [mask         (:mask filter-cfg :excluded)
            targets      (:params filter-cfg)
            filtered-pos (map-indexed (fn [idx val]
                                        (let [pname (get-in pos-defs [idx :param])]
                                          (if (param-matches? targets idx pname) mask val)))
                                      pos-args)
            filtered-nam (transform-named-part
                           named-part
                           (fn [k val] (if (param-matches? targets nil k) mask val)))]
        {:record?       true
         :filtered-args (reassemble-args filtered-pos filtered-nam)})

      :include
      (let [mask         (:mask filter-cfg :omitted)
            targets      (:params filter-cfg)
            filtered-pos (map-indexed (fn [idx val]
                                        (let [pname (get-in pos-defs [idx :param])]
                                          (if (param-matches? targets idx pname) val mask)))
                                      pos-args)
            filtered-nam (transform-named-part
                           named-part
                           (fn [k val] (if (param-matches? targets nil k) val mask)))]
        {:record?       true
         :filtered-args (reassemble-args filtered-pos filtered-nam)})

      :fix-val
      (let [fixed-map    (:values filter-cfg {})
            filtered-pos (map-indexed (fn [idx val]
                                        (let [pname          (get-in pos-defs [idx :param])
                                              [found? fixed] (lookup-by-param fixed-map idx pname)]
                                          (if found? fixed val)))
                                      pos-args)
            filtered-nam (transform-named-part
                           named-part
                           (fn [k val]
                             (let [[found? fixed] (lookup-by-param fixed-map nil k)]
                               (if found? fixed val))))]
        {:record?       true
         :filtered-args (reassemble-args filtered-pos filtered-nam)})

      :do-stats
      (let [preds      (:predicates filter-cfg {})
            nam-values (effective-named-map arity-descr named-part)
            record?    (every? (fn [[k pred-fn]]
                                 (cond
                                   (number? k)
                                   (and (< k (count pos-args))
                                        (boolean (pred-fn (nth pos-args k))))

                                   (contains? nam-values (keyword (name k)))
                                   (boolean (pred-fn (get nam-values (keyword (name k)))))

                                   :else
                                   (if-let [idx (first (keep-indexed
                                                         (fn [i p]
                                                           (when (= (name k) (name (:param p))) i))
                                                         pos-defs))]
                                     (and (< idx (count pos-args))
                                          (boolean (pred-fn (nth pos-args idx))))
                                     false)))
                               preds)]
        {:record? record? :filtered-args (vec args)})

      {:record? true :filtered-args (vec args)})))
