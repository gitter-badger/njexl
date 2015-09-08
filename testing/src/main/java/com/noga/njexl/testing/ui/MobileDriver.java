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
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * This is a driver for running Mobile app tests
 * Using Appium frameworks
 * Created by noga on 08/09/15.
 *
 */
public class MobileDriver extends AppiumDriver {

    @XStreamAlias("MobileConfig")
    public static class MobileConfiguration {

        @XStreamAlias("url")
        public String url;

        @XStreamAlias("deviceName")
        public String device;

        @XStreamAlias("platformVersion")
        public String platformVersion;

        @XStreamAlias("app")
        public String app;

        @XStreamAlias("appPackage")
        public String appPackage;

        @XStreamAlias("appActivity")
        public String appActivity;

        public URL urlObject;

        public Capabilities capabilities;

        public MobileConfiguration() {
            url = "http://127.0.0.1:4723/wd/hub" ;
            device = "";
            platformVersion = "";
            app = "" ;
            appPackage = "" ;
            appActivity = "" ;
        }

        public static MobileConfiguration loadFromXml(String xmlFile) throws Exception {
            XStream xStream = new XStream(new PureJavaReflectionProvider());
            xStream.alias("BSConfig", MobileConfiguration.class);
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
            configuration.appPackage = (String)map.get("appPackage");
            configuration.appActivity = (String)map.get("appActivity");

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

    public final AppiumDriver driver;

    public static boolean isAndroid(String device){
        return  ( device.toLowerCase().contains("android") );
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
        caps.setCapability("deviceName", configuration.device);
        caps.setCapability("platformVersion", configuration.platformVersion);
        caps.setCapability("app", configuration.app);
        caps.setCapability("appPackage", configuration.appPackage);
        caps.setCapability("appActivity", configuration.appActivity);
        configuration.capabilities = caps ;
        AppiumDriver driver;
        if ( isAndroid( configuration.device  ) ){
            //android
            driver = new AndroidDriver<>(configuration.urlObject, caps);
        }else{
            // ios
            driver = new IOSDriver<>(configuration.urlObject, caps);
        }

        return new MobileDriver( configuration.urlObject, configuration.capabilities, driver );
    }

    public MobileDriver(URL remoteAddress, Capabilities desiredCapabilities, AppiumDriver driver) {
        super(remoteAddress, desiredCapabilities);
        this.driver = driver;
    }

    @Override
    public WebElement scrollTo(String s) {
        return driver.scrollTo(s);
    }

    @Override
    public WebElement scrollToExact(String s) {
        return driver.scrollToExact(s);
    }
}
