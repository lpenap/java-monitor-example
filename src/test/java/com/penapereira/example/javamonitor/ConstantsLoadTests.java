package com.penapereira.example.javamonitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

public class ConstantsLoadTests {
    @Test
    public void constantsAccessibleViaReflection() throws Exception {
        Class<?> clazz = Class.forName(Constants.class.getName());
        Field stop = clazz.getField("STOP");
        assertEquals("stop", stop.get(null));
    }
}
