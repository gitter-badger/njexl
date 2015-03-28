package org.apache.commons.jexl2;

import org.junit.Test;

/**
 * Created by noga on 28/03/15.
 */
public class ExtendedScriptTest extends JexlTestCase {

    @Test
    public void testFullScript() throws Exception {
        JexlContext jc = new MapContext();
        JEXL.setFunctions(Main.getFunction(jc));
        Script e = JEXL.importScript("samples/main.jexl");
        e.execute(jc);
    }
    @Test
    public void testPerfScript() throws Exception {
        JexlContext jc = new MapContext();
        JEXL.setFunctions(Main.getFunction(jc));
        Script e = JEXL.importScript("samples/perf.jexl");
        long t = System.currentTimeMillis();
        e.execute(jc);
        t = System.currentTimeMillis() - t ;
        System.out.println("Time Taken (ms): " + t);
        assertTrue(10000 > t );
    }
}
