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

import com.noga.njexl.lang.extension.dataaccess.XmlMap;
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

        s = JEXL.createScript("fp.close(); lines('foo.txt') ");
        o = s.execute(jc);
        assertTrue(o instanceof List);

        s = JEXL.createScript("system('ls','foo.txt')");
        o = s.execute(jc);
        assertEquals(0,o);

    }

    @Test
    public void testREST() throws Exception{
        Script s = JEXL.createScript("data = read('http://www.thomas-bayer.com/sqlrest/CUSTOMER/', 10000 ,10000 ) ");
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue(o instanceof String );

        s = JEXL.createScript("xml = xml(data) ; ");
        o = s.execute(jc);
        assertTrue(o instanceof XmlMap);

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
    public void testAnonComparator() throws Exception{
        Script s = JEXL.createScript("#(m,M) = minmax{ size($.0) - size($.1) }( 'bbbbb' , 'a' ) ; m == 'a' and M == 'bbbbb' " );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue((Boolean)o);
    }

    @Test
    public void testLoadLibrary() throws Exception{
        Script s = JEXL.createScript("load('./target/lib')" );
        JexlContext jc = new MapContext();
        Object o = s.execute(jc);
        assertTrue((Boolean)o);
    }
}
