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

package com.noga.njexl.testing.api;

import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;


/**
 * Created by noga on 27/05/15.
 */
public final class Annotations {

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.TYPE )
    public @interface NApiService {

        /**
         * If true then use this for testing
         * @return true if one needs to run, false otherwise
         */
        boolean use() default true ;

        /**
         * The base directory where all data and script
         * would be stored
         * @return directory
         */
        String base() default "" ;

        /**
         * The before class script location ( not implemented )
         * @return before class script location
         */
        String beforeClass() default "" ;

        /**
         * The after class script location ( not implemented )
         * @return after class script location
         */
        String afterClass() default "" ;
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.TYPE )
    public @interface NApiServiceCreator {

        /**
         * The service creator class
         * @return service creator class
         */
        Class type() default  ServiceCreatorFactory.SimpleCreator.class ;

        /**
         * String representation of the arguments
         * needs to call constructor of the service creator class
         * @return arguments to the constructor
         */
        String[] args() default {};
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.CONSTRUCTOR )
    public @interface NApiServiceInit {

        /**
         * The Spring Bean Name
         * When one wants to instantiate the class using spring
         * @return bean name
         */
        String bean() default "";

        /**
         * String representation of the arguments
         * needs to call constructor of the service class
         * @return arguments to the constructor
         */
        String[] args() default {};
    }

    @Retention( RetentionPolicy.RUNTIME )
    public @interface Performance{

        /**
         * Is this a performance test
         * @return true if it is, false if it is not
         */
        boolean use() default true ;

        /**
         * Percentile for performance
         * Defaults to 90%
         * @return the percentile you are seeking
         */
        short percentile() default 90;

        /**
         * Percentile for performance
         * The experimental value should not exceed this,
         * Else exception will be thrown. Defaults to @{CallContainer.DEFAULT_PERCENTILE_VALUE}
         * @return the expected experimental upper cut-off of the percentile value
         */
        double lessThan()  default  CallContainer.DEFAULT_PERCENTILE_VALUE  ;
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    public @interface NApiThread {

        /**
         *  Whether or not use threaded mode
         * @return true/false
         */
        boolean use() default true;

        /**
         * Number of threads
         * @return no of threads, default 2
         */
        int numThreads() default 2 ;

        /**
         *
         * @return no of call per thread, default 2
         */
        int numCallPerThread() default 2 ;

        /**
         * In Milliseconds
         * @return time in ms to spawn threads
         */
        int spawnTime() default  1000 ;

        /**
         * In Milliseconds
         * @return time between two calls in same thread
         */
        int pacingTime() default 1000 ;

        /**
         * For DCD : [D]ifferent [C]all [D]ifferent (Data)
         * In case all the threads would execute different input or not
         * @return true if all calls needs to execute different input, false otherwise
         */
        boolean dcd() default false ;

        /**
         * The performance strategy if any, default off
         * @return the performance strategy
         */
        Performance performance() default @Performance(use=false);

    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    public @interface NApi {

        /**
         * If true then use this for testing
         * @return true if one needs to run, false otherwise
         */
        boolean use() default true ;

        /**
         * <pre>
         * The data source location :
         *   [1] url
         *   [2] Directory with flat files
         *   [3] Excel files
         * </pre>
         * @return the location
         */
        String dataSource();

        /**
         * The actual data table
         * <pre>
         *     A flat file
         *     A table ( index ) in a url
         *     A data sheet in excel file
         * </pre>
         * @return name of the table
         */
        String dataTable();

        /**
         * Ensures that the call order of data is random
         * @return randomized if true else executes test vectors in order
         */
        boolean randomize() default false;

        /**
         * The script that would invoke
         * before any of the test vector calls started ( not implemented )
         * @return relative location of the script w.r.t. base
         */
        String beforeAll() default "";

        /**
         * The script that would invoke
         * after all of the test vector calls done ( not implemented )
         * @return relative location of the script w.r.t. base
         */
        String afterAll() default "";

        /**
         * The script that would invoke
         * before per test vector call
         * @return relative location of the script w.r.t. base
         */
        String before() default "";

        /**
         * The script that would invoke
         * after per test vector call
         * @return relative location of the script w.r.t. base
         */
        String after() default "";

        /**
         * Global Variables which would be accessible
         * per test vector call inside all the scripts
         * <pre>
         *     The way to put vars is :
         *     { "x=a" , "y=b" ,...}
         *     This ensures x variable is assigned the value 'a'.
         * </pre>
         * @return global variables entries to a global dict
         */
        String[] globals() default {};
    }

    public static class MethodRunInformation{
        public String base;
        public Method method;
        public NApi nApi;
        public NApiThread nApiThread ;
    }

    public static NApiService NApiService(Class c){
        NApiService ns = (NApiService) c.getAnnotation( NApiService.class);
        if ( ns != null && ns.use() ){
            return ns;
        }
        return null;
    }

    public static NApiServiceCreator NApiServiceCreator(Class c){
        return (NApiServiceCreator) c.getAnnotation(NApiServiceCreator.class);
    }

    public static NApiServiceInit NApiServiceInit(Constructor c){
        return (NApiServiceInit) c.getAnnotation(NApiServiceInit.class);
    }

    public static NApi NApi(Method m){
        NApi nApi = m.getAnnotation(NApi.class);
        if ( nApi != null && nApi.use() ){
            return nApi;
        }
        return null;
    }

    public static NApiThread NApiThread(Method m){
        return m.getAnnotation(NApiThread.class);
    }

    public static List<MethodRunInformation> runs( Class c ){
        NApiService ns = NApiService(c);
        if ( ns == null ) { return Collections.emptyList() ; }
        Method[] methods = c.getDeclaredMethods();
        ArrayList<MethodRunInformation> l = new ArrayList();
        for ( int i = 0 ; i < methods.length; i++ ){
            NApi nApi = NApi(methods[i]);
            if ( nApi == null ){  continue; }
            NApiThread nApiThread = NApiThread(methods[i]);
            MethodRunInformation methodRunInformation = new MethodRunInformation();
            methodRunInformation.base = ns.base();
            methodRunInformation.method = methods[i];
            methodRunInformation.nApi = nApi ;
            methodRunInformation.nApiThread = nApiThread ;
            l.add(methodRunInformation);
        }
        return l;
    }
}
