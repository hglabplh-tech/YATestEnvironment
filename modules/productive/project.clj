(defproject org.clojars.hglabplh/yatest-productive "1.4.0-SNAPSHOT"
  :description "YATestEnvironment productive library"
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
                 [jakarta.validation/jakarta.validation-api "3.0.2"]
                 [org.hibernate.validator/hibernate-validator "8.0.1.Final"]
                 [org.glassfish/jakarta.el "4.0.2"]
                 [prismatic/schema "1.4.2"]]
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
                   :compile-path "target/dev-classes"
                   :dependencies [[org.clojars.hglabplh/yatest-reflect "1.4.0-SNAPSHOT"]]}})
