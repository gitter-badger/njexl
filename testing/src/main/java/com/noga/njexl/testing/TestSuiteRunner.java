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

import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.testing.dataprovider.DataSource;
import com.noga.njexl.testing.dataprovider.DataSourceTable;
import com.noga.njexl.testing.dataprovider.ProviderFactory;
import com.noga.njexl.testing.reporting.Reporter;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by noga on 17/04/15.
 */
public abstract class TestSuiteRunner implements Runnable{

    public static final String SCRIPT_OUT = "_o_" ;

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

    public final Set<TestRunEventListener> testRunEventListeners ;

    protected Set<Reporter> reporters;

    public static class DataSourceContainer{

        public final TestSuite.DataSource suiteDataSource;

        public final DataSource dataSource;

        public DataSourceContainer(TestSuite.DataSource ds )throws Exception{
            this.suiteDataSource = ds ;
            dataSource = ProviderFactory.dataSource(suiteDataSource.location);
            if ( dataSource == null ){
                throw new Exception("Can not create data source!");
            }
        }
    }

    protected Map<String,DataSourceContainer> dataSources;

    protected Map<String,TestSuite.DataSource> tsDataSources;

    protected Map<String,String> relocationVariables;

    protected TestSuiteRunner(Map<String,String> v){
        testRunEventListeners = new HashSet<>();
        relocationVariables = v;
    }


    protected void fireTestEvent(String feature, TestRunEventType type, DataSourceTable table, int row){
        TestRunEvent event = new TestRunEvent( this, type, feature, table, row);
        fireTestEvent(event);
    }

    protected void fireTestEvent(TestRunEvent event){

        switch (event.type ){
            case ABORT_TEST:
                aborts.add( event );
                break;
            case ERROR_TEST:
                errors.add(event);
                break;
            default:
                break;
        }

        for (TestRunEventListener listener : testRunEventListeners ){
            try {
                listener.onTestRunEvent( event );
            }catch (Throwable t){
                System.err.println("Error dispatching event : " + t);
            }
        }
    }

    protected abstract TestSuite testSuite();

    protected abstract TestSuite.Application application();

    protected abstract void prepare() throws Exception;

    protected abstract void beforeFeature(TestSuite.Feature feature) throws Exception;

    protected abstract String logLocation(String base, TestSuite.Feature feature) ;

    protected abstract  TestRunEvent runTest( TestRunEvent runEvent) throws Exception;

    protected abstract void afterFeature(TestSuite.Feature feature) throws Exception;

    protected abstract void shutdown() throws Exception;

    protected void prepareDSAndReporters() throws Exception {
        dataSources = new HashMap<>();
        tsDataSources = new HashMap<>();
        for (TestSuite.DataSource ds : testSuite.dataSources ){
            DataSourceContainer container = new DataSourceContainer(ds);
            dataSources.put( ds.name, container );
        }
        reporters = new HashSet<>();
        for (TestSuite.Reporter r : testSuite.reporters ){
            Reporter reporter = (Reporter) Utils.createInstance(r.type);
            reporter.init( r.params );
            reporters.add(reporter);
        }
        testRunEventListeners.addAll(reporters);
        aborts = new ArrayList<>();
        errors = new ArrayList<>();
    }

    protected void changeLogDirectory(TestSuite.Feature feature){
        String timeStamp = Utils.ts();
        String logDir = logLocation(timeStamp, feature);
        for ( Reporter r : reporters ){
            r.location(logDir);
        }
    }

    protected DataSourceTable dataSourceTable(TestSuite.Feature feature) {
        DataSourceContainer container = dataSources.get(feature.ds) ;
        if ( container == null ){
            System.err.printf("No Such data source : [%s]\n",feature.ds);
            return null;
        }
        DataSourceTable table = container.dataSource.tables.get(feature.table);
        if ( table == null ){
            System.err.printf("No Such data table in Data Source : [%s] [%s]\n",feature.table, feature.ds);
        }
        return table;
    }

    boolean skipTest(TestSuite.Feature feature, int row){
        DataSourceContainer container = dataSources.get(feature.ds) ;
        DataSourceTable t = container.dataSource.tables.get(feature.table);
        boolean exist = t.columns().containsKey( container.suiteDataSource.testEnableColumn );
        if ( !exist ){
            return false ;
        }
        String value = t.columnValue(container.suiteDataSource.testEnableColumn, row) ;
        boolean enable = TypeUtility.castBoolean(value, false);
        return !enable;
    }

    protected TestSuite testSuite ;

    protected Collection<TestRunEvent> aborts;

    public Collection<TestRunEvent> aborts(){ return aborts; }

    protected Collection<TestRunEvent> errors;

    public Collection<TestRunEvent> errors(){ return errors; }

    @Override
    public void run() {
        // get it first...
        testSuite = testSuite();
        try{
            prepareDSAndReporters();
            prepare();
        }catch (Exception e) {
            System.err.printf("Failed to prepare test Suite! %d\n", e);
             return;
         }

        TestSuite.Application application = application();
        for ( int i = 0 ; i < application.features.size() ;i++){

            TestSuite.Feature feature = application.features.get(i);
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
                boolean skip = skipTest(feature,row) ;
                if ( skip ){
                    fireTestEvent(feature.name, TestRunEventType.IGNORE_TEST, table, row);
                    continue;
                }
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
            testRunEventListeners.clear();
            shutdown();
        }catch (Exception e){
            System.err.println(e);
            return;
        }
    }
}
