## Test driven development (or do not drive through elsewhere to nowhere)

### What's the idea behind

The idea behind Test Driven Development is that of course tests are very imnportant to the quality and the comfort of a software. The method is not very old. In former times there was a running gag in the community of developers: Testing is something for cowards. 
Nowadays it is clear that the software gets more and more flexible and complex, The fact that most things run in parallel and there it is possible in cloud solutions is another reason to test software very intensive for being sure that the software is running even if there ara maybe 40 tasks running in parallel mode.

This leads to the conclusion that a software without tests will get useless.

Now one had the idea that the interfaces are one of the most important thing which ensures that the design is clear and clean, To get the coders , designers and all the ones who deal with the code in a mode of being forced to develop in a way that at first the interface has to be designed in a way all fits together the idea of writing tests (at first with empty stubs which define the interfaces and see if all things will work interlocked that means the return of the last step is exactly the thing the next step needs and defines.

And so the Test Driven Development was born.

#### How to realize this thoughts
To realize this thougths we have today test frameworks like **JUNIT** in Java or simply **deftest** in clojure.

Inside the different test methods which we are able to define in **JUNIT** with the **@Test** annotation the logic can be designed and here there at´re also the asserts if the last call was successful and delivers the correct input for the following task.

#### Example of such a test in Java

```Java
public class ATest extends BaseOfTests {

private Integer derfault = 110;
 /* CTOR*/
 public ATest() {
}

@Test
public aSpecificTest () 
{

		Integer result = myFirstMethod(this.default); /*here it seems that there is a addition of 86,,, but what the function does really is hidden for us and for sure in the begining it's empty*/
		String resultStr =  mySecondMethod (result);
  		assertThat(resultString is("196"));
}
}
 
```

And out of these tests stubs for the interface are created

```Java
public interface MyAppIfc {

	public Integer myFirstMethod (Integer input);

	public String mySecondMethod (Integer toConvert);
}

```

And the class implementing our great highly sophisticated interface ;-) may be look
as follows

```Java
public class MyRealApp implements MyAppIfc {

	@Override
	public Integer myFirstMethod (Integer input) {
			Integer result = (input + 86);
			return result;	
	}
	
	@Override
	public String mySecondMethod (Integer input) {
		return Integer.toString(input);
	}
}
```

And now we can test the thing out of the box for the test is already there.

The point is that in Test Driven Development we do not think at first about the technical details and afterwords wrap the tecnical details some kind of interface. No the opposite 
is the case: We think about which values are passed to a function and how the outcome must look like. Same for the used parameter - data types and the return value(s) datatype(s). We think about datastreams and the steps which work on them and each "worker" has well defined parameters and a well defined outcome for the specific values given as arguments. And NOW 
we can think about the technical details while we code inside a well defined interface.


## About automated test methods
### The different Test Methods in Test automation (UNIT -Test , Functional Test, Performance Functional Test)

Ok here is another point which is very important regarding automated testing:
In order to get the goal of a clean and correct code and functionality we must have a look on the program logic in different views. 
- We have a view where we look for a complete well defined transaction in the program and have to check wether input and output is correct. Here we use **Functional Tests.** The **Functional Tests** treat the methods called inside and the logic inside the borders of the transaction as a **BLACK BOX**. So it does not matter what the different functions classes or whatever process in what place even if the result is correct.

- The other view is more that we look at the atomic functions processing more or less complex logic. Here we also look for input / output of the functionality in that unit but we also keep in mind the internal logic and the functions which are called before / after and / or if third-party functionality isd called which can cause that we not really test our own logic / flow of control. These kind of tests are called **Unit Tests** 

- And now to the last view I will mention here where are for sure many more. We have to look how high the throughput of our application must be and if we reach that goal with our logic and the fact how we realized it in detail. These are **Performance Tests** 

 

#### UNIT Testing in an automated environment

So now our development team coded something and the specification is clear and clean transformed to code. Ups what shall we do now ? We have to test it and it gets more and more and more. The code is for quality reasons debugged by the developers but we need something more efficient. 

So we search for a test-framework where we are able to define Tests for the specific functions.
This test-framework can handle all these things if we use it right.

The advantage of this way testing things is that the tests are coded in Java / Clojure / Racket / Scheme48 / C / C++ and so on and executing them another time without code-change they will do the same and exactly the same.

This is one problem for manual testing. The Tester is a human being and we are full of mistakes in our doing. For manual tests we have later a look on it you have to create for each test a step by step list which shows up a detailed description for each step.

For **UNIT Tests** we also need a description what will happen step by step but we tell our coded test to do it for us thousands of times.

Now have a closer look on UNIT Testing in Java and Clojure. For Java the most popular framework will be **JUNIT**

**JUNIT** has been grown to a very large extensive test environment. 

- Ok but now first we need a function we are able to test so let's think about the following function:

As an algoritmic fuction example for testing we use the widely spreaded sorting algorithm Quick-Sort. I know this is a example of a very low level functionality which is covered already in the JDK since a long time e.g. : SortedList or the sort feature in collection streaming.
But I use it because it is a simple example.

```Java
public void quickSort(int arr[], int begin, int end) {
    if (begin < end) {
        int partitionIndex = partition(arr, begin, end);

        quickSort(arr, begin, partitionIndex-1);
        quickSort(arr, partitionIndex+1, end);
    }
}
```
**HINT for beginners: to get it running you need the partition method. For the beginners this algorithm is in APENDIX II***

So ok here we have now something to test.

What we need now is a testing framework containing assertation functions and annotations to annotate test methods. For this we use the most popular Java Testing framework **JUNIT**:

