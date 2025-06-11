package com.penapereira.example.javamonitor.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.beans.PropertyChangeListener;

import org.junit.jupiter.api.Test;

public class AbstractObservableTests {
    private static class DummyObservable extends AbstractObservable {
        void removeAllPublic() { removeAllListeners(); }
    }

    @Test
    public void addRemoveAndRemoveAllListeners() {
        DummyObservable obs = new DummyObservable();
        PropertyChangeListener l1 = evt -> {};
        PropertyChangeListener l2 = evt -> {};

        obs.addPropertyChangeListener(l1);
        obs.addPropertyChangeListener(l2);
        assertEquals(2, obs.getSupport().getPropertyChangeListeners().length);

        obs.removePropertyChangeListener(l1);
        assertEquals(1, obs.getSupport().getPropertyChangeListeners().length);

        obs.removeAllPublic();
        assertEquals(0, obs.getSupport().getPropertyChangeListeners().length);
    }
}
