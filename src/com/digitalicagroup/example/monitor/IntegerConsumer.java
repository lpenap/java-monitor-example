package com.digitalicagroup.example.monitor;

public class IntegerConsumer implements Runnable {

	private IntegerStorage monitor;
	private int id;

	public IntegerConsumer(IntegerStorage monitor, int id) {
		this.monitor = monitor;
		this.id = id;
	}

	@Override
	public void run() {
		int consumed = monitor.consumeInt(this.getId());
		while (consumed > 0) {
			consumed = monitor.consumeInt(this.getId());
		}
	}

	public int getId() {
		return this.id;
	}

}
