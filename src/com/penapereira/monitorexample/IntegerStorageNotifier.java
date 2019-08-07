package com.penapereira.monitorexample;

import java.util.Observable;

public class IntegerStorageNotifier extends Observable implements Runnable {

	private IntegerStorageMonitor intStorage;

	public IntegerStorageNotifier(IntegerStorageMonitor intStorage) {
		this.intStorage = intStorage;
	}

	@Override
	public void run() {
		try {
			intStorage.waitForAllThreads();
			setChanged();
			notifyObservers();
		} catch (InterruptedException ignored) {
		} catch (ForcedStopException ignored) {
		} finally {
			deleteObservers();
		}
	}
}
