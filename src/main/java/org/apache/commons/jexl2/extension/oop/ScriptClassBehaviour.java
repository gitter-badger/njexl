package org.apache.commons.jexl2.extension.oop;

import org.apache.commons.jexl2.Interpreter;

/**
 * Created by noga on 08/04/15.
 */
public final class ScriptClassBehaviour {


    public interface Executable {

        void setInterpreter(Interpreter interpreter);

        Object execMethod(String method, Object[] args) throws Exception;

    }

}
