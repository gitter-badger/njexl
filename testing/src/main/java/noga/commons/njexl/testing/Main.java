package noga.commons.njexl.testing;

import noga.commons.njexl.JexlContext;
import noga.commons.njexl.JexlEngine;
import noga.commons.njexl.JexlException;
import noga.commons.njexl.Script;
import noga.commons.njexl.testing.dataprovider.DataSource;
import noga.commons.njexl.testing.dataprovider.DataSourceTable;
import noga.commons.njexl.testing.dataprovider.ProviderFactory;
import noga.commons.njexl.testing.ui.WebSuiteRunner;
import noga.commons.njexl.testing.ui.XSelenium;

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

        JexlContext context = noga.commons.njexl.Main.getContext();
        context.set(Script.ARGS, args);
        XSelenium xSelenium = XSelenium.selenium(url, XSelenium.BrowserType.FIREFOX.toString());
        context.set("selenium", xSelenium);
        HashMap<String,Object> functions = noga.commons.njexl.Main.getFunction(context);
        functions.put("sel", xSelenium);
        JexlEngine JEXL = noga.commons.njexl.Main.getJexl(context);
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
