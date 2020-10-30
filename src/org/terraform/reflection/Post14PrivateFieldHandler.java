package org.terraform.reflection;

import org.terraform.main.TerraformGeneratorPlugin;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Post14PrivateFieldHandler extends PrivateFieldHandler {
    private static final Method LOOKUP;
    private static final Method FIND_VAR_HANDLE;

    static {
        Method lookup = null;
        Method findVarHandle = null;
        try {
            lookup = MethodHandles.class.getMethod("privateLookupIn", Class.class, Lookup.class);
            findVarHandle = Lookup.class.getMethod("findVarHandle", Class.class, String.class, Class.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        LOOKUP = lookup;
        FIND_VAR_HANDLE = findVarHandle;
    }

    @Override
    public void injectField(Object obj, String field, Object value) throws Exception {
        Field targetField = obj.getClass().getField(field);
        targetField.setAccessible(true);

        Object lookup = LOOKUP.invoke(null, Field.class, MethodHandles.lookup());
        Object varHandleModifiers = FIND_VAR_HANDLE.invoke(lookup, Field.class, "modifiers", int.class);

        //		Object lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
//		VarHandle modifiers = lookup.findVarHandle(Field.class, "modifiers", int.class);

        int mds = targetField.getModifiers();

        Object params = new Object[]{targetField, mds & ~Modifier.FINAL};
        try {
            varHandleModifiers.getClass().getMethod("set", Object[].class).invoke(varHandleModifiers, params);
        } catch (Exception e) {
            //e.printStackTrace();
            TerraformGeneratorPlugin.logger.info("Java 14 detected. TerraformGenerator may or may not work, but if it does, good on you!");
        }

        //modifiers.set(targetField, mds & ~Modifier.FINAL);
        targetField.set(obj, value);
    }
}
