package com.digitalicagroup.example.monitor.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;

public class MenuItemAbout implements ActionListener {
	JFrame parent;

	public MenuItemAbout(JFrame parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JTextPane text = new JTextPane();
		text.setEditable(false);
		text.setBackground(null);
		text.setBorder(null);
		text.setText("Java Monitor Example\n" + "by lpenap\n\n"
			+ "More info and documentation at:\n"
			+ "https://github.com/lpenap/java-monitor-example");
		JOptionPane.showMessageDialog(parent, text);
	}
}
