/**
 *Copyright 2015 Nabarun Mondal
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

import com.noga.njexl.lang.JexlContext;
import com.noga.njexl.lang.JexlEngine;
import com.noga.njexl.lang.Script;
import com.noga.njexl.lang.extension.TypeUtility;
import java.util.HashMap;

/**
 * Created by noga on 24/05/15.
 */
public class Graph {

    public static String id(String name){
        String id = name.replace(' ', '_');
        id = "$" + id;
        return id;
    }

    public static class Node{

        public static final String SCRIPT = "@X" ;

        public static final String NODES = "@N" ;

        public static final String REDIRECT = "@" ;

        public final String name;

        public final String id;

        public final HashMap<String,Object> properties;

        public final ListSet<String> nodes;

        public final String script;

        public Object get(String p){
            if ( properties.containsKey(p)){
                return properties.get(p);
            }
            return nodes.get(p);
        }

        public void set(String p, Object v){
            if ( properties.containsKey(p)){
                properties.put(p, v);
                return;
            }
        }

        public Node(String n, HashMap m) throws Exception {
            name = n;
            nodes = new ListSet( TypeUtility.from( m.get(NODES) ) );
            properties = new HashMap<>(m);
            String text  = (String)properties.get(SCRIPT);
            if ( text != null ){
                if ( text.startsWith(REDIRECT)){
                    text = TypeUtility.readToEnd(text.substring(1));
                }
            }
            script = text ;
            id = id(name);
            // remove specific properties :
            properties.remove( SCRIPT );
            properties.remove( NODES );

        }

        public Object execute(JexlContext context){
            context.set( TypeUtility._ITEM_ , this );
            JexlEngine jexlEngine = new JexlEngine();
            Script s = jexlEngine.createScript(script);
            return s.execute( context);
        }
    }

    public final HashMap<String,Node> nodes;

    public Graph(String file) throws Exception {
        nodes = new HashMap<>();
        HashMap<String,HashMap> m = (HashMap)TypeUtility.json(file);
        for (String n : m.keySet()){
            Node node = new Node( n, m.get(n));
            nodes.put(n,node);
        }
    }
}
