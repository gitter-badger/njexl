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

import com.noga.njexl.lang.extension.datastructures.Graph;
import com.noga.njexl.lang.extension.datastructures.ListSet;
import com.noga.njexl.lang.extension.SetOperations;
import com.noga.njexl.lang.extension.TypeUtility;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BooleanSupplier;

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

    }

    @Test
    public void testNumberEquality() throws Exception {
        Expression e = JEXL.createExpression("1 == '   1   '");
        JexlContext jc = new MapContext();
        Object o = e.evaluate(jc);
        assertTrue((Boolean) o);
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
        Script e = JEXL.createScript("t = thread{ $.sleep(1000)  ; x = x + 1 ; }()");
        JexlContext jc = new MapContext();
        jc.set("x",0);
        Object o = e.execute(jc);
        assertTrue( o instanceof Thread);
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
        e = JEXL.createScript("x=list('1.23', 2.0, -0.1,'0.0') ; x = sortd{ double($[0]) < double($[1]) }(x);");
        e.execute(jc);
        x = (List)jc.get("x");
        assertTrue(x.get(x.size() - 1).equals(-0.1f));
        assertTrue(x.get(0).equals(2.0f));

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
        Assert.assertTrue(ret.size() == l.length * r.length);
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
        Script e = JEXL.createScript("dict{ _ = { $.i : $ } }(l)");
        Object o = e.execute(jc);
        assertTrue(o instanceof Map);
        assertTrue(((Map)o).size() == 3);
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
        s = "until{ i = i - 1 ; i == 0 ; }(100,10)" ;
        e = JEXL.createScript(s);
        o = e.execute(jc);
        assertTrue((boolean) o);
        jc.set("i", 40);
        o = e.execute(jc);
        assertFalse((boolean) o);

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
        return;
        /*
        JexlContext jc = new MapContext();
        String s = "x = def(a,b){ a + b} ; x(2,3) ;" ;
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertEquals(5,o);
        */
    }
}
