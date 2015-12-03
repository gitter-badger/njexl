/*
*Copyright [2015] [Nabarun Mondal]
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

package com.noga.njexl.lang.extension.datastructures;

import com.noga.njexl.lang.extension.SetOperations;
import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.Main;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * An implementation of tuple.
 * See @link{http://en.wikipedia.org/wiki/Tuple}
 * Created by noga on 04/04/15.
 */
public class Tuple  {

    /**
     * The names of the data values
     */
    public final HashMap<String,Integer> names;

    /**
     * The caches value of mapping
     */
    private final String mapping ;

    /**
     * The objects are actually stored in fixed size array
     */
    public final Object[] t;

    /**
     * Creates a tuple from
     * @param n names
     * @param f a list/array of objects
     */
    public Tuple( List<String> n, Object f){
        t = TypeUtility.array(f);
        StringBuffer buf = new StringBuffer( "[" ) ;
        names = new HashMap<>() ;
        for ( int i = 0 ; i < t.length ; i++ ){
            String c = n.get(i);
            names.put(c, i);
            buf.append(String.format("'%s'->%d ", c,i));
        }
        buf.append("]");
        mapping = buf.toString();
    }

    /**
     * Get the indexed object
     * @param index the index of the item
     * @return the object at that index
     */
    public Object get(int index){
        return t[index];
    }

    /**
     * Sets the indexed object
     * @param index index of the item
     * @param object value to be set
     * @return the object at that index now after setting
     */
    public Object set(int index,Object object){
        return ( t[index] = object ) ;
    }

    /**
     * Given name, gets the corresponding object
     * @param name column name
     * @return the object with name
     */
    public Object get(String name){
        return t[ names.get(name) ] ;
    }

    /**
     * Given name, sets the corresponding object
     * @param name column name
     * @param object value to be set
     * @return the object after setting
     */
    public Object set(String name, Object object){
        return ( t[names.get(name)] = object ) ;
    }

    @Override
    public String toString(){
        return String.format( "<%s,%s>", mapping, Main.strArr(t) );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple tuple = (Tuple) o;
        if (SetOperations.SetRelation.EQUAL != SetOperations.set_relation(names.keySet(), tuple.names.keySet() ) ) return false ;
        for ( String name : names.keySet() ){
            int inxThis = names.get(name);
            int inxThat = tuple.names.get(name);
            if ( !Objects.equals( t[inxThis], tuple.t[inxThat] ) ){
                return false ;
            }
        }
        return true ;
    }

    @Override
    public int hashCode() {
        int result = names != null ? names.hashCode() : 0;
        result = 31 * result + (mapping != null ? mapping.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(t);
        return result;
    }
}
