package com.digitalicagroup.example.monitor;

public class IntegerStorage {
	private static IntegerStorage _instance = null;
	private int consumableInts;

	protected IntegerStorage(int consumableInts) {
		this.consumableInts = consumableInts;
	}

	public static IntegerStorage instance(int consumableInts) {
		if (_instance == null) {
			_instance = new IntegerStorage(consumableInts);
		}
		return _instance;
	}

	public synchronized int consumeInt(int id) {
		if (this.consumableInts > 0) {
			System.out.println("Consumer " + id + " consuming int: " + consumableInts);
			notifyAll();
			return this.consumableInts--;
		} else {
			return 0;
		}
	}

	public synchronized void waitForAllThreads() throws InterruptedException {
		while (this.consumableInts > 0) {
			wait();
		}
	}
}
