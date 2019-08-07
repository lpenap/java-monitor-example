package com.penapereira.example.javamonitor.monitor;

import java.util.Observable;

import com.penapereira.example.javamonitor.exception.ForcedStopException;

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
