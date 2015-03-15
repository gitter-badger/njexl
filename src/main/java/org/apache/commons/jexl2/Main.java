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

import org.apache.commons.jexl2.extension.Predicate;
import org.apache.commons.jexl2.extension.ReflectionUtility;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Test application for JEXL.
 *
 * @since 2.0
 */
public class Main {

    public static final String PROMPT = "njexl>" ;

    public static JexlContext getContext(){
        Map<Object,Object> m = System.getProperties();
        // dummy context to get variables
        JexlContext context = new MapContext();
        for(Map.Entry<Object,Object> e : m.entrySet()) {
            context.set(e.getKey().toString(), e.getValue());
        }
        return context;
    }

    public static HashMap<String,Object> getFunction(){
        HashMap<String,Object> map = new HashMap<>();
        map.put("lgc", new Predicate());
        map.put("str",String.class);
        map.put("math",Math.class);
        map.put("out", System.out);
        map.put("con", System.console());
        map.put("sys", System.class);
        map.put("cls", new ReflectionUtility());

        return map;
    }

    public static JexlEngine getJexl(){
        JexlEngine jexl = new JexlEngine();
        HashMap<String,Object> map = getFunction();
        jexl.setFunctions(map);
        return jexl;
    }

    public static void interpret(){
        JexlEngine JEXL = getJexl();
        JexlContext context = getContext();

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
                System.out.printf("=>%s\n", o);
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
        JexlEngine JEXL = getJexl();
        JexlContext jc = getContext();
        jc.set("args", args);
        try {
            Script sc = JEXL.createScript(new File(args[0]));
            Object o = sc.execute(jc);
            System.out.printf("======\n%s\n=====\n", o);
            System.exit(0);
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
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
