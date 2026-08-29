(ns io.github.hglabplh-tech.reflect.scala.reflection-caller
  (:require [clojure.pprint :refer :all])
  (:import (io.github.hglabplh_tech.reflect.reflscala  CompleteScalaFileReflector))

  )


(defn call-scala [class-name]
  (let [result (CompleteScalaFileReflector/inspectClass class-name)]
    (pprint result)
    ))

(call-scala "io.github.hglabplh_tech.reflect.reflscala.CompleteScalaFileReflector")
