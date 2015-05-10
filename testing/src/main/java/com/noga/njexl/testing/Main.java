/**
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

package com.noga.njexl.testing;

import com.noga.njexl.lang.JexlContext;
import com.noga.njexl.lang.JexlEngine;
import com.noga.njexl.lang.JexlException;
import com.noga.njexl.lang.Script;
import com.noga.njexl.testing.ui.WebSuiteRunner;
import com.noga.njexl.testing.ui.XSelenium;

import java.util.HashMap;

/**
 * Created by noga on 15/04/15.
 */
public class Main {

    static final boolean __DEBUG__ = true ;

    static void executeScript(String[] args){

        String file = args[0];
        if ( args.length > 1 ){
            // is this having http[s]:// start?
            if ( !args[1].startsWith("http")){
                com.noga.njexl.lang.Main.executeScript(args);
                return;
            }
        }
        // now I should be this ...
        String url = args[1];

        JexlContext context = com.noga.njexl.lang.Main.getContext();
        context.set(Script.ARGS, args);
        XSelenium xSelenium = XSelenium.selenium(url, XSelenium.BrowserType.FIREFOX.toString());
        context.set("selenium", xSelenium);
        HashMap<String,Object> functions = com.noga.njexl.lang.Main.getFunction(context);
        functions.put("sel", xSelenium);
        JexlEngine JEXL = com.noga.njexl.lang.Main.getJexl(context);
        JEXL.setFunctions(functions);

        try {
            Script sc = JEXL.importScript(file);
            Object o = sc.execute(context);
            int e = 0;
            if ( o instanceof Integer ){
                e = (int)o;
            }
            xSelenium.close();
            System.exit(e);
        }catch (Throwable e){
            if ( __DEBUG__ ){
                System.err.println(e);
                e.printStackTrace();
            }
            else {
                if (e instanceof JexlException) {
                    System.err.printf( "Error : %s\n", ((JexlException) e).getFaultyCode());
                } else {
                    System.err.println(e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println(e.getCause().getMessage());
                    }

                }
            }
            xSelenium.close();
            System.exit(1);
        }
    }

    private static void executeUISuite(String[] args) {
        try {
            WebSuiteRunner runner = new WebSuiteRunner(args[0]);
            runner.run();

        }catch (Throwable t){
            if ( __DEBUG__){
                t.printStackTrace();
            }else{
                System.err.println(t);
            }
        }
    }

    public static void main(String[] args) {
        if ( args.length ==  0 ){
            try {
                com.noga.njexl.lang.Main.interpret();
            }catch (Exception e){
                if ( __DEBUG__ ){
                    System.err.println(e);
                }
            }
            return;
        }
        if ( args[0].endsWith(".jexl") ){
            executeScript(args);
            return;
        }
        if ( args[0].endsWith(".xml") ){
            executeUISuite(args);
            return;
        }
    }
}
