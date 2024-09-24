package org.terraform.reflection;

import java.lang.reflect.Field;

public abstract class PrivateFieldHandler {
    public abstract void injectField(Object obj, String field, Object value) throws Throwable;


    public void injectField(Object obj, Field target, Object value)
            throws IllegalArgumentException, IllegalAccessException
    {
        throw new UnsupportedOperationException();
    }
}