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

package com.noga.njexl.testing.api.junit;

import com.noga.njexl.lang.*;
import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.internal.logging.Log;
import com.noga.njexl.lang.internal.logging.LogFactory;
import com.noga.njexl.testing.api.Annotations;
import com.noga.njexl.testing.api.CallContainer;
import jdk.nashorn.internal.codegen.CompilerConstants;
import org.junit.*;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;
import org.junit.runners.parameterized.TestWithParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by noga on 27/05/15.
 */
public class JApiRunner extends BlockJUnit4ClassRunnerWithParameters {


    public static class WorkerThread implements Runnable{

        final CallContainer cc;

        final int delay;

        final int numCalls;

        public static void delay(int ms){
            try{
                Thread.sleep(ms);
            }catch (Exception e){
            }
        }

        public WorkerThread(CallContainer c, int d, int nc ){
            cc = c ;
            delay = d ;
            numCalls = nc;
        }

        @Override
        public void run() {
            for (int i = 0; i < numCalls ; i++) {
                delay( delay );
                cc.call();
            }
        }
    }

    public static class ProxyTest {

        public static final String INPUT = "_cc_" ;

        public static final String GLOBALS = "_g_" ;

        Log logger =  LogFactory.getLog( ProxyTest.class );

        CallContainer callContainer;
        Annotations.NApiThread nApiThread ;

        public boolean script(String file) {
            JexlContext context = Main.getContext();
            JexlEngine engine = Main.getJexl(context);
            try {
                try{
                    Expression expression = engine.createExpression( callContainer.globals);
                    Object g = expression.evaluate( context );
                    context.set(GLOBALS,g);

                }catch (Exception e){
                    logger.error(
                            String.format("Error generating global : '%s' ",
                                    callContainer.globals), e);
                }
                Script script = engine.importScript(file);
                context.set(INPUT, callContainer );
                Object o = script.execute(context);
                return TypeUtility.castBoolean(o,false);
            }catch (Throwable t){
                logger.error(
                        String.format("Error running script : [ %s ] ", file), t);
            }
            finally {
                context.clear();
                System.gc(); //collect...
            }
            return false;
        }

        public ProxyTest(CallContainer callContainer, Annotations.NApiThread nApiThread){
            this.callContainer = callContainer ;
            this.nApiThread = nApiThread ;
        }


        @Test
        public void callMethod() throws Exception{
            if ( nApiThread.use() ){
               // make use of multi threading
                int nT = nApiThread.numThreads() ;
                ExecutorService executor = Executors.newFixedThreadPool(nT);
                int pt = nApiThread.pacingTime();
                int sd = nApiThread.spawnTime() ;
                int nc = nApiThread.numCallPerThread() ;
                for (int i = 0; i < nT ; i++) {
                    WorkerThread.delay( sd );
                    Runnable worker = new WorkerThread( callContainer , pt, nc);
                    executor.execute(worker);//calling execute method of ExecutorService
                }
                executor.shutdown();
                while (!executor.isTerminated()) {   }
            }else {
                callContainer.call();
            }
        }

        @Before
        public void before()throws Exception{
            if ( callContainer.pre.isEmpty() ) return;
            // no pre/post for Threaded/this is for performance testing only
            if ( nApiThread.use() ) return;

            callContainer.validationResult = false ;
            if ( callContainer.pre.endsWith(".jexl") ){
                callContainer.validationResult = script(callContainer.pre);
                if ( !callContainer.validationResult ){
                    throw new Exception( "Error running input : " + callContainer.toString() );
                }
            }
        }

        @After
        public void after()throws Exception{
            if ( callContainer.post.isEmpty() ) return;
            // no pre/post for Threaded/this is for performance testing only
            if ( nApiThread.use() ) return;

            callContainer.validationResult = false ;
            if ( callContainer.post.endsWith(".jexl") ){
                callContainer.validationResult = script(callContainer.post);
                if ( !callContainer.validationResult ){
                    throw new Exception( "Error running input : " + callContainer.toString() );
                }
            }
        }

    }

    public static final Class proxy = ProxyTest.class ;

    public static JApiRunner createRunner(CallContainer container, Annotations.NApiThread nApiThread) throws Exception{
        TestClass testClass = new TestClass(proxy);
        String name = container.method.toGenericString();
        List<Object> parameters = new ArrayList<>();
        parameters.add( container );
        parameters.add( nApiThread );
        TestWithParameters test = new TestWithParameters( name, testClass, parameters);
        return new JApiRunner(test);
    }

    public JApiRunner(TestWithParameters test) throws InitializationError {
        super(test);
    }
}
