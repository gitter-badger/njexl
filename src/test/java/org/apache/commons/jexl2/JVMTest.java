package org.apache.commons.jexl2;

import org.junit.Test;

/**
 * Created by noga on 19/03/15.
 */
public class JVMTest {

    JexlEngine jexlEngine;

    public JVMTest(){
        jexlEngine = new JexlEngine();
    }

    @Test
    public void testJavaClassCreation() throws Exception{
            Script e = jexlEngine.importScriptForJVM("samples/jvmExample.jexl", "dummy");
            e.executeJVM(null);
    }
}
