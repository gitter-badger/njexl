package noga.commons.njexl.testing;

import java.lang.reflect.Constructor;

/**
 * Created by noga on 15/04/15.
 */
public final class Utils {

    public static Object createInstance(String className, Object... params) {
        try {
            Class clazz = Class.forName(className);
            if ( params.length == 0 ) {
                return clazz.newInstance();
            }
            //else get the matching constructor ...
            Constructor[] constructors = clazz.getDeclaredConstructors();
            for ( int i = 0 ; i < constructors.length; i++ ){
                if ( params.length == constructors[i].getParameterCount() ){
                    // may be this ?
                    return constructors[i].newInstance( params );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
