/**
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

import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.lang.extension.datastructures.Graph;
import com.noga.njexl.lang.extension.datastructures.ListSet;
import com.noga.njexl.lang.extension.SetOperations;
import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.extension.iterators.YieldedIterator;
import com.noga.njexl.lang.extension.oop.ScriptMethod;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Tests for while statement.
 *
 * @since 2.2-N
 */
public class NogaExtendedTest extends JexlTestCase {

    public NogaExtendedTest(String testName) {
        super(testName);
    }

    private void testErrorMessageRun(String expression){
        try {
            Expression expr = JEXL.createExpression(expression);
            expr.evaluate(new MapContext());
        }catch (JexlException e){
            System.err.println(e.getFaultyCode());
        }
    }
    @Test
    public void testProperMessage() {
        testErrorMessageRun("x={}");
        testErrorMessageRun("[1,2] = [3,4]");
        testErrorMessageRun("x=x''");
    }

    @Test
    public void testDef() throws Exception {
        Expression e = JEXL.createExpression("#def x");
        JexlContext jc = new MapContext();
        Object o = e.evaluate(jc);
        assertFalse((Boolean) o);

        e = JEXL.createExpression("#def(10)");
        o = e.evaluate(jc);
        assertTrue((Boolean) o);

        e = JEXL.createExpression("#def(null)");
        o = e.evaluate(jc);
        assertTrue((Boolean) o);

        jc.set("x","");
        e = JEXL.createExpression("#def(x)");
        o = e.evaluate(jc);
        assertTrue((Boolean) o);

        Script s = JEXL.createScript("y = {'a' : 10 }; #def y.k");
        o = s.execute(jc);
        assertFalse((Boolean) o);

        s = JEXL.createScript(" #def y.a");
        o = s.execute(jc);
        assertTrue((Boolean) o);

    }

    @Test
    public void testNumerals() throws Exception {
        Expression e = JEXL.createExpression("1 == '   1   '");
        JexlContext jc = new MapContext();
        Object o = e.evaluate(jc);
        assertTrue((Boolean) o);

        e = JEXL.createExpression("char('a') + char('b') ");
        o = e.evaluate(jc);
        assertEquals("ab",o);

        e = JEXL.createExpression("char('A') + 0 "); // ascii out
        o = e.evaluate(jc);
        assertEquals( 65  ,o);

    }

    @Test
    public void testFloatString() throws Exception {
        Expression e = JEXL.createExpression("str(0.0001,1)");
        JexlContext jc = new MapContext();
        Object o = e.evaluate(jc);
        assertTrue(o.equals("0.0"));

        e = JEXL.createExpression("str(0.0001,6)");
        o = e.evaluate(jc);
        assertTrue(o.equals("0.000100"));

        e = JEXL.createExpression("str(0.0081,2)");
        o = e.evaluate(jc);
        assertTrue(o.equals("0.01"));

        // test for double
        e = JEXL.createExpression("str(0.0081d,2)");
        o = e.evaluate(jc);
        assertTrue(o.equals("0.01"));

        // test for big decimal
        e = JEXL.createExpression("str(0.0081b,2)");
        o = e.evaluate(jc);
        assertTrue(o.equals("0.01"));

        BigInteger bi = new BigInteger("11", 2);
        jc.set("bi",bi);
        // test for big int radix
        e = JEXL.createExpression("str(bi,2)");
        o = e.evaluate(jc);
        assertEquals(bi.toString(2), o);

        e = JEXL.createExpression("str(bi)");
        o = e.evaluate(jc);
        assertEquals("3", o);

        e = JEXL.createExpression("str(3,2)");
        o = e.evaluate(jc);
        assertEquals("11", o);

        e = JEXL.createExpression("str(3l,2l)");
        o = e.evaluate(jc);
        assertEquals("11", o);

        e = JEXL.createExpression("str(3,2.0)");
        o = e.evaluate(jc);
        assertEquals("11", o);

    }

    @Test
    public void testCastType() throws Exception {
        Expression e = JEXL.createExpression("x = float( 0.000000010100) ");
        JexlContext jc = new MapContext();
        Object o = e.evaluate(jc);
        assertTrue(o instanceof Float );

        e = JEXL.createExpression("y = double( 0.000001) ");
        o = e.evaluate(jc);
        assertTrue(o instanceof Double );

        e = JEXL.createExpression("x =  DEC ( 0.00000001) ");
        o = e.evaluate(jc);
        assertTrue(o instanceof BigDecimal );

        e = JEXL.createExpression("I = INT(100) ");
        o = e.evaluate(jc);
        assertEquals(BigInteger.valueOf(100), o );

        e = JEXL.createExpression("I = INT( ' 101  ' ,2) ");
        o = e.evaluate(jc);
        assertEquals(BigInteger.valueOf(5), o );

        e = JEXL.createExpression("I = INT( ' xxx  ' ,2, 1) ");
        o = e.evaluate(jc);
        assertEquals(BigInteger.ONE, o );

        e = JEXL.createExpression("I = INT( ' xxx  ' ,10) ");
        o = e.evaluate(jc);
        assertEquals(null, o );

        e = JEXL.createExpression("D = DEC( ' 1  ' ,0) ");
        o = e.evaluate(jc);
        assertEquals(BigDecimal.ONE, o );

        e = JEXL.createExpression("D = DEC( ' xxx  ' ) ");
        o = e.evaluate(jc);
        assertEquals(null, o );

        e = JEXL.createExpression("D = DEC( ' xxx  ' , 1) ");
        o = e.evaluate(jc);
        assertEquals(BigDecimal.ONE, o );


        e = JEXL.createExpression("byte(12)");
        o = e.evaluate(jc);
        assertTrue(o instanceof Byte);
        assertEquals((byte)12,o);

    }
    @Test
    public void testExponentialDecimalType() throws Exception {
        Expression e = JEXL.createExpression("x =  0e-3");
        JexlContext jc = new MapContext();
        Object o = e.evaluate(jc);
        assertTrue(o instanceof Float);

        e = JEXL.createExpression("x =  1.0e-3");
        o = e.evaluate(jc);
        assertTrue(o instanceof Float);

        e = JEXL.createExpression("x =  1.3e-4");
        o = e.evaluate(jc);
        assertTrue(o instanceof Float);

        e = JEXL.createExpression("x = 4.2223232323232233232e-20");
        o = e.evaluate(jc);
        assertTrue(o instanceof BigDecimal);

    }


