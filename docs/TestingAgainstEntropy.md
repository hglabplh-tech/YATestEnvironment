### The very secure RESTful API or the reason why you should never use it without strong testing

In a world where everybody uses the internet for fun for business for payment method storing and 
of course many other things where security is a must, we are still facing a lot of security issues.
One of the most common security issues is the lack of testing.

Mrs. Brown submits a request to the API with a valid token and parameters that seem to be valid if you look separated at each parameter.
But the parameters in combination have a high entropy for the combinations result in having
numerous possibilities to end up in a chaos. 
Of course the parameters are not really random but the combination of them is so complex 
that you are not able to test all possibilities. 

So you might say "Mrs. Brown is a bad user, she should think about what can happen".
But hey we invented and designed this RESTful service in the back of the virtuell mall 
so we should make sure that it is secure and well tested.

And now Houston we have a problem. Let's see how the WebService call looks like to 
see the dimensions of the problem.

#### Our little service call and the possible responses

First of all we need to get a selections of let us say different products.
In our case Mrs. Brown searches a dishwasher with a price lower 
than 1000 and a weight lower than 100.

Let's have a look on the internal data of the dishwashers:

The product Code is a unique identifier for the product.
:- Result: The type is a string that describes the type of the product.
:- Query Parameter: The price is a number that describes the price of the product.
:- Query Parameter: The weight is a number that describes the weight of the product.
:- Result: The product description is a string that describes the product.
:- Result: The rezension is a number that describes the rezension of the product.

O how nice is this we have only two paramerters that we 
need to pass to the service.
Ok that is enough for the first step. Now we need to get the products that 
match our criteria. The service call looks like this:

The first parameter is the price of the product and the
Definition in our case is: price < 1000
second parameter is the weight of the product.
Definition in our is weight < 100

Since price is not always a integer we need a floating point number with a precision
of two after the decimal point. The weight is always an integer, since it is rounded 
to the next full kilogram.

First we have a protocol that is used to communicate with the service.
Either HTTP or HTTPS. In most cases on a shoping website it has to be HTTPS 
so our first test is if the service is available over HTTPS. And if the HTTP protocol supports
a redirect to HTTPS.
The second parameter is the URL of the service. In our case it is: https://api.shop.com
The third parameter is the method of the request.
The fourth parameter is the list of parameters that we need to pass to the service.
In our case we hve two parameters: price and weight. The fifth parameter 
is the expected rsponse code in our case HTTP/OK(200).
For both parameters a range is given so we can test if the service returns a
list of products that match the range of the price AND the range of the weight.
The service is called with the parameters price and weight as follows:
https://api.shop.com/products:443?price=10-999.99&weight=10-99 
The lower limits are set because we assume that a dishwasher
with a price of less than 10 Euro and less than 10 kilogram is not plausible. 
This assumtion was done by the coder so the fiors test is if assumtion is realistic
but not only today but also in the future when dishwashers may be get more lightweight 
and cheaper. So we as quality team have to do a reality check for the 
lower limits and upper limits.
This can be for sure done by asking a worldwide database of all dishwashers
and check if the limits are realistic. But do we have such a database (for sure not for 
dishwashers ;-). So we have to do a reality check by testing the 
assumptions made by specialists who are able to design and produce dishwashers may be in that case
we find out that the query of Mrs. Brown is not realistic and the search result will in each case be 0.
But if we find out that the limits are realistic 
then we can be sure that the query is realistic and stop we have a database in our shop with all
kinds of dishwasher which are ac´vailable in the shop. So we can be sure that the query is 
realistic and stop.

