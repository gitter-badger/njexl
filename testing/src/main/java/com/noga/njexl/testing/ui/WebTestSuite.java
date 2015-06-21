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

package com.noga.njexl.testing.ui;

import com.noga.njexl.testing.TestSuite;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.Map;

/**
 * Created by noga on 15/04/15.
 */

@XStreamAlias("testSuite")
public class WebTestSuite extends TestSuite {

    @XStreamAsAttribute
    public XSelenium.BrowserType browserType;

    @XStreamAlias("webApp")
    public static class WebApplication extends Application{

        @XStreamAsAttribute
        public String url;

        @XStreamAsAttribute
        public String method;

        public WebApplication(){
            url = "" ;
            method = "" ;
        }
    }

    public WebApplication webApp;

    public WebTestSuite(){
        browserType = XSelenium.BrowserType.FIREFOX ;
        webApp = new WebApplication();
    }

    public static WebTestSuite loadFrom(String file, Map<String,String> variables) throws Exception{
        return loadFrom(WebTestSuite.class , file, variables);
    }

}
