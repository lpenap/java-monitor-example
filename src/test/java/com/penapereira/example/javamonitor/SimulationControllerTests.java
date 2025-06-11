package com.penapereira.example.javamonitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.penapereira.example.javamonitor.consumer.IntegerConsumer;
import com.penapereira.example.javamonitor.monitor.IntegerStorageMonitor;
import com.penapereira.example.javamonitor.monitor.IntegerStorageMonitorImpl;

public class SimulationControllerTests {

    private static class DummyUI implements com.penapereira.example.javamonitor.ui.UIManager {
        boolean activated = false;
        boolean startCalled = false;
        boolean startReturn = true;

        @Override
        public void activate() { activated = true; }

        @Override
        public boolean startSimulation() { startCalled = true; return startReturn; }

        @Override
        public void propertyChange(PropertyChangeEvent evt) { }
    }

    private static class DummyConsumer implements IntegerConsumer {
        boolean terminated = false;
        int id;
        DummyConsumer(int id) { this.id = id; }
        @Override
        public void run() { }
        @Override
        public int getId() { return id; }
        @Override
        public void terminate() { terminated = true; }
        @Override
        public java.beans.PropertyChangeSupport getSupport() { return new java.beans.PropertyChangeSupport(this); }
        @Override
        public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) { }
        @Override
        public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) { }
    }

    private static class DummyMonitor implements IntegerStorageMonitor {
        boolean started = false;
        @Override public int consumeInt() { return 0; }
        @Override public void waitForAllIntegersToBeConsumed() { }
        @Override public void setStarted(boolean started) { this.started = started; }
        @Override public void forceStop() { }
        @Override public boolean hasIntegers() { return false; }
    }

    private static class QuickMonitor extends DummyMonitor {
        boolean consumed = false;
        @Override public synchronized int consumeInt() { consumed = true; return 0; }
        @Override public synchronized boolean hasIntegers() { return !consumed; }
    }

    @BeforeEach
    public void resetSingleton() throws Exception {
        Field f = SimulationController.class.getDeclaredField("_uniqueInstance");
        f.setAccessible(true);
        f.set(null, null);
        Field m = IntegerStorageMonitorImpl.class.getDeclaredField("_instance");
        m.setAccessible(true);
        m.set(null, null);
    }

    @Test
    public void startSimulationStartsMonitorWhenUiReturnsTrue() throws Exception {
        DummyUI ui = new DummyUI();
        DummyMonitor monitor = new DummyMonitor();
        SimulationController sc = SimulationController.instance();
        Field uiField = SimulationController.class.getDeclaredField("userInterface");
        uiField.setAccessible(true);
        uiField.set(sc, ui);
        Field monField = SimulationController.class.getDeclaredField("intStorage");
        monField.setAccessible(true);
        monField.set(sc, monitor);

        sc.startSimulation();

        assertTrue(ui.startCalled);
        assertTrue(monitor.started);
    }

    @Test
    public void stopSimulationTerminatesAllConsumers() throws Exception {
        SimulationController sc = SimulationController.instance();
        List<IntegerConsumer> consumers = new ArrayList<>();
        consumers.add(new DummyConsumer(0));
        consumers.add(new DummyConsumer(1));
        Field consField = SimulationController.class.getDeclaredField("consumers");
        consField.setAccessible(true);
        consField.set(sc, consumers);

        sc.stopSimulation();

        for (IntegerConsumer ic : consumers) {
            assertTrue(((DummyConsumer)ic).terminated);
        }
    }

    @Test
    public void gettersAndSettersWork() {
        SimulationController sc = SimulationController.instance();
        sc.setConsumersQuantity(2);
        sc.setIntegersToConsume(3);
        sc.setSimulationStepMillis(4);
        assertEquals(2, sc.getConsumersQuantity());
        assertEquals(3, sc.getIntegersToConsume());
        assertEquals(4, sc.getSimulationStepMillis());
    }

    @Test
    public void initializeCreatesConsumersAndActivatesUi() throws Exception {
        SimulationController sc = SimulationController.instance();
        sc.setConsumersQuantity(1);
        sc.setIntegersToConsume(1);
        sc.setSimulationStepMillis(0);
        DummyUI ui = new DummyUI();
        QuickMonitor monitor = new QuickMonitor();

        Field inst = IntegerStorageMonitorImpl.class.getDeclaredField("_instance");
        inst.setAccessible(true);
        inst.set(null, monitor);

        sc.initialize(ui);
        Thread.sleep(50); // allow spawned threads to finish

        Field consField = SimulationController.class.getDeclaredField("consumers");
        consField.setAccessible(true);
        List<?> list = (List<?>) consField.get(sc);

        assertTrue(ui.activated);
        assertEquals(1, list.size());
    }
}
