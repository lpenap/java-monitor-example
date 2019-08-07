package com.penapereira.monitorexample;

public interface IntegerStorageMonitor {

	int consumeInt() throws InterruptedException, ForcedStopException;

	void waitForAllThreads() throws InterruptedException, ForcedStopException;

	void setStarted(boolean started);

	void forceStop();
	
	boolean hasIntegers();

}