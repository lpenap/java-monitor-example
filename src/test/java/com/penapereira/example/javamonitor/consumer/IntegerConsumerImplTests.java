package com.penapereira.example.javamonitor.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.penapereira.example.javamonitor.Constants;
import com.penapereira.example.javamonitor.exception.ForcedStopException;
import com.penapereira.example.javamonitor.monitor.IntegerStorageMonitor;
import com.penapereira.example.javamonitor.monitor.AbstractObservable;

public class IntegerConsumerImplTests {

    private static class TestMonitor extends AbstractObservable implements IntegerStorageMonitor {
        private final List<Integer> ints;
        private boolean started = true;
        private boolean throwOnConsume = false;

        TestMonitor(List<Integer> ints) {
            this.ints = new ArrayList<>(ints);
        }

        void setThrowOnConsume(boolean v) { this.throwOnConsume = v; }

        @Override
        public synchronized int consumeInt() throws InterruptedException, ForcedStopException {
            if (throwOnConsume) {
                throw new ForcedStopException();
            }
            return ints.isEmpty() ? 0 : ints.remove(0);
        }

        @Override
        public synchronized void waitForAllIntegersToBeConsumed() throws InterruptedException, ForcedStopException { }

        @Override
        public synchronized void setStarted(boolean started) { this.started = started; }

        @Override
        public synchronized void forceStop() { }

        @Override
        public synchronized boolean hasIntegers() { return started && !ints.isEmpty(); }
    }

    @Test
    public void runProcessesIntegersAndFiresEvents() {
        List<Integer> consumed = new ArrayList<>();
        TestMonitor monitor = new TestMonitor(List.of(3,2,1));
        IntegerConsumerImpl consumer = new IntegerConsumerImpl(monitor, 0);
        consumer.addPropertyChangeListener(evt -> {
            if (Constants.CONSUMED.equals(evt.getPropertyName())) {
                consumed.add((Integer) evt.getNewValue());
            }
        });

        consumer.run();

        assertEquals(List.of(3,2,1), consumed);
    }

    @Test
    public void runFiresStopEventOnForcedStop() {
        TestMonitor monitor = new TestMonitor(List.of(1));
        monitor.setThrowOnConsume(true);
        IntegerConsumerImpl consumer = new IntegerConsumerImpl(monitor, 0);
        final boolean[] stopped = {false};
        consumer.addPropertyChangeListener(evt -> {
            if (Constants.STOP.equals(evt.getPropertyName())) {
                stopped[0] = true;
            }
        });

        consumer.run();

        assertTrue(stopped[0]);
    }
}
