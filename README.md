# YATestEnvironment

YATestEnvironment is a mixed Clojure and Java test-engineering project. It brings together reflection utilities, generated test-data pipelines, function spying and mocking, annotation helpers, and early code-generation experiments for Java/Clojure test automation.

The project is usable as a library and as a research/workbench codebase. Some areas are mature enough for automated tests, while others are intentionally marked as foundations or experiments.

## Status Legend

- **Available**: implemented and covered by smoke or unit tests in this repository.
- **In progress**: implemented enough to use as a foundation, but the API or behavior may still change.
- **Experimental**: research-oriented code, prototypes, or partial implementations.

## What It Provides

### Available

- Java reflection helpers for classes, constructors, methods, fields, annotations, generic types, modifiers, and type attributes.
- Java annotation inspection and conversion into Clojure data.
- Reflection-to-Clojure-data conversion for class definitions, class bodies, constructors, fields, methods, enum data, record data, lambda-related structures, and switch-related structures.
- Clojure function metadata reading, including Active Data / realm schema extraction.
- Clojure function spying and mocking helpers with metadata tracking and call-flow collection.
- Generated test-data pipeline for Clojure functions and reflected Java methods.
- Payload generation for JSON, XML, CSV, text, printed Clojure data, and compact PDF-style payloads.
- Artifact-oriented test-data Player that reads EDN-style jobs and produces TXT, PDF, DOCX, JPEG, TIFF where supported by ImageIO, and relational EDN rows.
- Artifact storage backends for memory and filesystem output.
- SQLite persistence for generated analysis and generated payloads.
- Custom Java test annotation `@YATest` and test category enum.
- Independent Leiningen library packaging and a combined development project.

### In Progress

- Hook-based Java reflection code generation for JSON, XML, YAML, and Java-like output.
- PostgreSQL artifact storage. The implementation is wired through JDBC, but it requires the PostgreSQL JDBC driver on the runtime classpath and a reachable database.
- Python module inspection through JEP and a bundled Python inspector script.
- Static namespace/code analysis for Clojure namespaces.
- Java special-form analysis for lambdas, switch constructs, enums, and records. Enum and record paths are more concrete; lambda and switch inspection still need hardening.

### Experimental

- Hygienic macro-system experiments inspired by syntax objects and syntax-case style expansion.
- Agda theory notes.
- Research documentation around entropy-oriented testing and broader testing strategy.
- Legacy namespace compatibility between `reflect.java.*`, `reflect.clojure.*`, and newer `reflect.code.java.*` areas.

## Project Layout

YATestEnvironment is the umbrella project. Each library has its own Leiningen
`project.clj`, production sources/resources, and tests/resources:

| Directory | Clojars artifact | Contents |
| --- | --- | --- |
| `modules/inspect` | `org.clojars.hglabplh/yatest-inspect` | Python inspection and JEP bridge |
| `modules/reflect` | `org.clojars.hglabplh/yatest-reflect` | Java/Clojure reflection, code generation, Scala/Kotlin experiments |
| `modules/productive` | `org.clojars.hglabplh/yatest-productive` | Contracts and production annotations |
| `modules/test` | `org.clojars.hglabplh/yatest-test` | Spying, mocking, macros and test framework annotations |

Each module uses `src/main/clojure`, `src/main/java`, `src/main/resources`,
`src/test/clojure`, `src/test/java`, and `src/test/resources`. Java packages and
Clojure namespaces retain their existing names. `production` Java annotations
belong to the productive library. Test framework code in `modules/test/src/main`
is published library code; its own tests live in `modules/test/src/test`.

`test-support/clojure` holds shared fixtures used only in development and tests.
The root `project.clj` supports a combined REPL/test classpath; its published
umbrella artifact depends on all four libraries. It does not bundle their code.
The test library depends on reflect. Productive uses reflect only for its tests.

## Requirements

- Java 17 or newer; Java output targets Java 17.
- Leiningen.
- Python/JEP runtime when actually using Python inspection.
- Optional Scala 3 and Kotlin compilers for the experimental polyglot sources.

