package com.digitalicagroup.example.monitor.ui;

import java.util.Observer;

import com.digitalicagroup.example.monitor.IntegerStorage;

public interface OutputManager extends Observer {

	public void addIntegerStorage(IntegerStorage storage);

	public void activate();

	public void startSimulation();

}
