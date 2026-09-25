(ns io.github.hglabplh-tech.reflect.scala.reflection-caller
  (:require [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.scala.screflection :as scala-refl]
            [io.github.hglabplh-tech.reflect.scala.sctastyreflection :as tasty-refl]
            )
  )


(defn call-scala [class-name]
  (let [base-refl []
        tasty-refl (tasty-refl/reflect-scala-class-by-name class-name)
        result (conj base-refl tasty-refl)]
    (pprint result)
    ))

(call-scala "io.github.hglabplh_tech.reflect.reflscala.CompleteScalaFileReflector")
