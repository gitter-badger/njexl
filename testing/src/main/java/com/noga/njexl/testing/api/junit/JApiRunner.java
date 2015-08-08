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
import com.noga.njexl.testing.api.Annotations.MethodRunInformation;
import com.noga.njexl.testing.api.CallContainer;
import com.noga.njexl.testing.dataprovider.collection.XStreamIterator;
import org.junit.*;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;
import org.junit.runners.parameterized.TestWithParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An Implementation of the JUnit Runner
 */
public class JApiRunner extends BlockJUnit4ClassRunnerWithParameters {

    /* Conversion of nano second to milli second */
    public static final double NANO_TO_MILLIS = 1000000.0 ;

    public static class WorkerThread implements Runnable{

        final CallContainer[] cc;

        final int delay;

        /**
         * Delays ( waits ) by sleeping
         * @param ms amount of millisecond
         */
        public static void delay(int ms){
            try{
                Thread.sleep(ms);
            }catch (Exception e){
            }
        }

        /**
         * Creates a worker thread
         * @param c an array of call container
         * @param d the delay time
         */
        public WorkerThread(CallContainer[] c, int d ){
            cc = c ;
            delay = d ;
        }

        @Override
        public void run() {
            for (int i = 0; i < cc.length ; i++) {
                delay(delay);
                cc[i].call();
            }
        }
    }

    /**
     * This is the class that actually gets invoked by JUnit
     */
    public static class ProxyTest {

        /**
         * The call container - name to access from nJexl script
         */
        public static final String INPUT = "_cc_" ;

        /**
         * The globals - name to access from nJexl script
         */
        public static final String GLOBALS = "_g_" ;

        Log logger =  LogFactory.getLog( ProxyTest.class );

        int testNumber ;

        XStreamIterator<CallContainer> iterator;

        CallContainer callContainer ;

        Annotations.NApiThread nApiThread ;

        /**
         * Executes the nJexl script from the file
         * @param file the script file location
         * @return true if script returns true, false if failure
         */
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

        /**
         * Creates a proxy test
         * @param testNumber the unique order no of the test
         * @param iterator an iterator to get call container from
         * @param nApiThread the run thread information
         */
        public ProxyTest(int testNumber, XStreamIterator<CallContainer> iterator, Annotations.NApiThread nApiThread){
            this.nApiThread = nApiThread ;
            this.testNumber = testNumber ;
            this.iterator = iterator ;
            callContainer = this.iterator.get( this.testNumber );
        }

        /**
         * Gets the performance check done
         * @param workers  data gathered from these worker threads
         * @param percentile the percentile value to calculate
         * @param percentileValueLessThan the expected percentile benchmark value
         * @return true if performance benchmakring passes, false if fails
         */
        public boolean doPerformanceCheck(WorkerThread[] workers, double percentile,  double percentileValueLessThan ) {
            ArrayList<Double> results = new ArrayList<>();
            boolean oneFailed = false ;
            for ( int i = 0 ; i < workers.length;i++ ){
                for ( int j = 0 ; j < workers[i].cc.length ; j++ ){
                    double d = workers[i].cc[j].timing / NANO_TO_MILLIS   ;
                    String uId = workers[i].cc[j].uniqueId(j+1) ;
                    if ( d > 0 ){
                        results.add(d);
                        System.out.printf("%s -> %f \n", uId, d);
                    }
                    else{
                        oneFailed = true ;
                        System.err.printf("**ERROR %s -> %s \n", uId , workers[i].cc[j].error);
                        System.err.printf("**ON INPUT %s\n", Main.strArr(workers[i].cc[j].parameters));
                    }
                }
            }
            if ( results.isEmpty() ) {
                System.err.println("All Inputs failed, can not have any performance check!");
                return false ;
            }
            if ( oneFailed ){
                System.err.println("Some Input failed, still doing performance check!");
            }

            Collections.sort( results );
            int s  = results.size() ;
            double m = results.get(s - 1); // the max
            int minSample = (int)Math.ceil(1.0/( 1.0 - percentile ));
            if ( s >= minSample ){
                s = (int)Math.ceil(percentile * s);
                m = results.get(s);
            }
            results.clear();
            System.out.printf("Expected percentile[%f] ( %f ) , Actual ( %f )\n",
                    percentile, percentileValueLessThan, m );
            //always use open set, not closed or semi closed
            return ( m < percentileValueLessThan );
        }

        @Test
        public void callMethod() throws Exception{
            if ( nApiThread.use() ){
               // make use of multi threading
                int nT = nApiThread.numThreads() ;
                WorkerThread[] workers = new WorkerThread[nT];
                ExecutorService executor = Executors.newFixedThreadPool(nT);
                int pt = nApiThread.pacingTime();
                int sd = nApiThread.spawnTime() ;
                int nc = nApiThread.numCallPerThread() ;
                for (int i = 0; i < nT ; i++) {
                    WorkerThread.delay( sd );
                    CallContainer[] containers = new CallContainer[nc];
                    if ( nApiThread.dcd() ){
                        for ( int j = 0 ; j < nc ; j++ ){
                            callContainer = iterator.next() ;
                            containers[j] = CallContainer.clone(callContainer);
                        }
                    }else{
                        for ( int j = 0 ; j < nc ; j++ ){
                            containers[j] = CallContainer.clone(callContainer);
                        }
                    }
                    workers[i] = new WorkerThread( containers , pt);
                    //calling execute method of ExecutorService
                    executor.execute(workers[i]);
                }
                executor.shutdown();
                while (!executor.isTerminated()) {  WorkerThread.delay(100); }
                // if it was performance ...
                Annotations.Performance performance = nApiThread.performance();
                if ( performance.use() ){
                    Double lessThan = callContainer.percentile();
                    short intPercentile = performance.percentile() ;
                    double percentile = 0.9 ;
                    if ( intPercentile > 0 && intPercentile < 100 ){
                        percentile = intPercentile/100.0;
                    }
                    if ( lessThan == null ){
                        lessThan = performance.lessThan()  ;
                    }
                    boolean passed = doPerformanceCheck( workers, percentile , lessThan );
                    if ( !passed ){
                        throw new Exception("Performance Test Failed!");
                    }
                }
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

    /**
     * One single static class field
     */
    public static final Class proxy = ProxyTest.class ;

    /**
     * Creates a jUnit runner
     * @param testNumber unique test number
     * @param iterator iterator to get the data
     * @param mi method information to wrap to create tests
     * @return one runner
     * @throws Exception in case of failure to do so
     */
    public static JApiRunner createRunner( int testNumber,
             XStreamIterator<CallContainer> iterator,
                                           MethodRunInformation mi) throws Exception{
        TestClass testClass = new TestClass(proxy);
        String name = mi.method.getName() ;
        List<Object> parameters = new ArrayList<>();
        parameters.add( testNumber );
        parameters.add( iterator );
        parameters.add( mi.nApiThread );
        TestWithParameters test = new TestWithParameters( name, testClass, parameters);
        return new JApiRunner(test);
    }

    /**
     * Wraps a test
     * @param test a jUnit test with parameters
     * @throws InitializationError in case it fails to wrap
     */
    public JApiRunner(TestWithParameters test) throws InitializationError {
        super(test);
    }
}
