package com.digitalicagroup.example.monitor;

public class Main {

	public static void main(String[] args) {
		
		// Ammount of consumer (threads) to be launched.
		int consumerCount = 5;
		
		// Instance our integer storage with some integers.
		IntegerStorage intStorage = IntegerStorage.instance(30);

		// Launch the notifier.
		(new Thread(new StorageNotifier(intStorage))).start();

		// Launch all consumer threads.
		for (int i = 0; i < consumerCount; i++) {
			(new Thread(new IntegerConsumer(intStorage, i))).start();
		}

	}
}
