package com.penapereira.example.javamonitor.consumer;

import com.penapereira.example.javamonitor.monitor.Observable;

public interface IntegerConsumer extends Runnable, Observable {

	void run();

	int getId();

	void terminate();

}