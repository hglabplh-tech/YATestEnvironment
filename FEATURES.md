# Features

YATestEnvironment is a Clojure and Java test automation environment. It combines reflection APIs, function metadata analysis, generated test-data pipelines, spying and mocking utilities, and early code-generation research.

## Status Legend

- **Available**: implemented and exercised by repository tests or smoke tests.
- **In progress**: wired into the project, but still needs API hardening, dependency activation, or broader test coverage.
- **Experimental**: research/prototype code that should not yet be treated as a stable public API.

## Core Purpose

**Available**

- Provides reusable building blocks for automated tests.
- Supports generated test data from Clojure function metadata and Java reflection data.
- Helps inspect Java classes, constructors, methods, fields, annotations, generic types, modifiers, and selected special language forms.
- Provides Clojure-focused spying and mocking utilities for unit-test support.
- Includes documentation for unit testing, functional testing, performance testing, test-driven development, and entropy-oriented test design.

## Build and Runtime

**Available**

- Leiningen project published as `org.clojars.hglabplh/YATestEnvironment`.
- Uses Clojure 1.12.3.
- Compiles Java sources with Java 17 source and target settings.
- Uses Ahead-of-Time compilation for project namespaces.
- Integrates dependencies for Active Data, Active Clojure, Reflections, JSON handling, Cheshire, JUnit Jupiter, Codox, Javadoc, and JEP-based Python inspection.
- Provides aliases for combined Clojure tests, Java test compilation, Codox, and Javadoc workflows.
- Configures source, Java source, test, and resource paths for mixed Clojure/Java development.
- Provides source and javadoc classifier configuration.

## Java Test Annotations

**Available**

- Defines a custom `@YATest` annotation for test methods and annotation types.
- Marks `@YATest` with JUnit Platform `@Testable`.
- Stores test metadata including category, test name, and implementation date.
- Provides test categories for unit, functional, smoke, black-box, and white-box tests.

## Java Reflection API

**Available**

- Loads classes by canonical class name or existing `Class` object.
- Finds classes from package scanning.
- Retrieves constructors, public constructors, methods, public methods, fields, public fields, subclasses, public subclasses, annotations, interfaces, generic interfaces, superclasses, generic superclasses, enclosing classes, enclosing methods, and enclosing constructors.
- Retrieves members by name where supported.
- Supports annotation lookup by annotation type.
- Exposes direct access to the underlying reflected `Class`.
- Provides Clojure wrappers around Java utility classes for reflection operations.

## Member and Type Inspection

**Available**

- Inspects constructor names, parameter counts, parameter types, generic parameter types, exception types, generic exception types, modifiers, and declaring classes.
- Inspects method names, parameter counts, parameter types, generic parameter types, return types, generic return types, exception types, generic exception types, modifiers, and declaring classes.
- Inspects field names, field types, field modifiers, generic field types, and field annotations.
- Detects type/member characteristics such as annotation, anonymous class, array, enum, interface, local class, member class, primitive type, sealed type, synthetic item, record, varargs, default method, bridge method, enum constant, and synthetic field.
- Converts type and member data into readable Clojure values.

## Annotation Utilities

**Available**

- Reads annotations from classes, constructors, methods, fields, and method/constructor parameters.
- Retrieves annotation return types.
- Converts annotation information into Clojure data.
- Extracts annotation values from annotation methods and fields.
- Includes Java utility classes for lower-level annotation analysis.

## Reflection to Clojure Data

**Available**

- Converts constructors into maps containing object name, general metadata, parameter annotations, annotations, generic exception types, and declaring class.
- Converts methods into maps containing object name, general metadata, generic parameter types, parameter annotations, annotations, generic return type, generic exception types, and declaring class.
- Converts fields into maps containing object name, general metadata, generic type, and annotations.
- Converts classes into maps containing class definition data and class body data.
- Recursively includes constructor, field, method, and nested-class information in class body output.

## Special Java Form Analysis

