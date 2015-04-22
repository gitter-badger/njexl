/*
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

package com.noga.njexl.lang.extension.oop;

import java.util.regex.Pattern;

/**
 * Created by noga on 08/04/15.
 */
public final class ScriptClassBehaviour {


    public static final String STR = "__str__" ;

    public static final String EQ = "__eq__" ;

    public static final String HC = "__hc__" ;

    public static final String CMP = "__cmp__" ;


    public interface TypeAware{

        boolean isa( Object o);

    }

    public interface Eventing {

        /**
         * Thus, only 4 types of waiting pattern are possible
         * You should choose wisely
         */
        Pattern EVENTS = Pattern.compile("^[@\\$][@\\$].+", Pattern.DOTALL );

        /**
         * Before event call
         * @param pattern
         * @param method
         * @param args
         * @throws Exception
         */
        void before(String pattern, String method ,Object[] args) ;

        /**
         * After event call
         * @param pattern
         * @param method
         * @param args
         * @throws Exception
         */
        void after(String pattern, String method, Object[] args) ;

        final class Timer implements Eventing{

            public static Timer TIMER = new Timer();

            long t;

            @Override
            public void after(String pattern, String method, Object[] args) {
                long ts = System.currentTimeMillis() - t ;
                System.out.printf("<<%s%s:%d>>\n", pattern, method, ts);
            }

            @Override
            public void before(String pattern, String method, Object[] args) {
                 t = System.currentTimeMillis();
            }
        }
    }

    public interface Executable {

        Object execMethod(String method, Object[] args) ;

    }

    public interface Arithmetic{

        String NEG = "__neg__" ;

        Object neg()  ;

        String ADD = "__add__" ;

        Object add(Object o) ;

        String SUB = "__sub__" ;

        Object sub(Object o) ;

        String MUL = "__mul__" ;

        Object mul(Object o)  ;

        String DIV = "__div__" ;

        Object div(Object o)  ;

        String EXP = "__exp__" ;

        Object exp(Object o)  ;

    }

    public interface Logic{

        String COMPLEMENT = "__complement__" ;

        Object complement()  ;

        String OR = "__or__" ;

        Object or(Object o) ;

        String AND = "__and__" ;

        Object and(Object o) ;

        String XOR = "__xor__" ;

        Object xor(Object o)  ;

    }
}
