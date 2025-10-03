package org.terraform.utils.version;

import java.lang.reflect.Method;
import java.util.StringJoiner;

public class TerraformMethodHandler {
    public final Method method;

    public TerraformMethodHandler(Class<?> host, String[] possibleNames, Class<?>... parameters) throws NoSuchMethodException, SecurityException {
        Method tryMethod = null;
        boolean bound = false;
        for(String name:possibleNames){
            try{
                tryMethod = host.getDeclaredMethod(name, parameters);
                tryMethod.setAccessible(true);
                bound = true;
            }
            catch (NoSuchMethodException e) { /* Silent */}
            //Let SecurityException fall out
        }
        if(!bound){
            method = null;
            StringJoiner names = new StringJoiner(",");
            for(String name:possibleNames) names.add(name);
            throw new NoSuchMethodException("No method in class " + host + " named [" + names + "]");
        }else method = tryMethod;
    }
}
