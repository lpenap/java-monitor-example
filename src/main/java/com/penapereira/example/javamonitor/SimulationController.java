package com.penapereira.example.javamonitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.penapereira.example.javamonitor.consumer.IntegerConsumer;
import com.penapereira.example.javamonitor.consumer.IntegerConsumerImpl;
import com.penapereira.example.javamonitor.monitor.IntegerStorageMonitor;
import com.penapereira.example.javamonitor.monitor.IntegerStorageMonitorImpl;
import com.penapereira.example.javamonitor.monitor.EmptyIntegerStorageNotifier;
import com.penapereira.example.javamonitor.ui.UIManager;

public class SimulationController {
	private static SimulationController _uniqueInstance = null;

	protected int consumersQuantity;

	protected int integersToConsume;

	protected int simulationStepMillis;

	private UIManager userInterface;

	EmptyIntegerStorageNotifier emptyStorageNotifier;

	IntegerStorageMonitor intStorage;

	List<IntegerConsumer> consumers;

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
                // Instantiate our integer storage with some integers.
		intStorage = IntegerStorageMonitorImpl.instance(integersToConsume, simulationStepMillis);

		// Launch the notifier.
		emptyStorageNotifier = new EmptyIntegerStorageNotifier(intStorage);
		(new Thread(emptyStorageNotifier)).start();

		// create main interface
		this.userInterface = userInterface;
		this.userInterface.activate();

		// Register main interface as an observer on the storage notifier
		emptyStorageNotifier.addPropertyChangeListener(this.userInterface);

		// Launch all consumer threads.
		consumers = new ArrayList<>();
		for (int i = 0; i < consumersQuantity; i++) {
			IntegerConsumer consumer = new IntegerConsumerImpl(intStorage, i);
			consumer.addPropertyChangeListener(this.userInterface);
			consumers.add(consumer);
			(new Thread(consumer)).start();
		}
	}

	public void startSimulation() {
		if (userInterface.startSimulation()) {
			intStorage.setStarted(true);
		}
	}

	public void stopSimulation() {
		Iterator<IntegerConsumer> i = consumers.iterator();
		while (i.hasNext()) {
			IntegerConsumer consumer = i.next();
			consumer.terminate();
		}
	}
}
