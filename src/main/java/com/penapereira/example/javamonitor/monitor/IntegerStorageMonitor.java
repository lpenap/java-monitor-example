package com.penapereira.example.javamonitor.monitor;

import com.penapereira.example.javamonitor.exception.ForcedStopException;

public interface IntegerStorageMonitor {

	int consumeInt() throws InterruptedException, ForcedStopException;

	void waitForAllThreads() throws InterruptedException, ForcedStopException;

	void setStarted(boolean started);

	void forceStop();
	
	boolean hasIntegers();

}