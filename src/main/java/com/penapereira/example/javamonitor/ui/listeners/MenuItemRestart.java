package com.penapereira.example.javamonitor.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuItemRestart implements ActionListener {

	Runnable restartAction;

	public MenuItemRestart(Runnable restartAction) {
		this.restartAction = restartAction;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		restartAction.run();
	}

}
