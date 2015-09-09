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

import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.testing.Utils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * This class is the one connects to:
 * href : https://www.browserstack.com/automate/java#setting-os-and-browser
 * The idea is to generate a remote webdriver
 * Which sets the capabilities well
 * Created by noga on 25/08/15.
 */
public class BrowserStackDriver extends XWebDriver {

    @XStreamAlias("BSConfig")
    public static class BrowserStackConfiguration {

        @XStreamAlias("user")
        public String user;

        @XStreamAlias("key")
        public String key;

        @XStreamAlias("browser")
        public String browser;

        @XStreamAlias("browserVersion")
        public String browserVersion;

        @XStreamAlias("os")
        public String os;

        @XStreamAlias("osVersion")
        public String osVersion;

        @XStreamAlias("platform")
        public Platform platform;

        @XStreamAlias("mobileBrowser")
        public String mobileBrowser;

        @XStreamAlias("device")
        public String device;

        @XStreamAlias("resolution")
        public String resolution;

        @XStreamAlias("debug")
        public boolean debug;


        public BrowserStackConfiguration() {
            user = "";
            key = "";
            browser = "Firefox";
            browserVersion = "";
            os = "";
            osVersion = "";
            mobileBrowser = "";
            device = "";
            resolution = "";
            debug = true;
            platform = null;
        }

        public static BrowserStackConfiguration loadFromXml(String xmlFile) throws Exception {
            XStream xStream = new XStream(new PureJavaReflectionProvider());
            xStream.alias("BSConfig", BrowserStackConfiguration.class);
            xStream.autodetectAnnotations(true);
            String xml = Utils.readToEnd(xmlFile);
            BrowserStackConfiguration configuration = (BrowserStackConfiguration) xStream.fromXML(xml);
            return configuration;
        }

        public static BrowserStackConfiguration fromMap(Map map) {
            BrowserStackConfiguration configuration = new BrowserStackConfiguration();
            if (map.containsKey("user")) {
                configuration.user = map.get("user").toString();
            }
            if (map.containsKey("key")) {
                configuration.key = map.get("key").toString();
            }
            if (map.containsKey("os")) {
                configuration.os = map.get("os").toString();
            }
            if (map.containsKey("osVersion")) {
                configuration.osVersion = map.get("osVersion").toString();
            }
            if (map.containsKey("browser")) {
                configuration.browser = map.get("browser").toString();
            }
            if (map.containsKey("browserVersion")) {
                configuration.browserVersion = map.get("browserVersion").toString();
            }
            if (map.containsKey("resolution")) {
                configuration.resolution = map.get("resolution").toString();
            }
            if (map.containsKey("mobileBrowser")) {
                configuration.mobileBrowser = map.get("mobileBrowser").toString();
            }
            if (map.containsKey("platform")) {
                configuration.platform = Enum.valueOf(Platform.class, map.get("platform").toString());
            }
            if (map.containsKey("device")) {
                configuration.device = map.get("device").toString();
            }
            if (map.containsKey("debug")) {
                configuration.debug = TypeUtility.castBoolean(map.get("debug"), true);
            }

            return configuration;
        }

        public static BrowserStackConfiguration loadFromJSON(String jsonFile) throws Exception {
            Map map = (Map) TypeUtility.json(jsonFile);
            BrowserStackConfiguration configuration = fromMap(map);
            return configuration;
        }

        public static BrowserStackConfiguration loadFromText(String propertyFile) throws Exception {
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertyFile));
            BrowserStackConfiguration configuration = fromMap(properties);
            return configuration;
        }
    }

    public static BrowserStackDriver createDriver(String file){
        BrowserStackConfiguration config ;
        // create driver here...
        String s_file = file.toLowerCase();
        try {
            if (s_file.endsWith(".xml")) {
                config = BrowserStackConfiguration.loadFromXml(file);
            } else if (s_file.endsWith(".json")) {
                config = BrowserStackConfiguration.loadFromJSON(file);
            }else{
                config = BrowserStackConfiguration.loadFromText(file);
            }

        } catch (Throwable t) {
            throw new Error("Issue Creating a BrowserStack remote driver from !", t);
        }

        String url = String.format("http://%s:%s@hub.browserstack.com/wd/hub",
                config.user, config.key);

        DesiredCapabilities caps = new DesiredCapabilities();
        if (!config.resolution.isEmpty()) {
            caps.setCapability("resolution", config.resolution);
        }
        //setup JS
        // caps.setJavascriptEnabled(true);
        // force it
        if (config.platform == null) {
            // desktop
            caps.setCapability("browser", config.browser);
            caps.setCapability("browser_version", config.browserVersion);
            caps.setCapability("os", config.os);
            caps.setCapability("os_version", config.osVersion);
            caps.setCapability("browserstack.debug", config.debug);

        } else {
            caps.setPlatform(config.platform);
            caps.setCapability("browserName", config.mobileBrowser);
            caps.setCapability("device", config.device);
        }
        try {
            RemoteWebDriver driver = new RemoteWebDriver(new URL(url), caps);
            return new BrowserStackDriver(driver);
        }catch (Exception e){
            throw new Error(e);
        }
    }

    public BrowserStackDriver(WebDriver driver) {
        super(driver);
    }
}
