package com.penapereira.example.javamonitor.monitor;

import java.util.Observable;

import com.penapereira.example.javamonitor.exception.ForcedStopException;

public class IntegerStorageMonitorImpl extends Observable
	implements IntegerStorageMonitor {
	private static IntegerStorageMonitor _instance = null;
	private int consumableInts;
	private int waitMillis;

	private boolean started;

	private boolean forceStop;

	public int FORCE_STOP_VALUE = -99;

	protected IntegerStorageMonitorImpl(int consumableInts, int waitMillis) {
		this.consumableInts = consumableInts;
		this.started = false;
		this.forceStop = false;
		this.waitMillis = waitMillis;
	}

	public static IntegerStorageMonitor instance(int consumableInts,
		int waitMillis) {
		if (_instance == null) {
			_instance = new IntegerStorageMonitorImpl(consumableInts,
				waitMillis);
		}
		return _instance;
	}

	@Override
	public synchronized int consumeInt()
		throws InterruptedException, ForcedStopException {
		while (!started) {
			wait();
			if (forceStop) {
				throw new ForcedStopException();
			}
		}
		Thread.sleep(waitMillis);
		notifyAll();
		System.out.println("returning, current count " + consumableInts);
		return consumableInts > 0 ? consumableInts-- : 0;
	}

	@Override
	public synchronized void waitForAllThreads()
		throws InterruptedException, ForcedStopException {
		while (this.consumableInts > 0) {
			wait();
			System.out.println("notifier: " + consumableInts);
			if (forceStop) {
				throw new ForcedStopException();
			}
		}
	}

	@Override
	public synchronized void setStarted(boolean started) {
		this.started = started;
		notifyAll();
	}

	@Override
	public void forceStop() {
		forceStop = true;
		notifyAll();
	}

	@Override
	public boolean hasIntegers() {
		return consumableInts > 0;
	}
}
