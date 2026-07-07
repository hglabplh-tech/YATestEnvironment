# Features

YATestEnvironment is a Clojure and Java test automation environment. It combines test-support utilities, Java reflection helpers, Clojure function metadata analysis, generated test-data pipelines, and documentation around testing strategy.

## Core Purpose

- Provides a reusable test environment for automated tests.
- Supports test-data generation from Clojure function metadata and Java reflection data.
- Helps inspect Java classes, constructors, methods, fields, annotations, and special language forms.
- Provides Clojure-focused spying and mocking utilities for unit-test support.
- Includes testing-method documentation for unit testing, functional testing, performance testing, test-driven development, and entropy-oriented test design.

## Build and Runtime

- Leiningen project published as `org.clojars.hglabplh/YATestEnvironment`.
- Uses Clojure 1.12.3.
- Compiles Java sources with Java 17 source and target settings.
- Uses Ahead-of-Time compilation for project namespaces.
- Integrates dependencies for Active Data, Active Clojure, Reflections, JSON handling, JUnit Jupiter, Codox, and Javadoc generation.
- Provides Leiningen aliases for Java-oriented test/documentation workflows and combined test execution.
- Configures source, Java source, test, and resource paths for mixed Clojure/Java development.

## Java Test Annotations

- Defines a custom `@YATest` annotation for test methods and annotation types.
- Marks `@YATest` with JUnit Platform `@Testable`.
- Stores test metadata including category, test name, and implementation date.
- Provides test categories for unit, functional, smoke, black-box, and white-box tests.

## Clojure Function Mocking

- Provides metadata-driven function mocking through `prolog`, `mock`, and `restore-orig-funs`.
- Marks target functions with mock metadata before instrumentation.
- Rebinds namespace public vars while keeping original function references for restoration.
- Supports conditional mock rules with `call-cond->`.
- Supports rule matching by return predicate and parameter predicates.
- Includes built-in predicate keys for boolean, byte, char, collection, double, float, int, list, long, map, object, set, typed set, short, string, and vararg values.
- Provides mock actions for returning a value, throwing an exception, or doing nothing.
- Records mocked call flow as function name, argument values, and return value.

## Clojure Function Spying

- Provides function spying through `prolog` and `spy`.
- Marks target functions with spy metadata before instrumentation.
- Preserves original function execution while adding metadata and runtime recording.
- Captures call-flow information for instrumented functions.
- Captures stack-trace context to identify invocation locations.
- Structures function metadata for profiling and later test-data generation.

## Function Metadata and Active Data Support

- Reads function metadata such as namespace, name, file, line, column, argument lists, and schema information.
- Parses Active Data / realm schemas into return-type and argument metadata.
- Extracts argument names, schema types, concrete types, and optional flags.
- Provides helpers for inspecting Active Data realm structures.
- Handles realm forms including scalar, integer range, real range, union, intersection, sequence, set, map, enum, tuple, record, function, delayed, and named realms.
- Provides macros for extracting structured schema metadata from namespace interned functions and records.

## Test Data Generation Pipeline

- Loads Clojure source files and derives their declared namespace symbols.
- Analyzes public Clojure functions using function metadata and schema information.
- Analyzes Java classes and methods through the project reflection API.
- Normalizes Clojure functions and Java methods into a shared generated-test-data model.
- Infers parameter value categories such as boolean, string, integer, number, UUID, symbol, set, map, function, and generic values.
- Generates default sample values when no custom rule is provided.
- Supports generated cases per analyzed function or method.
- Can execute generated Clojure function cases and record them through the spy call-flow mechanism.

## Configurable Generation

- Provides default generation settings with JSON output and three samples per function.
- Reads XML generation configuration.
- Validates XML configuration before use.
- Supports configurable output formats.
- Supports per-function and per-parameter rules.
- Supports parameter type overrides, numeric minimum and maximum values, explicit value lists, and format hints.
- Includes an XSD resource for Java-oriented method/parameter rule structures.

## Generated Payload Formats

- Generates JSON payloads.
- Generates XML payloads.
- Generates CSV payloads.
- Generates text payloads.
- Generates Clojure string/printed data payloads.
- Includes simple PDF payload generation for compact test-case output.

## SQLite Persistence

- Creates and initializes a SQLite database for generated analysis results.
- Stores analyzed function rows with namespace, function name, source location, return type, and serialized analysis data.
- Stores analyzed parameter rows with position, name, type, and generated values.
- Stores generated test payloads by function, case index, and output format.
- Uses a default database path under `target/test-data/generated-test-data.sqlite`.

