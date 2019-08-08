package com.penapereira.example.javamonitor.monitor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public interface Observable {

	PropertyChangeSupport getSupport();

	void addPropertyChangeListener(PropertyChangeListener pcl);

	void removePropertyChangeListener(PropertyChangeListener pcl);

}