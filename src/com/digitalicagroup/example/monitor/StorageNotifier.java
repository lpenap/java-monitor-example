package com.digitalicagroup.example.monitor;

public class StorageNotifier implements Runnable {

	private IntegerStorage intStorage;

	public StorageNotifier(IntegerStorage intStorage) {
		this.intStorage = intStorage;
	}

	@Override
	public void run() {
		try {
			intStorage.waitForAllThreads();
			System.out.println("All integers consumed!");
		} catch (InterruptedException ignored) {
		}
	}

}