    @Test
    public void testAutoBigDecimalType() throws Exception {
        Expression e = JEXL.createExpression("x =  0.000000010100");
        JexlContext jc = new MapContext();
        Object o = e.evaluate(jc);
        assertTrue(o instanceof Float );

        e = JEXL.createExpression("x =  0.00000001010000001");
        o = e.evaluate(jc);
        assertTrue(o instanceof Double );

        e = JEXL.createExpression("x =  0.000000010100000010000011001010100010011");
        o = e.evaluate(jc);
        assertTrue(o instanceof BigDecimal );

        e = JEXL.createExpression("  0.000000010100000010000011001010100010011 + 0.01 ");
        o = e.evaluate(jc);
        assertTrue(o instanceof BigDecimal );

        e = JEXL.createExpression("  0.000000010100000010000011001010100010011 -  0.01 ");
        o = e.evaluate(jc);
        assertTrue(o instanceof BigDecimal );

        e = JEXL.createExpression("  0.000000010100000010000011001010100010011 * 0.01 ");
        o = e.evaluate(jc);
        assertTrue(o instanceof BigDecimal );

        e = JEXL.createExpression("  0.000000010100000010000011001010100010011 / 0.01 ");
        o = e.evaluate(jc);
        assertTrue(o instanceof BigDecimal );


        e = JEXL.createExpression("-3.12b ");
        o = e.evaluate(jc);
        assertTrue(o instanceof BigDecimal );

    }

    @Test
    public void testNumberTypeEquality() throws Exception {
        Expression e = JEXL.createExpression("1 === '   1   '");
        JexlContext jc = new MapContext();
        Object o = e.evaluate(jc);
        assertFalse((Boolean)o);
        e = JEXL.createExpression("1 === 1");
        o = e.evaluate(jc);
        assertTrue((Boolean) o);
        e = JEXL.createExpression("1 === int(1.0) ");
        o = e.evaluate(jc);
        assertTrue((Boolean) o);

        e = JEXL.createExpression("-1 === int(-1.4) ");
        o = e.evaluate(jc);
        assertTrue((Boolean) o);


        e = JEXL.createExpression("-1l === long(-1.4) ");
        o = e.evaluate(jc);
        assertTrue((Boolean) o);

    }

    @Test
    public void testStringEscape() throws Exception {
        Expression e = JEXL.createExpression("'x\ny\tz\rt'");
        JexlContext jc = new MapContext();
        Object o = e.evaluate(jc);
        assertTrue("x\ny\tz\rt".equals(o));
    }

    @Test
    public void testNonStandardEquality() throws Exception {
        Expression e = JEXL.createExpression("x == y");
        JexlContext jc = new MapContext();
        jc.set("x", new Integer(1));
        jc.set("y", "y");
        Object o = e.evaluate(jc);
        System.out.printf("%s\n", o);
    }

    @Test
    public void testNULLIndexer() throws Exception {
        Expression e = JEXL.createExpression("x[0]");
        JexlContext jc = new MapContext();
        Exception shouldBe = null;
        jc.set("x", null);
        jc.set("y", "10");
        try {
            e.evaluate(jc);
        } catch (Exception ex) {
            shouldBe = ex;
        }
        assertNotNull("Should be null ref exception", shouldBe);
    }

    @Test
    public void testLineStatement() throws Exception {
        Script e = JEXL.createScript("if ( true ) { \n x = y \\\r *10 \n return x*10 }else{\n  return 0 }");
        JexlContext jc = new MapContext();
        jc.set("y", new Integer(1));

        Object o = e.execute(jc);
        assertEquals(100, o);
    }

    @Test
    public void testThreading() throws Exception {
        Script e = JEXL.createScript("t = thread{ $.sleep(300)  ; x = x + 1 ; }()");
        JexlContext jc = new MapContext();
        jc.set("x",0);
        Object o = e.execute(jc);
        assertTrue( o instanceof Thread);
        Thread.sleep(600);
        o = jc.get("x");
        assertEquals(1,o);
    }

    @Test
    public void testCommaLineStatement() throws Exception {
        Script e = JEXL.createScript("minmax(1,\n2  ,  \n3)");
        JexlContext jc = new MapContext();
        Object[] o = (Object[])e.execute(jc);
        assertEquals(1, o[0]);
        assertEquals(3, o[1]);
    }

    @Test
    public void testShuffle() throws Exception{
        Script e = JEXL.createScript("x=[1,2,3,4,5] ; shuffle(x) ");
        JexlContext jc = new MapContext();
        e.execute(jc);
        Object o = jc.get("x");
        assertTrue(o != null);

        e = JEXL.createScript("x=list(1,2,3,4,5) ; shuffle(x) ");
        e.execute(jc);
        o = jc.get("x");
        assertTrue(o != null);
    }

