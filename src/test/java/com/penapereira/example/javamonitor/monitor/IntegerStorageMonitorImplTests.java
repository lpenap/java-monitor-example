package com.penapereira.example.javamonitor.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
}
