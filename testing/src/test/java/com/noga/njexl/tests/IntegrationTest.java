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
import com.noga.njexl.testing.TestSuiteRunner;
import com.noga.njexl.testing.Utils;
import com.noga.njexl.testing.api.Annotations;
import com.noga.njexl.testing.api.NApiAnnotationSample;
import com.noga.njexl.testing.api.junit.JClassRunner;
import com.noga.njexl.testing.dataprovider.DataSourceTable;
import com.noga.njexl.testing.dataprovider.ProviderFactory;
import com.noga.njexl.testing.dataprovider.excel.ExcelDataSource;
import com.noga.njexl.testing.dataprovider.uri.URIDataSource;
import com.noga.njexl.testing.ocr.OCR;
import com.noga.njexl.testing.speech.SpeechRecognizer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

public class IntegrationTest {

    public static final String URL = "http://www.w3schools.com/html/html_tables.asp" ;

    public static final String SIMPLE_WEB_SUITE = "samples/webTestSuite.xml" ;

    public static final String API_WEB_SUITE = "samples/webAPITestSuite.api.xml" ;

    public static final String EXCEL_FILE = "samples/UIData.xlsx" ;

    public static final String FORMULA_SHEET = "Formula" ;

    public static final String IMAGE_FILE = "samples/sampleImage.png" ;

    public static final String TRAINING_DIR = "samples/ocr_training" ;

    public static void runSuiteAndTest(String file) throws Exception {
        TestSuiteRunner runner = com.noga.njexl.testing.Main.runner( file);
        runner.run();
        Assert.assertTrue( runner.aborts().isEmpty() );
        Assert.assertTrue( runner.errors().isEmpty() );
    }

    @BeforeClass
    public static void beforeClass(){
        Object o = ProviderFactory.dataSource("");
        Assert.assertNull(o);
        // load the class - to ensure that we have extensions loaded!
    }

    @Test
    public void testOCR() throws Exception{
        OCR.train(TRAINING_DIR);
        String text = OCR.text(IMAGE_FILE);
        System.out.println(text);
    }

    @Test
    public void simpleSuiteRun() throws Exception {
        runSuiteAndTest(SIMPLE_WEB_SUITE);
    }

    @Test
    public void apiWebSuiteRun() throws Exception {
        runSuiteAndTest(API_WEB_SUITE);
    }

    @Test
    public void excelDataSourceTest() throws Exception{
        Object o = ProviderFactory.dataSource(EXCEL_FILE);
        Assert.assertTrue(o instanceof ExcelDataSource);
        ExcelDataSource eds = (ExcelDataSource)o;
        DataSourceTable table = eds.tables.get(FORMULA_SHEET);
        String[] row0 = table.row(0);
        Assert.assertNotNull(row0[1]);
    }

    @Test
    public void URIDataSourceTest() throws Exception{
        Object o = ProviderFactory.dataSource(URL);
        Assert.assertTrue(o instanceof URIDataSource);
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


    @Test
    public void mailTest() throws Exception {
        // now just call send to send mail
        boolean sent = Utils.Mailer.send("mondal", "test mail", "This is a body", "mondal");
        Assert.assertTrue(sent);
    }

    @Test
    public void annotationTest() throws Exception {
        Class c = NApiAnnotationSample.class ;
        Assert.assertNotNull(Annotations.NApiService(c));
        Method[] methods = c.getDeclaredMethods();
        for ( Method m : methods ){
            Annotations.NApi nApi = Annotations.NApi(m);
            if ( nApi != null ){
                System.out.println("Found a nApi : " + m.getName() );
            }
        }
    }

    @Test
    public void jClassTest() throws Exception{
        JClassRunner.run( NApiAnnotationSample.class );
    }

    @Test
    public void speechTest() throws Exception{
        SpeechRecognizer speechRecognizer = new SpeechRecognizer( "en-us" );
        List<String> words = speechRecognizer.recognize();
        System.out.println(words);
    }
}