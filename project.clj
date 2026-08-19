(defproject org.clojars.hglabplh/YATestEnvironment "1.4.0-SNAPSHOT"
  :description "Clojure project to have a test Environment "
  :url "https://github.com/hglabplh-tech/YATestEnvironment.git"
  :license {:name "MIT License"
            :url  "https://mit-license.org"}
  :distribution :repo
  :scm {:name "git"
        :url  "https://github.com/hglabplh-tech/YATestEnvironment"}
  :pom-addition ([:developers
                  [:developer
                   [:id "hglabplh-tech"]
                   [:name "Harald Glab-Plhak"]
                   [:url "https://hglabplh-tech.github.io"]
                   [:roles
                    [:role "developer"]
                    [:role "maintainer"]]]])
  :repositories [["java.net" "https://download.java.net/maven/2"]
                 ["jitpack" "https://jitpack.io"]
                 ["UniHildes"  "https://projects.sse.uni-hildesheim.de/qm/maven"]
                 ["sonatype" {:url "https://oss.sonatype.org/content/repositories/releases"
                              ;; If a repository contains releases only setting
                              ;; :snapshots to false will speed up dependencies.
                              :snapshots false
                              ;; Disable signing releases deployed to this repo.
                              ;; (Not recommended.)
                              :sign-releases false
                              ;; You can also set the policies for how to handle
                              ;; :checksum failures to :fail, :warn, or :ignore.
                              :checksum :fail
                              ;; How often should this repository be checked for
                              ;; snapshot updates? (:daily, :always, or :never)
                              :update :always
                              ;; You can also apply them to releases only:
                              :releases {:checksum :fail :update :always}}]]
  :dependencies [[org.clojure/clojure "1.12.3"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.scala-lang/scala3-library_3 "3.8.4"]
                 [org.scala-lang/scala-reflect "2.13.18"]
                 [org.scala-lang/tasty-core_3 "3.8.4"]
                 [org.scala-lang/scala3-tasty-inspector_3 "3.8.4"]
                 [org.jetbrains.kotlin/kotlin-stdlib "1.9.22"]
                 [org.jetbrains.kotlin/kotlin-metadata-jvm "2.4.20-RC"]
                 [lein-javadoc "0.3.0"]
                 [org.clojure/core.async "1.6.681"]
                 [de.active-group/active-clojure "0.45.1"]
                 [de.active-group/active-data "0.3.3"]
                 [com.github.technomancy/leiningen "2.11.2"]
                 [org.reflections/reflections "0.10.2"]
                 [org.clojure/data.json "2.5.1"]
                 [org.junit.jupiter/junit-jupiter-engine "5.9.1"]
                 [lein-asciidoctorj/lein-asciidoctorj "0.0.5"]
                 ;; https://mvnrepository.com/artifact/jdk.tools/jdk.tools
                 [jdk.tools/jdk.tools "1.7"]
                 ;; Source: https://mvnrepository.com/artifact/org.ninia/jep
                 [org.ninia/jep "4.3.1"]
                 [cheshire/cheshire "6.2.0"]
                ]


  :user {:signing {:gpg-key true
                   :ssh-key "~/.ssh/id_rsa"
                    }}


  :plugins [[lein-codox "0.10.8"]
            [lein-shell "0.5.0"]
            [lein-cljsbuild "1.0.1"]
            [lein-ubersource "0.1.1"]
            [lein-javadoc "0.3.0"]
            [kotlinc-lein "0.1.2"]

            [lein-javac-resources "0.1.1"]]
  :omit-source true  ; excludes .java and .clj files from the generated JAR file
            ; you may not want to set this unless all code is AOT-compiled
  :hooks [leiningen.javac-resources]

  :javac-options ["-target" "17" "-source" "17" "-Xlint:-options"]

  :jvm-opts ["-Dclojure.compiler.disable-locals-clearing=false"
             "-Dclojure.compiler.elide-meta=[:doc :file :line :added]"
             ; notice the array is not quoted like it would be if you passed it directly on the command line.
             "-Dclojure.compiler.direct-linking=true"]


  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]                      ; Java source is stored separately.
  :test-paths ["src/test/clojure" "src/test/java"]
  :resource-paths ["src/test/resources" "lib/tools.jar"]

  :aot :all

  :profiles {:java-tests-compile
             {:java-source-paths ["src/test/java"]}
             }
  :aliases {"java-tests" ["do" "compile," "with-profile"
                          "java-tests-compile" "javac," "codox.main/generate-docs"]
            "all-tests"  ["test," "java-tests"]
            "comp-sc3-kotlin" ["shell" "./sc_kotlin_comp.sh" "./src/scala" "./src/kotlin" "target/classes"]

            }

  :uberjar {:prep-tasks ["clean" "javac" "codox" "compile" "javadoc" "comp-sc3-kotlin" ]
            :aot        :all}
  :classifiers [["sources" {:source-paths      ^:replace ["src/main/clojure"]
                            :java-source-paths ^:replace ["src/main/java"]
                            :resource-paths    ^:replace ["javadoc"]}]
                ["javadoc" {:source-paths      ^:replace []
                            :java-source-paths ^:replace []
                            :resource-paths    ^:replace ["javadoc"]}]]

  :codox {:extra-deps {codox/codox                         {:mvn/version "0.10.7" :exclusions [org.ow2.asm/asm-all]}
                       codox-theme-rdash/codox-theme-rdash {:mvn/version "0.1.2"}
                       hiccup/hiccup                       {:mvn/version "1.0.5"}
                       enlive/enlive                       {:mvn/version "1.1.6"}
                       org.pegdown/pegdown                 {:mvn/version "1.6.0"}
                       org.ow2.asm/asm-all                 {:mvn/version "5.2"}}
          :jvm-opts   ["--add-opens" "java.base/java.lang=ALL-UNNAMED"]
          :exec-fn    codox.main/generate-docs
          :exec-args  {:language    :clojure
                       :metadata    {:doc/format :markdown}
                       :themes      [:rdash]
                       :output-path "docs"}}
  :javadoc-opts {:package-names ["io.github.hglabplh_tech.reflect.clojure.api",
                 "io.github.hglabplh-tech.tests"]

                 }


  :deploy-repositories [["clojars" {:url "https://repo.clojars.org/"
                                    :username :env/clojars_username
                                    :password :env/clojars_password}]]
  )