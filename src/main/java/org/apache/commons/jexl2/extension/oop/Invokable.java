package org.apache.commons.jexl2.extension.oop;

import org.apache.commons.jexl2.Interpreter;

/**
 * Created by noga on 08/04/15.
 */
public interface Invokable {

    String type();

    Object execMethod(String method,Interpreter interpreter ,Object[] args) throws Exception;

}
