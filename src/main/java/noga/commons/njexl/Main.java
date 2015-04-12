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

package noga.commons.njexl;

import jline.console.history.FileHistory;
import noga.commons.njexl.extension.ReflectionUtility;
import noga.commons.njexl.extension.TypeUtility;
import noga.commons.njexl.extension.SetOperations;
import noga.commons.njexl.extension.ReflectionUtility;
import noga.commons.njexl.extension.TypeUtility;

import java.io.File;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import jline.console.ConsoleReader;


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

    public static void interpret() throws Exception{
        JexlContext context = getContext();
        JexlEngine JEXL = getJexl(context);
        ConsoleReader console = new ConsoleReader();

        String historyFile = System.getProperty("user.home") + "/.njexl_cli_history" ;
        console.setHistory( new FileHistory(new File(historyFile)));
        console.setExpandEvents(false);
        console.setCopyPasteDetection(true);
        console.setPrompt(PROMPT);

        while(true){
            String line = console.readLine();
            if ( line == null || line.equals("q")){
                break;
            }
            if ( line == null || line.equals("cls")){
                console.clearScreen();
                continue;
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
                ((FileHistory)console.getHistory()).flush();
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
        jc.set(Script.ARGS, args);
        try {
            Script sc = JEXL.importScript(args[0]);
            Object o = sc.execute(jc);
            int e = TypeUtility.castInteger(o, -1);
            System.exit(e);
        }catch (Exception e){
            if ( e instanceof JexlException ){
                System.err.println(((JexlException) e).getFaultyCode());
            }
            else {
                e.printStackTrace();
            }
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

    public static void main(String[] args) throws Exception{
        if ( args.length == 0 ){
            interpret();
        }
        if ( args.length >= 1 ){
            executeScript(args);
        }
    }
}
