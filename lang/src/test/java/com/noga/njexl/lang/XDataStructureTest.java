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

import org.junit.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by noga on 02/02/16.
 */
public class XDataStructureTest extends JexlTestCase {

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

    }
}
