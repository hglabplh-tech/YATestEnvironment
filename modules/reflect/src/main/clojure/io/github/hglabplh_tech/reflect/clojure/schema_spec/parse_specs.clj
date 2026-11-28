(ns io.github.hglabplh-tech.reflect.clojure.schema-spec.parse-specs
  (:require [clojure.pprint :refer [pprint]]
            [io.github.hglabplh-tech.reflect.util.misc-utils :as mu]
            [clojure.spec.alpha :as spec]))



(clojure.core/defn printerr [& args]
  (binding [*out* *err*]
    (apply println args)))


(defn- select-param-list [fun-args-descr arg-count]
  (let [valid-arities (filter #(<= (count (:positional-parameters %)) arg-count)
                              fun-args-descr)
        candidates    (if (seq valid-arities) valid-arities fun-args-descr)
        min-diff-vect (mapv (fn [element]
                              (- arg-count (count (:positional-parameters element))))
                            candidates)
        min-val       (apply min min-diff-vect)
        index-of-min  (first (keep-indexed (fn [idx item]
                                             (when (= item min-val) idx))
                                           min-diff-vect))]
    (nth candidates index-of-min)))


(defn- format-pos-args
  [fun-args-descr arguments]
  (let [pos-params (:positional-parameters (select-param-list fun-args-descr (count arguments)))]
    (loop [pos-param-args  []
           pos-params-rest pos-params
           pos-args-rest   arguments]
      (if (empty? pos-params-rest)
        pos-param-args
        (let [arg-map (assoc (first pos-params-rest) :value (first pos-args-rest))]
          (recur (conj pos-param-args arg-map)
                 (rest pos-params-rest)
                 (rest pos-args-rest)))))))

(defn- format-named-args
  [fun-args-descr arguments]
  (let [selected-arity   (select-param-list fun-args-descr (count arguments))
        param-descr      (if (seq (:named-parameters selected-arity))
                           selected-arity
                           (some #(when (seq (:named-parameters %)) %) fun-args-descr))
        named-params-map (:named-parameters param-descr)
        size-pos-params  (count (:positional-parameters selected-arity))
        tail-args        (drop size-pos-params arguments)
        named-args-map   (cond
                           (empty? tail-args) {}
                           (and (= 1 (count tail-args)) (map? (first tail-args))) (first tail-args)
                           :else (into {} (map vec (partition 2 tail-args))))]
    (loop [named-param-args     []
           named-param-keys     (vec (keys named-params-map))
           named-param-keys-kwd (mapv #(keyword (name %)) named-param-keys)
           named-param-values   (vec (vals named-params-map))]
      (if (empty? named-param-keys)
        named-param-args
        (let [act-key     (first named-param-keys)
              act-key-kwd (first named-param-keys-kwd)
              param-def   (first named-param-values)
              default-val (:default param-def)
              arg-value   (if (contains? named-args-map act-key-kwd)
                            (get named-args-map act-key-kwd)
                            default-val)
              result      (assoc param-def
                            :param act-key
                            :arg-value arg-value)]
          (recur (conj named-param-args result)
                 (rest named-param-keys)
                 (rest named-param-keys-kwd)
                 (rest named-param-values)))))))




(clojure.core/defn format-args [fun-args-descr arguments]
  {:pos-args (format-pos-args fun-args-descr arguments) :named-args (format-named-args fun-args-descr arguments)}
  )

