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

    static void executeScriptUI(String[] args){
        if ( args.length < 2 ){
            System.exit(1);
        }
        String file = args[0];
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
            return;
        }
        if ( args[0].endsWith(".jexl") ){
            executeScriptUI(args);
            return;
        }
        if ( args[0].endsWith(".xml") ){
            executeUISuite(args);
            return;
        }
    }
}
