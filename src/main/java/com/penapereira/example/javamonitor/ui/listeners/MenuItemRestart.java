package com.penapereira.example.javamonitor.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MenuItemRestart implements ActionListener {

	JFrame parent;

	public MenuItemRestart(JFrame parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(parent, "Not yet implemented!\nPlease close the window\nand restart the app...\n\nIf you want to help with this\n,make a pull request\nand I will look into it.");
	}

}
