/**
 * Copyright 2015 Nabarun Mondal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.noga.njexl.testing.ui;

import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.testing.Utils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This is a driver for running Mobile app tests
 * Using Appium frameworks
 * Created by noga on 08/09/15.
 *
 */
public class MobileDriver extends XWebDriver {

    @XStreamAlias("IOSConfig")
    public static final class IOSConfig{

        @XStreamAlias("bundleId")
        public String bundleId ;

        public IOSConfig(){
            bundleId = "" ;
        }

    }

    @XStreamAlias("MobileConfig")
    public static final class MobileConfiguration {

        @XStreamAlias("url")
        public String url;

        @XStreamAlias("automationName")
        public String automationName;

        @XStreamAlias("platformName")
        public String platformName;

        @XStreamAlias("platformVersion")
        public String platformVersion;

        @XStreamAlias("deviceName")
        public String deviceName;

        @XStreamAlias("app")
        public String app;

        @XStreamAlias("browserName")
        public String browserName;

        @XStreamAlias("noReset")
        public boolean noReset;

        @XStreamAlias("fullReset")
        public boolean fullReset ;

        public URL urlObject;

        public Capabilities capabilities;

        @XStreamAlias("iOSConfig")
        public IOSConfig iOSConfig;

        public MobileConfiguration() {
            url = "http://127.0.0.1:4723/wd/hub" ;
            deviceName = "";
            platformVersion = "";
            app = "" ;
            automationName = "" ;
            platformName = "" ;
            platformVersion = "" ;
            browserName = "" ;
            noReset = false ;
            fullReset = false ;
            iOSConfig = null;
        }

        public static MobileConfiguration loadFromXml(String xmlFile) throws Exception {
            XStream xStream = new XStream(new PureJavaReflectionProvider());
            xStream.alias("MobileConfig", MobileConfiguration.class);
            xStream.autodetectAnnotations(true);
            String xml = Utils.readToEnd(xmlFile);
            MobileConfiguration configuration = (MobileConfiguration) xStream.fromXML(xml);
            configuration.urlObject = new URL(configuration.url);
            return configuration;
        }

        public static MobileConfiguration fromMap(Map map) {
            MobileConfiguration configuration = new MobileConfiguration();
            if ( map.containsKey("url")) {
                configuration.url = (String)map.get("url");
            }
            configuration.app = (String)map.get("app");
            configuration.platformVersion = (String)map.get("platformVersion");

            return configuration;
        }

        public static MobileConfiguration loadFromJSON(String jsonFile) throws Exception {
            Map map = (Map) TypeUtility.json(jsonFile);
            MobileConfiguration configuration = fromMap(map);
            return configuration;
        }

        public static MobileConfiguration loadFromText(String propertyFile) throws Exception {
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertyFile));
            MobileConfiguration configuration = fromMap(properties);
            return configuration;
        }
    }

    public static MobileDriver createDriver(String file) {
        MobileConfiguration configuration ;
        // create driver here...
        String s_file = file.toLowerCase();
        try {
            if (s_file.endsWith(".xml")) {
                configuration = MobileConfiguration.loadFromXml(file);
            } else if (s_file.endsWith(".json")) {
                configuration = MobileConfiguration.loadFromJSON(file);
            }else{
                configuration = MobileConfiguration.loadFromText(file);
            }

        } catch (Throwable t) {
            throw new Error("Issue Creating a BrowserStack remote driver from !", t);
        }
        DesiredCapabilities caps = new DesiredCapabilities();

        // desktop
        caps.setCapability("deviceName", configuration.deviceName);
        caps.setCapability("browserName", configuration.browserName );
        caps.setCapability("platformName", configuration.platformName);
        caps.setCapability("platformVersion", configuration.platformVersion);
        caps.setCapability("app", configuration.app);

        configuration.capabilities = caps ;
        AppiumDriver driver;
        if ( configuration.iOSConfig == null ){
            //android
            driver = new AndroidDriver<>(configuration.urlObject, caps);
        }else{
            caps.setCapability("bundleId", configuration.iOSConfig.bundleId );
            // ios
            driver = new IOSDriver<>(configuration.urlObject, caps);
        }
        return new MobileDriver(driver);
    }

    public MobileDriver(AppiumDriver driver) {
        super(driver);
    }
}
