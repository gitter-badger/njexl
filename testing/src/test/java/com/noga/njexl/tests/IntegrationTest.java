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

package com.noga.njexl.tests;

import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.testing.dataprovider.ProviderFactory;
import com.noga.njexl.testing.dataprovider.excel.ExcelDataSource;
import com.noga.njexl.testing.dataprovider.uri.URIDataSource;
import com.noga.njexl.testing.ocr.OCR;
import com.noga.njexl.testing.ui.WebTestSuite;
import com.noga.njexl.testing.ui.XRobot;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class IntegrationTest {

    public static final String URL = "http://www.w3schools.com/html/html_tables.asp" ;

    public static final String SUITE = "samples/webTestSuite.xml" ;

    public static final String EXCEL_FILE = "samples/UIData.xlsx" ;

    public static final String IMAGE_FILE = "samples/sampleImage.png" ;

    public static final String TRAINING_DIR = "samples/ocr_training" ;


    @BeforeClass
    public static void beforeClass(){
        Object o = ProviderFactory.dataSource("");
        Assert.assertNull(o);
        // load the class - to ensure that we have extensions loaded!
    }

    @Test
    public void testOCR() throws Exception{
        OCR.train(TRAINING_DIR);
        String text = OCR.text( IMAGE_FILE);
        System.out.println(text);
    }

    //@Test
    public void suiteLoading() throws Exception {
        WebTestSuite webTestSuite = WebTestSuite.loadFrom(SUITE);
        Assert.assertNotNull(webTestSuite);
    }

    @Test
    public void excelDataSourceTest() throws Exception{
        Object o = ProviderFactory.dataSource(EXCEL_FILE);
        Assert.assertTrue(o instanceof ExcelDataSource);
    }

    @Test
    public void URIDataSourceTest() throws Exception{
        Object o = ProviderFactory.dataSource(URL);
        Assert.assertTrue( o instanceof URIDataSource);
    }

    @Test
    public void excelDataLoading() throws Exception {
        // now just call data matrix
        DataMatrix matrix = DataMatrix.loc2matrix(EXCEL_FILE, "Data");
        Assert.assertNotNull(matrix);
    }

    @Test
    public void URIDataLoading() throws Exception {
        // now just call data matrix
        DataMatrix matrix = DataMatrix.loc2matrix(URL,"0");
        Assert.assertNotNull(matrix);
    }
}