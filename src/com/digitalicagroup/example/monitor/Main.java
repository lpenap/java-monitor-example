package com.digitalicagroup.example.monitor;

import com.digitalicagroup.example.monitor.ui.MainWindowObserver;

public class Main {

	public static void main(String[] args) {

		// Ammount of consumer (threads) to be launched.
		int consumerCount = 9;
		
		// Time to wait after each consumption.
		int waitMillis = 500;

		// Instance our integer storage with some integers.
		IntegerStorage intStorage = IntegerStorage.instance(10, waitMillis);

		// Launch the notifier.
		StorageNotifier emptyStorageNotifier = new StorageNotifier(intStorage); 
		(new Thread(emptyStorageNotifier)).start();

		// create main interface
		MainWindowObserver window = new MainWindowObserver(consumerCount);
		window.addIntegerStorage(intStorage);
		window.addEmptyStorageNotifier(emptyStorageNotifier);
		window.activate();

		// Launch all consumer threads.
		for (int i = 0; i < consumerCount; i++) {
			IntegerConsumer consumer = new IntegerConsumer(intStorage, i);
			consumer.addObserver(window);
			(new Thread(consumer)).start();
		}

		// start the consuming process
		window.startSimulation();
	}
}
