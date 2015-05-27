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


/**
 * Created by noga on 27/05/15.
 */
public final class Annotations {

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.TYPE )
    public @interface NApiService {

        String beforeClass() default "";

        String afterClass() default "";

    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.TYPE )
    public @interface NApiServiceCreator {

        String name();

        String type() default  "simple" ;

        String[] args() default {};
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.CONSTRUCTOR )
    public @interface NApiServiceInit {

        String creator();

        String[] args() default {};
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    public @interface NApiThread {

        boolean use() default false;

        int numThreads() default 2 ;

        int numCallPerThread() default 2 ;

        int pacingTime() default 1000 ;

        String shareMode() default "" ;

    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( ElementType.METHOD )
    public @interface NApi {

        String dataSource();

        String dataTable();

        String before() default "";

        String after() default "";

        String validatorCreationMode() default "";
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
}
