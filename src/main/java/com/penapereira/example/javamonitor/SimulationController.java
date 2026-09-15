package com.penapereira.example.javamonitor;

import java.util.ArrayList;
import java.util.List;

import com.penapereira.example.javamonitor.consumer.IntegerConsumer;
import com.penapereira.example.javamonitor.consumer.IntegerConsumerImpl;
import com.penapereira.example.javamonitor.monitor.EmptyIntegerStorageNotifier;
import com.penapereira.example.javamonitor.monitor.IntegerStorageMonitor;
import com.penapereira.example.javamonitor.monitor.IntegerStorageMonitorImpl;
import com.penapereira.example.javamonitor.ui.UIManager;

public class SimulationController {
	private static SimulationController _uniqueInstance = null;

	protected static final long THREAD_JOIN_TIMEOUT_MILLIS = 2000;

	protected int consumersQuantity;

	protected int integersToConsume;

	protected int simulationStepMillis;

	private UIManager userInterface;

	EmptyIntegerStorageNotifier emptyStorageNotifier;

	IntegerStorageMonitor intStorage;

	List<IntegerConsumer> consumers;

	List<Thread> threads;

	public static SimulationController instance() {
		if (_uniqueInstance == null) {
			_uniqueInstance = new SimulationController();
		}
		return _uniqueInstance;
	}

	protected SimulationController() {
	}

	public int getConsumersQuantity() {
		return consumersQuantity;
	}

	public void setConsumersQuantity(int consumersQuantity) {
		this.consumersQuantity = consumersQuantity;
	}

	public int getIntegersToConsume() {
		return integersToConsume;
	}

	public void setIntegersToConsume(int integersToConsume) {
		this.integersToConsume = integersToConsume;
	}

	public int getSimulationStepMillis() {
		return simulationStepMillis;
	}

	public void setSimulationStepMillis(int simulationStepMillis) {
		this.simulationStepMillis = simulationStepMillis;
	}

	public void initialize(UIManager userInterface) {
		// Create and show the main interface.
		this.userInterface = userInterface;
		this.userInterface.activate();

		launchSimulation();
	}

	private void launchSimulation() {
		// Instantiate our integer storage with some integers.
		intStorage = IntegerStorageMonitorImpl.instance(integersToConsume, simulationStepMillis);
		threads = new ArrayList<>();

		// Launch the notifier, observed by the main interface.
		emptyStorageNotifier = new EmptyIntegerStorageNotifier(intStorage);
		emptyStorageNotifier.addPropertyChangeListener(userInterface);
		launchThread(emptyStorageNotifier);

		// Launch all consumer threads, observed by the main interface.
		consumers = new ArrayList<>();
		for (int i = 0; i < consumersQuantity; i++) {
			IntegerConsumer consumer = new IntegerConsumerImpl(intStorage, i);
			consumer.addPropertyChangeListener(userInterface);
			consumers.add(consumer);
			launchThread(consumer);
		}
	}

	private void launchThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		threads.add(thread);
		thread.start();
	}

	public void startSimulation() {
		if (userInterface.startSimulation()) {
			intStorage.setStarted(true);
		}
	}

	public void stopSimulation() {
		if (consumers == null) {
			return;
		}
		// 1. Detach the interface so that no stale event reaches it.
		emptyStorageNotifier.removePropertyChangeListener(userInterface);
		for (IntegerConsumer consumer : consumers) {
			consumer.removePropertyChangeListener(userInterface);
			// 2. Ask every consumer to leave its loop.
			consumer.terminate();
		}
		// 3. Abort any Thread.sleep() or wait() in progress. This must come
		// before forceStop(): a sleeping consumer holds the monitor lock, and
		// forceStop() would otherwise block here until every sleep expires.
		for (Thread thread : threads) {
			thread.interrupt();
		}
		// 4. Wake up any thread still parked in wait().
		intStorage.forceStop();
		// 5. Return only once the old run is gone.
		for (Thread thread : threads) {
			joinQuietly(thread);
		}
	}

	private void joinQuietly(Thread thread) {
		try {
			thread.join(THREAD_JOIN_TIMEOUT_MILLIS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void restartSimulation() {
		if (consumers == null) {
			return;
		}
		stopSimulation();
		IntegerStorageMonitorImpl.reset();
		userInterface.reset();
		launchSimulation();
		intStorage.setStarted(true);
	}
}
