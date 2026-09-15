package com.penapereira.example.javamonitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
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
        boolean resetCalled = false;
        AtomicInteger consumedEvents = new AtomicInteger();
        AtomicBoolean finished = new AtomicBoolean(false);

        @Override
        public void activate() { activated = true; }

        @Override
        public boolean startSimulation() { startCalled = true; return startReturn; }

        @Override
        public void reset() { resetCalled = true; }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (Constants.CONSUMED.equals(evt.getPropertyName())) {
                consumedEvents.incrementAndGet();
            } else if (Constants.FINISHED.equals(evt.getPropertyName())) {
                finished.set(true);
            }
        }
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

    private SimulationController sc;

    @BeforeEach
    public void resetSingletons() throws Exception {
        Field f = SimulationController.class.getDeclaredField("_uniqueInstance");
        f.setAccessible(true);
        f.set(null, null);
        IntegerStorageMonitorImpl.reset();
        sc = SimulationController.instance();
    }

    @AfterEach
    public void stopLeftoverThreads() throws Exception {
        sc.stopSimulation();
        joinAll();
    }

    private void joinAll() throws InterruptedException {
        if (sc.threads == null) {
            return;
        }
        for (Thread t : sc.threads) {
            t.join(2000);
        }
    }

    private void setField(String name, Object value) throws Exception {
        Field field = SimulationController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(sc, value);
    }

    private DummyUI initializeRealSimulation(int consumers, int integers) {
        sc.setConsumersQuantity(consumers);
        sc.setIntegersToConsume(integers);
        sc.setSimulationStepMillis(0);
        DummyUI ui = new DummyUI();
        sc.initialize(ui);
        return ui;
    }

    @Test
    public void startSimulationStartsMonitorWhenUiReturnsTrue() throws Exception {
        DummyUI ui = new DummyUI();
        DummyMonitor monitor = new DummyMonitor();
        setField("userInterface", ui);
        setField("intStorage", monitor);

        sc.startSimulation();

        assertTrue(ui.startCalled);
        assertTrue(monitor.started);
    }

    @Test
    public void startSimulationLeavesMonitorStoppedWhenUiReturnsFalse() throws Exception {
        DummyUI ui = new DummyUI();
        ui.startReturn = false;
        DummyMonitor monitor = new DummyMonitor();
        setField("userInterface", ui);
        setField("intStorage", monitor);

        sc.startSimulation();

        assertTrue(ui.startCalled);
        assertFalse(monitor.started);
    }

    @Test
    public void gettersAndSettersWork() {
        sc.setConsumersQuantity(2);
        sc.setIntegersToConsume(3);
        sc.setSimulationStepMillis(4);
        assertEquals(2, sc.getConsumersQuantity());
        assertEquals(3, sc.getIntegersToConsume());
        assertEquals(4, sc.getSimulationStepMillis());
    }

    @Test
    public void initializeCreatesConsumersAndActivatesUi() throws Exception {
        sc.setConsumersQuantity(1);
        sc.setIntegersToConsume(1);
        sc.setSimulationStepMillis(0);
        DummyUI ui = new DummyUI();
        QuickMonitor monitor = new QuickMonitor();

        Field inst = IntegerStorageMonitorImpl.class.getDeclaredField("_instance");
        inst.setAccessible(true);
        inst.set(null, monitor);

        sc.initialize(ui);
        joinAll();

        assertTrue(ui.activated);
        assertEquals(1, sc.consumers.size());
        assertEquals(2, sc.threads.size());
    }

    @Test
    public void stopSimulationDetachesUiAndJoinsThreads() throws Exception {
        // Monitor is never started, so consumers park on the start gate and
        // the notifier waits for the storage to drain.
        DummyUI ui = initializeRealSimulation(3, 5);
        Thread.sleep(50);

        sc.stopSimulation();

        for (Thread t : sc.threads) {
            assertFalse(t.isAlive());
        }
        for (IntegerConsumer c : sc.consumers) {
            assertEquals(0, c.getSupport().getPropertyChangeListeners().length);
        }
        assertEquals(0, sc.emptyStorageNotifier.getSupport().getPropertyChangeListeners().length);
        assertEquals(0, ui.consumedEvents.get());
        assertFalse(ui.finished.get());
        assertTrue(sc.intStorage.hasIntegers());
    }

    @Test
    public void stopSimulationReturnsPromptlyWhileConsumersSleepInsideTheMonitor() throws Exception {
        sc.setConsumersQuantity(9);
        sc.setIntegersToConsume(100);
        sc.setSimulationStepMillis(500);
        sc.initialize(new DummyUI());
        sc.intStorage.setStarted(true);
        Thread.sleep(100); // one consumer is now sleeping while holding the lock

        long start = System.nanoTime();
        sc.stopSimulation();
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMillis < 300, "stopSimulation took " + elapsedMillis + " ms");
        for (Thread t : sc.threads) {
            assertFalse(t.isAlive());
        }
    }

    @Test
    public void stopSimulationBeforeInitializeIsNoOp() {
        sc.stopSimulation();
        assertNull(sc.consumers);
    }

    @Test
    public void stopSimulationPreservesCallerInterruptFlag() throws Exception {
        initializeRealSimulation(1, 1);

        Thread.currentThread().interrupt();
        sc.stopSimulation();

        // join() threw immediately; the flag must have been restored.
        assertTrue(Thread.interrupted());
        joinAll();
    }

    @Test
    public void restartSimulationStartsFreshRunImmediately() throws Exception {
        DummyUI ui = initializeRealSimulation(2, 3);
        List<IntegerConsumer> firstConsumers = sc.consumers;
        IntegerStorageMonitor firstMonitor = sc.intStorage;
        List<Thread> firstThreads = new ArrayList<>(sc.threads);

        sc.restartSimulation();

        assertTrue(ui.resetCalled);
        assertNotSame(firstConsumers, sc.consumers);
        assertNotSame(firstMonitor, sc.intStorage);
        assertEquals(2, sc.consumers.size());
        for (Thread t : firstThreads) {
            assertFalse(t.isAlive());
        }

        // The new run was started without any further call: it drains on its own.
        joinAll();
        assertFalse(sc.intStorage.hasIntegers());
        assertTrue(ui.consumedEvents.get() > 0);
        assertTrue(ui.finished.get());
    }

    @Test
    public void restartSimulationBeforeInitializeIsNoOp() {
        sc.restartSimulation();
        assertNull(sc.consumers);
        assertNull(sc.intStorage);
    }
}
