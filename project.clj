(defproject org.clojars.hglabplh/YATestEnvironment "1.4.0-SNAPSHOT"
  :description "YATestEnvironment umbrella for inspect, reflect, productive and test libraries"
  :url "https://github.com/hglabplh-tech/YATestEnvironment"
  :license {:name "MIT License" :url "https://mit-license.org"}
  :scm {:name "git" :url "https://github.com/hglabplh-tech/YATestEnvironment"}
  :pom-addition [:developers
                 [:developer [:id "hglabplh-tech"] [:name "Harald Glab-Plhak"]
                  [:url "https://hglabplh-tech.github.io"]]]
  :deploy-repositories [["clojars" {:url "https://repo.clojars.org/"
                                    :username :env/clojars_username
                                    :password :env/clojars_password}]]
  ;; The umbrella artifact depends on the four independently published libraries.
  :dependencies [[org.clojure/clojure "1.12.6"]
                 [org.clojars.hglabplh/yatest-inspect "1.4.0-SNAPSHOT"]
                 [org.clojars.hglabplh/yatest-reflect "1.4.0-SNAPSHOT"]
                 [org.clojars.hglabplh/yatest-productive "1.4.0-SNAPSHOT"]
                 [org.clojars.hglabplh/yatest-test "1.4.0-SNAPSHOT"]]
  :source-paths ["src/main/clojure"]
  :java-source-paths []
  :resource-paths []
  :test-paths []
  :compile-path "target/umbrella-classes"
  :javac-options ["--release" "17"]
  :omit-source false
  :plugins [[lein-shell "0.5.0"]]
  :aliases {"modules" ["shell" "bash" "scripts/modules.sh"]
            "all-tests" ["test"]}
  ;; Local development uses sibling sources after `lein modules install`.
  ;; Development dependencies are test-scoped in the POM; fixtures stay out of JARs.
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.12.6"]
                 [de.active-group/active-clojure "0.45.1"]
                 [de.active-group/active-data "0.4.0"]
                 [prismatic/schema "1.4.2"]
                 [org.reflections/reflections "0.10.2"]
                 [org.clojure/data.json "2.5.1"]
                 [com.google.code.findbugs/jsr305 "3.0.2"]
                 [org.ninia/jep "4.3.1"]
                 [org.junit.platform/junit-platform-commons "1.9.1"]]
                   :source-paths ["test-support/clojure"
                 "modules/inspect/src/main/clojure"
                 "modules/reflect/src/main/clojure"
                 "modules/productive/src/main/clojure"
                 "modules/test/src/main/clojure"]
                   :java-source-paths ["modules/inspect/src/main/java"
                 "modules/reflect/src/main/java"
                 "modules/productive/src/main/java"
                 "modules/test/src/main/java"
                 "modules/inspect/src/test/java"
                 "modules/reflect/src/test/java"
                 "modules/productive/src/test/java"
                 "modules/test/src/test/java"]
                   :resource-paths ["modules/inspect/src/main/resources"
                 "modules/reflect/src/main/resources"
                 "modules/productive/src/main/resources"
                 "modules/test/src/main/resources"
                 "modules/inspect/src/test/resources"
                 "modules/reflect/src/test/resources"
                 "modules/productive/src/test/resources"
                 "modules/test/src/test/resources"]
                   :test-paths ["modules/inspect/src/test/clojure"
                 "modules/reflect/src/test/clojure"
                 "modules/productive/src/test/clojure"
                 "modules/test/src/test/clojure"]
                   :compile-path "target/dev-classes"}})