## Java Reflection API

- Provides Clojure wrappers for Java reflection utilities.
- Loads classes by canonical class name or existing `Class` object.
- Finds classes from package scanning.
- Retrieves constructors, public constructors, methods, public methods, fields, public fields, subclasses, public subclasses, annotations, interfaces, generic interfaces, superclasses, generic superclasses, enclosing classes, enclosing methods, and enclosing constructors.
- Retrieves members by name where supported.
- Supports annotation lookup by annotation type.
- Exposes direct access to the underlying reflected `Class`.

## Member and Type Inspection

- Inspects constructor names, parameter counts, parameter types, generic parameter types, exception types, generic exception types, modifiers, and declaring classes.
- Inspects method names, parameter counts, parameter types, generic parameter types, return types, generic return types, exception types, generic exception types, modifiers, and declaring classes.
- Inspects field names, field types, field modifiers, generic field types, and field annotations.
- Reports class, constructor, method, and field attributes in readable Clojure data.
- Detects type/member characteristics such as annotation, anonymous class, array, enum, interface, local class, member class, primitive type, sealed type, synthetic item, record, varargs, default method, bridge method, and enum constant.

## Annotation Utilities

- Reads annotations from classes, constructors, methods, fields, and method/constructor parameters.
- Retrieves annotation return types.
- Converts annotation information into Clojure data.
- Extracts annotation values from class methods and fields.
- Includes Java utility classes for lower-level annotation analysis.

## Reflection to Clojure Data

- Converts constructors into maps containing object name, general metadata, parameter annotations, annotations, generic exception types, and declaring class.
- Converts methods into maps containing object name, general metadata, generic parameter types, parameter annotations, annotations, generic return type, generic exception types, and declaring class.
- Converts fields into maps containing object name, general metadata, generic type, and annotations.
- Converts classes into maps containing class definition data and class body data.
- Recursively includes constructor, field, method, and nested class information in class body output.

## Special Java Form Analysis

- Analyzes enum classes.
- Analyzes Java records.
- Includes experimental lambda analysis.
- Includes switch statement and switch expression analysis.
- Stores special-form results in reflected class definition data.

## Class Compilation and Code Generation Foundation

- Compiles a class by canonical name into structured reflection data.
- Searches package paths for a class and compiles the discovered class.
- Defines AST-like data tables for class definitions, constructors, class bodies, and enum definitions.
- Provides code-generation hook points for classes, constructors, methods, fields, enums, records, and lambdas.
- Includes placeholder generator namespaces for JSON, XML, YAML, and Java/code generation.

## Static Code Analysis

- Registers namespaces for later reflection.
- Captures namespace imports, interns, refers, publics, and aliases.
- Represents namespace reflection data with Active Data records.
- Includes early support for finding public functions from reflected namespace data.

## Hygienic Macro Experimentation

- Defines syntax objects with datum, scopes, and source metadata.
- Implements pattern matching and syntax-rule style expansion helpers.
- Supports generated renamings for introduced symbols.
- Provides `syntax-case`, `hgp-syntax-rules`, `defhgp`, and `define-syntax` style forms.
- Includes tests and examples for the macro-system experiment.

## Examples and Test Fixtures

- Includes Java fixture classes for interfaces, abstract classes, implementations, annotations, enums, records, lambdas, switch behavior, and application-style examples.
- Includes Clojure fixture namespaces for function metadata, Active Data records, generated data, spying, mocking, and static analysis tests.
- Contains duplicated legacy/current test namespace layouts under `src/test/clojure` and `src/test/clj`.

## Documentation

- README states the project goal: a test framework with automated-test tools, test-data generation, and logging in different formats.
- `docs/ClojureTestingFrame.md` documents Clojure-specific mocking, spying, and macro background.
- `docs/Test-Methods.md` explains test-driven development, unit tests, functional tests, and performance tests.
- `docs/TestingAgainstEntropy.md` explains entropy-driven complexity in testing and motivates generated/statistical/formal test approaches.
- `docs/ClojureDocu.md` and generated documentation configuration support API documentation.
- Project configuration supports Codox and Javadoc output.

## Project Metadata

- Licensed under the MIT License.
- Includes a code of conduct.
- Configured with SCM and deployment metadata for Clojars.
- Includes source and Javadoc classifier configuration.
