# LibThriftClient
a little、 light and simple thrift client library, for java,android,etc..

# Who need it
people use [thrift](https://thrift.apache.org/)  protocol, a RCP selection.
<ul>
<li> want a litte interface jar,such as android use.</li>
<li> the server has many iterface methods,but you or your customer just need little some of these.</li>
</ul>
# How to use
if you have a .thrift file like this:
```c
 struct Person{
   1:string name;
   2:i32 age;
 }
 service HelloService{
   String sayHi(1:Person aPerson)throws(1:HelloException ex);
   ... other methods
 }
 exception HelloException{
  1:i32 errorCode,
  2:string errorDesc;
 }
 ...
```

you can write a Iterface and a JavaBean like this:

```java
public interface MyService{// the name of this Iterface is random as you like
   // you could only have a single method,but the server has manay methods.
   // in this way,you can hide interface to your Customer
   String sayHi(Person p) throws HelloException;
   // the ParameterName of every parameter is same as it in the .thrift file
}
public class Person{
   public @Index(1) String name;
   public @Index(2) int age;
   ... optional Getters and Setter
}
```

Invoke example:
```java
TProtocol iprot =....
MyService clientIface = ClientInterfaceFactory.getClientInterface(MyService.class,iprot);
Person liming = new Person();
liming.name="LiMing";
String response = clientIface.sayHi(liming);
System.out.println(response);

```
