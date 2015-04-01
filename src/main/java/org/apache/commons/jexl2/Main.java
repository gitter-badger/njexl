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

import org.apache.commons.jexl2.extension.SetOperations;
import org.apache.commons.jexl2.extension.ReflectionUtility;
import org.apache.commons.jexl2.extension.TypeUtility;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Test application for JEXL.
 *
 * @since 2.0
 */
public class Main {

    public static final String PROMPT = "(njexl)" ;

    public static JexlContext getContext(){
        Map<Object,Object> m = System.getProperties();
        // dummy context to get variables
        JexlContext context = new MapContext();
        for(Map.Entry<Object,Object> e : m.entrySet()) {
            context.set(e.getKey().toString(), e.getValue());
        }
        return context;
    }

    public static HashMap<String,Object> getFunction(JexlContext context){
        HashMap<String,Object> map = new HashMap<>();
        map.put("set", SetOperations.class);
        map.put("str",String.class);
        map.put("math",Math.class);
        map.put("sys", System.class);
        context.set("sys",System.class);
        map.put("cls", ReflectionUtility.class);

        return map;
    }

    public static JexlEngine getJexl(JexlContext context){
        JexlEngine jexl = new JexlEngine();
        HashMap<String,Object> map = getFunction(context);
        jexl.setFunctions(map);
        return jexl;
    }

    public static void interpret(){
        JexlContext context = getContext();
        JexlEngine JEXL = getJexl(context);

        while(true){
            System.out.print(PROMPT);
            String line = System.console().readLine();
            if ( line == null || line.equals("q")){
                break;
            }
            line = line.trim();
            if ( line.isEmpty() ){
                continue;
            }
            try {
                Expression e = JEXL.createExpression(line);
                Object o = e.evaluate(context);
                context.set("_o_", o);
                context.set("_e_", null);
                System.out.printf("=>%s\n", str(o));
            }catch (Exception e){
                context.set("_e_", e);
                context.set("_o_", null);
                System.err.println(e.getMessage());
                if ( e.getCause() != null ){
                    System.err.println(e.getCause().getMessage());
                }
            }
        }
        System.exit(0);
    }

    public static void executeScript(String[] args){
        JexlContext jc = getContext();
        JexlEngine JEXL = getJexl(jc);
        jc.set("args", args);
        try {
            Script sc = JEXL.createScript(new File(args[0]));
            Object o = sc.execute(jc);
            int e = TypeUtility.castInteger(o,-1);
            System.exit(e);
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static String strArr( Object arr){
        StringBuffer buf = new StringBuffer("@[");
        int len = Array.getLength(arr);
        if ( len > 0 ) {
            buf.append(String.format("%s", Array.get(arr,0)) );
            for (int i = 1; i < len; i++) {
                buf.append(String.format(", %s", Array.get(arr, i)));
            }
        }
        buf.append("]");
        return buf.toString();
    }

    public static String str(Object o){
        if ( o != null ){
            if ( o.getClass().isArray() ){
                return  strArr(o);
            }
            return o.toString();
        }else{
            return "null" ;
        }
    }

    public static void main(String[] args) {
        if ( args.length == 0 ){
            interpret();
        }
        if ( args.length >= 1 ){
            executeScript(args);
        }
    }
}
