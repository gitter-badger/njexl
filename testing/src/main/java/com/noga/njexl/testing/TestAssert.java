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

package com.noga.njexl.testing;


import com.noga.njexl.lang.Interpreter.AnonymousParam;
import com.noga.njexl.lang.JexlException;
import com.noga.njexl.lang.extension.TypeUtility;

import java.util.EventObject;
import java.util.HashSet;

/**
 * A central assertion framework
 * Created by noga on 15/04/15.
 */
public final class TestAssert {

    public static final String ASSERT_VAR = "assert" ;

    public static final String ASSERT_NS = "assert" ;

    boolean error;

    /**
     * Does the assert has error?
     * @return true if it has, false if it does not
     */
    public boolean hasError(){
        return error;
    }

    /**
     * Clears the assert error
     */
    public void clearError(){ error = false ; }

    /**
     * Types of assertion
     */
    public enum AssertionType {
        /**
         * Test assertion : when true it passes, when false it fails
         */
        TEST,
        /**
         * When true, the current test script will be aborted , false means no effect
         */
        ABORT
    }

    /**
     * A standard class to axiomatize the assertions
     */
    public static final class AssertionAssignment extends Throwable{

        private AssertionAssignment(){ }

        @Override
        public String toString() {
            return "Assertion";
        }
    }

    /**
     * A final field to ensure default assertion type is axiomatic
     */
    public static final AssertionAssignment assertion = new AssertionAssignment();

    /**
     * An Assertion Event object
     */
    public static class AssertionEvent extends  EventObject {

        /**
         * Type of assertion
         */
        public  final  AssertionType type;

        /**
         * In case the caller passed any data
         */
        public final Object[] data;

        /**
         * The final value of this assertion
         */
        public final boolean value;

        /**
         * <pre>
         * What caused this assertion?
         * Either it is an expression evaluated, in which case it will be @{AssertionAssignment}
         * Else it is created by failure to evaluate block of code ( anonymous function )
         * where the resultant error would be stored.
         * </pre>
         */
        public final Throwable cause;

        /**
         * Creates an object
         * @param source source of the event
         * @param type what type the assertion is
         * @param value for test true passes, for abort false passes
         * @param cause cause of the assertion
         * @param data any data people wants to pass
         */
        public AssertionEvent(Object source, AssertionType type, boolean value, Throwable cause, Object[] data) {
            super(source);
            this.type = type;
            this.cause = cause ;
            this.data = data ;
            this.value = value ;
        }

        @Override
        public String toString(){
            boolean failed = ((TestAssert)getSource()).hasError();
            String ret = String.format("%s %s => %s | caused by : %s", type, value,
                    com.noga.njexl.lang.Main.strArr(data), cause );
            if ( failed ){
                return "!!!" + ret ;
            }
            return ret;
        }
    }

    /**
     * Anyone who wants to listen to assertions
     */
    public interface AssertionEventListener{

        void onAssertion(AssertionEvent assertionEvent);

    }

    /**
     * The event listeners
     */
    public final HashSet<AssertionEventListener> eventListeners;

    public TestAssert(){
        eventListeners = new HashSet<>();
    }

    /**
     * Fires a test assertion -
     * @param v - Either a boolean, or an anonymous function or an object to be evaluated as boolean
     *          true passes it, false fails it
     * @param args any args one may pass
     */
    public void test(Object v, Object...args){
        boolean value = false ;
        Throwable cause = assertion;
        if ( v instanceof Boolean ){
            value = (boolean)v;
        }
        else if ( v instanceof AnonymousParam ){
            AnonymousParam anon = (AnonymousParam)v;
            try {
                Object o = anon.execute();
                value = TypeUtility.castBoolean(o,false);
            }catch (Throwable t){
                cause = t.getCause();
                if ( cause == null ){
                    cause = t;
                }
            }
        }else{
            value = TypeUtility.castBoolean(v,false);
        }

        error = !value ;
        for ( AssertionEventListener listener : eventListeners ){
            AssertionEvent event = new AssertionEvent(this, AssertionType.TEST, value, cause, args);
            try {
                listener.onAssertion(event);
            }catch (Throwable t){
                System.err.printf("Error *test* asserting to listener : %s [%s]\n",listener,t);
            }
        }
    }

    /**
     * Fires a abort assertion -
     * @param v - Either a boolean, or an anonymous function or an object to be evaluated as boolean
     *          true aborts current script , false is a no operation
     * @param args any args one may pass
     */
    public void abort(Object v, Object...args) throws JexlException.Return {
        boolean value = true ;
        Throwable cause = assertion;
        if ( v instanceof Boolean ){
            value = (boolean)v;
        }
        else if ( v instanceof AnonymousParam ){
            AnonymousParam anon = (AnonymousParam)v;
            try {
                Object o = anon.execute();
                value = TypeUtility.castBoolean(o,true);
            }catch (Throwable t){
                cause = t.getCause();
                if ( cause == null ){
                    cause = t;
                }
            }
        }else{
            value = TypeUtility.castBoolean(v,true);
        }

        error = value ;
        for ( AssertionEventListener listener : eventListeners ){
            AssertionEvent event = new AssertionEvent(this, AssertionType.ABORT, value,cause,args);
            try {
                listener.onAssertion(event);
            }catch (Throwable t){
                System.err.printf("Error *abort* asserting to listener : %s [%s]\n",listener,t);
            }
        }
        if ( value ){
            TypeUtility.bye(args);
        }
    }
}
