package com.noga.njexl.tests;

import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.testing.dataprovider.excel.ExcelDataSource;
import com.noga.njexl.testing.ui.WebTestSuite;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class BuildIntegrationTest{

    @BeforeClass
    public static void beforeClass(){
        //load the class, so that we have it registered with data matrix
        Object o = ExcelDataSource.ExcelDataLoader.excelDataLoader ;
        o = null; // do not bother, it is pointless
    }

    @Test
    public void suiteLoading() throws Exception {
        WebTestSuite webTestSuite = WebTestSuite.loadFrom("samples/webTestSuite.xml");
        Assert.assertNotNull(webTestSuite);
    }

    @Test
    public void excelDataLoading() throws Exception {
        // now just call data matrix
        DataMatrix matrix = DataMatrix.file2matrix("samples/UIData.xlsx","Data");
        Assert.assertNotNull(matrix);
    }

}