**Available**

- Analyzes enum classes and exposes enum metadata.
- Analyzes Java records and exposes record component information.

**In progress**

- Captures lambda-related structures such as functional fields, functional method parameters, constructors, and capture fields.
- Captures switch-related structures such as detected switch methods, descriptors, return types, parameter types, opcodes, and counts.
- Special-form coverage is useful for smoke checks and generated structure, but lambda and switch analysis still need more complete semantic coverage.

## Clojure Function Metadata and Active Data Support

**Available**

- Reads function metadata such as namespace, name, file, line, column, argument lists, and schema information.
- Parses Active Data / realm schemas into return-type and argument metadata.
- Extracts argument names, schema types, concrete types, and optional flags.
- Provides helpers for inspecting Active Data realm structures.
- Handles realm forms including scalar, integer range, real range, union, intersection, sequence, set, map, enum, tuple, record, function, delayed, and named realms.

**In progress**

- Some Active Data parsing branches are marked with FIXME comments and need cleanup around edge cases and realm predicate handling.

## Clojure Function Spying

**Available**

- Provides function spying through `prolog` and `spy`.
- Marks target functions with spy metadata before instrumentation.
- Preserves original function execution while adding metadata and runtime recording.
- Captures call-flow information for instrumented functions.
- Captures stack-trace context to identify invocation locations.
- Structures function metadata for profiling and later test-data generation.

## Clojure Function Mocking

**Available**

- Provides metadata-driven function mocking through `prolog`, `mock`, and `restore-orig-funs`.
- Marks target functions with mock metadata before instrumentation.
- Rebinds namespace public vars while keeping original function references for restoration.
- Supports conditional mock rules with `call-cond->`.
- Supports rule matching by return predicate and parameter predicates.
- Includes built-in predicate keys for boolean, byte, char, collection, double, float, int, list, long, map, object, set, typed set, short, string, and vararg values.
- Provides mock actions for returning a value, throwing an exception, or doing nothing.
- Records mocked call flow as function name, argument values, and return value.

## Generated Test-Data Pipeline

**Available**

- Loads Clojure source files and derives their declared namespace symbols.
- Analyzes public Clojure functions using function metadata and schema information.
- Analyzes Java classes and methods through the project reflection API.
- Normalizes Clojure functions and Java methods into a shared generated-test-data model.
- Infers parameter value categories such as boolean, string, integer, number, UUID, symbol, set, map, function, and generic values.
- Generates default sample values when no custom rule is provided.
- Supports generated cases per analyzed function or method.
- Generates payloads per configured output format.
- Can execute generated Clojure function cases and record them through the spy call-flow mechanism.
- Persists generated analyses and payloads to SQLite.

**In progress**

- Java method execution from generated cases is not automatic yet because it needs an instance/static invocation target.

## Configurable Generation

**Available**

- Provides default generation settings with JSON output and three samples per function.
- Reads XML generation configuration.
- Validates XML configuration before use.
- Supports configurable output formats.
- Supports per-function and per-parameter rules.
- Supports parameter type overrides, numeric minimum and maximum values, explicit value lists, and format hints.
- Includes an XSD resource for Java-oriented method/parameter rule structures.
- Reads and validates EDN artifact-generation configuration for Player-based artifact jobs.
- Includes a sample EDN artifact-generation configuration under `src/test/resources/testdatagen/generator.edn`.

## Generated Payload Formats

**Available**

- Generates JSON payloads.
- Generates XML payloads.
- Generates CSV payloads.
- Generates text payloads.
- Generates Clojure string/printed data payloads.
- Includes compact PDF-style payload generation for generated test-case output.
- Generates standalone artifact payloads for TXT, PDF, DOCX, JPEG, TIFF where ImageIO supports it, and relational EDN rows.
- Creates portable DOCX artifacts through JDK ZIP/OOXML generation without introducing a new runtime dependency.

## Artifact Test-Data Player

**Available**

