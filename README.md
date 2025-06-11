[![CircleCI](https://dl.circleci.com/status-badge/img/circleci/2e6Hm8LHAzuw5AQRrJLyG6/UoewhgzBXwCsmVqmb3sy5o/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/circleci/2e6Hm8LHAzuw5AQRrJLyG6/UoewhgzBXwCsmVqmb3sy5o/tree/master)
[![GitHub release](https://img.shields.io/github/release/lpenap/java-monitor-example)](//github.com/lpenap/java-monitor-example/releases/latest)
[![codebeat badge](https://codebeat.co/badges/07bc5a39-cd80-49b7-a006-431ee69f53e5)](https://codebeat.co/projects/github-com-lpenap-java-monitor-example)

# Java Monitor Example
Basic example of how to implement the [monitor synchronization construct](https://en.wikipedia.org/wiki/Monitor_(synchronization))  to allow mutual exclusion in threads.

This is a common construct taught in computer science, and we will cover it's implementation using Java language while rendering a basic graphic interface to represent the simulation being done.

## Quickstart

#### Requirements
* JDK 11 

Clone and run project with:
```bash
git clone git@github.com:lpenap/java-monitor-example.git
cd java-monitor-example
./mvnw spring-boot:run
```
Running the project should render a graphic interface with a visual
representation of this simulation:
* The main window shows a number of random-colored panels.
* The window will *observe* each *observable* `IntegerConsumerImpl` and will update the matching panel with the corresponding consumed integer.
* The window will also observe an `EmptyIntegerStorageNotifier` to detect when all the integers are consumed.


## Example coverage
This basic example covers several Java topics:
- [x] Singleton pattern implementation.
- [x] Monitor synchronization mechanism for mutual exclusion.
- [x] Runnable implementation and Threads launching.
- [x] Thread Waiting and Signaling.
- [x] Observer pattern implementation in java using [PropertyChangeListener](https://docs.oracle.com/en/java/javase/11/docs/api/java.desktop/java/beans/PropertyChangeListener.html).

### Problem
A number of threads wants to consume data from the same place, and each piece of data can be consumed just once. Hence we need to implement a mutual exclusion mechanism, or a Monitor in this case.

### This Example
We have a finite quantity of integers available to be consumed (15 by default) by a certain amount of threads (9 by default). Also we would like to have another thread to notify us when all integers are consumed. In order to do that, let's take a look at the classes available:
* **IntegerStorageMonitorImpl**, will have the integers to be consumed and accessing methods. This is our synchronization monitor.
* **IntegerConsumerImpl**, The implementation of our consumer runnables that will be launched as threads. These will compete to acquire and consume integers from the monitor.
* **EmptyIntegerStorageNotifier**, will notify us when all integers are consumed.
* **SimulationController**, Will get the storage reference and launch the notifier and consumers.


## Topics
All source code should be self explanatory, so for each topic described here, please refer to the class in question. If you are not sure where to start, start reading the `JavaThreadsMonitorExampleApplication` class since the project is made with springboot.

### Singleton pattern implementation
We would like all the consumer (threads) to access our integers from the same source, hence, we implement our integer storage as Singleton.
The Singleton pattern will have a non-public constructor and a global access method to the Singleton's instance. In that way we will ensure, that only one instance of our integer storage class will be referenced.
This is covered by the `IntegerStorageMonitorImpl` class.

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

This is implemented by the `IntegerStorageMonitorImpl` class.

### Runnables and threads
The basic way to implement multithreading in Java is to implement the [Runnable](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Runnable.html) interface:
* We implement the Runnable interface and implement the logic in its `run()` method.
* We instantiate the [Thread](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Thread.html) class and pass an instance of our runnable to it. See `SimulationController` class.
* We call the `start` method of our thread class in order to **asynchronously** start the thread.
* Our code in the Runnable's `run()` method will be executed.
* Please note, that each thread is responsible for finishing in a clean way. Threads caught in loops or with unfinished conditions, will certainly create a memory leak. Assigning `null` to a thread reference will not get rid of the thread and it will not get caught by the GC.

See the class `IntegerConsumerImpl` for a runnable reference implementation and the class `SimulationController` to check how to instantiate and launch runnables.

### Thread waiting and signaling
We have two types of threads:
* `IntegerConsumerImpl`: Will fight for the Monitor's lock and consume each integer.
* `EmptyIntegerStorageNotifier`: Will check if there are pending integers to be consumed and wait (It will fight for the Monitor's lock every time it needs to check the storage)

To implement this, we need to code the following behavior:

* An `EmptyIntegerStorageNotifier` thread instance will enter a synchronized method in our monitor, hence, acquiring a lock.
* It will check if all integers were already consumed.
  * If there is no more integers available, then we will exit the monitor.
  * If there is more integers, it will wait. See `IntegerStorageMonitorImpl.waitForAllIntegersToBeConsumed()`.
* Other threads consuming the integers will need to notify our sleeping thread after each consuming operation.
  * The `notifyAll()` will wake all waiting threads and they will compete to acquire a lock over the monitor to resume executing code inside the monitor.
  
### Observer/Observable implementation
The java implementation of the [Observer](https://en.wikipedia.org/wiki/Observer_pattern) pattern is quite easy. We need first to design which classes will be *observable* and which classes will be the *observer(s)*. In this example:
* Our user interface will be the general observer: It needs to "observe" the consumer threads in order to update the visuals, and the storage notifier thread in order to detect when the simulation is finished.
* The observable work is implemented in `AbstractObservable` using the `PropertyChangeListener` where the observable must maintain an internal reference of a `PropertyChangeSupport` instace and use it to fire notifications when a property changes:
```java
class MyObservable {
	protected PropertyChangeSupport support;

	public MyObservable() {
		this.support = new PropertyChangeSupport(this);
	}

	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		support.addPropertyChangeListener(pcl);
	}

	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		support.removePropertyChangeListener(pcl);
	}

	...

	public someValueChanged() {
		...
		support.firePropertyChange("propertyName", oldValue, newValue);
	}
}
```
Then the Observer will need to implement the `PropertyChangeListener` interface:
```java
class MyObserver implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();

		if ("propertyName".equals(propertyName)) {
			//new "int" value
			int newValue = (int)event.getNewValue();

		}
	}
}
```
And finally, we need to register the listener in the observable:
```java
MyObservable observable = new MyObservable();
MyObserver observer = new MyObserver();

observable.addPropertyChangeListener(observer);

```
