(defproject org.clojars.hglabplh/yatest-test "1.4.0-SNAPSHOT"
  :description "YATestEnvironment test library"
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
                 [org.clojars.hglabplh/yatest-reflect "1.4.0-SNAPSHOT"]
                 [org.junit.platform/junit-platform-commons "1.9.1"]
                 [de.active-group/active-clojure "0.45.1"]
                 [de.active-group/active-data "0.4.0"]
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
                   :compile-path "target/dev-classes"}
             :java-compile
             {:java-source-paths ["src/main/java"]
              :compile-path "target/classes"}

             :java-tests
             {:java-source-paths ["src/test/java"]
              :compile-path "target/test-classes"}

             :clojure-tests
             {:test-paths ["src/test/clojure"]}}
  :aliases
  {"compile-java" ["javac"]
   "compile-for-tests" ["do"
                        ["with-profile" "+java-compile" "javac"]
                        ["with-profile" "+java-tests" "javac"]
                        ["install"]]
   "test-clojure" ["with-profile"
                   "+clojure-tests"
                   "test"]})
