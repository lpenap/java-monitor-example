package com.penapereira.example.javamonitor;

import java.awt.EventQueue;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.penapereira.example.javamonitor.ui.UIManagerSwingImpl;

@SpringBootApplication
public class JavaThreadsMonitorExampleApplication {

	static int CONSUMERS_COUNT = 9;
	static int INTEGERS_TO_CONSUME = 10;
	static int STEP_MILLIS = 500;

	public static void main(String[] args) {
		var ctx = new SpringApplicationBuilder(JavaThreadsMonitorExampleApplication.class).headless(false).run(args);

		EventQueue.invokeLater(() -> {
			var ex = ctx.getBean(JavaThreadsMonitorExampleApplication.class);
			ex.launchSimulation();
		});
	}

	protected void launchSimulation() {
		SimulationController simulationController = SimulationController.instance();

		simulationController.setConsumersQuantity(CONSUMERS_COUNT);
		simulationController.setIntegersToConsume(INTEGERS_TO_CONSUME);
		simulationController.setSimulationStepMillis(STEP_MILLIS);

		simulationController.initialize(new UIManagerSwingImpl(CONSUMERS_COUNT));
		simulationController.startSimulation();
	}

}