    @Test
    public void testPowerStatement() throws Exception {
        Script e = JEXL.createScript("x=0.1 ; x**2 ");
        JexlContext jc = new MapContext();
        Object o = e.execute(jc);
        assertEquals(0.01, o);
        // classic eqvp  cases
        e = JEXL.createScript("x=0.1 ; x**1 ");
        o = e.execute(jc);
        assertEquals(0.1, o);

        e = JEXL.createScript("x=0.1 ; x**0 ");
        o = e.execute(jc);
        assertEquals(1.0, o);

        e = JEXL.createScript("x=10 ; x**-1 ");
        o = e.execute(jc);
        assertEquals(0.1, o);

        e = JEXL.createScript("x=10 ; x**-2 ");
        o = e.execute(jc);
        assertEquals(0.01, o);

        e = JEXL.createScript("x=10 ; x**1 ");
        o = e.execute(jc);
        assertEquals(10, o);

        e = JEXL.createScript("x=10 ; x**0 ");
        o = e.execute(jc);
        assertEquals(1, o);

        e = JEXL.createScript("x=10 ; x**3 ");
        o = e.execute(jc);
        assertEquals(1000, o);


        e = JEXL.createScript("x=0.1 ; x**0.1 ");
        o = e.execute(jc);
        assertTrue(o instanceof Double);

        e = JEXL.createScript("x=0.1 ; x**0.1b ");
        o = e.execute(jc);
        assertTrue(o instanceof BigDecimal);


        e = JEXL.createScript("x=0.00000010102 ; x**-3.12 ");
        o = e.execute(jc);
        assertTrue(o instanceof Double);

        e = JEXL.createScript("x=0.00000010102 ; x**-3.12b ");
        o = e.execute(jc);
        assertTrue(o instanceof BigDecimal);

        e = JEXL.createScript("10h**3");
        o = e.execute(jc);
        assertTrue(o instanceof BigInteger);

        e = JEXL.createScript("10h**3h");
        o = e.execute(jc);
        assertTrue(o instanceof BigInteger);

        e = JEXL.createScript("10**3h");
        o = e.execute(jc);
        assertTrue(o instanceof BigInteger);

        e = JEXL.createScript("10h**3.0");
        o = e.execute(jc);
        assertTrue(o instanceof BigDecimal);

        e = JEXL.createScript("10h**3.0b");
        o = e.execute(jc);
        assertTrue(o instanceof BigDecimal);

        e = JEXL.createScript("x = [0,1]**-1 ; x.0 == 1 and x.1 == 0 ");
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean) o);

        e = JEXL.createScript("x = [0,1] ** 0 ; empty(x) ");
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean) o);

    }

    @Test
    public void testInOperation() throws Exception {
        Script e = JEXL.createScript("x={1:2} ; 1 @ x ;");
        JexlContext jc = new MapContext();
        Object o = e.execute(jc);
        assertTrue((Boolean)o);

        e = JEXL.createScript("x={1:2} ; y ={ 1:2  } ; x @ y ;");
        o = e.execute(jc);
        assertTrue((Boolean) o);

        e = JEXL.createScript("x={1:2} ; y ={ 1:2 , 3:4 } ; x @ y ;");
        o = e.execute(jc);
        assertTrue((Boolean) o);


        e = JEXL.createScript("x={1:2} ; y ={ 1:2 , 3:4 } ; y @ x ;");
        o = e.execute(jc);
        assertFalse((Boolean) o);

    }

    @Test
    public void testEventing() throws Exception{
        Script e = JEXL.createScript("import 'java.lang.System.out' as out ; @@out.println('hi'); ");
        JEXL.setFunctions(new HashMap<>());
        JexlContext jc = new MapContext();
        e.execute(jc);
    }

    @Test
    public void testNULLInSize() throws Exception{
        Script e = JEXL.createScript("size(null) == -1");
        JexlContext jc = new MapContext();
        Object o = e.execute(jc);
        assertTrue((Boolean)o);
        e = JEXL.createScript("#|null| == 0");
        o = e.execute(jc);
        assertTrue((Boolean) o);
    }

    @Test
    public void testGraph() throws Exception{
        Graph g = new Graph( "samples/graph.json");
        assertNotNull(g);
        assertFalse(g.nodes.isEmpty());
    }

    @Test
    public void testSorting() throws Exception {
        Script e = JEXL.createScript("x=list(2.2,1.2,1.0,-0.9) ; x = sorta(x);");
        JexlContext jc = new MapContext();
        e.execute(jc);
        List<Comparable> x = (List)jc.get("x");
        assertTrue( x.get(0).compareTo( x.get(x.size()-1) ) < 0 ) ;

        e = JEXL.createScript("x=list(2.2,1.2,1.0,-0.9) ; x = sortd(x);");
        e.execute(jc);
        x = (List)jc.get("x");
        assertTrue( x.get(0).compareTo( x.get(x.size()-1) ) > 0 ) ;

        // now the anonymous stuff!
        e = JEXL.createScript("x=list('1.23', 2.0, -0.1,'0.0') ; x = sorta{ double($[0]) < double($[1]) }(x);");
        e.execute(jc);
        x = (List)jc.get("x");
        assertTrue(x.get(0).equals(-0.1f));
        assertTrue(x.get(x.size() - 1).equals(2.0f));

        // now the anonymous stuff!
        e = JEXL.createScript("x=[ '1.23', 2.0, -0.1,'0.0' ] ; x = sorta{ double($[0]) < double($[1]) }(x);");
        e.execute(jc);
        Object[] xx = (Object[])jc.get("x");
        assertTrue(xx[0].equals(-0.1f));
        assertTrue(xx[xx.length - 1].equals(2.0f));


        // now the anonymous stuff!
        e = JEXL.createScript("x=list('1.23', 2.0, -0.1,'0.0') ; x = sortd{ double($[0]) < double($[1]) }(x);");
        e.execute(jc);
        x = (List)jc.get("x");
        assertTrue(x.get(x.size() - 1).equals(-0.1f));
        assertTrue(x.get(0).equals(2.0f));

        // array should be returned by array again

        e = JEXL.createScript("x=[ '1.23', 2.0, -0.1,'0.0']  ; x = sortd{ double($[0]) < double($[1]) }(x);");
        e.execute(jc);
        xx = (Object[])jc.get("x");
        assertTrue(xx[xx.length - 1].equals(-0.1f));
        assertTrue(xx[0].equals(2.0f));

    }

    @Test
    public void testSetFunctions() throws Exception {
        ListSet oe = TypeUtility.set(new int[]{});

        ListSet a = TypeUtility.set(new int[]{0, 1, 2, 3});
        ListSet b = TypeUtility.set(new int[]{0, 1, 2, 3, 4});
        ListSet c = TypeUtility.set(new int[]{5, 6});

        Assert.assertTrue(SetOperations.is_set_relation(oe, oe, "="));

        Assert.assertTrue(SetOperations.is_set_relation(oe, a, "<"));

        Assert.assertTrue(SetOperations.is_set_relation(a, oe, ">"));

        Assert.assertTrue(SetOperations.is_set_relation(a, b, "<"));

        Assert.assertTrue(SetOperations.is_set_relation(b, a, ">"));

        Assert.assertTrue(SetOperations.is_set_relation(c, a, "><"));

        Assert.assertTrue(SetOperations.is_set_relation(c, c, "="));

    }

    @Test
    public void testMultiSetFunctions() throws Exception {
        HashMap oe = SetOperations.multiset(new int[]{});

        HashMap a = SetOperations.multiset(new int[]{0, 1, 2, 3, 3, 3, 3});
        HashMap b = SetOperations.multiset(new int[]{0, 1, 2, 3, 3, 3, 3, 4, 5});
        HashMap c = SetOperations.multiset(new int[]{5, 6, 6, 7, 8});

        Assert.assertTrue(SetOperations.is_mset_relation(oe, oe, "="));

        Assert.assertTrue(SetOperations.is_mset_relation(oe, a, "<"));

        Assert.assertTrue(SetOperations.is_mset_relation(a, oe, ">"));

        Assert.assertTrue(SetOperations.is_mset_relation(a, b, "<"));

        Assert.assertTrue(SetOperations.is_mset_relation(b, a, ">"));

        Assert.assertTrue(SetOperations.is_mset_relation(c, a, "><"));

        Assert.assertTrue(SetOperations.is_mset_relation(c, c, "="));

    }

    @Test
    public void testListNotEquals() throws Exception {
        Expression e = JEXL.createExpression("[0,0,1] == [2,3]");
        JexlContext jc = new MapContext();
        Object o = e.evaluate(jc);
        assertFalse((Boolean)o);

        e = JEXL.createExpression("[0,0,1] != [2,3]");
        o = e.evaluate(jc);
        assertTrue((Boolean) o);
    }

    @Test
    public void testJoin() throws Exception{
        int[] l = new int[]{0, 1, 2, 3};
        String[] r = new String[]{"hi","hello"};
        List ret = SetOperations.join(l, r);
        assertTrue(ret.size() == l.length * r.length);
        JexlContext jc = new MapContext();
        Script e = JEXL.createScript("l = [0:2].list() ; x = join{ $ = '' + $.0 + $.1 ; true  }(l,l) ; x == ['00' ,'01', '10', '11' ]");
        Object o = e.execute(jc);
        assertTrue((Boolean) o);

        e = JEXL.createScript("l = [0:2].list() ; x = join{ where(true) { } }(l,l) ;" );
        o = e.execute(jc);
        assertTrue( o instanceof List);
        assertEquals( 4, ((List)o).size()  );
        assertTrue( ((List)o).get(0) instanceof List);

    }

    @Test
    public void testConditionalJoin() throws Exception{
        List l1 = TypeUtility.from(new int[]{0, 1, 2, 3, });
        List l2 = TypeUtility.from(new String[]{"hi","hello" , "bye"});
        List l3 = TypeUtility.from(new boolean[] {true,false });

        List ret = SetOperations.join_c(l1, l2, l3);
        Assert.assertTrue(ret.size() == l1.size() * l2.size() *l3.size());
    }

    @Test
    public void testAnonymousFunction() throws Exception {

        JexlContext jc = new MapContext();
        JEXL.setFunctions(Main.getFunction(jc));
        Script e = JEXL.createScript("set{$ * 10 }(y)");
        jc.set("y", new int[]{1, 1, 2, 2, 3, 4});

        Object o = e.execute(jc);
        assertTrue(((Set) o).size() == 4);
        e = JEXL.createScript("set{$ * 10 }(1,2,2,2,3,4)");
        o = e.execute(jc);
        assertTrue(((Set) o).size() == 4);
        e = JEXL.createScript("multiset{$ * 10} (1,2,2,3,3,3,4,4,4,4)");
        o = e.execute(jc);
        assertTrue(((Map) o).size() == 4);

        e = JEXL.createScript("a={1:2, 3:4};b=3 ; a[b]==4");
        o = e.execute(jc);
        assertTrue(o.equals(Boolean.TRUE));

        e = JEXL.createScript("x=set(1,2,2,3,3,3,4,4,4,4);x['y']");
        o = e.execute(jc);
        assertTrue(o.equals(Boolean.FALSE));
        e = JEXL.createScript("x=set(1,5,10);y=2; x[y]==10");
        o = e.execute(jc);
        assertTrue(o.equals(Boolean.TRUE));
    }

    @Test
    public void testScriptWithMethods() throws Exception {
        JexlContext jc = new MapContext();
        JEXL.setFunctions(Main.getFunction(jc));
        Script e = JEXL.createScript(new File("samples/dummy.jexl"));
        Object o = e.execute(jc);
        assertTrue(o != null);
    }

    @Test
    public void testScriptWithImportedMethods() throws Exception {
        JexlContext jc = new MapContext();
        JEXL.setFunctions(Main.getFunction(jc));
        Script e = JEXL.createScript(" import 'samples/dummy.jexl' as dummy ; some_func('Hello, World!') ");
        Object o = e.execute(jc);
        assertTrue(o != null);

    }

    @Test
    public void testWithMixedModeClass() throws Exception {
        JexlContext jc = new MapContext();
        // basically test accessing random fields - where another getter is available
        String s = "x = dict() ; x.MAXIMUM_CAPACITY " ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertNotNull(o);
    }

    @Test
    public void testCastString() throws Exception {
        JexlContext jc = new MapContext();
        String s = "str(null) == 'null' " ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "str([1,2]) == '1,2' " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "str(list(1,2) ) == '1,2' " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "str(2.3456,3) === '2.346' " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "str(2.3456,'##.###') === '2.346' " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "str(date(), 'dd-MM-yyyy' ) === str(time(), 'dd-MM-yyyy' ) " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "str{[1,2] }('','&') === '1&2'" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

    }

    @Test
    public void testRandomRange() throws Exception {
        JexlContext jc = new MapContext();

        String s = "random() " ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue(o instanceof Random );

        s = "random(0)" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Double );

        s = "random(0.2f)" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Float );

        s = "random(0.2d)" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Double );

        s = "random(1l)" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Long );

        s = "random(false)" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Boolean );

        s = "random(10h)" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof BigInteger );

        s = "random(0.00001b)" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof BigDecimal );

        s = "random(10, 100 )" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Integer );
        assertTrue((int)o < 100 && (int)o >=10  );

        s = "A = [0,1,2,3,4] ; r = random( A ) ; r @ A ;" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Boolean );
        assertTrue((Boolean)o);

        s = "r = random( A, 2  ) ;  A > set(r) and size(r) == 2  ;" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Boolean );
        assertTrue((Boolean)o);

        s = "L = [0:5] ; r = random( L ) ; r @ L " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Boolean );
        assertTrue((Boolean)o);

        s = "r = random( L , 3 ) ; L >= set(r) and size(r) == 3 ; " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Boolean );
        assertTrue((Boolean)o);


        s = "M = {1:2 , 3:4 , 5:6 , 7:8 } ; r = random( M ) ; r @ M " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Boolean );
        assertTrue((Boolean)o);

        s = " r = random( M,2 ) ; r <= M and size(r) <= 2 ; " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Boolean );
        assertTrue((Boolean)o);

        s = " r = random( 'abcdefghijklmnop',10 ) ; size(r) == 10 ; " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Boolean );
        assertTrue((Boolean)o);


    }

    private static class XClass {

        private long i;

        public Long L = null ;

        private XClass() {
            i = System.nanoTime() ;
        }

        private void m(String message){
            System.out.println(message);
        }

        public static void printHello(String message){
            System.out.println(message);
        }
    }

    @Test
    public void testWithNullField() throws Exception {
        JexlContext jc = new MapContext();
        HashMap x = new HashMap();
        x.put("y",null);
        jc.set("x", x );
        jc.set( "X", new XClass());

        Expression e = JEXL.createExpression("z = ( x.y == null )");
        Object o = e.evaluate(jc);
        assertTrue((Boolean)o);

        e = JEXL.createExpression("z = ( X.L == null )");
        o = e.evaluate(jc);
        assertTrue((Boolean)o);

    }

    @Test
    public void testWithPrivateFields() throws Exception {
        JexlContext jc = new MapContext();
        JEXL.setFunctions(Main.getFunction(jc));
        String s = String.format("x = new('%s') ; x.i = 204 ; x.i", XClass.class.getName());
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue(o.equals(204l));
    }

    @Test
    public void testDict() throws Exception {
        JexlContext jc = new MapContext();
        jc.set("l", new Object[]{  new XClass(),new XClass() , new XClass() }  );
        Script e = JEXL.createScript("dict{ [$.i , $] }(l)");
        Object o = e.execute(jc);
        assertTrue(o instanceof Map);
        assertTrue(((Map)o).size() == 3);

        e = JEXL.createScript("x = [date(), date(), date() ] ; "
                + " dict{ s = $.seconds ; [ s , (s @ _$_) ? (_$_[s]+= $) : list() ] }(x)");
        o = e.execute(jc);
        assertTrue(o instanceof Map);

        e = JEXL.createScript("x = [1,2,3] ; y = dict{ [$.0 , $.1 **2 ] }(x,x); y == {1:1 , 2:4 , 3:9 }");
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);

        e = JEXL.createScript("x = [1,2,3] ; y = dict(x,x); y == {1:1 , 2:2 , 3:3 }");
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);

        e = JEXL.createScript("x = [1,2,3] ; y = dict([],[],[]); y == {:}");
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);

    }

    @Test
    public void testWithPrivateMethods() throws Exception {
        JexlContext jc = new MapContext();
        JEXL.setFunctions(Main.getFunction(jc));
        String s = String.format("y= new('%s'); z = y.class.forName('java.lang.System'); ", XClass.class.getName());
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue(o.equals(System.class));

        s = String.format("import '%s' as zzz ; zzz:printHello('hi!'); xx = new(zzz) ; xx.i=42000", XClass.class.getName());
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o.equals(42000));
    }

    @Test
    public void testWithSelectAndContinue() throws Exception{
        JexlContext jc = new MapContext();
        String s = "x = [1,2,3,4,5,6] ; y = select{ continue( $%2==0){ $ } }(x) ; y == [2,4,6] ;" ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue((Boolean) o);
    }


    @Test
    public void testWithStaticFields() throws Exception{
        JexlContext jc = new MapContext();
        JEXL.setFunctions(Main.getFunction(jc));
        String s = "sys.out.println('hi!');" ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
    }

    @Test
    public void testStringCatenation() throws Exception{
        JexlContext jc = new MapContext();
        JEXL.setFunctions(Main.getFunction(jc));
        String s = "'1' + '1' " ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue("11".equals(o));
    }

    @Test
    public void testShortAssignment() throws Exception {
        JexlContext jc = new MapContext();
        JEXL.setFunctions(Main.getFunction(jc));
        String s = "x = short('jshjkdhfjs')" ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertEquals(null, o);

        s = "x = short('jshjkdhfjs' , 0 )" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals((short)0,o);

        s = "x = short('j')" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertNotNull(o);

        s = "x = short('j', 0)" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertNotSame((short)0,o);

    }

    @Test
    public void testTupleAssignment() throws Exception{
        JexlContext jc = new MapContext();
        JEXL.setFunctions(Main.getFunction(jc));
        String s = "x = [ 1,2,3] ; #(a,b,c) = x ; a==1 and b == 2 and c == 3 ;" ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "x = [ 1,2,3] ; #(a,b) = x ; a ==1 and b == 2 ; " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "import 'java.lang.Integer' as JInt ; #(o,:e) = JInt:parseInt('420') ; o == 420 and e == null ;" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "import 'java.lang.Integer' as JInt ; #(o,:e) = JInt:parseInt('Sri 420') ; o == null and e != null ;" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "l = [1,2,3,4]; #(a,:b) = list{ $ * 10 }(l) ; size(a) == 4 and b == null ; " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);


        s = "p1=#(0,0) ; p2 = #(0,0) ; p1 == p2 ;" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);


        s = "p = #(0,0) ; p[0] == 0 and p[1] == 0 ; " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        //  assignment from right
        s = "#(:a,b) = [1,2,3,4] ; a == 3 and b == 4" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        //  assignment from right
        s = "#(:a,b) = [1,2,3,4]" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(2, Array.getLength(o));



        //  assignment from right - again
        s = "#(:a,b,c) = [1,2,3,4] ; a == 2 and b == 3 and c == 4" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        //  assignment from right - exact
        s = "#(:a,b,c) = [1,2,3] ; a == 1 and b == 2 and c == 3" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        //  assignment from right - exact with one null
        s = "#(:a,b,c,d) = [1,2,3] ; a == null and b == 1 and c == 2 and d == 3" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);


        //  assignment from right - with many null
        s = "#(:a,b,c,d,e) = [1,2,3] ; e == 3 and d == 2 and c == 1 and b == null and a == null" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);


    }

    @Test
    public void testEquals() throws Exception {
        JexlContext jc = new MapContext();
        String s = "1h == 1.0" ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "1h == 1.01" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertFalse((Boolean) o);


        s = "1h == 1.0000000001001" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertFalse((Boolean) o);


        s = "1h == 1.0000000000000" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);


        s = "1h == 1.00000010001b" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertFalse((Boolean) o);

        s = "1h == 1.00000b" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "1.00000b == 1h" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "1.000001b == 1.000001" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);


        s = "1.000001000000b == 1.000001" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "1.000001d == 1.000001f" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);


        s = "char('a') == 'a' " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "x = 3L % 2 " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(1, o);

    }

    @Test
    public void testNegativeArrayAccess() throws Exception{
        JexlContext jc = new MapContext();
        jc.set("x", new int[]{ 0,1,2,3} );
        String s = "x[-1] + x[-2] + x[0] + x[1]" ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertEquals(6,o);

    }

    @Test
    public void testCurryingInAnon() throws Exception{
        JexlContext jc = new MapContext();
        jc.set("l", new int[]{ 0,1,2,3} );
        jc.set("P", "`$ #{op} #{val}`" );
        jc.set("val", 1);
        jc.set("op", ">=");

        String s = "select{ `#{P}` }(l)" ;
        Script e = JEXL.createScript(s);
        List l = (List)e.execute(jc);
        assertEquals(3, l.size() );
    }

    @Test
    public void testWaiters() throws Exception{
        JexlContext jc = new MapContext();
        jc.set("i", 4 );

        String s = "until(100)" ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue((boolean)o);
        s = "until{ j = 0 ; /* this j should be nullified */ j+=1 ; i = i - 1 ; return ( i == 0 ) ; }(100,10)" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((boolean) o);
        jc.set("i", 40);
        o = e.execute(jc);
        assertFalse((boolean) o);
        assertFalse(jc.has("j"));

    }

    @Test
    public void testListDivision() throws Exception{
        Object l = new Integer[][]{ {0 , 0 }, { 0, 1 }, {1, 0 } , {1,1 } } ;
        Object r = new Integer[]{ 0 , 1  }  ;
        JexlContext jc = new MapContext();
        jc.set("l",l);
        jc.set("r",r);
        String s = "x = l/r ; x == [0,1] ;" ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue((Boolean)o);
    }

    @Test
    public void testDictSetOperations() throws Exception{
        JexlContext jc = new MapContext();
        String s = "x = { 'a' : 1 , 'b' : 2 } - {'a':1 }; x == {'b' : 2 }" ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue((Boolean)o);

        s = "x = { 'a' : 1 , 'b' : 2 } - [ 'a' ] ; x == { 'b' : 2 }" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);

        s = "x = { 'a' : 1 , 'b' : 2 } -  'a' ; x == { 'b' : 2 }" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);

        s = "x = { 'a' : 1 , 'b' : 2 } | {'a': 1 , 'b' : 0 } ; x.keySet() == ['a', 'b' ] " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);

        s = "x = { 'a' : 1 , 'b' : 2 } & {'a': 1 , 'b' : 0 }; x.keySet() == [ 'a' ] " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);

        s = "x = { 'a' : 1 , 'b' : 2 } ^ {'a': 1 , 'b' : 0 }; empty ( x ) " ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);


        s = "x = { 'a' : 2 , 'b' : 2  , 'c' : 0 } / 2 ;  x == [ 'a' , 'b' ]" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);
    }


    @Test
    public void testFuncInVariable() throws Exception{
        JexlContext jc = new MapContext();
        String s = "x = def (a,b){ a + b} ; x(2,3)" ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertEquals(5,o);
        Expression expression = JEXL.createExpression("x(4,2)");
        o = expression.evaluate(jc);
        assertEquals(6,o);
        e = JEXL.createScript("z = x ; z(4,2)");
        o =  e.execute(jc);
        assertEquals(6,o);
        // now pass as param
        s = " def f(fp,a,b){ fp(a,b) } ; f(z,4,2)" ;
        e = JEXL.createScript(s);
        o =  e.execute(jc);
        assertEquals(6,o);
        // now pass the stuff in a hash - and call
        s = " d = {'m' : z } ; d.m(4,2)" ;
        e = JEXL.createScript(s);
        o =  e.execute(jc);
        assertEquals(6,o);

    }

    @Test
    public void testNullCoalesce() throws Exception{
        JexlContext jc = new MapContext();
        String s = "x = { 'a' : 10 , 'b' : { 'c' : 20 } } ; x??10" ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue(o instanceof Map);
        // not defined y
        s = "y??10" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(10, o);
        // defined and null
        s = "y = null ; y??9" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(9,o);
    }

    @Test
    public void testJSonOp() throws Exception{
        JexlContext jc = new MapContext();
        String s = "{\"firstName\":\"John\", \"lastName\":\"Doe\"} "  ;
        jc.set("t",s);
        Script e = JEXL.createScript("json('t',t);");
        Object o = e.execute(jc);
        assertTrue(o instanceof Map);
        e = JEXL.createScript("json(t);");
        o = e.execute(jc);
        assertTrue(o instanceof Map);
        e = JEXL.createScript("json('f','samples/graph.json');");
        o = e.execute(jc);
        assertTrue(o instanceof Map);
        e = JEXL.createScript("json('samples/graph.json');");
        o = e.execute(jc);
        assertTrue(o instanceof Map);
    }

    @Test
    public void testRange() throws Exception{
        JexlContext jc = new MapContext();
        String s = "r = [10:1]"  ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue( o instanceof YieldedIterator);
        s = "#|r.list()|"  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertFalse( o.equals(0) );
        s = "r = [0:2] ; r ** 2  "  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue( o instanceof List );
        assertEquals(4, ((List)o).size());

        s = "l = [0:2] ; r + l  "  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue( o instanceof List );
        assertEquals(4, ((List)o).size());

        s = "r * l  "  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue( o instanceof List );
        assertEquals(4, ((List)o).size());

        s = "x = r - l ; x  == []"  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue( (Boolean)o);

        s = "r == (r | l) "  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue( (Boolean)o);

        s = "(r & l)  == l "  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue( (Boolean)o);

        s = "(r ^ l)  == [] "  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue( (Boolean)o);

    }

    @Test
    public void testTokenizer() throws Exception{
        JexlContext jc = new MapContext();
        String s = "s = '11,12,13' ; tokens{ int($) }(s,'[0-9]+') ;"  ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue( o instanceof List);
        assertEquals(3, ((List) o).size());

        s = "s = '11,13,34,44,60' ; tokens{ n = int($) ; continue( n > 20 ) ; n  }(s,'[0-9]+') ;"  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue( o instanceof List);
        assertEquals(2, ((List) o).size());

        s = "s = '11,13,34,44,60' ; tokens{ n = int($) ; break( n > 20 ){ n } ; n  }(s,'[0-9]+') ;"  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue( o instanceof List);
        assertEquals(3, ((List) o).size());


    }

    @Test
    public void testArraySplicing() throws Exception{
        JexlContext jc = new MapContext();
        String s = "s = list(1,2,3,4) ; s[[0:3]]"  ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue( o instanceof List);
        assertEquals( 3 ,((List)o).size() );

        s = "s = list(1,2,3,4) ; s[[-1:-3]]"  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue( o instanceof List);
        assertEquals( 2 ,((List)o).size() );

    }


    @Test
    public void testObjectClassImport() throws Exception{
        JexlContext jc = new MapContext();
        String s = "import 'java.lang.Object.class' as OBJ_CLASS"  ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertEquals(Object.class, o);
    }


    @Test
    public void testLambda() throws Exception{
        JexlContext jc = new MapContext();
        String s = "def f( a, F ){ F(a) } ; f(a=10, F = def(a){ a**2 } ) ;"  ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertEquals(100, o);

        s = "def g( a, F ){ F(a) } ; g(10, def(a){ a**2 } ) ;"  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(100, o);


    }

    @Test
    public void testListInVar() throws Exception{
        JexlContext jc = new MapContext();
        String s = "var s =  [ 42 ] ; s[0] "  ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertEquals(42, o);

        s = "var s =  { 'a' : 42  , 1 : 0 } ; s.a + s.1 "  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(42, o);

        s = "var s =  { 'a' : 42  , 1 : 0 } ; s['a'] + s[1]"  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(42, o);

    }

    @Test
    public void testAssertions() throws Exception{
        JexlContext jc = new MapContext();
        String s = "assert()"  ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "assert(true, 'Hard coded to truth!')"  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        String msg = "Hard coded to Fail!" ;
        jc.set("msg", msg );
        s = " assert(false, msg) "  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o.getClass().isArray());
        assertTrue( msg.equals( Array.get(o,0) ) );

        s = " assert{ false }(msg) "  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o.getClass().isArray());
        assertTrue( msg.equals( Array.get(o,0) ) );

        s = " assert{ 1/0  }(msg) "  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o.getClass().isArray());
        assertTrue( msg.equals( Array.get(o,0) ) );

        s = " assert{ 1 == 1  }(msg) "  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);
    }


    @Test
    public void testAssignAdditive() throws Exception{
        JexlContext jc = new MapContext();
        // += test
        String s = "s = 40 ; s+= 2 ;"  ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertEquals( 42 ,o);
        assertEquals( 42 ,jc.get("s"));
        //-= test
        s = "s = 44 ; s-= 2 ;"  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals( 42 ,o);
        assertEquals( 42 ,jc.get("s"));
        // object += and -= tests
        s = "s = [ 0 ] ; s+= 42 ; s == [ 0, 42 ]"  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);

        s = "s = [ 42 , 0 ] ; s-= 0 ; s == [ 42 ]"  ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);

    }

    @Test
    public void testBooleanCast() throws Exception{
        JexlContext jc = new MapContext();
        String s = "bool(0,[1,0])";
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertFalse((Boolean)o );

        s = "bool(1,[1,0])";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);

        s = "bool('x', ['x','y'] )" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);

        // now unconventional code

        jc.set("x", new Object[]{ 1, 2 } );
        jc.set("y", new Object[]{ 3, 4 } );

        s = "bool(x, [x,y] )" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);

        s = "bool(y, [x,y] )" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertFalse((Boolean)o);

    }


    @Test
    public void testDataMatrix() throws Exception{
        JexlContext jc = new MapContext();
        String s = "m = matrix('samples/test.tsv')";
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue(o instanceof DataMatrix);

        s = "size ( m.c(0,[0:3]) );";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(3,o);

        s = "size ( m.c(0,[0,1,2]) );";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(3,o);

        s = "m.select{ $.'First Name' == 'Jill' }() ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof List);
        assertEquals(1, ((List)o).size()  );

        s = "m.select{ not empty ( $.Extra ) }() ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof List);
        assertEquals(2, ((List)o).size()  );

        s = "m.select{ continue( empty ( $.Extra ) ) ; true  }() ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof List);
        assertEquals(2, ((List)o).size()  );

        s = "m.select{ break( _ / 2 > 0  ) ; true  }() ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof List);
        assertEquals(2, ((List)o).size()  );


    }


    @Test
    public void testMethodSerializable() throws Exception{
        JexlContext jc = new MapContext();
        String s = "def f(a){ a**2 } ";
        ScriptMethod sm = ScriptMethod.fromDefinitionText(s,jc);
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream os =  new ObjectOutputStream(b);
        os.writeObject(sm);
        os.close();
        ObjectInputStream is = new ObjectInputStream( new ByteArrayInputStream(b.toByteArray()));
        Object o = is.readObject();
        assertNotNull(o);
        assertTrue(o instanceof ScriptMethod );
        Interpreter i = new Interpreter(JEXL,jc,true,false);
        o = sm.invoke(null,i, new Object[]{ 10 } );
        assertEquals(100,o);
    }


    @Test
    public void testWrite() throws Exception{
        JexlContext jc = new MapContext();
        String s = "a = { 'u' : 'noga' , 'm' : 'post' } ; s = write('https://httpbin.org/post',a)";
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue(o instanceof String);
        s = "p = json(s) ; p.form.u == 'noga' and p.form.m == 'post' ; ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);

        s = "a = { 'u' : 'noga' , 'm' : 'post' } ; s = send('https://httpbin.org/get','GET',a)";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof String);
        s = "p = json(s) ; p.args.u == 'noga' and p.args.m == 'post' ; ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);


    }

    @Test
    public void testFunctionComposition() throws Exception{
        JexlContext jc = new MapContext();
        // this darn 's' is called a successor function
        String s = "def s(){ int(__args__[0]) + 1 ; } ; g = s**42 ; g(0) ";
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertEquals(42,o);

        s = "def s(){ int(__args__[0]) + 1 ; } ; def p(){  int(__args__[0]) - 1 ; } ; h = s*p ; h(0) ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        // axiomatically composition of predecessor and successor is 0
        assertEquals(0, o);

        s = "def f(){  'This is like reality!' } ;  h = f**0 ; h(0); ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o.getClass().isArray() );
        assertEquals(0, Array.get(o,0) );

        s = "def X(){  __args__  } ; X(__args__ = [1,2,3] )  === array(1,2,3) ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);


        s = "def X(){  __args__  } ; X(1,2,3)  === array(1,2,3) ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);

    }

    @Test
    public void testFold() throws Exception{
        JexlContext jc = new MapContext();
        // this darn 's' is called a successor function
        String s = "lfold{ continue( $ > 5 ){ _$_[_] = $ ; _$_ } ; _$_ } ( [0:10] , dict() )  ";
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue(o instanceof Map);
        assertEquals(4, ((Map)o).size() );

        s = "rfold{ continue( $ > 5 ){ _$_[_] = $ ; _$_ } ; _$_ } ( [0:10] , dict() )  ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Map);
        assertEquals(5, ((Map)o).size() );

        s = "lfold{ continue( $ > 5 ){ _$_[_] = $ ; _$_ } ; _$_ } ( [0,1,2,3,4,5,6] , dict() )  ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Map);
        assertEquals(1, ((Map)o).size() );


        s = "rfold{ continue( $ > 5 ){ _$_[_] = $ ; _$_ } ; _$_ } ( [0,1,2,3,4,5,6] , dict() )  ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Map);
        assertEquals(1, ((Map)o).size() );


        s = "lfold{ break( $ > 5 ){ _$_[_] = $ ; _$_ } ; _$_ } ( [0,1,2,3,4,5,6,7,8,9] , dict() )  ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Map);
        assertEquals(1, ((Map)o).size() );

        s = "rfold{ break( $ > 5 ){ _$_[_] = $ ; _$_ } ; _$_ } ( [0,1,2,3,4,5,6,7,8,9] , dict() )  ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Map);
        assertEquals(1, ((Map)o).size() );

        s = "L = lfold( [0,1,2,3,4,5,6,7,8,9] )  ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof String);


        s = "R = rfold( [0,1,2,3,4,5,6,7,8,9] )  ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof String);

        s = "R == L**-1 ";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue(o instanceof Boolean);
        assertTrue((Boolean)o);
    }

    @Test
    public void testIndexFunction() throws Exception {
        JexlContext jc = new MapContext();
        String s = "index(null,null)";
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertEquals(-1, o);
        s = "index([],[1,2,3])";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        // axiomatically empty list is a member of any list
        assertEquals(0, o);
        s = "index([1,2],[1,2,3])";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(0, o);

        s = "index([2,3],[1,2,3])";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(1, o);

        s = "index([3,4],[1,2,3,4])";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(2, o);

        s = "x = [3,4] ; y = [1,2,3,4]; x =~ y ;";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean)o);

        s = "x = [5] ; y = [1,2,3,4]; x =~ y ;";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertFalse((Boolean) o);

        s = "null  =~ null ;";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((Boolean) o);

        s = "x = [5] ; y = null ; x =~ y ;";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertFalse((Boolean) o);

    }

    @Test
    public void testReverseIndexFunction() throws Exception {
        JexlContext jc = new MapContext();
        String s = "rindex(null,null)";
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertEquals(-1, o);
        s = "rindex([],[1,2,3])";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        // axiomatically empty list is a member of any list
        assertEquals(2, o);

        s = "rindex([2],[1,2,3])";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(1, o);

        s = "rindex([1],[1,2,3])";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(0, o);

        s = "rindex([2,3],[1,2,3])";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(1, o);

        s = "rindex([2,3],[1,2,3,2,3,4])";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(3, o);

        s = "rindex([2,3],[1,2,3,2,3])";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(3, o);

        s = "rindex{ $ > 3 } ( [ 6,4,5,6,9,1,2,3] )";
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertEquals(4, o);

    }

}
