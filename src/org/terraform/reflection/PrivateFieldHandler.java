package org.terraform.reflection;

public abstract class PrivateFieldHandler {
    public abstract void injectField(Object obj, String field, Object value) throws Exception;
}
