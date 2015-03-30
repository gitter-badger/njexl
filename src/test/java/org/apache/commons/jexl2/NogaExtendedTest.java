/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.jexl2;

import org.apache.commons.jexl2.extension.ListSet;
import org.apache.commons.jexl2.extension.SetOperations;
import org.apache.commons.jexl2.extension.TypeUtility;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests for while statement.
 *
 * @since 2.2-N
 */
public class NogaExtendedTest extends JexlTestCase {

    public NogaExtendedTest(String testName) {
        super(testName);
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
    public void testUndefinedVariable() throws Exception {
        Expression e = JEXL.createExpression("defined x");
        JexlContext jc = new MapContext();
        jc.set("x", null);
        jc.set("y", "10");
        assertEquals(true, e.evaluate(jc));
        e = JEXL.createExpression("defined y");
        assertEquals(true, e.evaluate(jc));
        e = JEXL.createExpression("defined z");
        assertEquals(false, e.evaluate(jc));


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
        Script e = JEXL.createScript("if ( true ) { \n x = y ...\r *10 \n return x*10 }else{\n  return 0 }");
        JexlContext jc = new MapContext();
        jc.set("y", new Integer(1));

        Object o = e.execute(jc);
        assertEquals(100, o);
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

        HashMap a = SetOperations.multiset(new int[]{0, 1, 2, 3,3,3,3});
        HashMap b = SetOperations.multiset(new int[]{0, 1, 2, 3,3,3,3, 4,5});
        HashMap c = SetOperations.multiset(new int[]{5, 6,6,7,8});

        Assert.assertTrue(SetOperations.is_mset_relation(oe, oe, "="));

        Assert.assertTrue(SetOperations.is_mset_relation(oe, a, "<"));

        Assert.assertTrue(SetOperations.is_mset_relation(a, oe, ">"));

        Assert.assertTrue(SetOperations.is_mset_relation(a, b, "<"));

        Assert.assertTrue(SetOperations.is_mset_relation(b, a, ">"));

        Assert.assertTrue(SetOperations.is_mset_relation(c, a, "><"));

        Assert.assertTrue(SetOperations.is_mset_relation(c, c, "="));

    }
    @Test
    public void testJoin() throws Exception{
        List l = TypeUtility.from(new int[]{0, 1, 2, 3});
        List r = TypeUtility.from(new String[]{"hi","hello"});
        List ret = SetOperations.join(l,r);
        Assert.assertTrue(ret.size() == l.size() * r.size());
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

    private static class XClass {
        private int i;

        private XClass() {
            i = 420;
        }

        private void m(String message){
            System.out.println(message);
        }

        public static void printHello(String message){
            System.out.println(message);
        }
    }

    @Test
    public void testWithPrivateFields() throws Exception {
        JexlContext jc = new MapContext();
        JEXL.setFunctions(Main.getFunction(jc));
        String s = String.format("x = new('%s') ; x.i = 204 ; x.i", XClass.class.getName());
        Script e = JEXL.createScript(s);
        Object o = e.execute(jc);
        assertTrue(o.equals(204));
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
}
