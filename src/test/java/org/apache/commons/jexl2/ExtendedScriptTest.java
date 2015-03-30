package org.apache.commons.jexl2;

import org.junit.Test;

/**
 * Created by noga on 28/03/15.
 */
public class ExtendedScriptTest extends JexlTestCase {

    private static Object runScript(JexlEngine JEXL, String path)throws Exception{
        JexlContext jc = new MapContext();
        JEXL.setFunctions(Main.getFunction(jc));
        Script e = JEXL.importScript(path);
        return e.execute(jc);
    }
    @Test
    public void testFullScript() throws Exception {
        runScript(JEXL,"samples/main.jexl");
    }
    @Test
    public void testPerfScript() throws Exception {
        long t = System.currentTimeMillis();
        runScript(JEXL,"samples/perf.jexl");
        t = System.currentTimeMillis() - t ;
        System.out.println("Time Taken (ms): " + t);
        assertTrue(10000 > t );
    }
    @Test
    public void testPredicateScript() throws Exception {
       runScript(JEXL, "samples/pred_sample.jexl");
    }
    @Test
    public void testListScript() throws Exception {
        runScript(JEXL,"samples/lists.jexl");
    }
}
