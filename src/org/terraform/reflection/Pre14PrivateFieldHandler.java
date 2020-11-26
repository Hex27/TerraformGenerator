package org.terraform.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Pre14PrivateFieldHandler extends PrivateFieldHandler {
    @Override
    public void injectField(Object obj, String field, Object value) throws Exception {
        Field targetField = obj.getClass().getField(field);
        targetField.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(targetField, targetField.getModifiers() & ~Modifier.FINAL);
        targetField.set(obj, value);
    }
}
