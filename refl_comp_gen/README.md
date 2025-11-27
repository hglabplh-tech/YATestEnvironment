## !!! THIS PROJECT IS CURRENTLY RESTRUCTURED AND IS NOT RUNNABLE AT THE MOMENT
## ABOUT TO BE RUNNABLE AGAIN WITH BUMPED VERSION


# clojure.new.api.reflect - An alternative reflection API for Clojure
 Java Reflection in Clojure to get Classes of a package load classes dynamically and take 
 advantage of the new reflection API since JDK 17 written based on openjdk 17.

The project has been made to make a full reflection of a class including annotations class attributes methods fields enumeratioons 
records and what is in work is a special reflection of lambda methods.

The project has the functions low level as well as a compiler compiling the Java Class reflected
to a clojure map structure which is used later by a generater (working with callback hooks 
to generate code data or whatever you want) and this is later used by another project to generate test data as well eas tests
with additional information from project specification.

But it can also simply be used as a reflection framework with low level functions for reflecting a Java 17 
class.
 
Information about the interfaces and methods in the project:
[Project Documentation](https://hglabplh-tech.github.io/IE.New.Clojure-reflect/clojdoc/index.html)

**_REMARK_** This API is inspired by the API for reflection in Clojure and by the API's 
in [Java Classpath](https://github.com/clojure/java.classpath) but I needed reflection in another way and I also liked to support Java 17 
which is not natively supported by Clojure up to now. If possible and if the developers are interested I like to contribute it. 
I just d´started this process.

The classpath.clj is developed by
;; by Stuart Sierra, [Stuart Sierry Web](http://stuartsierra.com/)
;; April 19, 2009


The hooks for the generator call-backs are:
:class-def-hook
:class-body-gen-hook
:ctor-gen-hook
:method-gen-hook
:field-gen-hook
:enum-gen-hook
:record-gen-hook
:lambda-gen-hook