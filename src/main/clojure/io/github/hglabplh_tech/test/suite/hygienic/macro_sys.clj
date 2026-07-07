(ns io.github.hglabplh_tech.test.suite.hygienic.macro-sys
  (:require   [clojure.walk :refer :all]
              [clojure.pprint :refer :all]
              [clojure.set :as set]
              [io.github.hglabplh_tech.test.suite.spy-and-mock.real-fun-checkers :refer :all]))

(def ellipsis-symbol '...)

(def special-form-symbols
  '#{. catch deftype* do finally fn if let let* loop monitor-enter
     monitor-exit new quote recur reify* set! throw try var})

(defrecord SyntaxObject [datum scopes source])

(defn make-syntax-object
  "Wraps a datum with lightweight syntax metadata.

  `scopes` is deliberately represented as data so tests and later analyzers can
  inspect it without depending on compiler internals."
  ([datum]
   (make-syntax-object datum #{} nil))
  ([datum scopes source]
   (->SyntaxObject datum (set scopes) source)))

(defn syntax-object? [value]
  (instance? SyntaxObject value))

(defn syntax-datum [value]
  (if (syntax-object? value)
    (:datum value)
    value))

(defmacro syntax-object
  "Creates a quoted syntax object for a form."
  [datum]
  `(make-syntax-object '~datum #{} {:form '~datum}))

(defn pattern-variable? [x literals]
      (and (symbol? x)
           (not= x '_)
           (not= x ellipsis-symbol)
           (not (contains? literals x))))

(defn merge-envs [& envs]
      (apply merge-with
             (fn [a b]
                 (cond
                   (and (vector? a) (vector? b)) (into a b)
                   (= a b) a
                   :else b))
             envs))

(declare match-seq-pattern)

(defn match-pattern [pattern form literals]
      (cond
        (= pattern '_) {}

        (pattern-variable? pattern literals)
        {pattern form}

        (symbol? pattern)
        (when (= pattern form) {})

        (and (seq? pattern) (seq? form))
        (match-seq-pattern pattern form literals)

        (and (vector? pattern) (vector? form))
        (match-seq-pattern pattern form literals)

        :else
        (when (= pattern form) {})))

(defn match-one [pattern form literals repeated?]
      (let [m (match-pattern pattern form literals)]
           (when m
                 (if repeated?
                   (into {} (map (fn [[k v]] [k [v]]) m))
                   m))))

(defn match-seq-pattern [pattern form literals]
      (loop [ps (seq pattern)
             fs (seq form)
             env {}]
            (cond
              (empty? ps)
              (when (empty? fs) env)

              (and (next ps) (= ellipsis-symbol (second ps)))
              (let [subpattern (first ps)
                    rest-patterns (nnext ps)]
                   (loop [remaining fs
                          repeated-env {}]
                         (if-let [rest-env (match-seq-pattern rest-patterns remaining literals)]
                                 (merge-envs env repeated-env rest-env)
                                 (when (seq remaining)
                                       (let [m (match-one subpattern (first remaining) literals true)]
                                            (when m
                                                  (recur (next remaining)
                                                         (merge-envs repeated-env m))))))))

              :else
              (when (seq fs)
                    (let [m (match-one (first ps) (first fs) literals false)]
                         (when m
                               (recur (next ps)
                                      (next fs)
                                      (merge-envs env m))))))))

(defn repeated-var? [env sym]
      (vector? (get env sym)))

(defn collect-template-vars [template env]
      (cond
        (symbol? template)
        (if (contains? env template) #{template} #{})

        (seq? template)
        (apply set/union #{} (map #(collect-template-vars % env) template))

        (vector? template)
        (apply set/union #{} (map #(collect-template-vars % env) template))

        :else
        #{}))

(defn introduced-symbol? [sym env literals]
      (and (symbol? sym)
           (not= sym '_)
           (not= sym ellipsis-symbol)
           (not (contains? env sym))
           (not (contains? literals sym))
           (not (contains? special-form-symbols sym))
           (not (contains? (ns-publics 'clojure.core) sym))
           (not (resolve sym))
           (not (namespace sym))))

(defn collect-introduced-symbols [template env literals]
      (cond
        (symbol? template)
        (if (introduced-symbol? template env literals)
          #{template}
          #{})

        (seq? template)
        (apply set/union
               #{}
               (map #(collect-introduced-symbols % env literals) template))

        (vector? template)
        (apply set/union
               #{}
               (map #(collect-introduced-symbols % env literals) template))

        :else
        #{}))

(defn expand-symbol [sym env literals renamings]
      (cond
        (contains? env sym)
        (env sym)

        (introduced-symbol? sym env literals)
        (get renamings sym sym)

        :else
        sym))

(declare expand-template)

(defn expand-template-items [items env literals renamings]
      (loop [remaining (seq items)
             result []]
            (cond
              (empty? remaining)
              result

              (and (nnext remaining)
                   (= ellipsis-symbol (nth remaining 2))
                   (seq (filter #(repeated-var? env %)
                                (collect-template-vars (first remaining) env)))
                   (empty? (collect-template-vars (second remaining) env)))
              (let [subtemplates [(first remaining) (second remaining)]
                    vars         (apply set/union
                                        (map #(collect-template-vars % env)
                                             subtemplates))
                    rep-vars     (filter #(repeated-var? env %) vars)
                    n            (count (get env (first rep-vars)))
                    expanded     (mapcat
                                    (fn [i]
                                      (let [item-env (into env
                                                           (for [v rep-vars]
                                                             [v (nth (env v) i)]))]
                                        (map #(expand-template % item-env literals renamings)
                                             subtemplates)))
                                    (range n))]
                   (recur (nnext (next remaining))
                          (into result expanded)))

              (and (next remaining)
                   (= ellipsis-symbol (second remaining)))
              (let [expanded (expand-template
                               (list (first remaining) ellipsis-symbol)
                               env
                               literals
                               renamings)]
                   (recur (nnext remaining)
                          (into result expanded)))

              :else
              (recur (next remaining)
                     (conj result
                           (expand-template
                             (first remaining)
                             env
                             literals
                             renamings))))))

(defn expand-template [template env literals renamings]
      (cond
        (symbol? template)
        (expand-symbol template env literals renamings)

        (and (seq? template)
             (next template)
             (= ellipsis-symbol (second template)))
        (let [subtemplate (first template)
              vars        (collect-template-vars subtemplate env)
              rep-vars    (filter #(repeated-var? env %) vars)
              n           (count (get env (first rep-vars)))]
             (apply list
                    (for [i (range n)]
                         (expand-template
                           subtemplate
                           (into env
                                 (for [v rep-vars]
                                      [v (nth (env v) i)]))
                           literals
                           renamings))))

        (seq? template)
        (apply list
               (expand-template-items template env literals renamings))

        (vector? template)
        (vec (expand-template-items template env literals renamings))

        :else
        template))

(defn syntax-case*
  "Runtime helper for Scheme-like syntax-case matching.

  Returns the expanded template for the first matching clause."
  [expr literals clauses]
  (let [datum (syntax-datum expr)
        literal-set (set literals)]
    (loop [remaining clauses]
      (when (empty? remaining)
        (throw (ex-info "Kein passendes HGP-syntax-case-Pattern"
                        {:form datum
                         :clauses clauses})))
      (let [[pattern template] (first remaining)
            env (match-pattern pattern datum literal-set)]
        (if env
          (let [introduced (collect-introduced-symbols template env literal-set)
                renamings (into {}
                                (map (fn [s]
                                       [s (gensym (str (name s) "__"))])
                                     introduced))]
            (expand-template template env literal-set renamings))
          (recur (rest remaining)))))))

(defmacro syntax-case
  "Small syntax-case facade over the same matcher used by hgp-syntax-rules."
  [expr literals & clauses]
  `(syntax-case* ~expr '~literals '~clauses))

(defmacro hgp-syntax-rules [literals & rules]
          `(fn [form#]
               (let [rules# '~rules
                     literals# (set '~literals)]
                    (loop [rs# rules#]
                          (when (empty? rs#)
                                (throw
                                  (ex-info "Kein passendes HGP-syntax-rules-Pattern"
                                           {:form form#
                                            :rules rules#})))
                          (let [[pattern# template#] (first rs#)
                                env# (match-pattern pattern# form# literals#)]
                               (if env#
                                 (let [introduced# (collect-introduced-symbols
                                                     template#
                                                     env#
                                                     literals#)
                                       renamings#  (into {}
                                                         (map (fn [s#]
                                                                  [s# (gensym
                                                                        (str (name s#) "__"))])
                                                              introduced#))]
                                      (expand-template template# env# literals# renamings#))
                                 (recur (rest rs#))))))))

(defmacro defhgp [name transformer]
          `(defmacro ~name [& args#]
                     (let [whole-form# (cons '~name args#)
                           transformer# ~transformer]
                          (transformer# whole-form#))))

;; Beispiele

(defhgp hgp-when
        (hgp-syntax-rules []
                          [(hgp-when test body ...)
                           (if test
                             (do body ...)
                             nil)]))

(defhgp hgp-let1
        (hgp-syntax-rules []
                          [(hgp-let1 name value body ...)
                           (let [name value]
                                body ...)]))

(defhgp hgp-or
        (hgp-syntax-rules []
                          [(hgp-or a b)
                           (let [tmp a]
                                (if tmp tmp b))]))

(defhgp hgp-and
        (hgp-syntax-rules []
                          [(hgp-and)
                           true]

                          [(hgp-and x)
                           x]

                          [(hgp-and x y ...)
                           (let [tmp x]
                                (if tmp
                                  (hgp-and y ...)
                                  tmp))]))

(defhgp hgp-begin
        (hgp-syntax-rules []
                          [(hgp-begin expr ...)
                           (do expr ...)]))

(defhgp hgp-cond
        (hgp-syntax-rules [else]
                          [(hgp-cond)
                           nil]

                          [(hgp-cond [else body ...])
                           (do body ...)]

                          [(hgp-cond [test body ...] rest ...)
                           (if test
                             (do body ...)
                             (hgp-cond rest ...))]))

(defn demo []
      {:when-result
       (hgp-when true
                 (println "eins")
                 (println "zwei")
                 :fertig)

       :let1-result
       (hgp-let1 x 42
                 (+ x 8))

       :or-result
       (let [tmp "äußeres tmp"]
            [(hgp-or false tmp)
             tmp])

       :begin-result
       (hgp-begin
         (+ 1 2)
         (+ 3 4)
         :ende)})

;; ------------------------------------------------------------
;; Rekursives define-syntax für HGP
;; ------------------------------------------------------------

(defmacro define-syntax
          "Scheme-artiger Alias für defhgp.
           Rekursion ist erlaubt: Ein HGP-Makro darf in seinem Template
           wieder sich selbst aufrufen."
          [name transformer]
          `(defhgp ~name ~transformer))

(define-syntax hgp-begin-rec
               (hgp-syntax-rules []
                                 [(hgp-begin-rec)
                                  nil]

                                 [(hgp-begin-rec x)
                                  x]

                                 [(hgp-begin-rec x y ...)
                                  (do
                                    x
                                    (hgp-begin-rec y ...))]))

(define-syntax hgp-let*
               (hgp-syntax-rules []
                                 [(hgp-let* [] body ...)
                                  (do body ...)]

                                 [(hgp-let* [name value rest ...] body ...)
                                  (let [name value]
                                       (hgp-let* [rest ...] body ...))]))


(defn hgp-values
      [& xs]
      xs)

(define-syntax hgp-let-values
               (hgp-syntax-rules []
                                 [(hgp-let-values [] body ...)
                                  (do body ...)]

                                 [(hgp-let-values [[[name ...] value-expr] rest ...] body ...)
                                  (let [[name ...] value-expr]
                                       (hgp-let-values [rest ...] body ...))]))

(define-syntax hgp-let-values*
               (hgp-syntax-rules []
                                 [(hgp-let-values* [] body ...)
                                  (do body ...)]

                                 [(hgp-let-values* [[[name ...] value-expr] rest ...] body ...)
                                  (let [[name ...] value-expr]
                                       (hgp-let-values* [rest ...] body ...))]))

(define-syntax hgp-letrec-cell
               (hgp-syntax-rules []
                                 [(hgp-letrec-cell [[name value] ...] body ...)
                                  (let [name (atom nil) ...]
                                       (reset! name value)
                                       ...
                                       body ...)]))
