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

import org.junit.Test;

import java.io.File;
import java.util.HashMap;

/**
 * Tests for while statement.
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
        System.out.printf("%s\n",o) ;
    }

    @Test
    public void testUndefinedVariable() throws Exception {
        Expression e = JEXL.createExpression("defined x");
        JexlContext jc = new MapContext();
        jc.set("x", null) ;
        jc.set("y", "10");
        assertEquals(true,e.evaluate(jc));
        e = JEXL.createExpression("defined y");
        assertEquals(true,e.evaluate(jc));
        e = JEXL.createExpression("defined z");
        assertEquals(false,e.evaluate(jc));


    }
    @Test
    public void testNULLIndexer() throws Exception {
        Expression e = JEXL.createExpression("x[0]");
        JexlContext jc = new MapContext();
        Exception shouldBe = null;
        jc.set("x", null) ;
        jc.set("y", "10");
        try{
            e.evaluate(jc);
        }catch (Exception ex){
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
        assertEquals(100,o);
    }
    @Test
    public void testAnonymousFunction() throws Exception {

        JEXL.setFunctions(Main.getFunction());
        Script e = JEXL.createScript("set{$_ * 10 }(y)");
        JexlContext jc = new MapContext();
        jc.set("y",new int[]{1,1,2,2,3,4} );

        Object o = e.execute(jc);
        assertTrue(o!=null);
        e = JEXL.createScript("set{$_ * 10 }(1,2,3,4)");
        o = e.execute(jc);
        assertTrue(o!=null);
        e = JEXL.createScript("lgc:multiset{$_ * 10} (1,2,2,3,3,3,4,4,4,4)");
        o = e.execute(jc);
        assertTrue(o!=null);

    }
    @Test
    public void testAnonymousExternalFunction() throws Exception {

        JEXL.setFunctions(Main.getFunction());
        JexlContext jc = new MapContext();
        Script e = JEXL.createScript("lgc:multiset{$_ * 10} (1,2,2,3,3,3,4,4,4,4)");
        Object o = e.execute(jc);
        assertTrue(o!=null);

    }

    public void testScriptWithMethods() throws Exception{
        JEXL.setFunctions(Main.getFunction());
        JexlContext jc = new MapContext();
        Script e = JEXL.createScript(new File("samples/dummy.jexl"));
        Object o = e.execute(jc);
        assertTrue(o!=null);
    }
}
