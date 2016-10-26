# Java Monitor Example
Basic example of how to implement the [monitor](https://en.wikipedia.org/wiki/Monitor_%28synchronization%29) synchronization construct to allow mutual exclusion in threads.

This is a common construct taught in computer science, and we will cover it's implementation using Java language while rendering a basic graphic interface to represent the simulation being done.

## Example coverage
This basic example covers several Java topics:
- [x] Singleton pattern implementation.
- [x] Monitor synchronization mechanism for mutual exclusion.
- [x] Runnable implementation and Threads launching.
- [x] Thread Waiting and Signaling.
- [x] Observer/Observable java implementation.

## Problem
A number of threads wants to consume data from the same place, and each piece of data can be consumed just once. Hence we need to implement a mutual exclusion mechanism, or a Monitor in this case.

## This Example
We have a finite quantity of integers available to be consumed by a certain amount of threads. Also we would like to have another thread to notify us when all integers are consumed. In order to do that, let's take a look at the classes available:
* **IntegerStorage**, will have the integers to be consumed and accessing methods.
* **IntegerConsumer**, The implementation of our consumer runnables that will be launched as threads.
* **StorageNotifier**, will notify us when all integers are consumed.
* **Main**, Executable class. Will get the storage reference and launch the notifier and consumers.

#### Requirements
* (Optional) Eclipse to import and run the project.
* Java 8 SE from Oracle (Not tested with others JVM but it should work).

## Topics
All source code should be self explanatory, so for each topic described here, please refer to the class in question. If you are not sure where to start, start reading the `Main` class.

### Singleton pattern implementation
We would like all the consumer (threads) to access our integers from the same source, hence, we implement our integer storage as Singleton.
The Singleton pattern will have a non-public constructor and a global access method to the Singleton's instance. In that way we will ensure, that only one instance of our integer storage class will be referenced.
This is covered by the `IntegerStorage` class.

### Monitor synchronization and mutual exclusion
Implementing a monitor construct in Java is very straightforward. The conditions that we need to meet are:
* Only one thread could access a single method exclusively.
* Other threads that would like to access that same method will block and wait for the method to be *free* again.
* After a thread is finished using the method, all blocked methods will compete to access the method and only one will win the exclusive access.

Java have a simple way to implement this, and it is accomplished using `synchronized` methods:
* If we add the `synchronized` [keyword](https://docs.oracle.com/javase/tutorial/essential/concurrency/syncmeth.html) to a method (other than constructors), the entire set of synchronized methods in a class will become a Monitor.
* The first thread to access a synchronized method in the Monitor will acquire a lock over the entire set of synchronized methods.
* Other threads wishing to access other synchronized methods (or the same one) will block until the first thread finish accessing the method.
* All threads that wants to access a synchronized method will compete to gain a new lock (including the thread that just released the lock). The JVM will decide which thread enters (it is nondeterministic).

This is implemented by the `IntegerStorage` class.

### Runnables and threads
The basic way to implement multithreading in Java is to implement the [Runnable](https://docs.oracle.com/javase/8/docs/api/java/lang/Runnable.html) interface:
* We implement the Runnable interface and implement the logic in its `run()` method.
* We instantiate the [Thread](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html) class and pass an instance of our runnable to it. See `Main` class.
* We call the `start` method of our thread class in order to **asynchronously** start the thread.
* Our code in the Runnable's `run()` method will be executed.
* Please note, that each thread is responsible for finish in a clean way. Threads caught in loops or with unfinished conditions, will certainly create a memory leak. Assigning `null` to a thread reference will not get rid of the thread and it will not get caught by the GC.

See the class `IntegerConsumer` for a runnable reference implementation and the class `Main` to check how to instantiate and launch runnables.

### Thread waiting and signaling
We have two types of threads:
* IntegerConsumer: Will fight for the Monitor's lock and consume each integer.
* StorageNotifier: Will check if there are pending integers to be consumed and wait (It will fight for the Monitor's lock too every time it needs to check the storage)

To implement this, we need to code the following behavior:

* A `StorageNotifer` thread instance will enter a synchronized method in our monitor, hence, acquiring a lock.
* It will check if all integers were already consumed.
  * If there is no more integers available, then we notify with a message `All integers consumed!`
  * If there is more integers, it will wait. See `IntegerStorage.waitForAllThreads()`.
* Other threads consuming the integers will need to notify our sleep thread after each consuming operation.
  * The `notifyAll()` will wake all waiting threads (we have only one, our StorageNotifier instance) and they will compete to acquire a lock over the monitor to resume executing code inside the monitor.
  
### Observer/Observable implementation
The java implementation of the [Observer](https://en.wikipedia.org/wiki/Observer_pattern) pattern is quite easy. We need first to design which classes will be *observable* and which classes will be the *observer(s)*. In this example:
* Instances of `IntegerConsumer` will be *observable* because our UI needs to update the panels with the integers being consumed.
* Extend the `Observable` class: 
```java
public class IntegerConsumer extends Observable implements Runnable {
```
  * Notify the observers of an action being taken:
```java
setChanged();
notifyObservers(consumed);
```
* Our `MainWindowObserver` will be the *observer* for each `IntegerConsumer` instance to reflect the changes in the UI, so we implement the `Observer` interface. That prompts us to code an additional method to take action every time an *observable* instance notify its observers.
```java
public void update(Observable o, Object arg) {
}
```
* The final step is to bind all together, we need to register our `MainWindowObserver` instance into each `IntegerConsumer` instance:
```java
MainWindowObserver window = new MainWindowObserver(consumerCount);
. . .
IntegerConsumer consumer = new IntegerConsumer(intStorage, i);
consumer.addObserver(window);
```
  

## Launching
You can clone the git repository and Import the project using Eclipse.
Running the `Main` class should render a graphic interface with a visual
representation of this simulation:
* The main window shows a number of random-colored panels.
* The window will *observe* each *observable* `IntegerConsumer` and will update the matching panel with the corresponding consumed integer.

