package org.terraform.reflection;

import org.jetbrains.annotations.NotNull;
import org.terraform.main.TerraformGeneratorPlugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Post14PrivateFieldHandler extends PrivateFieldHandler {
    private static final MethodHandle LOOKUP;
    private static final MethodHandle VAR_HANDLE_SET;
    private static final MethodHandle FIND_VAR_HANDLE;

    static {
        MethodHandle lookup = null;
        MethodHandle varHandleSet = null;
        MethodHandle findVarHandle = null;
        MethodHandles.Lookup publicLookup = MethodHandles.lookup();

        try {
            Class<?> varHandle = Class.forName("java.lang.invoke.VarHandle");
            lookup = publicLookup.findStatic(
                    MethodHandles.class,
                    "privateLookupIn",
                    MethodType.methodType(MethodHandles.Lookup.class, Class.class, Lookup.class)
            );
            findVarHandle = publicLookup.findVirtual(
                    MethodHandles.Lookup.class,
                    "findVarHandle",
                    MethodType.methodType(varHandle, Class.class, String.class, Class.class)
            );
            varHandleSet = publicLookup.findVirtual(
                    varHandle,
                    "set",
                    MethodType.methodType(void.class, Object[].class)
            );
        }
        catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }

        LOOKUP = lookup;
        VAR_HANDLE_SET = varHandleSet;
        FIND_VAR_HANDLE = findVarHandle;
    }

    @Override
    public void injectField(@NotNull Object obj, @NotNull String field, Object value) throws Exception {
        Field targetField = obj.getClass().getField(field);
        targetField.setAccessible(true);
        int mds = targetField.getModifiers();

        try {
            Object lookup = LOOKUP.invoke(null, Field.class, MethodHandles.lookup());
            Object varHandleModifiers = FIND_VAR_HANDLE.invoke(lookup, Field.class, "modifiers", int.class);
            VAR_HANDLE_SET.invoke(varHandleModifiers, new Object[] {targetField, mds & ~Modifier.FINAL});
        }
        catch (Throwable throwable) {
            // TerraformGeneratorPlugin.TerraformGeneratorPlugin.logger.stackTrace(throwable);
            TerraformGeneratorPlugin.logger.info("Java 14+ detected.");
        }

        targetField.set(obj, value);
    }

    @Override
    public void injectField(Object obj, @NotNull Field targetField, Object value)
            throws IllegalArgumentException, IllegalAccessException
    {
        targetField.setAccessible(true);
        int mds = targetField.getModifiers();

        try {
            Object lookup = LOOKUP.invoke(null, Field.class, MethodHandles.lookup());
            Object varHandleModifiers = FIND_VAR_HANDLE.invoke(lookup, Field.class, "modifiers", int.class);
            VAR_HANDLE_SET.invoke(varHandleModifiers, new Object[] {targetField, mds & ~Modifier.FINAL});
        }
        catch (Throwable throwable) {
            // TerraformGeneratorPlugin.TerraformGeneratorPlugin.logger.stackTrace(throwable);
            TerraformGeneratorPlugin.logger.info("Java 14+ detected.");
        }
        targetField.set(obj, value);
    }
}
