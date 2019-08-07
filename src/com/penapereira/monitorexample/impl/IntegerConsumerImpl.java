package com.penapereira.monitorexample.impl;

import java.util.Observable;

import com.penapereira.monitorexample.ForcedStopException;
import com.penapereira.monitorexample.IntegerConsumer;
import com.penapereira.monitorexample.IntegerStorageMonitor;

public class IntegerConsumerImpl extends Observable implements IntegerConsumer {

	private IntegerStorageMonitor monitor;
	private int id;
	private boolean running;

	public IntegerConsumerImpl(IntegerStorageMonitor monitor, int id) {
		this.monitor = monitor;
		this.id = id;
		this.running = true;
	}

	@Override
	public void run() {
		try {
			while (running && monitor.hasIntegers()) {
				processInteger(monitor.consumeInt());
			}
		} catch (InterruptedException ignored) {
		} catch (ForcedStopException forcedStopException) {
			setChanged();
			notifyObservers(forcedStopException);
		} finally {
			deleteObservers();
		}
	}

	private void processInteger(int consumed) throws InterruptedException {
		setChanged();
		notifyObservers(consumed);
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public void terminate() {
		this.running = false;
	}
}
