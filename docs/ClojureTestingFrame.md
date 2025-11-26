## Testing the thing many people do not like to think about ;-)

### Test helpers / Code Quality



#### Mock feature for Clojure

There is for sure a mocking feature available on the JVM (Mockito) but this toolkit is written in a way to support Java correctly and in a way that Object Orientation is supported. The mocking feature presented here is for the needs of functional programming in Clojure. Ok there are many things overlapping you may say but the problem here is that Mockito mocks classes. Classes are used by Clojure to define interfaces, functions, complex datatypes and so on. The main problem here is that one function (lambda in clojure) may have many underlying classes in there definition. The solution is to redefine the function bindings and to setup features what has to be done for different conditions added as atttributes to the mock. The other thing is to collect statistics and to have functionality to deal with them.

**_BUT PAY ATTENTION_**

To mock a function means that in that case the normal logic is **NOT** tested.
Mocking a function has only to be used to get rid of side effects coming up when using third-party software (e.g. EJB in Java (e.g. JBOSS) or storing data with Hadoop, Centera....). This third-party projects may cause side-effects not relevant for the processing of your function if you do **UNIT-Testing** . Mocking is a thing **ONLY** for **UNIT-Testing** in functional testing or even system testing mockinf functions will lead to a **Software Logic DISASTER** the results in real life product tests will stay unpredictable.

(To quote Mike Sperber: If you need to mock your own logic when something in your design is wrong. You have to rework you design in a way that mocking is then no more neccessary)

**RESUME: Do _NOT_ use _MOCKING_ for functionality under your _CONTROL_** 

Why do I support mocking if you only have to use it under certain conditions. The problem is that we have on the one hand a clean and proper design of a functional application where mocking is normally not needed and should not be neccessary if nothing is wrong with your design on the other hand we face sometimes the problem that our customer who has his own mindset does not want to give up his old components so that we can redesign it in a proper way. And here maybe we need mocking to separate our new design which we like to UNIT test from his components which have dependencies to EJB or other components which can be only included in a UNIT test with a very large effort and with big fat frameworks like SPRING is one to get them tested and if these frameworks are used we probably face the problem that they do not work without difficulties and dependencies to more and more other components. These things are ok for functional testing where we want to see if our overall design and functionality works. But in UNIT testing we like to test simply the logic in small UNITS functions abnd here it is not so good to have such dependencies for UNIT testing is some kind of "Development / Developer Testing" which has to make sure that the isolated logic of one function is running in a proper way. 

**AND AGAIN: If you need mocks in a functional design which is without dependencies to third party like EJB there is something wrong with your design. For if you have to mock your own function it is not designed in a proper way**




#### A example of "functional mocking"

- Create a mock of a function in our example it is the function **_read-document_**

```clojure


(prolog to-mock  read-document)

;; simple function mocking the function is completely mocked

 (mock  hgp.cljito.spy-and-mock.mocking-jay-test) ;; give the namespace 
;; the fun to mock is given by prolog 


```

The mock itself is of course senseless if the programmer does not tell what has to be happened in the case of different parameter values / types. 
Here another macro **_call-cond->_** takes place.
For this need there are some definitions with which the behaviour can be defined 

```clojure
(clojure.core/defn any-boolean? [value]...)
(clojure.core/defn any-byte? [value]...)
(clojure.core/defn any-char? [value]...)
(clojure.core/defn any-collection? [value]...)
(clojure.core/defn any-double? [value]...)
(clojure.core/defn any-float? [value]...)
(clojure.core/defn any-int? [value]...)
(clojure.core/defn any-list? [value]...)
(clojure.core/defn any-long? [value]...)
(clojure.core/defn any-map? [value]...)

(clojure.core/defn any-object? [value]...)
(clojure.core/defn any-set? [value]...)
(clojure.core/defn any-set-of? [value klass]...)
(clojure.core/defn any-short? [value]...)
(clojure.core/defn any-String? [value]...)
(clojure.core/defn any-vararg? [value]...)

;; create mapping for functions
;; Meta data
(def type-predicate-map
  {:any-boolean?-key    any-boolean?
   :any-byte?-key       any-byte?
   :any-char?-key       any-char?
   :any-collection?-key any-collection?
   :any-double?-key     any-double?
   :any-float?-key      any-float?
   :any-int?-key        any-int?
   :any-list?-key       any-list?
   :any-long?-key       any-long?
   :any-map?-key        any-map?
   :any-object?-key     any-object? 
   :any-set?-key        any-set?
   :any-set-of?-key     any-set-of?
   :any-short?-key      any-short?
   :any-string?-key     any-string?
   :any-vararg?-key     any-vararg?
   })

```

First a simple example of **_call-cond->_**

```clojure
(call-cond-> i-am-a-fake-fun-store
                :when
                	:any-boolean?-key :<-
               	    [[:any-int?-key :$] [:any-int?-key :$] [:any-int?-key :$]
                   [:any-set-of?-key :$ integer?]]
                 	[return-val 5]
                 :else
                 	[do-nothing])
```

#### Spy feature for Clojure

Spying in counterpart to mocking is a complete other thing but it should not be used in functional / system testing. The spy functionality is designed for making something like a monitoring e.g.: How often and in which places a function is called. What are the parameter data types of the input and what data type has the output. In this way we are able to profile the function in a way that we know for example the possible input for each parameter and the parameters in combination which can help to define a test data generation by the definition of the function. This is only one way to use the spy functionality. The spy functionalitiy should not be used in functional testing for it will extensively slow down execution. The functionality should be used to define test data to see the coverage by giving the data to a additional component or something other

In each case the original function is called without manipulation so that the flow of control and so on is the one we defined. 

- Create a spy _'HOOK'_ in our example it is the function **_read-document_**

```clojure
(prolog to-spy read-document)
 (spy  hgp.cljito.spy-and-mock.mocking-jay-test) ;; give the namespace 
;; the fun to spy is given by prolog 

```
- What is executed after setting up a function for spy



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
