package noga.commons.njexl.testing;

import noga.commons.njexl.testing.dataprovider.DataSourceTable;
import noga.commons.njexl.testing.reporting.Reporter;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

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

        public TestRunEventType type ;

        public final String feature ;

        public final DataSourceTable table;

        public final int row;

        public Object runObject;

        public Throwable error;

        public TestRunEvent(Object source,TestRunEventType type, String feature, DataSourceTable table ,int row) {
            super(source);
            this.type = type ;
            this.feature = feature;
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

    protected void fireTestEvent(String feature, TestRunEventType type, DataSourceTable table, int row){
        TestRunEvent event = new TestRunEvent( this, type, feature, table, row);
        fireTestEvent(event);
    }

    protected void fireTestEvent(TestRunEvent event){
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

    protected abstract String logLocation( TestSuite.BaseFeature feature);

    protected abstract Set<Reporter>  reporters();

    protected abstract void beforeFeature(TestSuite.BaseFeature feature) throws Exception;

    protected abstract  TestRunEvent runTest( TestRunEvent runEvent) throws Exception;

    protected abstract void afterFeature(TestSuite.BaseFeature feature) throws Exception;

    protected abstract void shutdown() throws Exception;

    protected void addReporters(){
        Set reporters = reporters();
        testRunEventListeners.addAll(reporters);
    }

    protected void removeReporters(){
        Set reporters = reporters();
        testRunEventListeners.removeAll(reporters);
    }

    protected void changeLogDirectory(TestSuite.BaseFeature feature){
        String logDir = logLocation(feature);
        Set<Reporter> reporters = reporters();
        for ( Reporter r : reporters ){
            r.location(logDir);
        }
    }

    @Override
    public void run() {

        try{
            prepare();
            addReporters();
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
            changeLogDirectory(feature);

            fireTestEvent(feature.name,TestRunEventType.BEFORE_FEATURE, null, -1);

            DataSourceTable table = dataSourceTable(feature);
            if ( table == null ){
                System.err.println("Sorry, can not create data source!");
                fireTestEvent(feature.name,TestRunEventType.AFTER_FEATURE, null, -1);
            }
            for ( int row = 1 ; row < table.length() ; row ++){
                fireTestEvent(feature.name, TestRunEventType.BEFORE_TEST, table, row);
                TestRunEvent runEvent = new TestRunEvent(this, TestRunEventType.ERROR_TEST, feature.name, table, row) ;
                try {
                    runEvent = runTest(runEvent);
                }catch (Throwable t){
                    System.err.println(t);
                    runEvent.error = t ;
                }
                fireTestEvent(runEvent);
            }
            fireTestEvent(feature.name,TestRunEventType.AFTER_FEATURE, null, -1);
            try{
                afterFeature(feature);
            }catch (Exception e){
                continue;
            }
        }
        try{
            removeReporters();
            shutdown();
        }catch (Exception e){
            System.err.println(e);
            return;
        }
    }
}
