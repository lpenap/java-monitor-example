package com.penapereira.example.javamonitor.monitor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.penapereira.example.javamonitor.Constants;

public class EmptyIntegerStorageNotifierTests {

    @Test
    public void runFiresFinishedWhenStorageEmpty() {
        IntegerStorageMonitorImpl monitor = new IntegerStorageMonitorImpl(0, 0);
        EmptyIntegerStorageNotifier notifier = new EmptyIntegerStorageNotifier(monitor);
        AtomicBoolean notified = new AtomicBoolean(false);

        notifier.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (Constants.FINISHED.equals(evt.getPropertyName()) && Boolean.TRUE.equals(evt.getNewValue())) {
                notified.set(true);
            }
        });

        notifier.run();
        assertTrue(notified.get());
    }

    @Test
    public void runDoesNotFireFinishedWhenForcedStop() throws Exception {
        IntegerStorageMonitorImpl monitor = new IntegerStorageMonitorImpl(1, 0);
        EmptyIntegerStorageNotifier notifier = new EmptyIntegerStorageNotifier(monitor);
        AtomicBoolean notified = new AtomicBoolean(false);

        notifier.addPropertyChangeListener(evt -> {
            if (Constants.FINISHED.equals(evt.getPropertyName())) {
                notified.set(true);
            }
        });

        Thread t = new Thread(notifier);
        t.start();
        Thread.sleep(100);
        monitor.forceStop();
        t.join(1000);
        assertFalse(notified.get());
    }

    private static class InterruptingMonitor extends IntegerStorageMonitorImpl {
        InterruptingMonitor() { super(0, 0); }
        @Override
        public synchronized void waitForAllIntegersToBeConsumed() throws InterruptedException {
            throw new InterruptedException();
        }
    }

    @Test
    public void runHandlesInterruptedExceptionAndRemovesListeners() {
        InterruptingMonitor monitor = new InterruptingMonitor();
        EmptyIntegerStorageNotifier notifier = new EmptyIntegerStorageNotifier(monitor);
        notifier.addPropertyChangeListener(evt -> {});

        notifier.run();

        assertEquals(0, notifier.getSupport().getPropertyChangeListeners().length);
    }
}
