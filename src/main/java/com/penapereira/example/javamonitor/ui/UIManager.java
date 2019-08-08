package com.penapereira.example.javamonitor.ui;

import java.beans.PropertyChangeListener;

public interface UIManager extends PropertyChangeListener {

	public void activate();

	public boolean startSimulation();

}