Now we have a cartesian product space of all possible combinations of the query parameters.
This means:
f( f("price", < 1000) x f("weight", < 100) 
That means in real life taking our limits we have a price between 10,00 Euro and 999.99 
Euro and a weight between 0.01 and 99.99 kg. So we have a cartesian product space of 
999.99 x 99.99 = 99.980.011 combinations.
For each of this combination the query has to get the valid results.
Good luck for us in our case we have not to make this query for each combination. We can
give the query a list of all possible combinations and the query will return 
the list of products faster.
In relational algebra we have the concept of a join. The join is a very powerful concept.
The join without condition results in the cartesian product. The join with a condition results 
in the intersection of the two tables. so it is not select all from table1 and table2 but
select all from table1 and table2 where table1.id = table2.id.
In our case the first table looks like this:
Product-Code Product-Type Product-Name Price
The second table holds the technical data like weight functionality and so on.
The join over this tables is :
```sql
select * from product, technical_data where product.product_code = technical_data.product_code
and product.price between 10.00 and 99.99
and technical_data.weight between 10 and 99.99
```
Now you will think what is the problem with this query to the WebContainer. 
The database is for sure tested and the parameters are checked. 
But the database accepts the query only with the datatypes which fit 
to the database schema.
So for the price this will be a float and for the weight a integer.
Or on a host for the price a PIC'9999999.99 and for the weight PIC'99999'.
That means the String we get with the parameter has to be converted via type cast
or a conversion function to the datatype of the database.Since coders are human here will be a mistake so wee have 
to make sure that the parameter is in the right format after conversion and 
that no overflow occurs and that the precision is right.
The same is true for all of the parameters.
The problem is that the database is not the only place where we have to check the data.

So now we have the following conditions must be met:
- One mistake is the selection of the right protocol the right port and the right host.
- One possible mistake is the parameter setting of the query parameters
- One possible mistake is the correct range of the parameter values.
- Last not least the correct character translation e.g. URLEncode  
- One possible mistake is the selection of the right database.
- One possible mistake is the selection of the right schema.
- One possible mistake is the selection of the right table.
- One possible mistake is that the data is in the right format.

I am sure there are mor possible mistakes but I think this is a good start.
You see the mistakes if viewed separated from each other but if you combine 
them all together you get a lot of different combinations.

And this increases the value of entropy the entropy of the data and logic 
is increased.
The entropy is system inherently unstable and the system is prone to errors.

Here the definition how to find out the entropy of a system:

A wise man once said: "The entropy of a system is the number of possible
mistakes that can be made in the system."
and
"The increase of disorder or entropy is what distinguishes 
the past from the future, giving a direction to time." 
(Stephen Hawking)
Ups what has this to do with testing and the complexity of a system?
Well, the more possible mistakes that can be made in a system, 
the more complex the system is.
Or other way round the more complex a system is the more possible mistakes.
that can be made in the system.

There is a formula for calculating the entropy of a system:

H(X)=−
i=1
∑
n
​
p(x
i
​
)log
2
​
(p(x
i
​
))

- _**H(X)**_ is the entropy of the random variable X. (Our variable X is the system (to be exact it is the RESTful service of the system))
- _**p(xᵢ)**_ is the probability of the event xᵢ. (distinct by the probability of the event xᵢ (the combinations given by the input data and output data
- and the kind of processing))
- _**log₂**_ is the logarithm base 2.(for calculating the entropy)

The thing is that the number of parameters and result values as ell as the range and datatype of each parameter
iven if the parameter is a structure or object (compound data - here the object itself 
has parameters and result values) and the primitive datatypes have a huge range 
of  numbers, characters, enumeration values and more this all together ends not only in 
multiplication of the combinations and the probabilities 
of the events may be leading to a high entropy it potentiates the 
entropy even more.

And this is the reason why we need to test against entropy. And to test it in a complex system
we cannot test even each combination for in a middleware ECM system we have to test each combination of the 
input data and output data and the kind of processing and this will lead to a trillion of 
combinations and this is not possible to test all of them for in that case a normal UNIX
workstation will take 100 years to test all of them. So we need to test against entropy
via other methods like statistical methods and other methods like inductive  
like formal methods and other. 
One language with which we can test against entropy is Python  
and we can use the Python language to develop 
a program that can check the correctness of a given function.
What also helps in the fight against entropy is the use of a random number generator
to test the different ranges an the limitations.
to test the correctness of a given function or to define test data out 
of the range definitions and possible as well as plausible combinations
of the input data and the output data and the kind of processing. 
The induktive method can be used to test the correctness of a given 
function or to define test data out of the range.

Another way e.g. in Racket or Clojure is to have schema data which not only define the types of input and output 
parameters but also defines the range of a variable or compound data 
in dependency to the other parameters given.
In active-data (Active Group (Mike Sperber and more)) and other schema libs 
this approach is used to define the range of a variable 
and we can easily add the definition of ranges 
in dependency to the other parameters given.

Another approach will be to use Agda which is a formal proof language. Working with a type system which is strongly 
bound to the logic processing the data. This leads to the fact that it is an effective
language to check if  a function is well defined.

This is all for now and I hope it helps to understand why such articles and knowledge about testing 
methods product life cycle and software quality is immense important.

I beg you if you like to become a test developer for 
test automation and test data generation as well as developing test plans , test cases 
or managing a test team to read the articles here more than once.
For I like to be honest since I was for over twenty years in 
each role regarding testing a product 
and I know what I am talking about.

- Harald G.P. IT-Consulting / Project Support
- 03.05.1966 Computer Sience since 1992

- &copy; Harald Glab-Plhak (2024)




