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

package com.noga.njexl.testing.ui;

import com.noga.njexl.lang.JexlContext;
import com.noga.njexl.lang.JexlEngine;
import com.noga.njexl.lang.Script;
import com.noga.njexl.testing.TestAssert;
import com.noga.njexl.testing.TestSuite;
import com.noga.njexl.testing.Utils;
import com.noga.njexl.testing.dataprovider.DataSourceTable;
import com.noga.njexl.testing.dataprovider.ProviderFactory;
import com.noga.njexl.testing.reporting.Reporter;
import com.noga.njexl.testing.TestSuiteRunner;
import com.noga.njexl.testing.dataprovider.DataSource;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by noga on 17/04/15.
 */
public class WebSuiteRunner extends TestSuiteRunner {

    public final WebTestSuite webTestSuite ;

    public XSelenium xSelenium ;

    Script script ;

    TestAssert testAssert;

    JexlContext jexlContext ;

    HashMap<String,Object> functions;

    protected JexlContext getContext(){
        JexlContext context = com.noga.njexl.lang.Main.getContext();
        context.set(Script.ARGS, new Object[]{});
        context.set(XSelenium.SELENIUM_VAR, xSelenium);
        context.set(Script.ARGS, new Object[]{});
        context.set(TestAssert.ASSERT_VAR, testAssert);
        return context;
    }

    protected HashMap<String,Object> getFunctions(){
        HashMap<String,Object> functions = com.noga.njexl.lang.Main.getFunction(jexlContext);
        functions.put(XSelenium.SELENIUM_NS, xSelenium);
        functions.put(TestAssert.ASSERT_NS, testAssert);
        return functions ;
    }

    public WebSuiteRunner(String file) throws Exception {
        webTestSuite = WebTestSuite.loadFrom(file);
    }

    @Override
    protected TestSuite testSuite() {
        return webTestSuite;
    }

    @Override
    protected void prepare() throws Exception {
        xSelenium = XSelenium.selenium(webTestSuite.webApp.url, webTestSuite.browserType.toString());
        testAssert = new TestAssert();
        testAssert.eventListeners.add(xSelenium);
        testAssert.eventListeners.addAll(reporters);
    }

    @Override
    protected TestSuite.Application application() {
        return webTestSuite.webApp ;
    }

    @Override
    protected String logLocation(String base, TestSuite.BaseFeature feature) {
        String loc = webTestSuite.webApp.logs + "/" + base +"/" + feature.name ;
        File file = new File(loc);
        if ( !file.exists() ){
            file.mkdirs();
        }
        xSelenium.screenShotDir(loc);
        return loc;
    }

    @Override
    protected void afterFeature(TestSuite.BaseFeature feature) throws Exception {
        jexlContext = null;
        functions.clear();
        functions = null;
        script = null;
    }

    @Override
    protected void beforeFeature(TestSuite.BaseFeature feature) throws Exception {
        jexlContext = getContext();
        functions = getFunctions();
        JexlEngine engine = new JexlEngine();
        engine.setFunctions(functions);
        String scriptLocation = webTestSuite.webApp.scriptDir + "/" + feature.script ;
        script = engine.importScript(scriptLocation);
    }

    @Override
    protected TestRunEvent runTest(TestRunEvent runEvent) throws Exception {
        testAssert.setError(false);
        String[] columns = runEvent.table.row(0);
        String[] values = runEvent.table.row(runEvent.row);
        JexlContext local = jexlContext.copy();
        // put into context
        for ( int i = 0 ; i < columns.length;i++ ){
            local.set(columns[i],values[i]);
        }
        // set output if need be?
        runEvent.runObject = script.execute(local);

        if (testAssert.hasError()){
            runEvent.type = TestRunEventType.ERROR_TEST ;
        }else {
            runEvent.type = TestRunEventType.OK_TEST;
        }
        return runEvent ;
    }

    @Override
    protected void shutdown() throws Exception {
        xSelenium.close();
        xSelenium = null;
        testAssert.eventListeners.clear();
        testAssert = null;
    }
}