## Build and Test

Install the sibling artifacts once, then run the whole project against local sources:

```sh
lein modules install
lein test
lein repl
```

Build each independent JAR, installing siblings in dependency order:

```sh
lein modules jar
```

Other whole-project tasks:

```sh
lein modules test
lein modules install
lein modules pom
lein modules clean
```

`modules test` stops at the first failure. For an individual library, run
`lein test` or `lein jar` inside its directory after installing siblings.
Development Java fixtures compile into `target/dev-classes`; published JARs
use `target/classes` and include only production resources and Clojure sources.
Generated POMs come from the corresponding `project.clj`; the old root POM is
not the source of truth. Run `lein pom` to regenerate the umbrella POM.

Optional Scala/Kotlin compilation, from `modules/reflect`:

```sh
lein with-profile +polyglot comp-sc3-kotlin
lein with-profile +polyglot jar
```

The polyglot profile adds the Scala/Kotlin runtime dependencies to the artifact.
Run the compilation command again after cleaning its generated classes.

## Clojars Packaging

All artifacts currently use version `1.4.0-SNAPSHOT`. Consumers can select one:

```clojure
[org.clojars.hglabplh/yatest-reflect "1.4.0-SNAPSHOT"]
```

Or retain the umbrella dependency:

```clojure
[org.clojars.hglabplh/YATestEnvironment "1.4.0-SNAPSHOT"]
```

Deployment uses `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` (a Clojars deploy token,
not an account password). See the [Clojars token documentation](https://github.com/clojars/clojars-web/wiki/Deploy-Tokens).
The account must have permission to publish in `org.clojars.hglabplh`.

After testing, publish the libraries in dependency order, then the umbrella:

```sh
lein modules deploy clojars
lein deploy clojars
```

No publishing happens during normal builds. Before a release, update the four
module versions and sibling references plus the root version and dependencies
in their `project.clj` files together. Existing experimental tests may reference
unimplemented namespaces; packaging does not implement those missing features.

## Artifact Test-Data Player

The artifact Player lives under:

```clojure
io.github.hglabplh_tech.test.suite.datagen.artifact.player
```

Example:

```clojure
(require '[io.github.hglabplh_tech.test.suite.datagen.artifact.player :as player])

(player/run!
 {:defaults {:seed 42}
  :storage {:type :file
            :config {:base-dir "target/generated-test-artifacts"}}
  :jobs [{:format :txt
          :count 2
          :hierarchy {:project "atlas"
                      :feature "document-import"
                      :subfeature "plain-text"}}
         {:format :relational
          :count 1
          :hierarchy {:project "customer-service"
                      :class "CustomerRepository"
                      :method "findActiveCustomers"}}]})
```

A sample EDN configuration is available at:

```text
modules/test/src/test/resources/testdatagen/generator.edn
```

## Generated Analysis Pipeline

The generated test-data pipeline can analyze Clojure namespaces and Java classes, generate parameter samples, render payloads, and persist the result to SQLite:

```clojure
(require '[io.github.hglabplh_tech.test.suite.datagen.pipeline :as pipeline])

(pipeline/analyze-files
 {:namespaces ['some.project.ns]
  :java-classes ["java.lang.String"]
  :db-path "target/test-data/generated-test-data.sqlite"})
```

Java method execution is not automatic in this pipeline yet because it needs a concrete instance or static invocation target.

## Documentation

- `FEATURES.md`: detailed feature inventory with completion status.
- `docs/ClojureTestingFrame.md`: Clojure testing, mocking, spying, and macro notes.
- `docs/Test-Methods.md`: testing-method overview.
- `docs/TestingAgainstEntropy.md`: entropy-oriented testing ideas.
- `docs/ClojureDocu.md`: Clojure documentation notes.
- `refl_comp_gen/README.md`: reflection compilation and generation notes.

## Funding

GitHub Sponsors metadata is configured in `.github/FUNDING.yml`, with additional project funding context in `.github/SPONSORSHIP.md`.

## License

YATestEnvironment is licensed under the MIT License.
