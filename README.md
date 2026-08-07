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
- Leiningen, Codox, Javadoc, source classifier, and javadoc classifier project configuration.

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

- `src/main/clojure/io/github/hglabplh_tech/reflect`: Clojure wrappers and data conversion around Java reflection and code generation.
- `src/main/clojure/io/github/hglabplh_tech/test/suite`: spying, mocking, generated test data, static analysis, and macro experiments.
- `src/main/java/io/github/hglabplh_tech/reflect`: Java reflection utility classes used by the Clojure API.
- `src/main/java/io/github/hglabplh_tech/tests/framework/annots`: custom Java test annotation support.
- `src/main/java/io/github/hglabplh_tech/python`: Java bridge for Python inspection.
- `src/main/resources`: XML schema and Python inspector resources.
- `src/test/clojure` and `src/test/java`: Clojure and Java fixtures plus tests.
- `docs`: testing-method and project design notes.
- `refl_comp_gen`: notes for reflection compilation and generation.

## Requirements

- Java 17.
- Leiningen.
- Clojure dependencies are resolved through `project.clj`.
- Optional: PostgreSQL JDBC driver and a PostgreSQL database when using the PostgreSQL artifact storage backend.
- Optional: working Python/JEP runtime when using Python inspection.

## Build and Test

Run the Clojure test suite:

```sh
lein test
```

Run the combined alias for Clojure tests, Java test compilation, Codox, and Javadoc generation:

```sh
lein all-tests
```

Build source and javadoc classifiers through the configured Leiningen tasks:

```sh
lein jar
```

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
src/test/resources/testdatagen/generator.edn
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
