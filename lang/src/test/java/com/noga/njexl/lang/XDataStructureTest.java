/**
 * Copyright 2016 Nabarun Mondal
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
import com.noga.njexl.lang.extension.dataaccess.XmlMap;
import com.noga.njexl.lang.extension.datastructures.ListSet;
import com.noga.njexl.lang.extension.iterators.DateIterator;
import com.noga.njexl.lang.extension.iterators.RangeIterator;
import com.noga.njexl.lang.extension.iterators.SymbolIterator;
import com.noga.njexl.lang.extension.iterators.YieldedIterator;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by noga on 02/02/16.
 */
public class XDataStructureTest extends JexlTestCase {

    @BeforeClass
    public static void beforeClass() throws Exception{

    }

    @AfterClass
    public static void afterClass() throws Exception{
        File file = new File("foo.txt");
        if ( file.exists() ){
            file.delete();
        }
    }

    public XDataStructureTest(String testName) {
        super(testName);
    }

    @Test
    public void testXList() throws Exception {
        Script s = JEXL.createScript("x = list( {1:1, 2:4, 3:9 } ) ");
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof List );

        s = JEXL.createScript("y = str(x)");
        o = s.execute(jc);
        assertTrue(o instanceof String );
        assertFalse(((String)o).isEmpty() );

        s = JEXL.createScript("x.select()");
        o = s.execute(jc);
        assertTrue(o instanceof List );

        s = JEXL.createScript("x.select{ $.key < 3 }()");
        o = s.execute(jc);
        assertTrue(o instanceof List );
        assertEquals( 2, ((List)o).size() );

        s = JEXL.createScript("x.indexOf{ $.key == 3 }()");
        o = s.execute(jc);
        assertEquals( 2 , o );

        s = JEXL.createScript("x.lastIndexOf{ $.key == 1 }()");
        o = s.execute(jc);
        assertEquals( 0 , o );

        s = JEXL.createScript("x.map{  $.0 + $.value }()");
        o = s.execute(jc);
        assertTrue(o instanceof List);
        assertEquals( 3, ((List)o).size() );

        s = JEXL.createScript("x.set{  $.0 + $.1  }() == [ 2, 6, 12] ");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("x.set()");
        o = s.execute(jc);
        assertTrue(o instanceof Set);
        assertEquals( 3, ((Set)o).size() );

        s = JEXL.createScript("x.mset()");
        o = s.execute(jc);
        assertTrue(o instanceof Map);
        assertEquals( 3, ((Map)o).size() );

