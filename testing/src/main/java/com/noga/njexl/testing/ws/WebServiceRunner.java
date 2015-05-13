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

package com.noga.njexl.testing.ws;

import com.noga.njexl.lang.JexlContext;
import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.testing.TestAssert;
import com.noga.njexl.testing.ui.WebSuiteRunner;

import java.util.Map;

/**
 * Created by noga on 12/05/15.
 */
public class WebServiceRunner extends WebSuiteRunner {

    protected RestCaller restCaller;

    public WebServiceRunner(String file) throws Exception {
        super(file);
        restCaller = new RestCaller(webTestSuite.webApp.url);
    }

    @Override
    protected void prepare() throws Exception {
        testAssert = new TestAssert();
        testAssert.eventListeners.addAll(reporters);
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
        boolean run = false ;
        //run before test
        if ( before != null ){
            try{
                Object ret = before.execute(local);
                run = TypeUtility.castBoolean(ret,false);
            }catch (Exception e){
                //ignore now
            }
        }
        if ( !run ){
            runEvent.type = TestRunEventType.ABORT_TEST ;
            return runEvent ;
        }
        Object result = null;
        try {
            //make a dict using two Arrays...
            Map args = TypeUtility.makeDict(columns, values);
            result = restCaller.get(args);

        }catch (Throwable t){
            run = false ;
        }
        if ( !run ){
            runEvent.type = TestRunEventType.ERROR_TEST ;
            return runEvent ;
        }
        if ( after != null ){
            local.set(SCRIPT_OUT, result);
            try{
                Object ret = after.execute(local);
                run = TypeUtility.castBoolean(ret,false);
            }catch (Exception e){
                //ignore now
            }
        }
        if ( !run ){
            runEvent.type = TestRunEventType.ERROR_TEST ;
        }

        return runEvent ;
    }

    @Override
    protected void shutdown() throws Exception {
        // do nothing
        testAssert.eventListeners.clear();
        testAssert = null;
    }
}