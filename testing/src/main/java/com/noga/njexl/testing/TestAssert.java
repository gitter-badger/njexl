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


import com.noga.njexl.lang.JexlException;
import com.noga.njexl.lang.extension.TypeUtility;

import java.util.EventObject;
import java.util.HashSet;

/**
 * Created by noga on 15/04/15.
 */
public final class TestAssert {

    public static final String ASSERT_VAR = "assert" ;

    public static final String ASSERT_NS = "assert" ;

    boolean error;

    public boolean hasError(){
        return error;
    }

    public void clearError(){ error = false ; }

    public enum AssertionType {
        TEST,
        ABORT
    }

    public static class AssertionEvent extends  EventObject {

        public  final  AssertionType type;

        public final Object[] data;

        public final boolean value;

        public AssertionEvent(Object source, AssertionType type, boolean value, Object[] data) {
            super(source);
            this.type = type;
            this.data = data ;
            this.value = value ;
        }

        @Override
        public String toString(){
            boolean failed = ((TestAssert)getSource()).hasError();
            String ret = String.format("%s %s => %s", type, value, com.noga.njexl.lang.Main.strArr(data));
            if ( failed ){
                return "!!!" + ret ;
            }
            return ret;
        }
    }

    public interface AssertionEventListener{

        void onAssertion(AssertionEvent assertionEvent);

    }

    public final HashSet<AssertionEventListener> eventListeners;

    public TestAssert(){
        eventListeners = new HashSet<>();
    }

    public void test(boolean value, Object...args){
        error = !value ;
        for ( AssertionEventListener listener : eventListeners ){
            AssertionEvent event = new AssertionEvent(this, AssertionType.TEST, value,args);
            try {
                listener.onAssertion(event);
            }catch (Throwable t){
                System.err.println("Error in sending to listener : " + t);
            }
        }
    }

    public void abort(boolean value, Object...args) throws JexlException.Return {
        error = value ;
        for ( AssertionEventListener listener : eventListeners ){
            AssertionEvent event = new AssertionEvent(this, AssertionType.ABORT, value,args);
            listener.onAssertion(event);
        }
        if ( value ){
            TypeUtility.bye(args);
        }
    }
}
