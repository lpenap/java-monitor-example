package com.digitalicagroup.example.monitor;

import java.util.Observable;

public class IntegerStorage extends Observable {
	private static IntegerStorage _instance = null;
	private int consumableInts;
	private int waitMillis;

	private boolean started;

	protected IntegerStorage(int consumableInts, int waitMillis) {
		this.consumableInts = consumableInts;
		this.started = false;
		this.waitMillis = waitMillis;
	}

	public static IntegerStorage instance(int consumableInts, int waitMillis) {
		if (_instance == null) {
			_instance = new IntegerStorage(consumableInts, waitMillis);
		}
		return _instance;
	}

	public synchronized int consumeInt() throws InterruptedException {
		while (!started) {
			wait();
		}
		Thread.sleep(waitMillis);
		notifyAll();
		return consumableInts > 0 ? consumableInts-- : 0;
	}

	public synchronized void waitForAllThreads() throws InterruptedException {
		while (this.consumableInts > 0) {
			wait();
		}
	}

	public synchronized void setStarted(boolean started) {
		this.started = started;
		notifyAll();
	}
}
