package com.digitalicagroup.example.monitor.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.digitalicagroup.example.monitor.IntegerConsumer;
import com.digitalicagroup.example.monitor.IntegerStorage;
import com.digitalicagroup.example.monitor.StorageNotifier;

public class MainWindowObserver implements OutputManager {

	private JFrame frame;

	private List<JPanel> panels;

	private int threadsQuantity;

	private Random random;

	private IntegerStorage storage;

	private JLabel lastLabel;

	private StorageNotifier emptyStorageNotifier;

	/**
	 * Create the application.
	 */
	public MainWindowObserver(int threadsQuantity) {
		this.threadsQuantity = threadsQuantity;
		this.random = new Random();
		this.lastLabel = null;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(3, 3, 2, 2));

		panels = new ArrayList<JPanel>(this.threadsQuantity);

		for (int i = 0; i < threadsQuantity; i++) {
			// Building a Pane
			JPanel panel = new JPanel();
			panel.setMaximumSize(new Dimension(50, 50));
			Color background = getRandomColor();
			Color foreground = getContrastColor(background);
			panel.setBackground(background);
			frame.getContentPane().add(panel);

			// Building a label for the Pane
			JLabel label = new JLabel(" ");
			label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 32));
			label.setForeground(foreground);
			panel.setLayout(new GridBagLayout());
			panel.add(label);

			panels.add(panel);
		}
	}

	protected Color getRandomColor() {
		return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}

	protected Color getContrastColor(Color color) {
		int octet = 0;
		double luminance = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
		if (luminance < 0.5) {
			octet = 0;
		} else {
			octet = 255;
		}
		return new Color(octet, octet, octet);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == emptyStorageNotifier) {
			JOptionPane.showMessageDialog(frame, "All Integers Consumed!");
			System.out.println("All Integers Consumed!");
		} else {
			updatePanel(o, arg);
		}
	}

	protected synchronized void updatePanel(Observable consumer, Object consumedInt) {
		hidePreviousLabel();
		int id = ((IntegerConsumer) consumer).getId();
		JPanel panel = panels.get(id);
		JLabel label = (JLabel) panel.getComponent(0);
		label.setText("" + (int) consumedInt);
		lastLabel = label;
		System.out.println("Thread [" + id + "] consumed int " + consumedInt);
	}

	protected void hidePreviousLabel() {
		if (this.lastLabel != null) {
			lastLabel.setText(" ");
		}
	}

	@Override
	public void addIntegerStorage(IntegerStorage storage) {
		this.storage = storage;

	}

	@Override
	public void activate() {
		this.frame.setVisible(true);
	}

	@Override
	public void startSimulation() {
		JOptionPane.showMessageDialog(frame, "Greetings!\nEach panel observes a Consumer Thread\n"
				+ "and prints the consumed int.\n\n"
				+ "Each thread is competing for acquiring a lock\n"
				+ "over the integer storage monitor.\nClick OK to start!");
		storage.setStarted(true);
	}

	public void addEmptyStorageNotifier(StorageNotifier emptyStorageNotifier) {
		this.emptyStorageNotifier = emptyStorageNotifier;
		this.emptyStorageNotifier.addObserver(this);
	}

}
