package org.terraform.reflection;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Post14PrivateFieldHandler extends PrivateFieldHandler {

	@Override
	public void injectField(Object obj, String field, Object value) throws Exception {
		Field targetField = obj.getClass().getField(field);
		targetField.setAccessible(true);
		
		Object lookup = MethodHandles.class
				.getMethod("privateLookupIn", Class.class, Lookup.class)
				.invoke(null, Field.class, MethodHandles.lookup());
		Object varHandleModifiers = lookup.getClass()
				.getMethod("findVarHandle", Class.class, String.class, Class.class)
				.invoke(lookup, Field.class, "modifiers", int.class);
//		Object lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
//		VarHandle modifiers = lookup.findVarHandle(Field.class, "modifiers", int.class);

		int mds = targetField.getModifiers();
		
		varHandleModifiers.getClass().getMethod("set", Object[].class)
		.invoke(varHandleModifiers, new Object[]{targetField,mds& ~Modifier.FINAL});
		
		//modifiers.set(targetField, mds & ~Modifier.FINAL);
		
		targetField.set(obj, value);
	}

}
