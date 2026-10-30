(defproject org.clojars.hglabplh/yatest-reflect "1.4.0-SNAPSHOT"
  :description "YATestEnvironment reflect library"
  :url "https://github.com/hglabplh-tech/YATestEnvironment"
  :license {:name "MIT License" :url "https://mit-license.org"}
  :scm {:name "git" :url "https://github.com/hglabplh-tech/YATestEnvironment"}
  :pom-addition [:developers
                 [:developer [:id "hglabplh-tech"] [:name "Harald Glab-Plhak"]
                  [:url "https://hglabplh-tech.github.io"]]]
  :deploy-repositories [["clojars" {:url "https://repo.clojars.org/"
                                    :username :env/clojars_username
                                    :password :env/clojars_password}]]
  :dependencies [[org.clojure/clojure "1.12.6"]
                 [de.active-group/active-clojure "0.45.1"]
                 [de.active-group/active-data "0.4.0"]
                 [prismatic/schema "1.4.2"]
                 [org.reflections/reflections "0.10.2"]
                 [org.clojure/data.json "2.5.1"]
                 [com.google.code.findbugs/jsr305 "3.0.2"]
                 [jakarta.validation/jakarta.validation-api "3.0.2"]
                 [org.hibernate.validator/hibernate-validator "8.0.1.Final"]
                 [org.glassfish/jakarta.el "4.0.2"]]
  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]
  :resource-paths ["src/main/resources"]
  :test-paths ["src/test/clojure"]
  :javac-options ["--release" "17"]
  ;; Clojure sources are required by consumers; do not use :omit-source true.
  :omit-source false
  :test-selectors {:default (complement :integration)
                   :integration :integration
                   ;; This selector matches absolutely nothing
                   :none (constantly false)}

  :jar-exclusions [#"(^|/)\.gitkeep$" #"(^|/)\.DS_Store$"]
  :profiles {:dev {:java-source-paths ["src/test/java"]
                   :resource-paths ["src/test/resources"]
                   :source-paths ["../../test-support/clojure"]
                   :compile-path "target/dev-classes"}
             ;; Scala/Kotlin inspection is optional; compile with +polyglot.
             :polyglot {:dependencies [[org.scala-lang/scala3-library_3 "3.8.4"]
                                       [org.scala-lang/scala-reflect "2.13.18"]
                                       [org.scala-lang/tasty-core_3 "3.8.4"]
                                       [ch.epfl.scala/tasty-query_3 "1.8.0"]
                                       [org.scala-lang/scala3-tasty-inspector_3 "3.8.4"]
                                       [org.jetbrains.kotlin/kotlin-stdlib "2.1.20"]
                                       [org.jetbrains.kotlin/kotlin-metadata-jvm "2.1.20"]
                                       [org.jetbrains.kotlin/kotlin-reflect "2.1.20"]]
                        :plugins [[lein-shell "0.5.0"]]
                        :aliases {"comp-sc3-kotlin"
                                  ["shell" "bash" "../../sc_kotlin_comp.sh"
                                   "src/main/scala" "src/main/kotlin" "target/classes"]}}})
