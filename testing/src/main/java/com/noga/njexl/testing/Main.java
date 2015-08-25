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
import com.noga.njexl.testing.reporting.SimpleTextReporter;
import com.noga.njexl.testing.ui.WebSuiteRunner;
import com.noga.njexl.testing.ui.XSelenium;
import com.noga.njexl.testing.ws.WebServiceRunner;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import static org.kohsuke.args4j.ExampleMode.ALL;


import java.util.*;

/**
 * A Main class to have command line interface, if need be.
 * Created by noga on 15/04/15.
 */
public class Main {

    @Option(name="-X",usage="run with full debug information")
    boolean __DEBUG__ = false ;

    @Option(name="-u",usage="url to run script against")
    private String url = "" ;

    @Option(name="-s",usage="Test Suite Xml file")
    private String suite = "" ;

    @Option(name="-b",usage="Local Browser Type to Use")
    private XSelenium.BrowserType  browserType = XSelenium.BrowserType.FIREFOX  ;

    @Option(name="-bc",usage="Remote Browser Configuration to Use, if no local browser")
    private String remoteBrowserConfiguration = ""  ;


    // receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<>();

    /**
     * In case of normal script w/o url
     * Executes the script
     * @param args the arguments passed to the script
     */
    public void executeScript(String[] args){
        JexlContext jc = com.noga.njexl.lang.Main.getContext();
        HashMap<String,Object> functions = com.noga.njexl.lang.Main.getFunction(jc);
        functions.put(Utils.Mailer.MAIL_NS, Utils.Mailer.class);
        JexlEngine JEXL = com.noga.njexl.lang.Main.getJexl(jc);
        JEXL.setFunctions(functions);
        jc.set(Script.ARGS, args);
        try {
            Script sc = JEXL.importScript(args[0]);
            Object o = sc.execute(jc);
            int e = 0;
            if ( o instanceof Integer ){
                e = (int)o;
            }
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
            System.exit(1);
        }
    }

    private void executeScript(){

        if ( arguments.isEmpty()) {
            System.err.println("No args given to run!");
            return;
        }

        String[] args = new String[ arguments.size() ];
        args =  arguments.toArray(args);
        String file = arguments.get(0);

        JexlContext context = com.noga.njexl.lang.Main.getContext();
        context.set(Script.ARGS, args);
        XSelenium xSelenium;
        if ( remoteBrowserConfiguration.isEmpty() ){
             xSelenium = XSelenium.selenium(url, browserType);
        }else{
            xSelenium = XSelenium.selenium(url, remoteBrowserConfiguration);
        }

        context.set(XSelenium.SELENIUM_VAR, xSelenium);
        context.set(XSelenium.BASE_URL, url);

        HashMap<String,Object> functions = com.noga.njexl.lang.Main.getFunction(context);
        functions.put(XSelenium.SELENIUM_NS, xSelenium);
        functions.put(Utils.Mailer.MAIL_NS, Utils.Mailer.class);
        TestAssert testAssert = new TestAssert();
        SimpleTextReporter textReporter = SimpleTextReporter.reporter(SimpleTextReporter.Sync.CONSOLE,"");
        functions.put(TestAssert.ASSERT_NS, testAssert );
        context.set(TestAssert.ASSERT_VAR, testAssert);
        testAssert.eventListeners.add(xSelenium);
        testAssert.eventListeners.add(textReporter);
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

    public static TestSuiteRunner runner(String suiteFile) throws Exception {
        if ( suiteFile.endsWith(".api.xml")){
            return new WebServiceRunner(suiteFile);
        }
        return new WebSuiteRunner(suiteFile);
    }

    private void executeTestSuite(String suiteFile) {
        try {
            // get a runner...
            TestSuiteRunner runner = runner(suiteFile);
            // get on with the show...
            runner.run();

        }catch (Throwable t){
            if ( __DEBUG__){
                t.printStackTrace();
            }else{
                System.err.println(t);
            }
        }
    }

    private void usage(CmdLineParser parser){
        System.err.println("java -jar <jar-file> [options...] arguments...");
        // print the list of available options
        parser.printUsage(System.err);
        System.err.println();

        // print option sample. This is useful some time
        System.err.println("  Example: java -jar <jar-file> " + parser.printExample(ALL));
        System.exit(-1);

    }

    private void run(String[] args){
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(80);
        try {
            // parse the arguments.
            parser.parseArgument(args);
            if ( !suite.isEmpty() ){
                executeTestSuite(suite);
                return;
            }
            if ( !url.isEmpty() ){
                executeScript();
                return;
            }
            // go with free call
            executeScript(args);

        } catch( Exception e ) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            usage(parser);
        }

    }

    private Main(){}

    public static void main(String[] args) {
        Main main = new Main();
        main.run(args);
    }
}
