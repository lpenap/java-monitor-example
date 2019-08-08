package com.penapereira.example.javamonitor.consumer;

import com.penapereira.example.javamonitor.Constants;
import com.penapereira.example.javamonitor.exception.ForcedStopException;
import com.penapereira.example.javamonitor.monitor.IntegerStorageMonitor;
import com.penapereira.example.javamonitor.monitor.AbstractObservable;

public class IntegerConsumerImpl extends AbstractObservable implements IntegerConsumer {

	private IntegerStorageMonitor monitor;
	private int id;
	private boolean running;
	private int consumedInt;

	public IntegerConsumerImpl(IntegerStorageMonitor monitor, int id) {
		super();
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
			getSupport().firePropertyChange(Constants.STOP, running, !running);
			this.running = !running;
		} finally {
			removeAllListeners();
		}
	}

	private void processInteger(int consumed) throws InterruptedException {
		getSupport().firePropertyChange(Constants.CONSUMED, this.consumedInt, consumed);
		this.consumedInt = consumed;
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
