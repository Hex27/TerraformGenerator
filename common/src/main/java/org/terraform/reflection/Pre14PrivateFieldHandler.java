package org.terraform.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Pre14PrivateFieldHandler extends PrivateFieldHandler {
    private static final MethodHandle FIELD_MODIFIERS;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle fieldModifiers = null;

        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            fieldModifiers = lookup.unreflectSetter(modifiersField);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        FIELD_MODIFIERS = fieldModifiers;
    }

    @Override
    public void injectField(Object obj, String field, Object value) throws Throwable {
        Field targetField = obj.getClass().getField(field);
        targetField.setAccessible(true);
        FIELD_MODIFIERS.invoke(targetField, targetField.getModifiers() & ~Modifier.FINAL);
        targetField.set(obj, value);
    }
}
