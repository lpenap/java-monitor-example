package com.penapereira.example.javamonitor;

import java.awt.EventQueue;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.penapereira.example.javamonitor.ui.UIManagerSwingImpl;

@SpringBootApplication
public class JavaThreadsMonitorExampleApplication {

	public static void main(String[] args) {
		var ctx = new SpringApplicationBuilder(JavaThreadsMonitorExampleApplication.class).headless(false).run(args);

		EventQueue.invokeLater(() -> {
			var ex = ctx.getBean(JavaThreadsMonitorExampleApplication.class);
			ex.launchSimulation();
		});
	}

	protected void launchSimulation() {
		SimulationController simulationController = SimulationController.instance();

		simulationController.setConsumersQuantity(Constants.CONSUMERS_COUNT);
		simulationController.setIntegersToConsume(Constants.INTEGERS_TO_CONSUME);
		simulationController.setSimulationStepMillis(Constants.STEP_MILLIS);

		simulationController.initialize(new UIManagerSwingImpl(Constants.CONSUMERS_COUNT));
		simulationController.startSimulation();
	}

}
