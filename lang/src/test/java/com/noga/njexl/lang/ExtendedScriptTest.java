/*
* Copyright 2015 Nabarun Mondal
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package com.noga.njexl.lang;


import com.noga.njexl.lang.extension.dataaccess.DBManager;
import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.lang.extension.dataaccess.XmlMap;
import com.noga.njexl.lang.extension.datastructures.Tuple;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by noga on 28/03/15.
 */
public class ExtendedScriptTest extends JexlTestCase {

    private static Object runScript(JexlEngine JEXL, String path,Object...args)throws Exception{
        System.out.println("==========START=============");

        JexlContext jc = new MapContext();
        jc.set(Script.ARGS, args);
        JEXL.setFunctions(Main.getFunction(jc));
        Script e = JEXL.importScript(path);
        Object o = e.execute(jc);
        System.out.println("=============END==========");
        return o;
    }

    @Test
    public void testMSETDiffScript() throws Exception{
        Object o = runScript(JEXL, "samples/mset.jexl");
        assertTrue(((Map) o).isEmpty());
    }


    @Test
    public void testDateTimeScript() throws Exception{
        runScript(JEXL, "samples/date_time.jexl");
    }

    @Test
    public void testMultilineScript() throws Exception{
        runScript(JEXL, "samples/multiline.jexl");
    }

    @Test
    public void testNamedParameters()throws Exception {
        runScript(JEXL, "samples/named_sample.jexl");
    }

    @Test
    public void testClassFile()throws Exception {
        runScript(JEXL, "samples/class_demo.jexl");
    }

    @Test
    public void testJavaInheritanceClassFile()throws Exception {
        runScript(JEXL, "samples/java_inherit.jexl");
    }

    @Test
    public void testFunctionalInNormalFunction()throws Exception {
        runScript(JEXL,"samples/functional_sample.jexl");
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
        jc.set("T", t);
        Expression e = JEXL.createExpression("T[0]");
        Object o = e.evaluate(jc);
        assertTrue("A".equals(o));
    }

    @Test
    public void testCurrying()throws Exception {
        JexlContext jc = new MapContext();
        jc.set("op","+");
        Expression e = JEXL.createExpression("`2 #{op} 3`");
        Object o = e.evaluate(jc);
        assertTrue(o.equals(5));
        // now the string substitution test
        e = JEXL.createExpression("`a #{op} b`");
        o = e.evaluate(jc);
        assertTrue(o.equals("a + b"));
        // now step by step substitution
        jc.remove("op");
        jc.set("a",10);
        e = JEXL.createExpression("`#{a} #{op} #{b}`");
        o = e.evaluate(jc);
        assertTrue(o.equals("10 #{op} #{b}"));
        jc.set("op",'+');
        jc.set("s",o);
        e = JEXL.createExpression("`#{s}`");
        o = e.evaluate(jc);
        assertTrue(o.equals("10 + #{b}"));
        jc.set("s",o);
        jc.set("b",10);
        e = JEXL.createExpression("`#{s}`");
        o = e.evaluate(jc);
        assertTrue(o.equals(20));

    }


    @Test
    public void testModulas()throws Exception {
        JexlContext jc = new MapContext();
        jc.set("a", Integer.MAX_VALUE );
        jc.set("b", Integer.MIN_VALUE );

        Expression e = JEXL.createExpression("#|b-a|");
        Object o = e.evaluate(jc);
        assertTrue(o!= null);
    }

    @Test
    public void testClassScript() throws Exception {
        runScript(JEXL, "samples/class_demo.jexl");
    }

    @Test
    public void testPerfScript() throws Exception {
        long t = System.currentTimeMillis();
        runScript(JEXL,"samples/perf.jexl");
        t = System.currentTimeMillis() - t ;
        System.out.println("Time Taken (ms): " + t);
        assertTrue(20000 > t);
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
        DataMatrix m1 = DataMatrix.loc2matrix("samples/test.tsv");
        m1 = m1.sub(0,3);
        assertTrue(m1 != null);
        DataMatrix m2 = DataMatrix.loc2matrix("samples/test.tsv");
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


    @Test
    public void testMethodArgOverWrite() throws Exception{
        Object o = runScript(JEXL, "samples/combination.jxl");
        Object[] r = (Object[])o;
        // 4P2 : 12
        assertEquals(12, ((List) r[0]).size());
        //4C2 : 6
        assertEquals(6, ((List) r[1]).size());
    }

    @Test
    public void testExtendedBreak() throws Exception{
        Object o = runScript(JEXL, "samples/break.jxl");
        assertTrue(o instanceof List);
    }

    @Test
    public void testThreading() throws Exception{
        Object o = runScript(JEXL, "samples/thread_demo.jxl");
        assertEquals(3, o);
    }

    @Test
    public void testTupleScript() throws Exception{
        Object o = runScript(JEXL, "samples/multireturn.jxl");
        assertTrue(o instanceof Collection);
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
