package org.apache.commons.jexl2;

import org.apache.commons.jexl2.extension.ListSet;
import org.apache.commons.jexl2.extension.Tuple;
import org.apache.commons.jexl2.extension.dataaccess.DBManager;
import org.apache.commons.jexl2.extension.dataaccess.DataMatrix;
import org.apache.commons.jexl2.extension.dataaccess.XmlMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
    public void testTupleIndexing()throws Exception {
        JexlContext jc = new MapContext();
        ArrayList<String> cnames = new ArrayList<>();
        cnames.add("a");
        cnames.add("b");
        cnames.add("c");
        ArrayList<String> values = new ArrayList<>();
        values.add("A");
        values.add("B");
        values.add("C");
        Tuple t = new Tuple(cnames,values);
        jc.set("T",t);
        Expression e = JEXL.createExpression("T[0]");
        Object o = e.evaluate(jc);
        assertTrue("A".equals(o));
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

    @Test
    public void testXmlLoading() throws Exception{
        XmlMap xmlMap = XmlMap.file2xml("pom.xml");
        assertTrue(xmlMap != null);
    }

    @Test
    public void testJSONLoading() throws Exception{
        DBManager.init("samples/" + DBManager.DB_CONFIG_FILE_LOC);
        assertTrue(DBManager.dataBaseDOMHash != null );
    }

    @Test
    public void testDataMatrixComparison() throws Exception{
        DataMatrix m1 = DataMatrix.file2matrix("samples/test.tsv");
        m1 = m1.sub(0,3);
        assertTrue(m1 != null);
        DataMatrix m2 = DataMatrix.file2matrix("samples/test.tsv");
        m2 = m2.sub(0,3);
        assertTrue(m2 != null );
        m1.keys(0);
        m1 = m1.aggregate("Points");
        assertTrue(m1.columns.size() == 1 );
        m2.keys(0);
        m2 = m2.aggregate("Points");
        assertTrue(m2.columns.size() == 1 );

        DataMatrix.MatrixDiff diff  = DataMatrix.diff(m1, m2);
        assertFalse( diff.diff()  );

    }


    /****
    @Test
    public void testDB() throws Exception{
        testJSONLoading();
        Object l = DBManager.results("noga", "select * from noga_demo");
        assertTrue( l instanceof DataMatrix);
        DataMatrix dm = (DataMatrix)l;
        l = dm.select(0);
        assertTrue( l instanceof ArrayList);
        l = dm.select("Name");
        assertTrue( l instanceof ArrayList);
    }
    ****/

}
