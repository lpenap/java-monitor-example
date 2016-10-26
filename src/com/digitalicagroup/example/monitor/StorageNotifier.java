package com.digitalicagroup.example.monitor;

import java.util.Observable;

public class StorageNotifier extends Observable implements Runnable {

	private IntegerStorage intStorage;

	public StorageNotifier(IntegerStorage intStorage) {
		this.intStorage = intStorage;
	}

	@Override
	public void run() {
		try {
			intStorage.waitForAllThreads();
			setChanged();
			notifyObservers();
		} catch (InterruptedException ignored) {
		}
	}

}
