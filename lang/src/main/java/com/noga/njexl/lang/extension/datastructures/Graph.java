/**
 *Copyright 2016 Nabarun Mondal
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
 * A Standard Graph Data Structure,
 * capable enough to achieve generic stuff
 * Created by noga on 24/05/15.
 */
public class Graph {

    /**
     * Generates a identifier from a name
     * @param name the name
     * @return the identifier
     */
    public static String id(String name){
        String id = name.replace(' ', '_');
        id = "$" + id;
        return id;
    }

    /**
     * Individual nodes of a Graph
     */
    public static class Node{

        /**
         * Property name which is script
         */
        public static final String SCRIPT = "@X" ;

        /**
         * Property name which is list of nodes
         */
        public static final String NODES = "@N" ;

        /**
         * If this is there, that means redirect for script
         */
        public static final String REDIRECT = "@" ;

        /**
         * Name of the node
         */
        public final String name;

        /**
         * Auto-generated id of the node
         */
        public final String id;

        /**
         * The bucket of properties
         */
        public final HashMap<String,Object> properties;

        /**
         * The set of nodes
         */
        public final ListSet<String> nodes;

        /**
         * The executable code for the node
         */
        public final String script;

        /**
         * Get a property from the node
         * @param p the property name , if not found, tries to get a node
         * @return the value of the property
         */
        public Object get(String p){
            if ( properties.containsKey(p)){
                return properties.get(p);
            }
            return nodes.get(p);
        }

        /**
         * Sets the property
         * @param p name of the property
         * @param v value of the propery
         */
        public void set(String p, Object v){
            if ( properties.containsKey(p)){
                properties.put(p, v);
                return;
            }
        }

        /**
         * Creates a node
         * @param n name of the node
         * @param m the property bag including the child nodes
         * @throws Exception in case of error
         */
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

        /**
         * Executes the script associated with the node
         * @param context the context of the script
         * @return the result
         */
        public Object execute(JexlContext context){
            context.set( Script._ITEM_ , this );
            JexlEngine jexlEngine = new JexlEngine();
            Script s = jexlEngine.createScript(script);
            return s.execute( context);
        }
    }

    /**
     * The nodes of the graph
     */
    public final HashMap<String,Node> nodes;

    /**
     * Creates a graph
     * @param file from the json file
     * @throws Exception in case of any error
     */
    public Graph(String file) throws Exception {
        nodes = new HashMap<>();
        HashMap<String,HashMap> m = (HashMap)TypeUtility.json(file);
        for (String n : m.keySet()){
            Node node = new Node( n, m.get(n));
            nodes.put(n,node);
        }
    }
}
