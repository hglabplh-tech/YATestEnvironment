(ns io.github.hglabplh-tech.reflect.clojure.fun-reader
  (:refer-clojure :exclude [def defn fn])
  (:require [active.data.realm.attach :refer :all]
            [active.data.realm.attach :refer :all]
            [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.clojure.active-data.parse-meta :as pactd]
            [io.github.hglabplh-tech.reflect.clojure.analyze.fun-analyzer :refer :all]
            [schema.core :as s]))

(declare get-structured-meta-data)
(clojure.core/defn printout_excp [excp]
  (println (.getMessage excp))
  (.printStackTrace excp))

(clojure.core/defn structure-out-meta [meta-data]
  "do it think about a well formed struct "
  (let [schema-data-part (get-structured-meta-data meta-data)]
    schema-data-part
    ))

(clojure.core/defn get-schema-structured [namespace-sym function-sym]
  (let [ns-intern-map (ns-interns namespace-sym)
        fun (get ns-intern-map function-sym)]
    (if-not (or (nil? ns-intern-map)
                (empty? ns-intern-map))
      (let [meta-data (meta fun)]
        (structure-out-meta meta-data)))))

(clojure.core/defn really-get-meta [namespace-sym fun-sym]
  (require [namespace-sym :refer :all])
  (let [ns-intern-map (ns-interns namespace-sym)
        the-fun (get ns-intern-map fun-sym)]
    (if-not (or (nil? ns-intern-map)
                (empty? ns-intern-map))
      (let [meta-data (meta the-fun)]
        (try
          (let [decompiled (get-structured-meta-data meta-data)]
            decompiled)
          (catch Throwable excp
            (printout_excp excp)
            ))
        ;;(get-rec-meta meta-data)
        ))))

(clojure.core/defn get-decompiled-meta [meta-data]
  (try
    (let [decompiled (get-structured-meta-data meta-data)]
      decompiled)
    (catch Throwable excp
      (printout_excp excp)
      ))
  ;;(get-rec-meta meta-data)
  )

(clojure.core/defn parse-base-schema [input]
  (let [ret-val (first input)
        params (first (rest input))]
    [ret-val params]
    ))
(defmacro get-fun-meta-val-by-key [fun-name the-tag]
  `(get (meta (var ~fun-name))
        ~the-tag))

(defmacro get-fun-meta-args [the-fun-name]
  `(get-fun-meta-val-by-key ~the-fun-name
                            :arglists))

(defmacro get-fun-meta-args-count [the-fun-name]
  `(count (first (get-fun-meta-val-by-key ~the-fun-name
                                          :arglists))))

(defmacro get-fun-meta-ns [the-fun-name]
  `(get-fun-meta-val-by-key ~the-fun-name
                            :ns))

(defmacro get-fun-meta-ns-sym [the-fun-name]
  `(symbol (.toString (get-fun-meta-val-by-key ~the-fun-name
                                               :ns))))

(defmacro get-fun-meta-name [the-fun-name]
  `(get-fun-meta-val-by-key ~the-fun-name
                            :name))

(defmacro get-fun-meta-line [the-fun-name]
  `(get-fun-meta-val-by-key ~the-fun-name
                            :line))

(defmacro get-fun-meta-doc [the-fun-name]
  `(get-fun-meta-val-by-key ~the-fun-name
                            :doc))


(defmacro get-fun-meta-col [the-fun-name]
  `(get-fun-meta-val-by-key ~the-fun-name
                            :column))

(defmacro get-fun-meta-schema [the-fun-name]
  `(let [result# (get-fun-meta-val-by-key ~the-fun-name
                                          :schema)
         cooked-result# (parse-base-schema result#)
         ]
     cooked-result#
     ))

(clojure.core/defn get-meta-full [namespace-sym record-sym]
  (let [ns-intern-map (ns-interns namespace-sym)
        rec (get ns-intern-map record-sym)]
    (if-not (or (nil? ns-intern-map)
                (empty? ns-intern-map))
      (let [meta-data (meta rec)]
        meta-data
        ))))

(defmacro schema-struct [ns-in & objects]
  `(do ~@(map (clojure.core/fn [obj]
                (get-schema-structured ns-in obj))
              objects
              ))
  )

(defmacro analyze-struct [ns-in & objects]
  `(let [result# ~@(map (clojure.core/fn [obj]
                          (require [ns-in :refer :all])
                          (get-schema-structured ns-in obj))
                        objects
                        )]
     (analyze-fun result#)))



(clojure.core/defn parse-arg-types [arg-type-defs]
  (let [descriptors (first (second (first arg-type-defs)))
        the-schema-compiled (mapv (schema.core/fn [val] {:schema-type (get val :schema)                         ;;       (mapv (schema.core/fn [val]
                ;;                                    {:schema-type (mapv (clojure.core/fn [inner-val]
                ;;                    (update-vals inner-val decompile-if)) (get val :schema))
                :name (get val :name)
                :optional? (get val :optional?)
                :types (get val :types-vect)})
                                  descriptors)
     ]

    the-schema-compiled)
    )

(clojure.core/defn parse-meta-to-map [schema-val]

  (let [return-type (second (first schema-val))
        arg-info-map (parse-arg-types (rest schema-val))]
    {:return-type return-type
     :arg-infos arg-info-map}))

(clojure.core/defn get-decompiled-active-data [raw-meta]
  (let [active-fun-meta (get raw-meta fn-realm-meta-key {})
        parsed-active-meta     (pactd/decompile-active-rec active-fun-meta)
        ]
    (pprint parsed-active-meta)
    (println "all ok up to this step")
    parsed-active-meta
    ))

(clojure.core/defn get-structured-meta-data [fun-meta]
  (let [the-schema (get fun-meta :schema)
        the-ns (get fun-meta :ns)
        the-name (get fun-meta :name)
        the-line (get fun-meta :line)
        the-doc  (get fun-meta :doc)
        priv     (get fun-meta :private)
        the-column (get fun-meta :column)
        argument-list (get fun-meta :arglists)
        param-defs    (map #(or (-> % meta :tag) :any) (first argument-list))
        return-def   (or (:tag fun-meta)
                                  (:tag (meta (first argument-list)))
                                  :any)]
    (let [schema-val (parse-base-schema the-schema)]
      {:structured-meta {:base-data {:ns            the-ns
                                     :name          the-name
                                     :doc           the-doc
                                     :private       priv
                                     :tag           {:return-type return-def :param-types param-defs}
                                     :line          the-line
                                     :column        the-column
                                     :arguments-list argument-list
                                     }
                         :add-on    (conj {:schema (parse-meta-to-map schema-val)
                                           :active-data (get-decompiled-active-data fun-meta)})}}

      )))

(clojure.core/defmacro decompile-meta [ns-in obj]
  (do ~@(do(require [ns-in :refer :all])
         (really-get-meta ns-in obj))))