        s = JEXL.createScript("x.mset{ $.key /3 }( )");
        o = s.execute(jc);
        assertTrue(o instanceof Map);
        assertEquals( 2 , ((Map)o).size() );

    }

    @Test
    public void testListSet() throws Exception {
        Script s = JEXL.createScript("x = set( 1,2,3,4,3,4,5,5,6 ) ");
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof List );
        assertTrue(o instanceof Set );

        s = JEXL.createScript("y = str(x)");
        o = s.execute(jc);
        assertTrue(o instanceof String );
        assertFalse(((String)o).isEmpty() );

        s = JEXL.createScript("x.select()");
        o = s.execute(jc);
        assertTrue(o instanceof List );

        s = JEXL.createScript("x.select{ $ < 3 }()");
        o = s.execute(jc);
        assertTrue(o instanceof Set );
        assertTrue(o instanceof List );
        assertEquals( 2, ((Set)o).size() );

        s = JEXL.createScript("x.map{  $**3 }()");
        o = s.execute(jc);
        assertTrue(o instanceof List);
        assertEquals( 6 , ((List)o).size() );

        s = JEXL.createScript(" x.set{ 2 * $  }() == [1:7].list{ 2* int($) }() ");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("x.equals(x)");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("x.equals(null)");
        o = s.execute(jc);
        assertFalse((Boolean)o);

        s = JEXL.createScript("x.equals(list(1))");
        o = s.execute(jc);
        assertFalse((Boolean)o);

        s = JEXL.createScript("x.indexOf(5) > 0 ");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("x.lastIndexOf(5) > 0 ");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("x[0] = 42 ; 42 == x[0] ");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("x.select{ continue( $%2 == 0 ){true} ; break( $ > 1 ){true} }() ");
        o = s.execute(jc);
        assertTrue(o instanceof Set );
        assertTrue(o instanceof List );

    }

    @Test
    public void testIterators() throws Exception {
        Script s = JEXL.createScript("[0:10].equals( [0:10] )");
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("range(9).equals ( range(10) ) ");
        o = s.execute(jc);
        assertFalse((Boolean)o);

        s = JEXL.createScript("t1 = time() ; t2 = t1.plusDays(10); [t1:t2].equals([t1:t2])");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("[t1:t2].equals([t2 : t2])");
        o = s.execute(jc);
        assertFalse((Boolean)o);

        s = JEXL.createScript("['a' : 'z' ].equals([ 'a': 'z' ])");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("range('z').equals(range('y'))");
        o = s.execute(jc);
        assertFalse((Boolean)o);

        // now the advanced stuff ...

        s = JEXL.createScript("l = [0:2].mul( [0:2] )");
        o = s.execute(jc);
        assertTrue(o instanceof List);
        assertEquals(4, ((List)o).size() );

        s = JEXL.createScript("l = [0:2].exp( 2 ) ");
        o = s.execute(jc);
        assertTrue(o instanceof List);
        assertEquals(4, ((List)o).size() );

        s = JEXL.createScript("[0:10].select{ $ < 2 }() == [0,1] ");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("[0:10].index{ $**2 > 4 }() == 3");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("[0:10].rindex{ $**2 < 4 }() == 1");
        o = s.execute(jc);
        assertTrue((Boolean)o);

    }

    @Test
    public void testYieldedIterators() throws Exception {
        Script s = JEXL.createScript("[0:10].add( [0:10] )");
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof List);

        s = JEXL.createScript("x = [0:10].add(20) ; 20 @ x ");
        o = s.execute(jc);
        assertTrue((Boolean) o);

        s = JEXL.createScript("x = [0:10].sub([4:10]) ;  x  == [ 0,1,2,3] ");
        o = s.execute(jc);
        assertTrue((Boolean) o);

        s = JEXL.createScript("x = [0:10].sub(0) ; 0 @ x ");
        o = s.execute(jc);
        assertFalse((Boolean) o);

        s = JEXL.createScript("x = [0:10].mul([0:10]) ;");
        o = s.execute(jc);
        assertTrue(o instanceof List);

        s = JEXL.createScript("[0:10]");
        o = s.execute(jc);
        assertTrue(o instanceof YieldedIterator);
        YieldedIterator y1 = (YieldedIterator) o;


        s = JEXL.createScript("[0:10:2]");
        o = s.execute(jc);
        assertTrue(o instanceof YieldedIterator);
        YieldedIterator y2 = (YieldedIterator) o;

        assertTrue ( y1.and(y2) instanceof List);
        assertTrue ( y1.and(y2.list()) instanceof List);

        assertTrue ( y1.or(y2) instanceof List);
        assertTrue ( y1.or(y2.list()) instanceof List);

        assertTrue ( y1.xor(y2) instanceof List);
        assertTrue ( y1.xor(y2.list()) instanceof List);

    }

    @Test
    public void testHeap() throws Exception {
        Script s = JEXL.createScript("h = heap(2)");
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof Collection);

        s = JEXL.createScript("lfold{ h+= $ }([0:10]) ; h[0] == 8 and h[1] == 9 ");
        o = s.execute(jc);
        assertTrue((Boolean) o);

        s = JEXL.createScript("h = heap(2,true) ; rfold{ h+= $ }([0:10]) ; h[0] == 2 and h[1] == 1 ");
        o = s.execute(jc);
        assertTrue((Boolean) o);

        s = JEXL.createScript("h = heap(2,true) ; h.addAll([10:0].list ) ; h[0] == 2 and h[1] == 1 ");
        o = s.execute(jc);
        assertTrue((Boolean) o);

        s = JEXL.createScript("empty(h)");
        o = s.execute(jc);
        assertFalse((Boolean) o);

        s = JEXL.createScript("#|h|");
        o = s.execute(jc);
        assertEquals(2, o);

        s = JEXL.createScript("h.find(1)");
        o = s.execute(jc);
        assertEquals(1, o);

        s = JEXL.createScript("l = list(1,2) ; l @ h ;");
        o = s.execute(jc);
        assertTrue( (Boolean) o);

        s = JEXL.createScript("h.containsAll(l) ;");
        o = s.execute(jc);
        assertTrue( (Boolean) o);

        s = JEXL.createScript("h.clear() ;");
        // returns void
        o = s.execute(jc);
        assertNull(o);

        s = JEXL.createScript("h = heap{ $.0 - $.1 }(2) ;");
        // returns void
        o = s.execute(jc);
        assertTrue(o instanceof Collection);

        s = JEXL.createScript("h.addAll( list( 3,4,1,2,10,9 ) ) ;");
        o = s.execute(jc);
        assertTrue(o instanceof Boolean);

        s = JEXL.createScript("h[0] == 9 and h[1] == 10");
        o = s.execute(jc);
        assertTrue(o instanceof Boolean);

    }

    @Test
    public void testFopen() throws Exception{
        Script s = JEXL.createScript("fp = fopen('foo.txt', 'w' ) ");
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof PrintStream );

        s = JEXL.createScript("write(fp,'hi, hello') ; fp.close() ; fp = fopen('foo.txt','r') ;");
        o = s.execute(jc);
        assertTrue(o instanceof BufferedReader);

        s = JEXL.createScript("l = fp.readLine() ; fp.close() ; l ");
        o = s.execute(jc);
        assertTrue(o instanceof String );

        s = JEXL.createScript("fp = fopen('foo.txt','a') ");
        o = s.execute(jc);
        assertTrue(o instanceof PrintStream);

        s = JEXL.createScript("fp.printf('foo bar\n') ; fp.close() ; fp = fopen('foo.txt','r') ;");
        o = s.execute(jc);
        assertTrue(o instanceof BufferedReader);

        s = JEXL.createScript("fp.close(); lines('foo.txt',true) ");
        o = s.execute(jc);
        assertTrue(o instanceof List);

        // implementing wc -l : LOL
        s = JEXL.createScript("lfold{ _$_ + 1  }(lines('foo.txt') , 0 ) ");
        o = s.execute(jc);
        assertTrue(o instanceof Integer);

        s = JEXL.createScript("system('ls','foo.txt')");
        o = s.execute(jc);
        assertEquals(0,o);

    }

    @Test
    public void testREST() throws Exception{
        Script s = JEXL.createScript("data = read('http://httpbin.org/ip', 10000 ,10000 ) ");
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof String );

        s = JEXL.createScript("jo = json(data) ; ");
        o = s.execute(jc);
        assertTrue(o instanceof Map);

        // test encode and decode data
        String data = "hello, World" ;
        jc.set("data", data );

        s = JEXL.createScript("hash('e64', data) ");
        o = s.execute(jc);
        assertTrue(o instanceof String );
        jc.set("data", o );

        s = JEXL.createScript("hash('d64', data) ");
        o = s.execute(jc);
        assertEquals(data, o);
    }

    @Test
    public void testExtendedJexlArithmetic() throws Exception{
        Script s = JEXL.createScript("t1 = time() ; t2 = t1.plusDays(1) ; t2 - t1 ");
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof Long );

        s = JEXL.createScript("t1 -= 1000 ");
        o = s.execute(jc);
        assertTrue(o instanceof DateTime );

        s = JEXL.createScript("x = [0:2].list() ; x.0 ");
        o = s.execute(jc);
        assertTrue(o instanceof Integer );

    }

    @Test
    public void testEnum() throws Exception{
        Script s = JEXL.createScript("import 'com.noga.njexl.lang.extension.SetOperations$SetRelation' as SR ;");
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof Class );
        assertTrue(((Class)o).isEnum() );

        s = JEXL.createScript("random(SR)");
        o = s.execute(jc);
        assertTrue(o instanceof Enum );

        s = JEXL.createScript("random(SR, 3 )");
        o = s.execute(jc);
        assertTrue(o instanceof List );
        assertEquals(3,  ((List)o).size() );

        // can I now access something?
        s = JEXL.createScript("str(SR.OVERLAP)");
        o = s.execute(jc);
        assertEquals("OVERLAP",o);

        // use the enum stuff

        // can I now create an enum ?
        s = JEXL.createScript("e1 = enum(SR)");
        o = s.execute(jc);
        assertNotNull(o);

        // can I now create another same enum?
        s = JEXL.createScript("e2 = enum('com.noga.njexl.lang.extension.SetOperations$SetRelation')");
        o = s.execute(jc);
        assertNotNull(o);

        // let's do equality ?
        s = JEXL.createScript("e2 == e1 ");
        o = s.execute(jc);
        assertTrue((Boolean) o);

        // access properties?
        s = JEXL.createScript("x = e1.OVERLAP  ");
        o = s.execute(jc);
        assertTrue(o instanceof Enum);

        // call toString?
        s = JEXL.createScript("str(e1)");
        o = s.execute(jc);
        assertTrue(o instanceof String);
        assertTrue( ((String)o).startsWith("E") );

        // Something not enum ?
        s = JEXL.createScript("enum('foobar')");
        o = s.execute(jc);
        assertNull(o);
    }

    @Test
    public void testRange() throws Exception{
        Script s = JEXL.createScript("[0:10:2]");
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof RangeIterator);

        s = JEXL.createScript("t = time() ; [t : t.plusDays(7) : 6000000 ] ");
        o = s.execute(jc);
        assertTrue(o instanceof DateIterator);

        s = JEXL.createScript("['a':'z':2]");
        o = s.execute(jc);
        assertTrue(o instanceof SymbolIterator);

    }

    @Test
    public void testAdditiveSameInstance() throws Exception{
        // list
        Script s = JEXL.createScript("x = list(1,2,3,4,5)");
        JexlContext jc = new MapContext();
        Object x = s.execute(jc);
        s = JEXL.createScript(" x+= 42");
        Object y = s.execute(jc);
        assertTrue(x == y);

        s = JEXL.createScript(" x -= 1");
        x = s.execute(jc);
        assertTrue(x == y);

        s = JEXL.createScript(" x -= [ 3,4,5 ]");
        x = s.execute(jc);
        assertTrue(x == y);

        // array
        s = JEXL.createScript(" x = array(1,2,3)");
        x = s.execute(jc);
        s = JEXL.createScript(" x += 42");
        y = s.execute(jc);
        assertTrue(x.getClass() == y.getClass() );
        s = JEXL.createScript(" x -= 2");
        y = s.execute(jc);
        assertTrue(x.getClass() == y.getClass());


        // set
        s = JEXL.createScript(" x = set(1,2,3)");
        x = s.execute(jc);
        s = JEXL.createScript(" x += 42");
        y = s.execute(jc);
        assertTrue(x == y);
        s = JEXL.createScript(" x -= 2");
        y = s.execute(jc);
        assertTrue(x == y);

        s = JEXL.createScript(" x += [4,5,6] ");
        y = s.execute(jc);
        assertTrue(x == y);

        // dict
        s = JEXL.createScript(" x = {1:2 , 3: 4 , 42:42 , 96:69 }");
        x = s.execute(jc);
        s = JEXL.createScript(" x += {5:6}");
        y = s.execute(jc);
        assertTrue(x == y);

        s = JEXL.createScript(" x -= {1:2}");
        y = s.execute(jc);
        assertTrue(x == y);
        assertEquals(4, ((Map)x).size());

        s = JEXL.createScript(" x -= list(42,96)");
        y = s.execute(jc);
        assertTrue(x == y);
        assertEquals(2, ((Map)x).size());


        s = JEXL.createScript(" x -= [3,5]");
        y = s.execute(jc);
        assertTrue(x == y);
        assertTrue(((Map)x).isEmpty());


        s = JEXL.createScript(" x = atomic(0)");
        x = s.execute(jc);
        s = JEXL.createScript(" x += 42");
        y = s.execute(jc);
        assertTrue(x == y);

        s = JEXL.createScript(" x = atomic(0l)");
        x = s.execute(jc);
        s = JEXL.createScript(" x += 42");
        y = s.execute(jc);
        assertTrue(x == y);

        s = JEXL.createScript(" x = atomic(0)");
        x = s.execute(jc);
        s = JEXL.createScript(" x -= 42");
        y = s.execute(jc);
        assertTrue(x == y);

        s = JEXL.createScript(" x = atomic(0l)");
        x = s.execute(jc);
        s = JEXL.createScript(" x -= 42");
        y = s.execute(jc);
        assertTrue(x == y);

    }


    @Test
    public void testCollectionRelations() throws Exception{
        Script s = JEXL.createScript("l = [1,1,2] ; s = set(l) ; s == l " );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertFalse((Boolean)o);

        s = JEXL.createScript("s <= l ");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript(" l >= s  ");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript(" l > s  ");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript(" s < l ");
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript(" s == l ");
        o = s.execute(jc);
        assertFalse((Boolean)o);

        s = JEXL.createScript(" s > l ");
        o = s.execute(jc);
        assertFalse((Boolean)o);

        s = JEXL.createScript(" s != l ");
        o = s.execute(jc);
        assertTrue((Boolean)o);
    }

    @Test
    public void testContinueInIndex() throws Exception{
        Script s = JEXL.createScript("l = [1,2,3,4] ; index{ continue( $%3 == 0 ){true} }(l)" );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertEquals(2,o);

        s = JEXL.createScript("l = [1,2,3,4,5] ; rindex{ continue( $%3 == 2 ){true} }(l)" );
        o = s.execute(jc);
        assertEquals(4,o);

        s = JEXL.createScript("l = [1,2,3,4,5,6,9] ; rindex{ continue( $%3 == 0 ) ; $ > 2 }(l)" );
        o = s.execute(jc);
        assertEquals(4,o);

        s = JEXL.createScript("l = [1,2,3,4,5,6,9] ; index{ continue( $%3 == 0 ) ; $ > 2 }(l)" );
        s = JEXL.createScript("l = [1,2,3,4,5,6,9] ; index{ continue( $%3 == 0 ) ; $ > 2 }(l)" );
        o = s.execute(jc);
        assertEquals(3,o);
    }

    @Test
    public void testAnonComparator() throws Exception{
        Script s = JEXL.createScript("#(m,M) = minmax{ size($.0) - size($.1) }( 'bbbbb' , 'a' ) ; m == 'a' and M == 'bbbbb' " );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue((Boolean)o);
    }

    @Test
    public void testObj2Xml() throws Exception{
        Script s = JEXL.createScript("o = json('samples/demo.json') ; x = xml(o);" );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof String );

        s = JEXL.createScript("y = xml(x)" );
        o = s.execute(jc);
        assertTrue(o instanceof XmlMap );

        s = JEXL.createScript("x = xml('samples/sample.xml') ; x.exists('//item')" );
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript(" x.exists('//foobar')" );
        o = s.execute(jc);
        assertFalse((Boolean)o);

        s = JEXL.createScript(" x.xpath('//item/text()')" );
        o = s.execute(jc);
        assertTrue(o instanceof String);
        assertFalse(((String)o).isEmpty() );

        s = JEXL.createScript(" x.xpath('//foobar/text()')" );
        o = s.execute(jc);
        assertEquals(null,o );

        s = JEXL.createScript(" x.xpath('//foobar/text()', '__moron__' )" );
        o = s.execute(jc);
        assertEquals("__moron__" ,o);
    }

    @Test
    public void testXisA() throws Exception{
        Script s = JEXL.createScript("10 isa '@num' " );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue((Boolean) o);

        s = JEXL.createScript("10 isa '@Z'" );
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("10.1 isa '@Q'" );
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("10.1 isa '@num'" );
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("[] isa '@arr'" );
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("[2,3] isa '@array'" );
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("(l = list(2,3) ) isa '@list'" );
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("(s = set(2,3) ) isa '@set'" );
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("s isa '@list'" );
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("{:} isa '@map'" );
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("{:} isa '@dict'" );
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript(" e = try{ foo = bar }()" );
        o = s.execute(jc);
        assertTrue(o instanceof Throwable );
        s = JEXL.createScript(" e isa '@err' " );
        o = s.execute(jc);
        assertTrue((Boolean) o);

        s = JEXL.createScript("#(o,:e) = (foo = bar )" );
        o = s.execute(jc);
        s = JEXL.createScript(" e isa '@error' " );
        o = s.execute(jc);
        assertTrue((Boolean) o);

        s = JEXL.createScript("s = 'abc' ; s isa '@String' " );
        o = s.execute(jc);
        assertTrue((Boolean) o);

        s = JEXL.createScript(" s isa '@str' " );
        o = s.execute(jc);
        assertTrue((Boolean) o);

        s = JEXL.createScript(" s isa '@s' " );
        o = s.execute(jc);
        assertFalse((Boolean) o);

        s = JEXL.createScript(" s isa '@foo' " );
        o = s.execute(jc);
        assertFalse((Boolean) o);


    }

    @Test
    public void testOmniscientTypeCast() throws Exception{
        Script s = JEXL.createScript("n = Z(2.0)" );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertEquals(2,o);

        s = JEXL.createScript("n = Z('222222222212222')" );
        o = s.execute(jc);
        assertEquals(222222222212222l,o);

        s = JEXL.createScript("n = NUM('222222222212222')" );
        o = s.execute(jc);
        assertEquals(222222222212222l,o);

        s = JEXL.createScript("n = Z('2222222222122221211131311313131311')" );
        o = s.execute(jc);
        assertTrue(o instanceof BigInteger);

        s = JEXL.createScript("n = NUM('2222222222122221211131311313131311')" );
        o = s.execute(jc);
        assertTrue(o instanceof BigInteger);

        s = JEXL.createScript("n = NUM('2')" );
        o = s.execute(jc);
        assertEquals(2,o);

        s = JEXL.createScript("q = Q('2')" );
        o = s.execute(jc);
        assertEquals(2.0f,o);

        s = JEXL.createScript("q = Q('2.12111121211212')" );
        o = s.execute(jc);
        assertTrue(o instanceof Double);

        s = JEXL.createScript("q = NUM('2.12111121211212')" );
        o = s.execute(jc);
        assertTrue(o instanceof Double);


        s = JEXL.createScript("q = Q('2.121111212112121010010101019101101101918911')" );
        o = s.execute(jc);
        assertTrue(o instanceof BigDecimal);

        s = JEXL.createScript("q = NUM('2.121111212112121010010101019101101101918911')" );
        o = s.execute(jc);
        assertTrue(o instanceof BigDecimal);


    }

    @Test
    public void testShuffleListSet() throws Exception{
        Script s = JEXL.createScript("s = set(1,2,3) ; shuffle(s) ; s ;" );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof List );
        assertTrue(o instanceof Set );
        ListSet ls = (ListSet)o;
        ls.add(0,4);
        ls.add(0,4);
        ls.add(0,6);
        assertEquals(5,ls.size());
        ls.set(2,4);
        ls.set(3,4);
        ls.set(4,6);
        assertEquals(5,ls.size());

    }

    @Test
    public void testMap2JSON() throws Exception{
        Script s = JEXL.createScript("x = json('samples/demo.json') ; s = str(x) ; write(s) ; json(s) " );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof Map);
    }

    @Test
    public void testEventAdd() throws Exception{
        Script s = JEXL.createScript("f = def(){} ; def g(){} ;  f.after += g ; " );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof List);
    }

    @Test
    public void testInterestingArithmetic() throws Exception{
        Script s = JEXL.createScript("x = '0' ** 3 " );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertEquals("000", o );
        s = JEXL.createScript("x = '1' ** 3 " );
        o = s.execute(jc);
        assertEquals("111", o );

        s = JEXL.createScript("x = '0.' + '3' " );
        o = s.execute(jc);
        assertEquals("0.3", o );

        s = JEXL.createScript("x = '1234' ; x[0] == '1'" );
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("x = set(1,2,3) ; [1,2] @ x " );
        o = s.execute(jc);
        assertTrue((Boolean)o);

        s = JEXL.createScript("x = {1:2, 3:4, 5:6} ; [1,3] @ x " );
        o = s.execute(jc);
        assertTrue((Boolean)o);

    }

    @Test
    public void testMatrixSelectOrder() throws Exception{
        Script s = JEXL.createScript("m = matrix('samples/test.tsv') ; l = m.select(2,0) ; " );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof List );

        s = JEXL.createScript("index{ $.1 !~ '[0-9]+'  }(l) < 0 " );
        o = s.execute(jc);
        assertTrue(o instanceof Boolean );
        assertTrue((Boolean)o);

        s = JEXL.createScript("m2 = m.matrix(2,0) " );
        o = s.execute(jc);
        assertTrue(o instanceof DataMatrix);

        s = JEXL.createScript("index{ $.1 !~ '[0-9]+'  }( m2.rows ) < 0 " );
        o = s.execute(jc);
        assertTrue(o instanceof Boolean );
        assertTrue((Boolean)o);

        s = JEXL.createScript("str(m2.columns) #^ 'Number'" );
        o = s.execute(jc);
        assertTrue(o instanceof Boolean );
        assertFalse((Boolean)o);

    }

    /*
    Commented out because it is not needed now
    @Test
    public void testLoadLibrary() throws Exception{
        Script s = JEXL.createScript("load('./target/lib')" );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue((Boolean)o);
    }
    */

}