- Provides a `Player` protocol and default `TestDataPlayer` implementation.
- Processes configured jobs by walking jobs and sample sequences.
- Calls format generators through a multimethod-based artifact generator.
- Supports `project/feature/subfeature` and `project/class/method` hierarchy concepts for generated artifact paths.
- Stores artifacts through memory and filesystem storage backends.
- Returns generated artifact references and storage summaries.

**In progress**

- PostgreSQL storage is wired through JDBC and validates schema/table creation, but it requires the PostgreSQL JDBC driver and database configuration at runtime.
- TIFF generation depends on an available `ImageIO` TIFF writer in the runtime environment.

## Persistence

**Available**

- Creates and initializes a SQLite database for generated analysis results.
- Stores analyzed function rows with namespace, function name, source location, return type, and serialized analysis data.
- Stores analyzed parameter rows with position, name, type, and generated values.
- Stores generated test payloads by function, case index, and output format.
- Uses a default database path under `target/test-data/generated-test-data.sqlite`.
- Provides artifact storage through memory and filesystem backends.

**In progress**

- Provides JDBC-backed PostgreSQL artifact storage as an optional runtime feature.

## Class Compilation and Code Generation

**Available**

- Compiles a Java class by canonical name into structured reflection data.
- Searches package paths for a class and compiles the discovered class.
- Defines AST-like data tables for class definitions, constructors, class bodies, and enum definitions.
- Provides a registry of hook points for classes, constructors, methods, fields, enums, records, lambdas, switches, and final emission.
- Includes JSON, XML, YAML, and Java-like generator namespaces.

**In progress**

- The hook-based generator walks compiled class data and dispatches to the active registry.
- The JSON, XML, YAML, and Java-like outputs are useful as generated structural output, but they should still be treated as a foundation rather than a complete source-code generator.
- Some namespace/path compatibility needs consolidation between older and newer reflection/code generation namespaces.

## Static Code Analysis

**In progress**

- Registers namespaces for later reflection.
- Captures namespace interns, publics, and related namespace data.
- Represents namespace reflection data with Active Data records.
- Includes early support for finding public functions from reflected namespace data.

## Python Inspection

**In progress**

- Provides a Clojure wrapper around `InspectPythonCode`.
- Includes a Python inspector resource.
- Depends on JEP/runtime setup and needs more integration tests before being treated as stable.

## Hygienic Macro Experimentation

**Experimental**

- Defines syntax objects with datum, scopes, and source metadata.
- Implements pattern matching and syntax-rule style expansion helpers.
- Supports generated renamings for introduced symbols.
- Provides `syntax-case`, `hgp-syntax-rules`, `defhgp`, and `define-syntax` style forms.
- Includes tests and examples for the macro-system experiment.

## Examples and Test Fixtures

**Available**

- Includes Java fixture classes for interfaces, abstract classes, implementations, annotations, enums, records, lambdas, switch behavior, and application-style examples.
- Includes Clojure fixture namespaces for function metadata, Active Data records, generated data, spying, mocking, and static analysis tests.
- Contains both Java-oriented and Clojure-oriented reflection namespace layouts for compatibility and migration.

## Documentation

**Available**

- `README.md` summarizes purpose, status, setup, and usage examples.
- `docs/ClojureTestingFrame.md` documents Clojure-specific mocking, spying, and macro background.
- `docs/Test-Methods.md` explains test-driven development, unit tests, functional tests, and performance tests.
- `docs/TestingAgainstEntropy.md` explains entropy-driven complexity in testing and motivates generated/statistical/formal test approaches.
- `docs/ClojureDocu.md` and generated documentation configuration support API documentation.
- `refl_comp_gen/README.md` documents reflection compilation and generation notes.

## Project Metadata

**Available**

- Licensed under the MIT License.
- Includes a code of conduct.
- Configured with SCM and deployment metadata for Clojars.
- Includes GitHub Sponsors funding metadata and sponsorship context.
