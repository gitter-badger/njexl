package org.apache.commons.jexl2.extension.oop;

import org.apache.commons.jexl2.Interpreter;

/**
 * Created by noga on 08/04/15.
 */
public final class ScriptClassBehaviour {


    public static final String STR = "__str__" ;

    public static final String EQ = "__eq__" ;

    public static final String HC = "__hc__" ;

    public static final String CMP = "__cmp__" ;


    public interface Executable {

        void setInterpreter(Interpreter interpreter);

        Object execMethod(String method, Object[] args) ;

    }


    public interface Arithmetic{

        String NEG = "__neg__" ;

        Object neg()  ;

        String ADD = "__add__" ;

        Object add(Object o) ;

        String SUB = "__sub__" ;

        Object sub(Object o) ;

        String MUL = "__mul__" ;

        Object mul(Object o)  ;

        String DIV = "__div__" ;

        Object div(Object o)  ;

        String EXP = "__exp__" ;

        Object exp(Object o)  ;

    }

    public interface Logic{

        String COMPLEMENT = "__complement__" ;

        Object complement()  ;

        String OR = "__or__" ;

        Object or(Object o) ;

        String AND = "__and__" ;

        Object and(Object o) ;

        String XOR = "__xor__" ;

        Object xor(Object o)  ;

    }
}
