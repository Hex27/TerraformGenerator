package org.terraform.utils.version;

import java.lang.reflect.Field;
import java.util.StringJoiner;

/**
 * For dealing with reflection against fields with multiple names.
 *
 */
public class TerraformFieldHandler {
    public final Field field;

    public TerraformFieldHandler(Class<?> host, String... possibleNames) throws NoSuchFieldException, SecurityException {
        Field tryField = null;
        boolean bound = false;
        for(String name:possibleNames){
            try{
                tryField = host.getDeclaredField(name);
                tryField.setAccessible(true);
                bound = true;
            }
            catch (NoSuchFieldException e) { /* Silent */}
            //Let SecurityException fall out
        }
        if(!bound){
            field = null;
            StringJoiner names = new StringJoiner(",");
            for(String name:possibleNames) names.add(name);
            throw new NoSuchFieldException("No field in class " + host + " named [" + names + "]");
        }else field = tryField;
    }
}
