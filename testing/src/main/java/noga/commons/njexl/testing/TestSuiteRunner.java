package noga.commons.njexl.testing;

import noga.commons.njexl.testing.dataprovider.DataSourceTable;

import java.util.EventObject;
import java.util.HashSet;

/**
 * Created by noga on 17/04/15.
 */
public abstract class TestSuiteRunner implements Runnable{

    public enum TestRunEventType{
        BEFORE_FEATURE,
        BEFORE_TEST,
        OK_TEST,
        ERROR_TEST,
        ABORT_TEST,
        IGNORE_TEST,
        AFTER_FEATURE
    }

    public static class TestRunEvent extends EventObject{

        public final TestRunEventType type ;

        public final DataSourceTable table;

        public final int row;

        public TestRunEvent(Object source,TestRunEventType type, DataSourceTable table ,int row) {
            super(source);
            this.type = type ;
            this.row = row;
            this.table = table;
        }
    }

    public interface TestRunEventListener{

        void onTestRunEvent(TestRunEvent testRunEvent);

    }

    public final HashSet<TestRunEventListener> testRunEventListeners ;

    protected TestSuiteRunner(){
        testRunEventListeners = new HashSet<>();
    }

    protected void fireTestEvent(TestRunEventType type, DataSourceTable table, int row){
        TestRunEvent event = new TestRunEvent( this, type, table, row);
        for (TestRunEventListener listener : testRunEventListeners ){
            try {
                listener.onTestRunEvent( event );
            }catch (Throwable t){
                System.err.println("Error dispatching event : " + t);
            }
        }
    }

    protected abstract void prepare() throws Exception;

    protected abstract TestSuite.Application application();

    protected abstract DataSourceTable dataSourceTable( TestSuite.BaseFeature feature);

    protected abstract void beforeFeature(TestSuite.BaseFeature feature) throws Exception;

    protected abstract  TestRunEventType runTest( DataSourceTable table, int row) throws Exception;

    protected abstract void afterFeature(TestSuite.BaseFeature feature) throws Exception;

    protected abstract void shutdown() throws Exception;


    @Override
    public void run() {
         try{
             prepare();
         }catch (Exception e){
             System.err.println(e);
             return;
         }

        TestSuite.Application application = application();
        for ( int i = 0 ; i < application.features.size() ;i++){

            TestSuite.BaseFeature feature = application.features.get(i);
            if ( !feature.enabled ){
                continue;
            }
            try{
                beforeFeature(feature);
            }catch (Exception e){
                System.err.printf("Error : %s\n Skipping Feature %s\n", e , feature.name );
                continue;
            }
            fireTestEvent(TestRunEventType.BEFORE_FEATURE, null, -1);

            DataSourceTable table = dataSourceTable(feature);
            if ( table == null ){
                System.err.println("Sorry, can not create data source!");
                fireTestEvent(TestRunEventType.AFTER_FEATURE, null, -1);
            }
            for ( int row = 1 ; row < table.length() ; row ++){
                fireTestEvent(TestRunEventType.BEFORE_TEST, table, row);
                TestRunEventType result = TestRunEventType.ERROR_TEST ;
                try {
                    result = runTest(table, row);
                }catch (Throwable t){
                    System.err.println(t);
                }
                fireTestEvent(result, table, row);
            }
            fireTestEvent(TestRunEventType.AFTER_FEATURE, null, -1);
            try{
                afterFeature(feature);
            }catch (Exception e){
                continue;
            }
        }
        try{
            shutdown();
        }catch (Exception e){
            System.err.println(e);
            return;
        }
    }
}
