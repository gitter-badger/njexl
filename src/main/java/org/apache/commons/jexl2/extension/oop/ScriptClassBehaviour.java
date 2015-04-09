package org.apache.commons.jexl2.extension.oop;

import org.apache.commons.jexl2.Interpreter;

/**
 * Created by noga on 08/04/15.
 */
public final class ScriptClassBehaviour {


    public static final String STR = "__str__" ;

    public static final String EQ = "__eq__" ;

    public static final String HC = "__hc__" ;


    public interface Executable {

        void setInterpreter(Interpreter interpreter);

        Object execMethod(String method, Object[] args) throws Exception;

    }

    public interface ObjectComparable {

        public static final String COMPARE = "__cmp__" ;

        int compare(Object o) throws Exception;

    }
}
