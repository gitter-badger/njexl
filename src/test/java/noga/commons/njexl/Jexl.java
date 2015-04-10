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

import java.io.File;
import java.util.Map;

/**
 * Command line interface for Jexl for use in testing
 * @since 1.0
 */
public class Jexl {

    public static final String PROMPT = "njexl>" ;
    private Jexl() {}

    public static JexlContext getContext(){
        Map<Object,Object> m = System.getProperties();
        // dummy context to get variables
        JexlContext context = new MapContext();
        for(Map.Entry<Object,Object> e : m.entrySet()) {
            context.set(e.getKey().toString(), e.getValue());
        }
        return context;
    }

    public static void interpret(){
        final JexlEngine JEXL = new JexlEngine();
        JexlContext context = getContext();

        while(true){
            System.out.print(PROMPT);
            String line = System.console().readLine();
            if ( line == null || line.equals("q")){
                break;
            }
            line = line.trim();
            try {
                Expression e = JEXL.createExpression(line);
                Object o = e.evaluate(context);
                context.set("_o_", o);
                context.set("_e_", null);
                System.out.println(o);
            }catch (Exception e){
                context.set("_e_", e);
                context.set("_o_", null);
                System.err.println(e);
            }
        }
        System.exit(0);
    }

    public static void executeScript(String[] args){
        JexlEngine JEXL = new JexlEngine();
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
