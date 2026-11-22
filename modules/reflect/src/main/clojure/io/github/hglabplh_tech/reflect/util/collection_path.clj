(ns io.github.hglabplh-tech.reflect.util.collection-path
    (:require [com.rpl.specter :as spect]))


;; =============================================================================
;; 1. PARAMETERIZED RECURSIVE NAVIGATOR WITH BRANCH PRUNING
;; =============================================================================

(def WALK-NODES-WITH-PRUNE
  "Parameterized recursive-path taking `prune-pred` (1-arg fn).
   - If `(prune-pred node)` is truthy, halts immediately via `spect/STOP` (neither selects nor descends).
   - Otherwise yields the current node and continues into map values, sequences, and sets."
  (spect/recursive-path [prune-pred] p
                        (spect/if-path prune-pred
                                       spect/STOP
                                       (spect/stay-then-continue
                                         (spect/cond-path
                                           map?        [spect/MAP-VALS p]
                                           sequential? [spect/ALL p]
                                           set?        [spect/ALL p]
                                           :else       spect/STOP)))))

(def ALL-NODES
  "Convenience non-pruning recursive navigator over all nested maps, sequences, and sets."
  (WALK-NODES-WITH-PRUNE (constantly false)))

;; =============================================================================
;; 2. PATH-AGNOSTIC PREDICATES & MULTI-CRITERIA MATCHER
;; =============================================================================

(defn- normalize-path
  "Ensures a single key or vector path is always a vector suitable for `get-in`."
  [k-or-path]
  (if (sequential? k-or-path)
    (vec k-or-path)
    [k-or-path]))

(defn has-value-at?
  "Checks if `node` is a map whose value at `path` equals `expected-val`."
  [node path expected-val]
  (and (map? node)
       (= expected-val (get-in node (normalize-path path)))))

(defn has-value-in?
  "Checks if `node` is a map whose value at `path` is contained in `allowed-values`."
  [node path allowed-values]
  (and (map? node)
       (let [actual  (get-in node (normalize-path path))
             allowed (if (or (set? allowed-values) (sequential? allowed-values))
                       (set allowed-values)
                       #{allowed-values})]
         (contains? allowed actual))))

(defn matches-criteria?
  "Checks whether `node` satisfies 1..n `[path expected-or-allowed]` conditions."
  ([node criteria]
   (matches-criteria? node criteria :and))
  ([node criteria mode]
   (if-not (map? node)
     false
     (let [check-entry (fn [[path expected]]
                         (has-value-in? node path expected))
           quantifier  (case mode
                         :and every?
                         :or  some
                         (throw (ex-info "Invalid match mode; expected :and or :or" {:mode mode})))]
       (boolean (quantifier check-entry criteria))))))

;; =============================================================================
;; 3. UPDATED SMART WALKERS USING WALK-NODES-WITH-PRUNE
;; =============================================================================

(defn- build-bound-predicate
  [{:keys [pred-fun values value]}]
  (let [extra-args (cond
                     (some? values) (if (sequential? values) values [values])
                     (some? value)  [value]
                     :else          [])]
    (if (seq extra-args)
      (fn [node] (apply pred-fun node extra-args))
      pred-fun)))

(defn- normalize-walk-spec
  [idx spec]
  (cond
    (map? spec)    (update spec :id #(or % idx))
    (vector? spec) {:id       idx
                    :pred-fun (first spec)
                    :values   (rest spec)}
    (fn? spec)     {:id       idx
                    :pred-fun spec}
    :else          (throw (ex-info "Invalid walk specification" {:spec spec}))))

(defn smart-walk-through
  "Runs 1..n Specter walks over `the-collection` using WALK-NODES-WITH-PRUNE.
   Optional `:prune-pred` in a map spec stops descent into ignored subtrees."
  [the-collection & select-values]
  (into {}
        (map-indexed
          (fn [idx raw-spec]
            (let [{:keys [id select-path prune-pred] :as spec} (normalize-walk-spec idx raw-spec)
                  bound-pred (build-bound-predicate spec)
                  walker     (WALK-NODES-WITH-PRUNE (or prune-pred (constantly false)))
                  full-path  (into [walker bound-pred] (or select-path []))]
              [id (spect/select full-path the-collection)])))
        select-values))

(defn smart-walk-through-ext
  "Executes 1..n multi-criteria walk requests over `the-collection`.
   Supports:
   - :id          Result key (defaults to positional index)
   - :criteria    Map or pairs of `{path expected-val-or-coll}`
   - :mode        `:and` (default) or `:or`
   - :prune-pred  Optional 1-arg predicate to skip entire branches
   - :select-path Optional sub-path to extract from each matched node"
  [the-collection & requests]
  (into {}
        (map-indexed
          (fn [idx {:keys [id criteria mode prune-pred select-path] :or {mode :and}}]
            (let [res-key   (or id idx)
                  pred      (fn [node] (matches-criteria? node criteria mode))
                  walker    (WALK-NODES-WITH-PRUNE (or prune-pred (constantly false)))
                  full-path (into [walker pred] (or select-path []))]
              [res-key (spect/select full-path the-collection)])))
        requests))
