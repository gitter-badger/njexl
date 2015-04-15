package noga.commons.njexl.testing;

import noga.commons.njexl.JexlException;
import noga.commons.njexl.extension.TypeUtility;

import java.util.EventObject;
import java.util.HashSet;

/**
 * Created by noga on 15/04/15.
 */
public final class TestAssert {

    public static final String ASSERT_VAR = "assert" ;

    public static final String ASSERT_NS = "assert" ;


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

    }

    public interface AssertionEventListener{

        void onAssertion(AssertionEvent assertionEvent);

    }

    public final HashSet<AssertionEventListener> eventListeners;

    public TestAssert(){
        eventListeners = new HashSet<>();
    }

    public void test(boolean value, Object...args){
        for ( AssertionEventListener listener : eventListeners ){
            AssertionEvent event = new AssertionEvent(this, AssertionType.TEST, value,args);
            listener.onAssertion(event);
        }
    }

    public void abort(boolean value, Object...args) throws JexlException.Return {
        for ( AssertionEventListener listener : eventListeners ){
            AssertionEvent event = new AssertionEvent(this, AssertionType.ABORT, value,args);
            listener.onAssertion(event);
        }
        if ( value ){
            TypeUtility.bye(args);
        }
    }
}
