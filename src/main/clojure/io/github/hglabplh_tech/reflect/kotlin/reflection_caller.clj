(ns io.github.hglabplh-tech.reflect.kotlin.reflection-caller
  (:require [clojure.pprint :refer :all]
            [io.github.hglabplh-tech.reflect.kotlin.kreflection :refer :all]
            )
  (:import (io.github.hglabplh_tech.reflect.reflkotlin  CompleteKTFileReflector))

  )


(defn call-kotlin [class-name]
  (let [                                                    ;;instance (CompleteKTFileReflector.)
        ;;result-native (.inspectClass instance class-name)
        result (parse-kotlin-meta-by-name class-name)
        ;;result (conj result-native result-meta)
        ]
    (pprint result)
    ))

(call-kotlin "io.github.hglabplh_tech.reflect.reflkotlin.CompleteKTFileReflector")
