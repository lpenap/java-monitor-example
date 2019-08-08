package com.penapereira.example.javamonitor.monitor;

import com.penapereira.example.javamonitor.Constants;
import com.penapereira.example.javamonitor.exception.ForcedStopException;

public class EmptyIntegerStorageNotifier extends AbstractObservable implements Runnable {

	private IntegerStorageMonitor intStorage;

	public EmptyIntegerStorageNotifier(IntegerStorageMonitor intStorage) {
		super();
		this.intStorage = intStorage;
	}

	@Override
	public void run() {
		try {
			intStorage.waitForAllIntegersToBeConsumed();
			getSupport().firePropertyChange(Constants.FINISHED, false, true);
		} catch (InterruptedException ignored) {
		} catch (ForcedStopException ignored) {
		} finally {
			removeAllListeners();
		}
	}
}
