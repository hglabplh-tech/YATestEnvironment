;; Copyright (c) 2026 Harald Glab-Plhak

(ns io.github.hglabplh-tech.reflect.java.api.java-reflector
  (:require [io.github.hglabplh-tech.reflect.java.api.reflect-class :as rcl]
            [io.github.hglabplh-tech.reflect.java.api.reflect-meths-ctors :as mct]
            [io.github.hglabplh-tech.reflect.java.api.reflect-field :as rfld]
            ))

(clojure.core/defn field-def [field-def]
  (let [field-name (rfld/get-field-name field-def)
        field-type {:field-type (rfld/get-annot-type field-def)
                    :gen-field-type (rfld/get-generic-type field-def)
                    :annot-field-type  (rfld/get-annot-type field-def)}
        field-annotations (rfld/get-all-annots field-def)
        ]

    ))
(clojure.core/defn ctor-def [ctor-def]
  (let [ctor-name (mct/get-ctor-name ctor-def)
        ctor-parms {:parm-count (mct/get-ctor-param-count ctor-def)
                    :parm-types (mct/get-ctor-param-types ctor-def)}
        ctor-exceptions {:exception-types (mct/get-ctor-exception-types ctor-def)
                         :gen-excp-types (mct/get-ctor-gen-exception-types ctor-def)}
        ann (mct/)
        ]
        {:ctor-def {:ctor-name ctor-name :params ctor-parms :ctor-exceptions ctor-exceptions}}

    ))

(clojure.core/defn method-def [meth-def]
  (let [meth-name (mct/get-meth-name meth-def)
        meth-parms {:parm-count (mct/get-meth-param-count meth-def)
                    :parm-types (mct/get-meth-param-types meth-def)}
        meth-exceptions {:exception-types (mct/get-meth-exception-types meth-def)
                         :gen-excp-types (mct/get-meth-gen-exception-types meth-def)}
        ]
    {:method-def {:meth-name meth-name :params meth-parms :meth-exceptions meth-exceptions}}
    ))

(clojure.core/defn get-structured-meta-data [^String class-name]
(let [class-util (rcl/get-class-util class-name)
      class-name (rcl/get-class-name class-util)
      ctors (rcl/get-all-ctors class-util)
      interfaces (rcl/get-interfaces class-util)
      fields (rcl/get-public-fields class-util)
      methods (rcl/get-public-methods class-util)
      constructors (rcl/get-all-ctors class-util)
      struct-data ({:structured-class
                    { :constructors {(mapv ctor-def constructors)}
                      :methods {(mapv method-def methods)}
                     }})
      ]
  {:language :clojure :ns-name (str class-name) :reflect-data struct-data}

  ))