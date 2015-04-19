package com.noga.njexl.tests;

import com.noga.njexl.testing.ui.WebTestSuite;
import org.junit.Assert;
import org.junit.Test;

public class BuildIntegrationTest{

    @Test
    public void suiteLoading() throws Exception {
        WebTestSuite webTestSuite = WebTestSuite.loadFrom("samples/webTestSuite.xml");
        Assert.assertNotNull(webTestSuite);
    }
}