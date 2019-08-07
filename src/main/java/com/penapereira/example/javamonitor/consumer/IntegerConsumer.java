package com.penapereira.example.javamonitor.consumer;

import java.util.Observer;

public interface IntegerConsumer extends Runnable {

	void run();

	int getId();

	void terminate();

	void addObserver(Observer o);

}