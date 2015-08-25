package com.noga.njexl.testing.ui;

import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.testing.Utils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This class is the one connects to:
 * href : https://www.browserstack.com/automate/java#setting-os-and-browser
 * The idea is to generate a remote webdriver
 * Which sets the capabilities well
 * Created by noga on 25/08/15.
 */
public class BrowserStackDriver implements WebDriver {

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

    RemoteWebDriver driver;

    public static RemoteWebDriver createDriver(BrowserStackConfiguration config) throws Exception {
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

        RemoteWebDriver driver = new RemoteWebDriver(new URL(url), caps);
        return driver;
    }

    public BrowserStackDriver(String file) {
        driver = null;
        BrowserStackConfiguration configuration = null;
        // create driver here...
        String s_file = file.toLowerCase();
        try {
            if (s_file.endsWith(".xml")) {
                configuration = BrowserStackConfiguration.loadFromXml(file);
            } else if (s_file.endsWith(".json")) {
                configuration = BrowserStackConfiguration.loadFromJSON(file);
            }else{
                configuration = BrowserStackConfiguration.loadFromText(file);
            }
            driver = createDriver(configuration);

        } catch (Throwable t) {
            throw new Error("Issue Creating a BrowserStack remote driver from !", t);
        }
    }

    @Override
    public void get(String s) {
        driver.get(s);
    }

    @Override
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return driver.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return driver.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return driver.findElement(by);
    }

    @Override
    public String getPageSource() {
        return driver.getPageSource();
    }

    @Override
    public void close() {
        driver.close();
    }

    @Override
    public void quit() {
        driver.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return driver.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return driver.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return driver.switchTo();
    }

    @Override
    public Navigation navigate() {
        return driver.navigate();
    }

    @Override
    public Options manage() {
        return driver.manage();
    }
}
