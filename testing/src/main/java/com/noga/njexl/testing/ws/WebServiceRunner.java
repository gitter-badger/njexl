/**
 * Copyright 2015 Nabarun Mondal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.noga.njexl.testing.ws;

import com.noga.njexl.lang.JexlContext;
import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.internal.logging.Log;
import com.noga.njexl.lang.internal.logging.LogFactory;
import com.noga.njexl.testing.TestAssert;
import com.noga.njexl.testing.TestSuite;
import com.noga.njexl.testing.ui.WebSuiteRunner;

import java.util.Collections;
import java.util.Map;

/**
 * Created by noga on 12/05/15.
 */
public class WebServiceRunner extends WebSuiteRunner {

    final static Log logger = LogFactory.getLog(WebServiceRunner.class);

    protected RestCaller restCaller;

    protected void createCaller(TestSuite.Feature feature) throws Exception{
        String callUrl = webTestSuite.webApp.url;
        if (!feature.base.isEmpty()) {
            callUrl += "/" + feature.base + "/";
        }
        String method = webTestSuite.webApp.method;
        if (!feature.method.isEmpty()) {
            method = feature.method;
        }
        restCaller = new RestCaller(callUrl, method);

    }

    @Override
    protected void beforeFeature(TestSuite.Feature feature) throws Exception {
        createCaller(feature);
        super.beforeFeature(feature);
    }

    @Override
    protected void afterFeature(TestSuite.Feature feature) throws Exception {
        restCaller = null;
        super.afterFeature(feature);
    }

    public WebServiceRunner(String file, Map<String,String> variables) throws Exception {
        super(file,variables);
        restCaller = null;
    }

    public WebServiceRunner(String file) throws Exception {
        this(file, Collections.EMPTY_MAP);
    }

    @Override
    protected void prepare() throws Exception {
        testAssert = new TestAssert();
        testAssert.eventListeners.addAll(reporters);
    }

    @Override
    protected TestRunEvent runTest(TestRunEvent runEvent) throws Exception {
        testAssert.clearError();
        JexlContext local = jexlContext.copy();
        setLocalContext(local,runEvent);
        boolean run = true;
        //run before test
        if (before != null) {
            try {
                Object ret = before.execute(local);
                run = TypeUtility.castBoolean(ret, false);
            } catch (Exception e) {
                //ignore now , just disable test
                run = false;
                logger.error("Pre validator encountered error", e);
                runEvent.error = e;
            }
        }
        if (!run) {
            runEvent.type = TestRunEventType.ABORT_TEST;
            return runEvent;
        }
        Object result = null;
        try {
            Map args = runEvent.table.tuple( runEvent.row );
            result = restCaller.call(args);
        } catch (Throwable t) {
            run = false;
            runEvent.error = t;
            logger.error("Web Service Call encountered error", t);
        }
        // any assertions failed?
        if ( run ){
            run = !testAssert.hasError();
        }

        if (!run) {
            runEvent.type = TestRunEventType.ERROR_TEST;
            return runEvent;
        }

        if (after != null) {
            //set up nicely
            local.set(SCRIPT_OUT, result);

            try {
                Object ret = after.execute(local);
                run = TypeUtility.castBoolean(ret, false);
            } catch (Exception e) {
                runEvent.error = e;
                logger.error("Post Validator encountered error", e);
            }
        }
        if (run) {
            runEvent.type = TestRunEventType.OK_TEST;
            runEvent.runObject = result;
        } else {
            runEvent.type = TestRunEventType.ERROR_TEST;
        }

        return runEvent;
    }

    @Override
    protected void shutdown() throws Exception {
        // do nothing
        testAssert.eventListeners.clear();
        testAssert = null;
    }
}
