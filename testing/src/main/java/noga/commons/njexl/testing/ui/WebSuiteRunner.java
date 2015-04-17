package noga.commons.njexl.testing.ui;

import noga.commons.njexl.JexlContext;
import noga.commons.njexl.JexlEngine;
import noga.commons.njexl.Script;
import noga.commons.njexl.testing.TestAssert;
import noga.commons.njexl.testing.TestAssert.*;
import noga.commons.njexl.testing.TestSuite;
import noga.commons.njexl.testing.TestSuiteRunner;
import noga.commons.njexl.testing.dataprovider.DataSource;
import noga.commons.njexl.testing.dataprovider.DataSourceTable;
import noga.commons.njexl.testing.dataprovider.ProviderFactory;

import java.util.HashMap;

/**
 * Created by noga on 17/04/15.
 */
public class WebSuiteRunner extends TestSuiteRunner {

    public final WebTestSuite webTestSuite ;

    public XSelenium xSelenium ;

    Script script ;

    TestAssert testAssert;

    JexlContext jexlContext ;

    HashMap<String,Object> functions;

    public final HashMap<String,DataSource> dataSources;

    protected JexlContext getContext(){
        JexlContext context = noga.commons.njexl.Main.getContext();
        context.set(Script.ARGS, new Object[]{});
        context.set(XSelenium.SELENIUM_VAR, xSelenium);
        context.set(Script.ARGS, new Object[]{});
        context.set(TestAssert.ASSERT_VAR, testAssert);
        return context;
    }

    protected HashMap<String,Object> getFunctions(){
        HashMap<String,Object> functions = noga.commons.njexl.Main.getFunction(jexlContext);
        functions.put(XSelenium.SELENIUM_NS, xSelenium);
        functions.put(TestAssert.ASSERT_NS, testAssert);
        return functions ;
    }


    public WebSuiteRunner(String file) throws Exception {
        webTestSuite = WebTestSuite.loadFrom(file);
        dataSources = new HashMap<>();
        for (TestSuite.DataSource ds : webTestSuite.dataSources ){
            DataSource dataSource = ProviderFactory.dataSource(ds.location);
            if ( dataSource == null ){
                throw new Exception("Can not create data source!");
            }
            dataSources.put( ds.name, dataSource );
        }
    }

    @Override
    protected void prepare() throws Exception {
        xSelenium = XSelenium.selenium(webTestSuite.webApp.url, webTestSuite.browserType.toString());
        testAssert = new TestAssert();
        testAssert.eventListeners.add(xSelenium);
    }

    @Override
    protected TestSuite.Application application() {
        return webTestSuite.webApp ;
    }

    @Override
    protected DataSourceTable dataSourceTable(TestSuite.BaseFeature feature) {
        DataSource source = dataSources.get(feature.ds) ;
        if ( source == null ){
            System.err.printf("No Such data source : [%s]\n",feature.ds);
            return null;
        }
        DataSourceTable table = source.tables.get(feature.table);
        if ( table == null ){
            System.err.printf("No Such data table in Data Source : [%s] [%s]\n",feature.table, feature.ds);
        }
        return table;
    }

    @Override
    protected void afterFeature(TestSuite.BaseFeature feature) throws Exception {
        jexlContext = null;
        functions.clear();
        functions = null;
        script = null;
    }

    @Override
    protected void beforeFeature(TestSuite.BaseFeature feature) throws Exception {
        xSelenium.screenShotDir( webTestSuite.webApp.logs + "/" + feature.name  );
        jexlContext = getContext();
        functions = getFunctions();
        JexlEngine engine = new JexlEngine();
        engine.setFunctions(functions);
        String scriptLocation = webTestSuite.webApp.scriptDir + "/" + feature.script ;
        script = engine.importScript(scriptLocation);
    }

    @Override
    protected TestRunEventType runTest(DataSourceTable table, int row) throws Exception {
        testAssert.setError(false);
        String[] columns = table.row(0);
        String[] values = table.row(row);
        JexlContext local = jexlContext.copy();
        // put into context
        for ( int i = 0 ; i < columns.length;i++ ){
            local.set(columns[i],values[i]);
        }
        script.execute(local);
        if (testAssert.hasError()){
            return TestRunEventType.ERROR_TEST ;
        }
        return TestRunEventType.OK_TEST ;
    }

    @Override
    protected void shutdown() throws Exception {
        xSelenium.close();
        xSelenium = null;
        testAssert.eventListeners.clear();
        testAssert = null;
    }
}
