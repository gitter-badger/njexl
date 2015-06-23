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

package com.noga.njexl.lang.extension.oop;

import com.noga.njexl.lang.Main;

import java.util.regex.Pattern;

/**
 * Created by noga on 08/04/15.
 */
public final class ScriptClassBehaviour {

    /**
     *  string representation function name
     */
    public static final String STR = "__str__" ;

    /**
     * object equal function name
     */
    public static final String EQ = "__eq__" ;

    /**
     * hash code function name
     */
    public static final String HC = "__hc__" ;

    /**
     * object compare function name
     */
    public static final String CMP = "__cmp__" ;


    /**
     * This is used to compare nJexl extended types
     */
    public interface TypeAware{

        /**
         * if the object is of type of o
         * @param o the other object
         * @return true if this isa o type ( derivable from ) else false
         */
        boolean isa( Object o);

    }

    /**
     * Used in eventing before/after method call using namespaces
     */
    public interface Eventing {

        /**
         * Method name for before event
         */
        String BEFORE = "__before__" ;

        /**
         * Method name for after event
         */
        String AFTER = "__after__" ;

        /**
         * Thus, only 4 types of waiting pattern are possible
         * You should choose wisely
         */
        Pattern EVENTS = Pattern.compile("^[@\\$][@\\$].+", Pattern.DOTALL );

        /**
         * Before event call
         * @param event what event occurred
         */
        void before(Event event) ;

        /**
         * After event call
         * @param event what event occurred
         */
        void after(Event event) ;

        /**
         * an implementation of the eventing
         */
        final class Timer implements Eventing{

            public static final Timer TIMER = new Timer();

            long t;

            @Override
            public void after(Event event) {
                long ts = System.currentTimeMillis() - t ;
                System.out.printf("<<%s%s:%d>>\n", event.pattern, event.method, ts);
            }

            @Override
            public void before(Event event) {
                 t = System.currentTimeMillis();
            }
        }

        /**
         * Event container
         */
        final class Event{

            /**
             * the pattern
             */
            public final String pattern;

            /**
             * the method
             */
            public final Object method;

            /**
             * the arguments to the method
             */
            public final Object[] args;

            /**
             * cached copy of the description
             */
            public final String description;

            /**
             * Creates an event object
             * @param p what pattern was used
             * @param m what method was called
             * @param a what argument was passed
             */
            public Event(String p, Object m, Object[] a){
                pattern = p;
                method = m;
                args = a;
                description = String.format( "%s | %s | %s" , p,m, Main.strArr(a));
            }

            @Override
            public String toString(){
                return description;
            }
        }
    }

    /**
     * This lets one execute arbitrary method, given args
     */
    public interface Executable {

        /**
         * Execute a method
         * @param method name of the method
         * @param args to be passed as argument
         * @return result of the method
         */
        Object execMethod(String method, Object[] args) ;

    }

    /**
     * This lets us do arithmetic
     */
    public interface Arithmetic{

        /**
         * name of the negation function
         */
        String NEG = "__neg__" ;

        /**
         * negates the object
         * @return new object which is the negation of the object
         */
        Object neg()  ;

        /**
         * name of addition function
         */
        String ADD = "__add__" ;

        /**
         * Addition operation
         * @param o other object
         * @return a new object with this + other
         */
        Object add(Object o) ;

        String SUB = "__sub__" ;

        /**
         * Subtraction operation
         * @param o other object
         * @return a new object with this - other
         */
        Object sub(Object o) ;

        /**
         * name of multiplication function
         */
        String MUL = "__mul__" ;

        /**
         * Multiplication operation
         * @param o other object
         * @return a new object with this X other
         */
        Object mul(Object o)  ;

        /**
         * name of division function
         */
        String DIV = "__div__" ;

        /**
         * Division operation
         * @param o other object
         * @return a new object with this divide other
         */
        Object div(Object o)  ;

        /**
         * name of exponentiation function
         */
        String EXP = "__exp__" ;

        /**
         * Exponentiation operation
         * @param o other object
         * @return a new object with this exponent the other
         */
        Object exp(Object o)  ;

    }

    public interface Logic{

        /**
         * name of complementing function
         */
        String COMPLEMENT = "__complement__" ;

        /**
         * complement operation
         * @return a new object which is complement of this
         */
        Object complement()  ;

        /**
         * name of OR function
         */
        String OR = "__or__" ;

        /**
         * OR operation
         * @param o other object
         * @return a new object with this OR other
         */
        Object or(Object o) ;

        /**
         * name of AND function
         */
        String AND = "__and__" ;

        /**
         * AND operation
         * @param o other object
         * @return a new object with this AND other
         */
        Object and(Object o) ;

        /**
         * name of XOR function
         */
        String XOR = "__xor__" ;

        /**
         * XOR operation
         * @param o other object
         * @return a new object with this XOR other
         */
        Object xor(Object o)  ;

    }
}
