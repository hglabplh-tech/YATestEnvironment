## YET ANOTHER book ABOUT Clojure functional programming

Searching for some hints and help and advice for getting into designing and coding with clojure I often faced the problem that I had to search on more than one website to find out what a specific function does or how the evaluation and the other things work. 

So after a few months of surfing and surfing and 'google' I decided to make a try to put the knoledge I saw in the Internet to one place.

#### The first thing is to explain what a kind of language Clojure is:

Clojure is a language running on JVM based on Java Byte Code. But this is te only connection between Clojure and Java. Clojure is based on the idea of List processing first implemented in the language LISP. 
And it is also based on ideas from Scheme which is itself a "Lisp dialect" - LISP in its origin is a pure functional language. There are only a few things in Clojure which are non functional like e.g.: set bang (```clojure set!```) or the ``` (do...)``` macro which is for sequential processing of more than one function call.
  


### Appendix

## Clojure Macros for the test framework

#### Background

In rackets scheme (ChezScheme) or Scheme48  and other Scheme Interpreters the macros are hygienic that means the scope of the variables and so on used in the macros ends at their border. The other significant point is that the operands designed for macros can be only used in:

```scheme
(define-syntax... )
```
and friends.

In Clojure the macros are non-hygienic and the macro specific statements like **`@~** can also be used in (defn / defn- or even in an fn block.

This is somehow hard to understand because Clojure stated that they like to make it easier to get used to code in a Lisp dialect and on the other side define-syntax and co. are much clearer and easier to use.
In Clojure Macros can be coded `"hygienic"` if a coder uses gensym or var-name# 


This is extremly anoying.

#### The special syntax in macros as well as special features

- ```when-let```: set up bindings
- ```@```: deref de-reference the following expression
- ```#```: reader
- ```~```: evaluate one specific expression
- `: - quote for fully qualified symbol
- ```~@```; evaluate the content of (the dereferenced content)
- ```'```:  quote for simple symbol
- ```var```: get a java class for a expression... 
- ```ref```: reference 