The most important annotations in **JUNIT** are the following:

All methods following such a annotation need to be ``public``

- **_@Test_** : annotate the test method as a method containing a test
- **_@Before_** : Execute before each test
- **_@After_** : Execute after each test

Here first a example of a very tiny test:

```Java

@Test
public void simpleQSortTest() {
		Integer[] theArrayToSort = {6, 7, 4, 2, 78, 45 ,1 ,5, 15};
		Integer[] expectedSorted = {1, 2, 4, 5, 6, 7, 15, 45, 78};
		quickSort(theArrayToSort, 0, (arrayToSort.length - 1));
		assertThat(arrayToSort, is(expectedSorted)); // if this assert fails quicksort does not
// work correctly
}
```

Here in this test the easiest case is tested. It is tested if the array given is sorted after quiccksort. The other cases like given wrong data by type e.g. Strings instead of Integer are not tested. In our case this won't work because the whole thing won't even compile in that casse. So to show how such a Test will work let us elaborete the whole function a bit.

Here is our new function which is much more flexible :

```Java

/**
* innerclass 
*
*/

public enum SortDirection {
	DESC,
	ASC,;
}


public <T>void quickSort(T arr[], SortDirection direction,, int begin, int end) {
 		quickSort(arr, direction, begin, partitionIndex-1);
        quickSort(arr, direction, partitionIndex+1, end);

}

/**
* Now we need to show also this function to see what happens
*
*/
private <T>int partition(T arr[], SortDirection direction, int begin, int end) {
    T pivot = arr[end];
    int i = (begin-1);

    for (int j = begin; j < end; j++) {

        if ((direction.equals(SortDirection.ASC) 
					&& arr[j].compareTo(pivot) <= 0)
		 || (direction.equals(SortDirection.DESC) 
					&& arr[j].compareTo(pivot) >= 0 )) {
            i++;

            int swapTemp = arr[i];
            arr[i] = arr[j];
            arr[j] = swapTemp;
        }
    }

    int swapTemp = arr[i+1];
    arr[i+1] = arr[end];
    arr[end] = swapTemp;

    return i+1;
}


```

And here are a few tests for it to see how **UNIT Testing** works:



```Java

```

Ok now we have some Tests checking random some aspects. To get a real good coverage when testing a function we first have to know it's specification that means what is the input and what is the output of a function we test. We also have to know what happens in the function when processing the input to get either the valid output or a clear error which really explains what is missing or wrong. The more parameters we have the more the complexity tests for the function grows. If we wwhen have a function with 5 parameters and each parameter has 15 combinations of values (The parameter may be a Object reference) we get a exponential growth of possibilities what would went wrong.

The main error by people who have less experience in Testing is to check the parameters compound in one test function . **_THIS IS A NOT TO DO_**

To get the complexity broken down we schould make tests where 4 parameters are in a good range and only one is out of range or has a value which does not fit the four others parameters values.

So you might argue what is with the combination where two parameters or three parameters have values which do not fit in combination. For this cases we can write some generic tests which walk via recursion through each valid combination. But do not test the case in that way where one or two parameters have a value out of their range which is defined by the functionality and which values and combination of values the function covers. 

Even if you plan to do test driven development this kind of proceeding gets more important for otherwise you do not know in the end in which case and why the function fails.

To be sure all works you have to write **UNIT Tests** for each public method in each class. The private methods are tested indirect. 
A bad style is (with only a very very few exceptions - should never happen) to access private functions for the reason of testing them via previlleged access. Ok this works in Java. But one thing has to be clear: 

For making good functional unit or other testing in a proper way it should never be neccessary to use algorithms which break the normal ways of coding. No good developer will design a program and the time he is ready use a function which accesses another function which is e.g. private and the language is not designed to do it in that way. If you see such code begin looking at the design of this part delete the code and rewrite it completely following the design in a clean way.

Why write it completely new. One reason is if you do after theese functions are written test driven developmwent for some parts such parts will lead you off the road of a clean design.


#### Functional Testing in an automated environment




#### How to get the Input for the functions to test

Ok now we face the problem that we need a high amount of different test data in 1..n and n..m combinations. 

Some people think. Why data I simply mock the input functionality. Ok so let's state one thing mocking makes **ONLY** sense if there is third party functionality which we do not like to be involved in **UNIT Testing** or functionality like a complicated file output which may fail so that we don't see in that case if the problem is the output function or our functionality. For functions we use and which belong to our project mocking is a very bad idea and may also lead to the fact that our logic is not tested in the right context and fails if it runs in the real environment.

So how can we get the test data:

###### Here is one approach

Let us think about reflecting the input and output of the function in case of Java with a low part of dynamic typing this approach works if we trace the classes static. 
Now we have for example one function with these prior mentioned input data:
We can simply check each type and by reflecting the type itself if it is a complex class we can also find out the range of the different values. The same can be done with the return parameter which is then the input for the next method / function / message 
If we have collected this informations throughot the whole project we can trace the data and find the test data inside the range for each constellation and each range. 
We are also able to generate for all of this test data which cover the out of range type and so on cases.

If this is not enough to get a good coverage may be in cause the program works very generic in many parts there is another possibility. We write code which wraps the function ibn a transparent way so that we cam collect data during runtime. The most important thing is to call afterwards the real function and give back its value calculated so that the flow of control does not get broken.

This seems to be simple but it is for sure very complex to write such code.

This leads us to the next part of this article. The Test data generator 
(a example in Clojure & Java) - this project:


 

#### APENDIX II

Here the additional stuff takes place 


 



