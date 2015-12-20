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


import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.extension.dataaccess.DBManager;
import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.lang.extension.dataaccess.XmlMap;
import com.noga.njexl.lang.extension.datastructures.Tuple;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
    public void testClosureAndDefinedMethodScript() throws Exception{
        Object o = runScript(JEXL, "samples/closure.jxl");
        assertEquals(3,o);
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
        runScript(JEXL, "samples/class_demo2.jexl");
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
    public void testIteratorsScript() throws Exception {
        runScript(JEXL, "samples/iterators");
    }

    @Test
    public void testIOScript() throws Exception {
        Object o = runScript(JEXL, "samples/io");
        assertTrue(o instanceof String);
    }

    @Test
    public void testListScript() throws Exception {
        runScript(JEXL,"samples/lists.jexl");
    }

    @Test
    public void testXmlLoading() throws Exception{
        Object o = runScript(JEXL, "samples/xml_load.jxl", "samples/xml_load.jxl", "samples/sample.xml");
        assertTrue(o instanceof Map);
        JexlContext jc = new MapContext();
        Script e = JEXL.createScript("xml('samples/sample.xml') ;");
        o = e.execute(jc);
        assertTrue(o instanceof XmlMap);
        XmlMap xmlMap = (XmlMap)o;
        o = xmlMap.elements("//slide");
        assertTrue(o instanceof List);
        o = xmlMap.element("//slide[1]");
        assertTrue(o instanceof XmlMap.XmlElement);
        o = xmlMap.xpath("//slide/title/text()");
        assertTrue(o instanceof String);
    }

    @Test
    public void testJSONLoading() throws Exception{
        DBManager mgr =(DBManager) TypeUtility.dataBase("samples/" + DBManager.DB_CONFIG_FILE_LOC);
        assertTrue(mgr.dataBaseDOMHash != null );
    }

    @Test
    public void testAtomicTypeFunction() throws Exception{
        JexlContext jc = new MapContext();
        Script e = JEXL.createScript("x = 0 ; ax = atomic(x) ; ");
        Object o = e.execute(jc);
        assertTrue( o instanceof AtomicInteger );

        e = JEXL.createScript("ax += 42 ");
        o = e.execute(jc);
        assertEquals(42,o);

        e = JEXL.createScript("x = true ; ax = atomic(x) ; ");
        o = e.execute(jc);
        assertTrue( o instanceof AtomicBoolean);

        e = JEXL.createScript("x = 0l ; ax = atomic(x) ; ");
        o = e.execute(jc);
        assertTrue( o instanceof AtomicLong);

        e = JEXL.createScript("x = [ 0 ] ; ax = atomic(x) ; ");
        o = e.execute(jc);
        assertTrue( o instanceof Tuple );

        e = JEXL.createScript("x = list( 0 ) ; ax = atomic(x) ; ");
        o = e.execute(jc);
        assertTrue( o instanceof List );

        e = JEXL.createScript("x = { 0 : 0 } ; ax = atomic(x) ; ");
        o = e.execute(jc);
        assertTrue( o instanceof Map );
    }

    @Test
    public void testRaisingError() throws Throwable{
        JexlContext jc = new MapContext();
        Script e = JEXL.createScript("error()");
        Object o = e.execute(jc);
        assertFalse((Boolean)o);

        e = JEXL.createScript("error(null)");
        o = e.execute(jc);
        assertFalse((Boolean)o);

        e = JEXL.createScript("#(o,:e) = error(true,'Some Error!')");
        o = e.execute(jc);
        assertNotNull(o);
        assertTrue(jc.get("e") instanceof Error );

        e = JEXL.createScript("#(o,:e) = error{true}('Some Error!')");
        o = e.execute(jc);
        assertNotNull(o);
        assertTrue(jc.get("e") instanceof Error );

        e = JEXL.createScript(" error{false}('Some Error!')");
        o = e.execute(jc);
        assertFalse((Boolean)o);

    }

    @Test
    public void testInspect() throws Exception{
        JexlContext jc = new MapContext();
        Script e = JEXL.createScript("x = 10 ; inspect(x) ; ");
        Object o = e.execute(jc);
        assertTrue(o instanceof Map);
        assertFalse(((Map)o).isEmpty() );

        e = JEXL.createScript("inspect(null)");
        o = e.execute(jc);
        assertNotNull(o);
        e = JEXL.createScript("inspect()");
        o = e.execute(jc);
        assertNotNull(o);

        e = JEXL.createScript("dict('hello')");
        o = e.execute(jc);
        assertTrue( o instanceof Map);
        Map m = (Map)o;
        assertTrue(m.containsKey("hash"));
        assertTrue(m.containsKey("value"));

    }

    @Test
    public void testCollectionAddAll() throws Exception{
        JexlContext jc = new MapContext();
        Script e = JEXL.createScript("x = set(1,2) ; y = set(3,4) ; z = x + y ; z == set(1,2,3,4)");
        Object o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);

        e = JEXL.createScript("x = list(1,2) ; z += x ; z == set(1,2,3,4) ");
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);

        e = JEXL.createScript("x = {1:2} ; y = {3:4} ; z = x + y ;  z == { 1:2, 3:4 } ");
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);

    }

    @Test
    public void testCollectionSubtractAll() throws Exception{
        JexlContext jc = new MapContext();
        Script e = JEXL.createScript("x = set(1,2) ; z = set(1,2,3,4) ; y = z - x ; y == set(3,4) ; ");
        Object o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);

        e = JEXL.createScript("x = list(1,2) ; y = z - x ; y == set(3,4) ; ");
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);


        e = JEXL.createScript("y -= 3 ; y == [4] ");
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);


        e = JEXL.createScript("x = {1:2} ; z = { 1:2, 3:4 } ; y = z - x ; y == {3:4} ; ");
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);

    }

    @Test
    public void testVariousTypeUtilityMethods() throws Exception{
        JexlContext jc = new MapContext();
        Script e = JEXL.createScript("type('foobar');");
        Object o = e.execute(jc);
        assertEquals(String.class, o );

        e = JEXL.createScript("type() == type(null)  ");
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);

        e = JEXL.createScript("x = LIST(1,2,list(3,4)) ");
        o = e.execute(jc);
        assertTrue(o instanceof List);
        assertEquals(3,((List)o).size() );

        e = JEXL.createScript("y = project(x,1) ");
        o = e.execute(jc);
        assertTrue(o instanceof List);
        assertEquals(2,((List)o).size() );

        e = JEXL.createScript("y = project(x,1,2) ");
        o = e.execute(jc);
        assertTrue(o instanceof List);
        assertEquals(2,((List)o).size() );

        e = JEXL.createScript("y = project(x,-1) ; y.0 == 1 and y.1 == 2 ;");
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);

        e = JEXL.createScript("y = project([0,1,2], -1) ; y.0 == 0 and y.1 == 1 and y isa [] ");
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);

        e = JEXL.createScript("system('ls')");
        o = e.execute(jc);
        assertEquals(0,o);

        e = JEXL.createScript("system('ls -l foobar')");
        o = e.execute(jc);
        assertEquals(1,o);

        boolean oL = JEXL.isLenient() ;
        boolean oS = JEXL.isStrict() ;
        JEXL.setLenient(false);
        JEXL.setStrict(true);

        e = JEXL.createScript(" try{ system('foobar') }( 'error' ) ");
        o = e.execute(jc);
        assertEquals("error",o);

        e = JEXL.createScript(" try{ system('foobar') }( ) ");
        o = e.execute(jc);
        assertTrue(o instanceof IOException );

        e = JEXL.createScript(" clock{ for ( i = 0 ; i < 1001; i+= 1){ i }  }( ) ");
        o = e.execute(jc);
        assertTrue(o instanceof Object[] );
        assertTrue(((Object[])o)[0] instanceof Long );
        assertEquals(1000, ((Object[])o)[1]);


        e = JEXL.createScript(" clock{ system('foobar')  }( ) ");
        o = e.execute(jc);
        assertTrue(o instanceof Object[] );
        assertTrue(((Object[])o)[1] instanceof IOException );
        assertTrue(((Object[])o)[0] instanceof Long);

        e = JEXL.createScript(" fopen('samples/test.tsv') ");
        o = e.execute(jc);
        assertTrue(o instanceof BufferedReader);
        ((BufferedReader)o).close();

        JEXL.setLenient(oL);
        JEXL.setStrict(oS);

    }

    @Test
    public void testHeaderOnlyTable() throws Exception{
        JexlContext jc = new MapContext();
        Script e = JEXL.createScript("m = matrix('samples/test_header_only.tsv') ; m.select('Number', 'First Name') ;");
        Object o = e.execute(jc);
        assertTrue(o instanceof List);
        assertTrue(((List)o).isEmpty() );
        // now load again w/o header
        e = JEXL.createScript("m = matrix('samples/test_header_only.tsv','\\t', false) ; ");
        o = e.execute(jc);
        assertTrue(o instanceof DataMatrix );
        assertEquals(1, ((DataMatrix)o).rows.size() );

        e = JEXL.createScript("m.t(0)");
        o = e.execute(jc);
        assertTrue(o instanceof Tuple );
        assertEquals(5, ((Tuple)o).t.length );

        e = JEXL.createScript("m.c(0)");
        o = e.execute(jc);
        assertTrue(o instanceof List );
        assertEquals(1, ((List)o).size() );

    }

    @Test
    public void testDataMatrixComparison() throws Exception{
        DataMatrix m1 = DataMatrix.loc2matrix("samples/test.tsv");
        System.out.println(m1);
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
        assertTrue(diff.id.isEmpty());
        assertTrue(diff.lr.isEmpty());
        assertTrue(diff.rl.isEmpty());


        DataMatrix m3 = DataMatrix.loc2matrix("samples/test_header_only.tsv");
        System.out.println(m3);

        m3 = m3.sub(0,3);
        m3.keys(0);
        m3 = m3.aggregate("Points");
        diff  = DataMatrix.diff(m1, m3);
        assertTrue( diff.diff()  );
        assertTrue(diff.rl.isEmpty());
        assertFalse(diff.lr.isEmpty());

        System.out.println(diff);

        diff  = DataMatrix.diff(m3, m1);
        assertTrue( diff.diff()  );

        assertTrue(diff.lr.isEmpty());
        assertFalse(diff.rl.isEmpty());

        System.out.println(diff);

        JexlContext jc = new MapContext();
        jc.set("m1",m1);
        jc.set("m2",m2);
        jc.set("m3",m3);

        // the equals of the tuple should kick in
        Script e = JEXL.createScript("d = m1.diff{ $[0] == $[1]  }(m1,m2) ; d.diff() ");
        Object o = e.execute(jc);
        assertFalse((Boolean)o);

        //the general stuff should kick in
        e = JEXL.createScript("d = m1.diff{ $[0].Points == $[1].Points  }(m1,m2) ; d.diff() ");
        o = e.execute(jc);
        assertFalse((Boolean)o);

    }


    @Test
    public void testMethodArgOverWrite() throws Exception{
        Object o = runScript(JEXL, "samples/combination.jxl");
        Object[] r = (Object[])o;
        // 4P2 : 12
        assertEquals(12, ((List) r[0]).size());
        assertEquals(2, ((List)((List) r[0]).get(0)).size() );

        //4C2 : 6
        assertEquals(6, ((List) r[1]).size());

        assertEquals(2, ((List)((List) r[1]).get(0)).size() );

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
        assertNotNull(o);
        assertTrue(o.getClass().isArray());
    }


    @Test
    public void testSimpleForScript() throws Exception{
        Object o = runScript(JEXL, "samples/simplefor.jxl");
        assertEquals(3, o);
    }

    @Test
    public void testLabelledScript() throws Exception{
        Object o = runScript(JEXL, "samples/label.jxl");
        assertEquals(0,o);
    }

    @Test
    public void testInstanceMethod() throws Exception{
        Object o = runScript(JEXL, "samples/instance_method.jxl");
        assertEquals(174,o);
    }

    @Test
    public void testStaticMethod() throws Exception{
        Object o = runScript(JEXL, "samples/class_static.jxl");
        assertEquals(42,o);
    }

    @Test
    public void testMultipleInherit() throws Exception{
        Object o = runScript(JEXL, "samples/multiple_inheritance.jxl");
        assertEquals(92,o);
    }
    @Test
    public void testMultipleInheritWithSameName() throws Exception{
        Object o = runScript(JEXL, "samples/same_name_diff_ns.jxl");
        assertEquals(2,o);
    }
    @Test
    public void testFunctionTakingDefaultFunctionAsArg() throws Exception{
        Object o = runScript(JEXL, "samples/function_arg");
        assertEquals(0,o);
    }
}
