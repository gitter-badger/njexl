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

        String base() default "" ;
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.TYPE )
    public @interface NApiServiceCreator {

        Class type() default  ServiceCreatorFactory.SimpleCreator.class ;

        String[] args() default {};
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.CONSTRUCTOR )
    public @interface NApiServiceInit {

        String bean() default "";

        String[] args() default {};

    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    public @interface NApiThread {

        /**
         *  Whether or not use threaded mode
         * @return true/false
         */
        boolean use() default false;

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
         * Is this a performance test
         * @return true if it is, false if it is not
         */
        boolean performance() default  false ;

        /**
         * 90% for performance
         * @return the percentile value
         */
        double ninetyPercentile() default  CallContainer.DEFAULT_NINETY_PERCENTILE  ;

    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    public @interface NApi {

        String dataSource();

        String dataTable();

        String before() default "";

        String after() default "";

        String[] globals() default {};

        String validatorCreationMode() default "";
    }

    public static class MethodRunInformation{
        public String base;
        public Method method;
        public NApi nApi;
        public NApiThread nApiThread ;
    }

    public static NApiService NApiService(Class c){
        return (NApiService) c.getAnnotation( NApiService.class);
    }
    public static NApiServiceCreator NApiServiceCreator(Class c){
        return (NApiServiceCreator) c.getAnnotation(NApiServiceCreator.class);
    }

    public static NApiServiceInit NApiServiceInit(Constructor c){
        return (NApiServiceInit) c.getAnnotation(NApiServiceInit.class);
    }

    public static NApi NApi(Method m){
        return  m.getAnnotation(NApi.class);
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
