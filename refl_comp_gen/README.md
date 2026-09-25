# Clojure.new.api.reflect - An alternative reflection API for Clojure, reflecting Clojure, Java, Kotlin, Scala, Python

 Java Reflection in Clojure to get Classes of a package, load classes dynamically, and take 
 advantage of the new reflection API since JDK 17, written based on OpenJDK 17.

The project has been made to make a full reflection of a class, including annotations, class attributes, methods, fields, enumerations 
records, and it also supports reflection on lambda methods.

The project has low-level functions as well as a compiler that compiles the reflected Java class
to a Clojure map structure, which is used later by a generator (working with callback hooks 
to generate code data or whatever you want) and this is later used by another project to generate test data as well as tests
with additional information from project specification.

But it can also simply be used as a reflection framework with low-level functions for reflecting a Java 17 
class.
 
Information about the interfaces and methods in the project:
[Project Documentation](https://hglabplh-tech.github.io/yatestenv/index.html)

**_REMARK:_** This API is inspired by the API for reflection in Clojure and by the API's 
in [Java Classpath](https://github.com/clojure/java.classpath), but I needed reflection in another way, and I also liked to support Java 17 
which Clojure doesn't natively support yet. If possible, and if the developers are interested, I would like to contribute. 
I just started this process.

The classpath.clj is developed by Stuart Sierra, [Stuart Sierra Web](http://stuartsierra.com/), April 19, 2009

### **_CHANGE IT HERE_**

## How the output of reflection is generated

The base structure is defined as follows

The hooks for the generator callbacks are in keywords:
- :class-def-hook
- :class-body-gen-hook
- :ctor-gen-hook
- :method-gen-hook
- :field-gen-hook
- :enum-gen-hook
- :record-gen-hook
- :lambda-gen-hook
- :annotation-hook

The hooks are the same for each supported language. The language may have additional hooks. They specify add-ons, such as special types or constructions in Scala, Kotlin, Clojure, or Python.

1. Scala 



### Description of the unified output

#### The base


1. class/namespace
- attributes
	- extends
	- implements
	- access-level (private, public)

- members 
	- member-fields
	- static-fields
	- static- initializer
	- member-methods
	- static-methods
	- enums
	- lambdas
	- class (classifier inner)
	- constructors
- annotations
2. Globals/Functions/Variables

	