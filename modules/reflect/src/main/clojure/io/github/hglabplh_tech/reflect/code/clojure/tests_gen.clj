(ns io.github.hglabplh-tech.reflect.code.clojure.tests-gen)


(defn extract-fun-def [fun-def-case]
  (let [fdef fun-def-case
        freturn (get fdef :ret-val-def)
        fposargs (get fdef :pos-arg-def)
        foptargs (get fdef :opt-arg-def)]
    ))