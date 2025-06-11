package com.penapereira.example.javamonitor.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.penapereira.example.javamonitor.monitor.IntegerStorageMonitor;
import com.penapereira.example.javamonitor.monitor.AbstractObservable;

public class IntegerConsumerImplAdditionalTests {
    private static class InterruptMonitor extends AbstractObservable implements IntegerStorageMonitor {
        int consumeCount = 0;
        boolean throwInterrupted = false;
        boolean started = true;
        @Override
        public synchronized int consumeInt() throws InterruptedException {
            consumeCount++;
            if (throwInterrupted) throw new InterruptedException();
            return 0;
        }
        @Override public void waitForAllIntegersToBeConsumed() {}
        @Override public void setStarted(boolean started) { this.started = started; }
        @Override public void forceStop() {}
        @Override public boolean hasIntegers() { return started; }
    }

    @Test
    public void runHandlesInterruptedExceptionAndRemovesListeners() {
        InterruptMonitor monitor = new InterruptMonitor();
        monitor.throwInterrupted = true;
        IntegerConsumerImpl consumer = new IntegerConsumerImpl(monitor, 0);
        consumer.addPropertyChangeListener(e -> {});

        consumer.run();

        assertEquals(1, monitor.consumeCount);
        assertEquals(0, consumer.getSupport().getPropertyChangeListeners().length);
    }

    @Test
    public void terminatePreventsConsumptionAndGetIdReturns() throws Exception {
        InterruptMonitor monitor = new InterruptMonitor();
        IntegerConsumerImpl consumer = new IntegerConsumerImpl(monitor, 5);
        consumer.terminate();
        consumer.run();
        assertEquals(0, monitor.consumeCount);
        assertEquals(5, consumer.getId());
    }
}
