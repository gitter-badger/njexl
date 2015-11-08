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

import com.noga.njexl.spark.Main;
import org.junit.Test;
import static org.junit.Assert.*;

public class IntegrationTest {

    public static final String WC_SCRIPT = "samples/wc.jxl" ;

    public static final String PI_SCRIPT = "samples/pi.jxl" ;

    @Test
    public void wcTest() throws Exception{
        Main.executeScript(WC_SCRIPT);
    }

    @Test
    public void piTest() throws Exception{
        Object o = Main.executeScript(PI_SCRIPT);
        assertTrue((Boolean)o);
    }
}