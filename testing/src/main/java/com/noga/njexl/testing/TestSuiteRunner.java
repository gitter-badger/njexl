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
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;

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

    public final HashSet<TestRunEventListener> testRunEventListeners ;

    protected HashSet<Reporter> reporters;

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

    protected HashMap<String,DataSourceContainer> dataSources;

    protected HashMap<String,TestSuite.DataSource> tsDataSources;

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

    protected abstract TestSuite testSuite();

    protected abstract TestSuite.Application application();

    protected abstract void prepare() throws Exception;

    protected abstract void beforeFeature(TestSuite.BaseFeature feature) throws Exception;

    protected abstract String logLocation(String base, TestSuite.BaseFeature feature) ;

    protected abstract  TestRunEvent runTest( TestRunEvent runEvent) throws Exception;

    protected abstract void afterFeature(TestSuite.BaseFeature feature) throws Exception;

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
    }

    protected void changeLogDirectory(TestSuite.BaseFeature feature){
        String timeStamp = Utils.ts();
        String logDir = logLocation(timeStamp, feature);
        for ( Reporter r : reporters ){
            r.location(logDir);
        }
    }

    protected DataSourceTable dataSourceTable(TestSuite.BaseFeature feature) {
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

    boolean skipTest(TestSuite.BaseFeature feature, int row){
        DataSourceContainer container = dataSources.get(feature.ds) ;
        DataSourceTable t = container.dataSource.tables.get(feature.table);
        boolean exist = t.columns().containsKey( container.suiteDataSource.testEnableColumn );
        if ( !exist ){
            return false ;
        }
        boolean result = TypeUtility.castBoolean(t.columnValue(container.suiteDataSource.testEnableColumn, row), false);
        return result;
    }

    TestSuite testSuite ;

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
                if ( skipTest(feature,row)){
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
