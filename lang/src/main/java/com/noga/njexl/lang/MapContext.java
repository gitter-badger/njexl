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

import java.util.HashMap;
import java.util.Map;

/**
 * Wraps a map in a context.
 * <p>Each entry in the map is considered a variable name, value pair.</p>
 */
public class MapContext implements JexlContext {

    public static final String CURRENT = "__current__" ;

    /**
     * The wrapped variable map.
     */
    protected final Map<String, Object> map;

    /**
     * Creates a MapContext on an automatically allocated underlying HashMap.
     */
    public MapContext() {
        this(null);
    }

    /**
     * Creates a MapContext wrapping an existing user provided map.
     * @param vars the variable map
     */
    public MapContext(Map<String, Object> vars) {
        super();
        map = vars == null ? new HashMap<String, Object>() : vars;
        map.put(CURRENT, this);
    }

    /** {@inheritDoc} */
    public boolean has(String name) {
        return map.containsKey(name);
    }

    /** {@inheritDoc} */
    public Object get(String name) {
        return map.get(name);
    }

    /** {@inheritDoc} */
    public void set(String name, Object value) {
        map.put(name, value);
    }

    /** {@inheritDoc} */
    public void remove(String name) {
        if ( map.containsKey(name)){
            map.remove(name);
        }
    }

    /** {@inheritDoc} */
    @Override
    public JexlContext copy() {
        HashMap<String,Object> copy = new HashMap(map);
        copy.put(PARENT_CONTEXT, this);
        return new MapContext(copy);
    }


    /** {@inheritDoc} */
    public void clear() {
        map.clear();
    }


    @Override
    public String toString(){
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        if ( map!= null ){

            for ( String key : map.keySet() ){
                buf.append("'" + key +"'" ).append(" : ");
                if ( key.equals(CURRENT ) ){
                    buf.append("'.'");
                    continue;
                }
                buf.append("'" + map.get(key ) + "'" );
                buf.append(",\n");
            }
        }
        buf.setCharAt(buf.lastIndexOf(","), ' ');
        buf.append("}");
        return buf.toString();
    }
}
