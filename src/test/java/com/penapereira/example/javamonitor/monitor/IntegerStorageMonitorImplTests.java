package com.penapereira.example.javamonitor.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.penapereira.example.javamonitor.exception.ForcedStopException;

public class IntegerStorageMonitorImplTests {

    @Test
    public void consumeIntDecrementsUntilZero() throws InterruptedException, ForcedStopException {
        // instantiate monitor with three integers and no delay
        IntegerStorageMonitorImpl monitor = new IntegerStorageMonitorImpl(3, 0);

        // start the monitor so consumeInt() proceeds
        monitor.setStarted(true);

        // first integer
        assertTrue(monitor.hasIntegers());
        assertEquals(3, monitor.consumeInt());

        // second integer
        assertTrue(monitor.hasIntegers());
        assertEquals(2, monitor.consumeInt());

        // third integer
        assertTrue(monitor.hasIntegers());
        assertEquals(1, monitor.consumeInt());

        // no integers left
        assertFalse(monitor.hasIntegers());
        assertEquals(0, monitor.consumeInt());
        assertFalse(monitor.hasIntegers());
    }

    @Test
    public void waitForAllIntegersReturnsImmediatelyWhenZero() throws InterruptedException, ForcedStopException {
        IntegerStorageMonitorImpl monitor = new IntegerStorageMonitorImpl(0, 0);
        monitor.waitForAllIntegersToBeConsumed();
        assertFalse(monitor.hasIntegers());
    }

    @Test
    public void consumeIntThrowsWhenForceStopped() throws Exception {
        IntegerStorageMonitorImpl monitor = new IntegerStorageMonitorImpl(1, 0);

        Thread t = new Thread(() -> {
            try {
                monitor.consumeInt();
                fail("Expected ForcedStopException");
            } catch (ForcedStopException e) {
                // expected
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }
        });

        t.start();
        Thread.sleep(100);
        monitor.forceStop();
        t.join(1000);
        assertFalse(t.isAlive());
    }

    @Test
    public void waitForAllIntegersThrowsWhenForceStopped() throws Exception {
        IntegerStorageMonitorImpl monitor = new IntegerStorageMonitorImpl(1, 0);

        Thread t = new Thread(() -> {
            try {
                monitor.waitForAllIntegersToBeConsumed();
                fail("Expected ForcedStopException");
            } catch (ForcedStopException e) {
                // expected
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }
        });

        t.start();
        Thread.sleep(100);
        monitor.forceStop();
        t.join(1000);
        assertFalse(t.isAlive());
    }
}
