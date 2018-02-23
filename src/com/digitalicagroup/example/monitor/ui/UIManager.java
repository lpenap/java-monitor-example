package com.digitalicagroup.example.monitor.ui;

import java.util.Observer;

public interface UIManager extends Observer {

	public void activate();

	public boolean startSimulation();

}
