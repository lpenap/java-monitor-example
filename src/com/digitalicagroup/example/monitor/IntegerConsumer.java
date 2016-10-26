package com.digitalicagroup.example.monitor;

import java.util.Observable;

public class IntegerConsumer extends Observable implements Runnable {

	private IntegerStorage monitor;
	private int id;

	public IntegerConsumer(IntegerStorage monitor, int id) {
		this.monitor = monitor;
		this.id = id;
	}

	@Override
	public void run() {
		try {
			int consumed = 0;
			while ((consumed = monitor.consumeInt()) > 0) {
				processInteger(consumed);
			}
		} catch (InterruptedException ignored) {
		}
	}

	private void processInteger(int consumed) throws InterruptedException {
		setChanged();
		notifyObservers(consumed);
	}

	public int getId() {
		return this.id;
	}

}
