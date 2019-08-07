package com.penapereira.monitorexample;

import com.penapereira.monitorexample.ui.UIManagerSwingImpl;

public class Main {

	static int CONSUMERS_COUNT = 9;
	static int INTEGERS_TO_CONSUME = 10;
	static int STEP_MILLIS = 500;

	public static void main(String[] args) {
		SimulationController simulationController = SimulationController
			.instance();

		simulationController.setConsumersQuantity(CONSUMERS_COUNT);
		simulationController.setIntegersToConsume(INTEGERS_TO_CONSUME);
		simulationController.setSimulationStepMillis(STEP_MILLIS);

		simulationController
			.initialize(new UIManagerSwingImpl(CONSUMERS_COUNT));
		simulationController.startSimulation();
	}
}
