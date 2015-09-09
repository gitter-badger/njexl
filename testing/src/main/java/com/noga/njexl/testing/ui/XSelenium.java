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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.noga.njexl.lang.Interpreter;
import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.testing.TestAssert;
import com.noga.njexl.testing.Utils;
import com.noga.njexl.testing.dataprovider.DataSource;
import com.noga.njexl.testing.dataprovider.ProviderFactory;
import com.thoughtworks.selenium.Selenium;
import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour.Eventing;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A Selenium RC style guy - using raw webdriver component,
 * No fancy stuff at all.
 * Final goal is to remove @{Selenium} and use XSelenium as the protocol
 * Created by noga on 24/02/15.
 */
public class XSelenium  implements Selenium, Eventing , TestAssert.AssertionEventListener {

    public static final String SELENIUM_VAR = "selenium" ;

    public static final String SELENIUM_NS = "sel" ;

    public static final String BASE_URL = "__URL__" ;

    public static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    public static final boolean IS_MAC = OS_NAME.startsWith("mac ");

    public static final boolean IS_WIN = OS_NAME.startsWith("win");

    public static final String CHROME_DRIVER_PATH = "CHROME_DRIVER" ;

    public static final String OPERA_DRIVER_PATH = "OPERA_DRIVER" ;


    public static final int BLINK_TIMES = 3 ;

    public static final String BLINK_WIDTH = "3" ;

    public static final int BLINK_DELAY = 1000 ;

    public static final String BLINK_COLOR = "red" ;

    protected int typeDelay = 580 ;

    protected String baseUrl;

    public void typeDelay( int delay){
        if ( delay > 100 ) {
            typeDelay = delay;
        }
    }

    public int typeDelay( ){
        return typeDelay ;
    }

    /**
     * Do we want to clear field before typing?
     * Probably YES.
     */
    boolean __CLEAR_FIELD__ = true;

    public static final String[] xpathListButtons = {"//button", "//input[@type=\'submit\']",
            "//input[@type=\'button\']"};
    public static final String[] xpathListFields = {"//input[@type=\'text\']", "//input[@type=\'radio\']",
            "//input[@type=\'password\']", "//input[@type=\'submit\']", "//input[@type=\'button\']",
            "//input[@type=\'color\']", "//input[@type=\'date\']", "//input[@type=\'datetime\']",
            "//input[@type=\'datetime-local\']", "//input[@type=\'checkbox\']", "//input[@type=\'email\']",
            "//input[@type=\'hidden\']",
            "//input[@type=\'image\']", "//input[@type=\'email\']", "//input[@type=\'month\']",
            "//input[@type=\'number\']", "//input[@type=\'range\']", "//input[@type=\'search\']",
            "//input[@type=\'tel\']", "//input[@type=\'time\']", "//input[@type=\'url\']", "//input[@type=\'week\']",
            "//input[@type=\'file\']", "//input[@type=\'reset\']"};

    public static final String[] xpathListLinks = {"//a"};

    String screenShotDir = System.getProperty("user.dir") ;

    /**
     * Gets the directory of screen shots
     * @return the screen shot dir
     */
    public String screenShotDir(){
        return screenShotDir ;
    }

    /**
     * Sets the directory of screen shots
     * @param dir the directory of the screen shot
     * @return true if it was successful, false if not
     */
    public boolean screenShotDir(String dir){
        File file = new File(dir);
        if ( file.isDirectory() ) {
            screenShotDir = dir;
            return true ;
        }
        return false ;
    }

    String getScreenShotFile(){
        String tsFormatted = TypeUtility.castString( new Date(), "yyyy-MMM-dd-hh-mm-ss-ms" );
        String file = screenShotDir + "/" + tsFormatted + ".png" ;
        return file ;
    }

    @Override
    public void onAssertion(TestAssert.AssertionEvent assertionEvent) {
        boolean screenShot = ((TestAssert)assertionEvent.getSource()).hasError();
        if ( screenShot ){
            captureScreenshot(getScreenShotFile());
        }
    }

    int timeout = 30000; // 30 sec should be good

    /**
     * What is the time out for the wait operations
     * @return the time out in ms
     */
    public int timeout(){
        return timeout ;
    }

    /**
     * Sets the timeout, must be greater than 0
     * @param ms the time out in ms greater than  0
     */
    public void timeout(int ms){
        if ( ms > 0 ) {
            timeout = ms;
        }
    }

    int poll =  100 ; // 100 msec is fine

    /**
     * What is the polling time for the wait operations
     * @return the polling time in ms
     */
    public int poll() {
        return poll;
    }

    /**
     * Sets the polling time for the wait operations  must be greater than 0
     * @param poll the poll time in ms greater than  0
     */
    public void poll(int poll) {
        if ( poll >  0 ) {
            this.poll = poll;
        }
    }

    protected void waitForAppear(String locator){
        long start = System.currentTimeMillis() ;
        long now = start ;
        while( true ){
            boolean in = isElementPresent(locator) && isVisible(locator);
            if ( in ){
                System.out.printf("@@[%s]%d\n", locator,now-start);
                break;
            }
            now = System.currentTimeMillis();
            if ( now - start > timeout ){
                String message = String.format("Element (%s) is still absent!" , locator );
                throw new Error(message);
            }
            try{
                Thread.sleep(poll);
            }catch (Exception e){}
        }
    }

    protected void waitForDisappear(String locator){
        long start = System.currentTimeMillis() ;
        long now = start ;
        while( true ){
            boolean out = !isElementPresent(locator) || !isVisible(locator);
            if ( out ){
                System.out.printf("@@[%s]%d\n", locator,now-start);
                break;
            }
            now = System.currentTimeMillis();
            if ( now - start > timeout ){
                String message = String.format("Element (%s) is still present!" , locator );
                throw new Error(message);
            }
            try{
                Thread.sleep(poll);
            }catch (Exception e){}
        }
    }

    public static final String APPEAR = "@@" ;

    public static final String DISAPPEAR = "$$" ;

    int delay = 1000 ;

    public void setDelay(int ms){
        if ( ms > 10 ){
            delay = ms ;
        }
    }

    public void delay(int ms){
        try{
            Thread.sleep(ms);
        }catch (Exception e){
        }
    }

    @Override
    public void after(Event event) {
        if ( DISAPPEAR.equals(event.pattern) && event.args.length > 0 ){
            waitForDisappear(event.args[0].toString());
        }else{
            delay(delay);
        }
    }

    @Override
    public void before(Event event) {
        if ( APPEAR.equals(event.pattern) && event.args.length > 0 ){
            waitForAppear(event.args[0].toString());
        }else{
            delay(delay);
        }
    }

    /**
     * The Types of Browser we support, as of now
     */
    public enum BrowserType{
        FIREFOX,
        HTML_UNIT,
        IE,
        CHROME,
        OPERA,
        SAFARI
    }

    /**
     * Gets the XSelenium with
     * @param type the type of the browser
     * @return an @{WebDriver} object
     */
    public static WebDriver  local(BrowserType type){
        WebDriver driver = null;
        ChromeDriverService service = null;
        switch (type){
            case HTML_UNIT:
                //set javascript support : with chrome mode on
                driver = new HtmlUnitDriver(BrowserVersion.CHROME);
                ((HtmlUnitDriver)driver).setJavascriptEnabled(true);
                break;
            case CHROME:
                String chromeDriverBin = System.getenv(CHROME_DRIVER_PATH);
                try {
                    service = new ChromeDriverService.Builder()
                            .usingDriverExecutable(new File(chromeDriverBin))
                            .usingAnyFreePort()
                            .build();
                    service.start();
                }catch (Exception e){
                    System.err.println(e);
                    return null;
                }
                driver = new ChromeDriver(service,DesiredCapabilities.chrome());
                break;
            case OPERA:
                String operaDriverBin = System.getenv(OPERA_DRIVER_PATH);
                try {
                    service = new ChromeDriverService.Builder()
                            .usingDriverExecutable(new File(operaDriverBin))
                            .usingAnyFreePort()
                            .build();
                    service.start();
                }catch (Exception e){
                    System.err.println(e);
                    return null;
                }
                driver = new ChromeDriver(service, DesiredCapabilities.opera());
                break;

            case SAFARI:
                driver = new SafariDriver();
                break;
            case IE:
                driver = new InternetExplorerDriver();
                break;
            case FIREFOX:
            default:
                driver = new FirefoxDriver();
                break;
        }
        return driver ;
    }


    public static final Pattern MOBILE_LOCAL_DRIVER =
            Pattern.compile(".*\\.[mM][oO][bB]([iI][lL][eE])?\\..*",
                    Pattern.DOTALL|Pattern.CASE_INSENSITIVE);

    /**
     * Gets the remote XSelenium with
     * @param config the configuration file location of :
     *               "*.mob(ile)?.*" : use Appium configuration
     *               "else" : use browserstack configuration
     * @return an @{WebDriver} object
     */
    public static WebDriver remote(String config){
        WebDriver driver;
        Matcher m = MOBILE_LOCAL_DRIVER.matcher( config ) ;
        if ( m.matches() ){
            driver = MobileDriver.createDriver(config);
            return driver ;
        }
        driver = BrowserStackDriver.createDriver(config);
        return driver ;
    }

    /**
     * Gets the XSelenium with
     * @param baseUrl the base url
     * @param browserType the type of the browser
     * @return an @{XSelenium} object
     */
    public static XSelenium  selenium(String baseUrl, BrowserType browserType) {
        WebDriver driver = local(browserType);
        return new XSelenium(driver,baseUrl);
    }

    /**
     * Gets the XSelenium with
     * @param baseUrl the base url
     * @param configFile the remote config file for the browser
     * @return an @{XSelenium} object
     */
    public static XSelenium  selenium(String baseUrl, String configFile) {
        WebDriver driver = remote(configFile);
        return new XSelenium(driver,baseUrl);
    }

    public XSelenium(WebDriver driver, String baseUrl) {
        this.driver = driver ;
        this.baseUrl = baseUrl ;
    }

    public static By getByFromLocator(String locator) {
        if (locator.startsWith("/")) {
            // This is xpath
            return (By.xpath(locator));
        }
        if (locator.startsWith("xpath=")) {
            String xpath = locator.substring(6);
            return (By.xpath(xpath));
        }
        if (locator.startsWith("class=")) {
            String cl = locator.substring(6);
            return (By.className(cl));
        }
        if (locator.startsWith("id=")) {
            String id = locator.substring(3);
            return (By.id(id));
        }
        if (locator.startsWith("link=")) {
            String linkText = locator.substring(5);
            return (By.partialLinkText(linkText));
        }
        if (locator.startsWith("name=")) {
            String name = locator.substring(5);
            return (By.name(name));
        }
        if (locator.startsWith("css=")) {
            String css = locator.substring(4);
            return (By.cssSelector(css));
        }
        if (locator.startsWith("tag=")) {
            String tag = locator.substring(4);
            return (By.tagName(tag));
        }
        // when nothing matches -take as id.
        return (By.id(locator));
    }

    public final WebDriver driver;

    public String getXPath(WebElement webElement) {
        String jscript = "function getPathTo(node) {" +
                "  var stack = [];" +
                "  while(node.parentNode !== null) {" +
                "    stack.unshift(node.tagName);" +
                "    node = node.parentNode;" +
                "  }" +
                "  return stack.join('/');" +
                "}" +
                "return getPathTo(arguments[0]);";
        return (String) ((JavascriptExecutor) driver).executeScript(jscript, webElement);
    }

    Select getSelect(String locator) {
        By by = getByFromLocator(locator);
        WebElement elem = driver.findElement(by);
        Select select = new Select(elem);
        return select;
    }

    public static class SelectOption {
        public enum Using {
            id,
            label,
            value,
            index
        }

        public Using using;
        public String item;

        public SelectOption(Using u, String item) {
            using = u;
            this.item = item;
        }

    }

    SelectOption getSelectOption(String optionLocator) {
        String item;
        if (optionLocator.startsWith("value=")) {
            item = optionLocator.substring("value=".length());
            return new SelectOption(SelectOption.Using.value, item);

        } else if (optionLocator.startsWith("id=")) {
            item = optionLocator.substring("id=".length());
            return new SelectOption(SelectOption.Using.id, item);


        } else if (optionLocator.startsWith("index=")) {
            item = optionLocator.substring("index=".length());
            return new SelectOption(SelectOption.Using.index, item);

        }
        item = optionLocator;
        if (optionLocator.startsWith("label=")) {
            item = optionLocator.substring("label=".length());
        }
        return new SelectOption(SelectOption.Using.label, item);
    }

    public void selectItem(Select select, String optionLocator) {
        SelectOption option = getSelectOption(optionLocator);
        switch (option.using) {
            case id:
                WebElement element = driver.findElement(getByFromLocator(optionLocator));
                if (!element.isSelected()) {
                    element.click();
                }
                break;
            case value:
                select.selectByValue(option.item);
                break;
            case index:
                select.selectByIndex(Integer.parseInt(option.item));
                break;
            case label:
                select.selectByVisibleText(option.item);
                break;
            default:
                break;
        }
    }

    public void deSelectItem(Select select, String optionLocator) {
        SelectOption option = getSelectOption(optionLocator);
        switch (option.using) {
            case id:
                WebElement element = driver.findElement(getByFromLocator(optionLocator));
                if (element.isSelected()) {
                    element.click();
                }
                break;
            case value:
                select.deselectByValue(option.item);
                break;
            case index:
                select.deselectByIndex(Integer.parseInt(option.item));
                break;
            case label:
                select.deselectByVisibleText(option.item);
                break;
            default:
                break;
        }
    }

    public WebElement element(String locator){
        By by = getByFromLocator(locator);
        return driver.findElement(by);
    }

    public List<WebElement> elements(String locator){
        By by = getByFromLocator(locator);
        return driver.findElements(by);
    }

    @Override
    public void click(String locator) {
        By by = getByFromLocator(locator);
        driver.findElement(by).click();
    }

    @Override
    public void doubleClick(String locator) {
        By by = getByFromLocator(locator);
        Actions action = new Actions(driver);
        action.moveToElement(driver.findElement(by)).doubleClick().build().perform();

    }


    @Override
    public void focus(String locator) {
        By by = getByFromLocator(locator);
        new Actions(driver).moveToElement(driver.findElement(by)).perform();
    }

    public String replaceSpecialCharacters(String value) {
        value = value.replaceAll("\\(", Keys.chord(Keys.SHIFT, "9"));
        value = value.replaceAll("&", Keys.chord(Keys.SHIFT, "7"));
        return value;
    }

    @Override
    public void type(String locator, String value) {
        By by = getByFromLocator(locator);
        WebElement element = driver.findElement(by);
        if (__CLEAR_FIELD__) {
            element.clear();
        }
        value = replaceSpecialCharacters(value);
        element.sendKeys(value);
    }

    @Override
    public void typeKeys(String locator, String value) {
        By by = getByFromLocator(locator);
        WebElement element = driver.findElement(by);
        value = replaceSpecialCharacters(value);
        for ( int i = 0 ; i < value.length();i++){
            element.sendKeys(Character.toString( value.charAt(i)));
            try{
                Thread.sleep(typeDelay);
            }catch (Exception e){
            }
        }
    }


    @Override
    public void check(String locator) {
        By by = getByFromLocator(locator);
        WebElement element = driver.findElement(by);
        if (!element.isSelected()) {
            element.click();
        }
    }

    @Override
    public void uncheck(String locator) {
        By by = getByFromLocator(locator);
        WebElement element = driver.findElement(by);
        if (element.isSelected()) {
            element.click();
        }
    }

    @Override
    public void select(String selectLocator, String optionLocator) {
        Select select = getSelect(selectLocator);
        if (select.isMultiple()) {
            select.deselectAll();
        }
        selectItem(select, optionLocator);
    }

    @Override
    public void addSelection(String locator, String optionLocator) {
        Select select = getSelect(locator);
        selectItem(select, optionLocator);
    }

    @Override
    public void removeSelection(String locator, String optionLocator) {
        Select select = getSelect(locator);
        deSelectItem(select, optionLocator);
    }

    @Override
    public void removeAllSelections(String locator) {
        Select select = getSelect(locator);
        select.deselectAll();
    }

    @Override
    public void submit(String formLocator) {
        By by = getByFromLocator(formLocator);
        driver.findElement(by).submit();
    }

    @Override
    public void mouseMove(String locator) {
        WebElement el = element(locator);
        Actions builder = new Actions(driver);
        builder.moveToElement(el).perform();
    }

    @Override
    public void deleteCookie(String name, String optionsString) {
        driver.manage().deleteCookieNamed(name);
    }

    @Override
    public String getCookie() {
        Set<Cookie> cookies = driver.manage().getCookies();
        StringBuffer buf = new StringBuffer();
        for ( Cookie c : cookies ){
            buf.append(c.toString()).append(";;;");
        }
        return buf.toString();
    }

    @Override
    public String getCookieByName(String name) {
       return driver.manage().getCookieNamed(name).toString();
    }

    @Override
    public void attachFile(String fieldLocator, String fileLocator) {
        type(fieldLocator, fileLocator);
    }

    @Override
    public void contextMenu(String locator) {
        Actions action= new Actions(driver);
        WebElement el = element(locator);
        action.contextClick(el).build().perform();
    }

    @Override
    public void dragAndDrop(String locator, String movementsString) {
        dragdrop(locator, movementsString);
    }

    @Override
    public void dragAndDropToObject(String locatorOfObjectToBeDragged, String locatorOfDragDestinationObject) {
        Actions actions = new Actions(driver);
        WebElement someElement = element(locatorOfObjectToBeDragged);
        WebElement otherElement = element(locatorOfDragDestinationObject);
        actions.dragAndDrop(someElement, otherElement).perform();
    }

    @Override
    public void dragdrop(String locator, String movementsString) {
        String[] cords = movementsString.split(",");
        Actions actions = new Actions(driver);
        int X = TypeUtility.castInteger(cords[0],0);
        int Y = TypeUtility.castInteger(cords[1], 0);
        WebElement someElement = element(locator);
        actions.dragAndDropBy(someElement, X, Y).perform();
    }

    @Override
    public void mouseOver(String locator) {
        Actions actions = new Actions(driver);
        WebElement someElement = element(locator);
        actions.moveToElement(someElement).perform();
    }

    @Override
    public void goBack() {
        driver.navigate().back();
    }

    @Override
    public void refresh() {
        driver.navigate().refresh();
    }

    @Override
    public void close() {
        driver.quit();
    }

    @Override
    public boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        }   // try
        catch (NoAlertPresentException ex) {
            return false;
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public boolean isPromptPresent() {
        return isAlertPresent();
    }

    @Override
    public boolean isConfirmationPresent() {
        return isAlertPresent();
    }

    @Override
    public String getAlert() {
        try {
            return driver.switchTo().alert().getText();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Accepts the alert
     * @return true, if there was an alert to accept,
     *         false otherwise
     */
    public boolean acceptAlert(){
        if ( isAlertPresent() ){
            driver.switchTo().alert().accept();
            return true;
        }
        return false ;
    }

    /**
     * Dismisses an alert
     * @return true, if there was an alert to dismiss,
     *         false otherwise
     */
    public boolean dismissAlert(){
        if ( isAlertPresent() ){
            driver.switchTo().alert().dismiss();
            return true;
        }
        return false ;
    }

    /**
     * Accepts or Dismiss the alert
     * @param accept true to accept, false to dismiss if possible
     * @return true, if there was an alert to act on,
     *         false otherwise
     */
    public boolean alert(boolean accept){
        if ( accept ){ return  acceptAlert(); }
        return dismissAlert();
    }

    @Override
    public String getConfirmation() {
        return getAlert();
    }

    @Override
    public String getPrompt() {
        return getAlert();
    }

    @Override
    public String getLocation() {
        return driver.getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return driver.getTitle();
    }

    @Override
    public String getBodyText() {
        String source = driver.getPageSource();
        String textOnly = Jsoup.parse(source).text();
        return textOnly;
    }

    @Override
    public String getValue(String locator) {
        By by = getByFromLocator(locator);
        WebElement element = driver.findElement(by);
        switch (element.getTagName()) {

            case "select" :
                return getSelectedValue(locator);
            case "input" :
                String type = element.getAttribute("type");
                if ( "checkbox".equals(type)){
                    return Boolean.toString(element.isSelected());
                }
                return element.getAttribute("value") ;
            default:
                return element.getText();
        }
    }

    @Override
    public String getText(String locator) {
        By by = getByFromLocator(locator);
        return driver.findElement(by).getText();
    }


    @Override
    public String getEval(String script) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        return executor.executeScript(script).toString();
    }

    @Override
    public boolean isChecked(String locator) {
        By by = getByFromLocator(locator);
        return driver.findElement(by).isSelected();
    }


    @Override
    public String[] getSelectedLabels(String selectLocator) {
        Select select = getSelect(selectLocator);
        List<WebElement> options = select.getAllSelectedOptions();
        String[] labels = new String[options.size()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = options.get(i).getText();
        }
        return labels;
    }

    @Override
    public String getSelectedLabel(String selectLocator) {
        Select select = getSelect(selectLocator);
        return select.getFirstSelectedOption().getText();
    }

    @Override
    public String[] getSelectedValues(String selectLocator) {
        Select select = getSelect(selectLocator);
        List<WebElement> options = select.getAllSelectedOptions();
        String[] values = new String[options.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = options.get(i).getAttribute("value");
        }
        return values;
    }

    @Override
    public String getSelectedValue(String selectLocator) {
        Select select = getSelect(selectLocator);
        return select.getFirstSelectedOption().getAttribute("value");
    }

    @Override
    public String[] getSelectedIndexes(String selectLocator) {
        Select select = getSelect(selectLocator);
        List<WebElement> selected = select.getAllSelectedOptions();
        List<WebElement> options = select.getOptions();
        String[] indices = new String[selected.size()];
        int j = 0;
        for (int i = 0; i < options.size(); i++) {
            String value = options.get(i).getAttribute("value");
            String sel = selected.get(j).getAttribute("value");
            if (value.equals(sel)) {
                indices[j] = Integer.toString(i);
                j++;
            }
        }
        return indices;
    }

    @Override
    public String getSelectedIndex(String selectLocator) {
        String[] indices = getSelectedIndexes(selectLocator);
        if (indices.length > 0) {
            return indices[0];
        }
        return null;
    }

    @Override
    public String[] getSelectedIds(String selectLocator) {
        Select select = getSelect(selectLocator);
        List<WebElement> options = select.getOptions();
        String[] ids = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            ids[i] = options.get(i).getAttribute("id");
        }
        return ids;
    }

    @Override
    public String getSelectedId(String selectLocator) {
        String[] ids = getSelectedIds(selectLocator);
        if (ids.length > 1) {
            return ids[0];
        }
        return null;
    }

    @Override
    public boolean isSomethingSelected(String selectLocator) {
        Select select = getSelect(selectLocator);
        return select.getFirstSelectedOption() != null;
    }

    @Override
    public String[] getSelectOptions(String selectLocator) {
        Select select = getSelect(selectLocator);
        List<WebElement> options = select.getOptions();
        String[] labels = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            labels[i] = options.get(i).getText();
        }
        return labels;
    }

    @Override
    public String getAttribute(String attributeLocator) {
        int i = attributeLocator.lastIndexOf('@');
        String locator = attributeLocator.substring(0, i);
        String attribute = attributeLocator.substring(i + 1);
        By by = getByFromLocator(locator);
        return driver.findElement(by).getAttribute(attribute);
    }

    @Override
    public boolean isTextPresent(String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.DOTALL | Pattern.MULTILINE);
        String text = getBodyText();
        return p.matcher(text).find();
    }

    @Override
    public boolean isElementPresent(String locator) {
        try {
            By by = getByFromLocator(locator);
            driver.findElement(by);
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    @Override
    public boolean isVisible(String locator) {
        WebElement element = element(locator);
        return element.isDisplayed();
    }

    @Override
    public boolean isEditable(String locator) {
        WebElement element = element(locator);
        return element.isEnabled();
    }

    @Override
    public String getHtmlSource() {
        return driver.getPageSource();
    }

    @Override
    public Number getCssCount(String css) {
        return driver.findElements(By.cssSelector(css)).size();
    }

    @Override
    public Number getXpathCount(String xpath) {
        return driver.findElements(By.xpath(xpath)).size();
    }

    public String[] idByXpath(String[] xpathList) {
        List<WebElement> elements = new ArrayList();
        for (int i = 0; i < xpathList.length; i++)
            elements.addAll(driver.findElements(By.xpath(xpathList[i])));
        String[] listOfId = new String[elements.size()];
        for (int i = 0; i < listOfId.length; i++) {
            if (elements.get(i).getAttribute("id") == null) {
                //populate listOfId[i] with xpath for elements.get(i)
                try {
                    listOfId[i] = getXPath(elements.get(i));
                } catch (Exception e) {
                }
            } else {
                listOfId[i] = elements.get(i).getAttribute("id");
            }
        }
        return listOfId;
    }

    @Override
    public String[] getAllButtons() {
        return idByXpath(xpathListButtons);
    }

    @Override
    public String[] getAllFields() {
        return idByXpath(xpathListFields);
    }

    @Override
    public String[] getAllLinks() {
        return idByXpath(xpathListLinks);
    }

    @Override
    public String[] getAllWindowTitles() {
        String current = driver.getWindowHandle();
        String[] titleList;
        int i = -1;
        Set<String> handles = driver.getWindowHandles();
        titleList = new String[handles.size()];
        for (String handle : handles) {
            driver.switchTo().window(handle);
            titleList[++i] = driver.getTitle();
        }
        driver.switchTo().window(current);
        return titleList;
    }

    public String[] getAllWindowSpecs(String attribute) {
        String current = driver.getWindowHandle();
        int i = -1;
        Set<String> handles = driver.getWindowHandles();
        String[] specsList = new String[handles.size()];
        try {
            for (String handle : handles) {
                List<WebElement> e = driver.findElements(By.xpath("//meta"));
                for (WebElement element : e) {
                    if (element.getAttribute(attribute) == null) continue;
                    specsList[++i] = element.getAttribute(attribute);
                    break;
                }
                driver.switchTo().window(handle);
            }
            driver.switchTo().window(current);
        } catch (Exception e) {
            System.err.println("Exception found in switching windows: " + e);
        }
        return specsList;
    }

    @Override
    public String[] getAllWindowIds() {
        return getAllWindowSpecs("id");
    }

    @Override
    public String[] getAllWindowNames() {
        return getAllWindowSpecs("name");
    }

    @Override
    public Number getElementHeight(String locator) {
        By by = getByFromLocator(locator);
        return driver.findElement(by).getSize().getHeight();
    }

    @Override
    public Number getElementWidth(String locator) {
        By by = getByFromLocator(locator);
        return driver.findElement(by).getSize().getWidth();
    }

    @Override
    public Number getElementPositionLeft(String locator) {
        By by = getByFromLocator(locator);
        return driver.findElement(by).getLocation().getX();
    }

    @Override
    public Number getElementPositionTop(String locator) {
        By by = getByFromLocator(locator);
        return driver.findElement(by).getLocation().getY();
    }

    /**
     * Executes JavaScript from script synchronously
     * @param script a file location or a string
     * @param args attached arguments
     * @return the marshall-ed javascript object
     * @throws Exception in case of error
     */
    public Object js(String script,Object... args) throws Exception {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        File file =  new File(script);
        if ( file.exists() ){
            script = Utils.readToEnd(script);
        }
        return executor.executeScript(script, args);
    }

    /**
     * Executes JavaScript from script asynchronously
     * @param script a string
     * @param args attached arguments
     * @return the marshall-ed javascript object
     */
    public Object jsa(String script,Object... args){
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        return executor.executeAsyncScript(script, args);
    }

    @Override
    public void captureEntirePageScreenshot(String filename, String kwargs) {
        this.captureScreenshot(filename);
    }

    /**
     * Selects a frame by frame index
     * @param inx 0 based index
     */
    public void frame(int inx){
        driver.switchTo().frame(inx);
    }

    /**
     * Selects a frame by frame name Or Id
     * @param nameOrId name or id
     */
    public void frame(String nameOrId){
        driver.switchTo().frame(nameOrId);
    }
    @Override
    public void selectFrame(String locator) {
        Integer inx = TypeUtility.castInteger(locator);
        if (  inx != null ){
            driver.switchTo().frame(inx);
        }else {
            WebElement frame = element(locator);
            driver.switchTo().frame(frame);
        }
    }

    @Override
    public void selectPopUp(String windowID) {
        selectWindow(windowID);
    }

    @Override
    public void selectWindow(String windowID) {
        driver.switchTo().window(windowID);
    }

    @Override
    public void windowMaximize() {
        driver.manage().window().maximize();
    }

    @Override
    public void runScript(String script) {
        try {
            js(script);
        }catch (Exception e){
            System.err.println("Error in runScript : " + e);
        }
    }

    @Override
    public String captureEntirePageScreenshotToString(String kwargs) {
       return ((TakesScreenshot)driver).getScreenshotAs(OutputType.BASE64);
    }

    @Override
    public void mouseDown(String locator) {
        Actions actions = new Actions(driver);
        WebElement element = element(locator);
        actions.clickAndHold(element);
    }


    @Override
    public void mouseUp(String locator) {
        Actions actions = new Actions(driver);
        WebElement element = element(locator);
        actions.release(element);
    }

    @Override
    public void keyPress(String locator, String keySequence) {
        Actions actions = new Actions(driver);
        WebElement element = element(locator);
        actions.sendKeys(element, keySequence);
    }

    @Override
    public void captureScreenshot(String filename) {
        try {
            File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File(filename));
        }catch (Exception e){
            System.err.printf("Error Taking Screenshot : %s", e);
        }
    }

    public void highlight(String locator, Interpreter.NamedArgs... args){
        WebElement element = element(locator);
        highlight(element,args);
    }

    public void highlight(WebElement element, Interpreter.NamedArgs... args) {

        String lineWidth = BLINK_WIDTH ;
        String color = BLINK_COLOR ;
        int times = BLINK_TIMES ;
        int delay = BLINK_DELAY ;

        for ( int i = 0 ; i<args.length ; i++){
            switch ( args[i].name ){
                case "width":
                case "w" :
                    lineWidth = args[i].value.toString() ;
                    break;

                case "c" :
                case "color":
                    color = args[i].value.toString();
                    break;
                case "times" :
                case "n" :
                    times = TypeUtility.castInteger(args[i].value, BLINK_TIMES );
                    break;
                case "delay" :
                case "t" :
                    delay = TypeUtility.castInteger(args[i].value, BLINK_DELAY );
                    break;
                default:
                    break;
            }
        }

        JavascriptExecutor js = (JavascriptExecutor) driver;
        String style = element.getAttribute("style");
        String highlight = String.format("border: %spx solid %s;", lineWidth, color) ;
        String script = "arguments[0].setAttribute('style', arguments[1]);" ;


        for (int i = 0; i < times ; i++) {
            js.executeScript(script, element, highlight );
            try{
                Thread.sleep(delay);
            }catch (Exception e){}

            js.executeScript( script,  element, style);
            try{
                Thread.sleep(delay);
            }catch (Exception e){}
        }
    }

    @Override
    public void highlight(String locator) {
        WebElement element = element(locator);
        highlight(element);
    }

    /**
     * Downloads a target file from URL into a file
     * @param url the target
     * @param file the destination
     * @return true if ok, false otherwise
     */
    public boolean download(String url, String file){
        return Utils.copyFileFromUrl(url,file);
    }

    /**
     * Downloads a target file from locator into a file
     * @param locator which is the target
     * @param file the destination file
     * @return true if ok, false otherwise
     */
    public boolean downloadTarget(String locator, String file){
        By by = getByFromLocator(locator);
        WebElement element = driver.findElement(by);
        String tag = element.getTagName();
        if ( tag.equals("a")){
            String url = element.getAttribute("href");
            if ( !url.startsWith("http")){
                //relative, make it full
                url = driver.getCurrentUrl()+"/" + url;
            }
            return Utils.copyFileFromUrl(url,file);
        }
        return false;
    }

    /**
     * Gets this current HTML page as data source
     * which hosts the HTML tables as data tables
     * @return a data source
     */
    public DataSource dataSource(){
        return ProviderFactory.dataSource(driver.getPageSource());
    }

    /**
     * Gets an HTML table as a data table
     * @param tableIndex 0 based index
     * @return if possible, the [tableIndex] HTML table as Data Matrix object
     * @throws Exception in case of error
     */
    public DataMatrix table(Object tableIndex) throws Exception {
        DataSource ds = dataSource();
        if ( ds == null ){ throw  new Exception("Data Source Can not be Initialized!") ;}
        tableIndex = TypeUtility.castInteger(tableIndex,null);
        return DataMatrix.loc2matrix(driver.getPageSource(), tableIndex);
    }

    /**
     * Zooms in the view port
     * @param count how much zoom
     */
    public void zoomIn(int count){
        WebElement html = driver.findElement(By.tagName("html"));
        for ( int i = 0 ; i < count ; i++ ) {
            if (IS_WIN) {
                html.sendKeys(Keys.chord(Keys.CONTROL, Keys.ADD));
            } else if (IS_MAC) {
                html.sendKeys(Keys.chord(Keys.COMMAND, Keys.ADD));
            } else {
                //linux?
            }
        }
    }

    /**
     * Zooms in the view port by one unit
     */
    public void zoomIn(){ zoomIn(1);}

    /**
     * Zooms out the view port
     * @param count how much zoom
     */
    public void zoomOut(int count){
        WebElement html = driver.findElement(By.tagName("html"));
        for ( int i = 0 ; i < count ; i++ ) {
            if (IS_WIN) {
                html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
            } else if (IS_MAC) {
                html.sendKeys(Keys.chord(Keys.COMMAND, Keys.SUBTRACT));
            } else {
                // linux ?
            }
        }
    }

    /**
     * Sets the per-session extension Javascript
     *
     * @param extensionJs string for javascript extension
     */
    @Override
    public void setExtensionJs(String extensionJs) {
        System.err.println("Sorry, no support for extensions!");
    }

    /**
     * Launches the browser with a new Selenium session
     */
    @Override
    public void start() {
        System.err.println("Sorry, no need for start!");
    }

    /**
     * Starts a new Selenium testing session with a String, representing a configuration
     *
     * @param optionsString options string option=value format
     */
    @Override
    public void start(String optionsString) {
        start();
    }

    /**
     * Starts a new Selenium testing session with a configuration options object
     *
     * @param optionsObject options string json format
     */
    @Override
    public void start(Object optionsObject) {
        start();
    }

    /**
     * Ends the test session, killing the browser
     */
    @Override
    public void stop() {
        System.err.println("Sorry, no need for stop!");
    }

    /**
     * Shows in the RemoteRunner a banner for the current test The banner is 'classname : methodname'
     * where those two are derived from the caller The method name will be unCamelCased with the
     * insertion of spaces at word boundaries
     */
    @Override
    public void showContextualBanner() {
        System.err.println("Sorry, no support for context banner!");
    }

    /**
     * Shows in the RemoteRunner a banner for the current test The banner is 'classname : methodname'
     * The method name will be unCamelCased with the insertion of spaces at word boundaries
     *
     * @param className see doc
     * @param methodName see doc
     */
    @Override
    public void showContextualBanner(String className, String methodName) {
        showContextualBanner();
    }

    /**
     * Clicks on a link, button, checkbox or radio button. If the click action causes a new page to
     * load (like a link usually does), call waitForPageToLoad.
     *
     * @param locator     an element locator
     * @param coordString specifies the x,y position (i.e. - 10,20) of the mouse event relative to the
     */
    @Override
    public void clickAt(String locator, String coordString) {
        System.err.println("Sorry, no support for clickAt!");
    }

    /**
     * Doubleclicks on a link, button, checkbox or radio button. If the action causes a new page to
     * load (like a link usually does), call waitForPageToLoad.
     *
     * @param locator     an element locator
     * @param coordString specifies the x,y position (i.e. - 10,20) of the mouse event relative to the
     */
    @Override
    public void doubleClickAt(String locator, String coordString) {
        System.err.println("Sorry, no support for doubleClickAt!");
    }

    /**
     * Simulates opening the context menu for the specified element (as might happen if the user
     * "right-clicked" on the element).
     *
     * @param locator     an element locator
     * @param coordString specifies the x,y position (i.e. - 10,20) of the mouse event relative to the
     */
    @Override
    public void contextMenuAt(String locator, String coordString) {
        System.err.println("Sorry, no support for contextMenuAt!");
    }

    /**
     * Explicitly simulate an event, to trigger the corresponding "on<em>event</em>" handler.
     *
     * @param locator   an <a href="#locators">element locator</a>
     * @param eventName the event name, e.g. "focus" or "blur"
     */
    @Override
    public void fireEvent(String locator, String eventName) {
        WebElement element = element(locator);
        String script = String.format("return arguments[0].%s() ; ", eventName );
                ((JavascriptExecutor) driver).executeScript(script, element);
    }

    /**
     * Press the shift key and hold it down until doShiftUp() is called or a new page is loaded.
     */
    @Override
    public void shiftKeyDown() {
        System.err.println("Sorry, no support for firing event!");
    }

    /**
     * Release the shift key.
     */
    @Override
    public void shiftKeyUp() {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Press the meta key and hold it down until doMetaUp() is called or a new page is loaded.
     */
    @Override
    public void metaKeyDown() {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Release the meta key.
     */
    @Override
    public void metaKeyUp() {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Press the alt key and hold it down until doAltUp() is called or a new page is loaded.
     */
    @Override
    public void altKeyDown() {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Release the alt key.
     */
    @Override
    public void altKeyUp() {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Press the control key and hold it down until doControlUp() is called or a new page is loaded.
     */
    @Override
    public void controlKeyDown() {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Release the control key.
     */
    @Override
    public void controlKeyUp() {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Simulates a user pressing a key (without releasing it yet).
     *
     * @param locator     an <a href="#locators">element locator</a>
     * @param keySequence Either be a string(
     *                    "\" followed by the numeric keycode  of the key to be pressed, normally the ASCII value of that key), or a single  character. For example: "
     */
    @Override
    public void keyDown(String locator, String keySequence) {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Simulates a user releasing a key.
     *
     * @param locator     an <a href="#locators">element locator</a>
     * @param keySequence Either be a string(
     *                    "\" followed by the numeric keycode  of the key to be pressed, normally the ASCII value of that key), or a single  character. For example: "
     */
    @Override
    public void keyUp(String locator, String keySequence) {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Simulates a user moving the mouse pointer away from the specified element.
     *
     * @param locator an <a href="#locators">element locator</a>
     */
    @Override
    public void mouseOut(String locator) {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Simulates a user pressing the right mouse button (without releasing it yet) on the specified
     * element.
     *
     * @param locator an <a href="#locators">element locator</a>
     */
    @Override
    public void mouseDownRight(String locator) {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Simulates a user pressing the left mouse button (without releasing it yet) at the specified
     * location.
     *
     * @param locator     an <a href="#locators">element locator</a>
     * @param coordString specifies the x,y position (i.e. - 10,20) of the mouse event relative to the
     */
    @Override
    public void mouseDownAt(String locator, String coordString) {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Simulates a user pressing the right mouse button (without releasing it yet) at the specified
     * location.
     *
     * @param locator     an <a href="#locators">element locator</a>
     * @param coordString specifies the x,y position (i.e. - 10,20) of the mouse event relative to the
     */
    @Override
    public void mouseDownRightAt(String locator, String coordString) {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Simulates the event that occurs when the user releases the right mouse button (i.e., stops
     * holding the button down) on the specified element.
     *
     * @param locator an <a href="#locators">element locator</a>
     */
    @Override
    public void mouseUpRight(String locator) {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Simulates the event that occurs when the user releases the mouse button (i.e., stops holding
     * the button down) at the specified location.
     *
     * @param locator     an <a href="#locators">element locator</a>
     * @param coordString specifies the x,y position (i.e. - 10,20) of the mouse event relative to the
     */
    @Override
    public void mouseUpAt(String locator, String coordString) {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Simulates the event that occurs when the user releases the right mouse button (i.e., stops
     * holding the button down) at the specified location.
     *
     * @param locator     an <a href="#locators">element locator</a>
     * @param coordString specifies the x,y position (i.e. - 10,20) of the mouse event relative to the
     */
    @Override
    public void mouseUpRightAt(String locator, String coordString) {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Simulates a user pressing the mouse button (without releasing it yet) on the specified element.
     *
     * @param locator     an <a href="#locators">element locator</a>
     * @param coordString specifies the x,y position (i.e. - 10,20) of the mouse event relative to the
     */
    @Override
    public void mouseMoveAt(String locator, String coordString) {
        System.err.println("Sorry, no support for keys events!");
    }

    /**
     * Set execution speed (i.e., set the millisecond length of a delay which will follow each
     * selenium operation). By default, there is no such delay, i.e., the delay is 0 milliseconds.
     *
     * @param value the number of milliseconds to pause after operation
     */
    @Override
    public void setSpeed(String value) {
        System.err.println("Sorry, no support for Speed!");
    }

    /**
     * Get execution speed (i.e., get the millisecond length of the delay following each selenium
     * operation). By default, there is no such delay, i.e., the delay is 0 milliseconds.
     * <p>
     * See also setSpeed.
     *
     * @return the execution speed in milliseconds.
     */
    @Override
    public String getSpeed() {
        System.err.println("Sorry, no support for Speed! I would return empty!");
        return "";
    }

    /**
     * Get RC logs associated with this session.
     *
     * @return the remote control logs associated with this session
     */
    @Override
    public String getLog() {
        System.err.println("Sorry, no support for logs! I would return empty!");
        return "";
    }

    /**
     * Opens an URL in the test frame. This accepts both relative and absolute URLs.
     * <p>
     * The "open" command waits for the page to load before proceeding, ie. the "AndWait" suffix is
     * implicit.
     * <p>
     * <em>Note</em>: The URL must be on the same domain as the runner HTML due to security
     * restrictions in the browser (Same Origin Policy). If you need to open an URL on another domain,
     * use the Selenium Server to start a new browser session on that domain.
     *
     * @param url                the URL to open; may be relative or absolute
     * @param ignoreResponseCode if set to true, ignores http response code.
     */
    @Override
    public void open(String url, String ignoreResponseCode) {
        System.err.println("Sorry, no support for ignoring anything, I would do it normally!");
        open(url);
    }

    /**
     * Opens an URL in the test frame. This accepts both relative and absolute URLs.
     * <p>
     * The "open" command waits for the page to load before proceeding, ie. the "AndWait" suffix is
     * implicit.
     * <p>
     * <em>Note</em>: The URL must be on the same domain as the runner HTML due to security
     * restrictions in the browser (Same Origin Policy). If you need to open an URL on another domain,
     * use the Selenium Server to start a new browser session on that domain.
     *
     * @param url the URL to open; may be relative or absolute
     */
    @Override
    public void open(String url) {
        if ( url.toLowerCase().startsWith("http")){
            driver.get(url);
        } else {
            driver.get(baseUrl + url);
        }
    }

    /**
     * Opens a popup window (if a window with that ID isn't already open). After opening the window,
     * you'll need to select it using the selectWindow command.
     * <p>
     * This command can also be a useful workaround for bug SEL-339. In some cases, Selenium will be
     * unable to intercept a call to window.open (if the call occurs during or before the "onLoad"
     * event, for example). In those cases, you can force Selenium to notice the open window's name by
     * using the Selenium openWindow command, using an empty (blank) url, like this: openWindow("",
     * "myFunnyWindow").
     * </p>
     *
     * @param url      the URL to open, which can be blank
     * @param windowID the JavaScript window ID of the window to select
     */
    @Override
    public void openWindow(String url, String windowID) {
        System.err.println("Sorry, no support for popping windows now!");
    }

    /**
     * Selects the main window. Functionally equivalent to using <code>selectWindow()</code> and
     * specifying no value for <code>windowID</code>.
     */
    @Override
    public void deselectPopUp() {
        driver.switchTo().defaultContent();
    }

    /**
     * Determine whether current/locator identify the frame containing this running code.
     * <p>
     * This is useful in proxy injection mode, where this code runs in every browser frame and window,
     * and sometimes the selenium server needs to identify the "current" frame. In this case, when the
     * test calls selectFrame, this routine is called for each frame to figure out which one has been
     * selected. The selected frame will return true, while all others will return false.
     * </p>
     *
     * @param currentFrameString starting frame
     * @param target             new frame (which might be relative to the current one)
     * @return true if the new frame is this code's window
     */
    @Override
    public boolean getWhetherThisFrameMatchFrameExpression(String currentFrameString, String target) {
        System.err.println("Sorry, no idea what this even means!");
        return false;
    }

    /**
     * Determine whether currentWindowString plus target identify the window containing this running
     * code.
     * <p>
     * This is useful in proxy injection mode, where this code runs in every browser frame and window,
     * and sometimes the selenium server needs to identify the "current" window. In this case, when
     * the test calls selectWindow, this routine is called for each window to figure out which one has
     * been selected. The selected window will return true, while all others will return false.
     * </p>
     *
     * @param currentWindowString starting window
     * @param target              new window (which might be relative to the current one, e.g., "_parent")
     * @return true if the new window is this code's window
     */
    @Override
    public boolean getWhetherThisWindowMatchWindowExpression(String currentWindowString, String target) {
        return false;
    }

    /**
     * Waits for a popup window to appear and load up.
     *
     * @param windowID the JavaScript window "name" of the window that will appear (not the text of
     *                 the title bar) If unspecified, or specified as "null", this command will wait for the
     *                 first non-top window to appear (don't rely on this if you are working with multiple
     *                 popups simultaneously).
     * @param timeout  a timeout in milliseconds, after which the action will return with an error. If
     *                 this value is not specified, the default Selenium timeout will be used. See the
     */
    @Override
    public void waitForPopUp(String windowID, String timeout) {
        System.err.println("Sorry, no support for popping windows now!");
    }

    /**
     * <p>
     * By default, Selenium's overridden window.confirm() function will return true, as if the user
     * had manually clicked OK; after running this command, the next call to confirm() will return
     * false, as if the user had clicked Cancel. Selenium will then resume using the default behavior
     * for future confirmations, automatically returning true (OK) unless/until you explicitly call
     * this command for each confirmation.
     * </p>
     * <p>
     * Take note - every time a confirmation comes up, you must consume it with a corresponding
     * getConfirmation, or else the next selenium operation will fail.
     * </p>
     */
    @Override
    public void chooseCancelOnNextConfirmation() {
        System.err.println("Sorry, no support for this sort of alerting");
    }

    /**
     * <p>
     * Undo the effect of calling chooseCancelOnNextConfirmation. Note that Selenium's overridden
     * window.confirm() function will normally automatically return true, as if the user had manually
     * clicked OK, so you shouldn't need to use this command unless for some reason you need to change
     * your mind prior to the next confirmation. After any confirmation, Selenium will resume using
     * the default behavior for future confirmations, automatically returning true (OK) unless/until
     * you explicitly call chooseCancelOnNextConfirmation for each confirmation.
     * </p>
     * <p>
     * Take note - every time a confirmation comes up, you must consume it with a corresponding
     * getConfirmation, or else the next selenium operation will fail.
     * </p>
     */
    @Override
    public void chooseOkOnNextConfirmation() {
        System.err.println("Sorry, no support for this sort of alerting");
    }

    /**
     * Instructs Selenium to return the specified answer string in response to the next JavaScript
     * prompt [window.prompt()].
     *
     * @param answer the answer to give in response to the prompt pop-up
     */
    @Override
    public void answerOnNextPrompt(String answer) {
        System.err.println("Sorry, no support for this sort of alerting");
    }

    /**
     * Gets the text from a cell of a table. The cellAddress syntax tableLocator.row.column, where row
     * and column start at 0.
     *
     * @param tableCellAddress a cell address, e.g. "foo.1.4"
     * @return the text from the specified cell
     */
    @Override
    public String getTable(String tableCellAddress) {
        System.err.println("Sorry, no support for this sort of slow table access, use table() instead");
        return "";
    }

    /**
     * Returns every instance of some attribute from all known windows.
     *
     * @param attributeName name of an attribute on the windows
     * @return the set of values of this attribute from all known windows.
     */
    @Override
    public String[] getAttributeFromAllWindows(String attributeName) {
        System.err.println("Sorry, no support for this sort of attribute get");
        return new String[0];
    }

    /**
     * Configure the number of pixels between "mousemove" events during dragAndDrop commands
     * (default=10).
     * <p>
     * Setting this value to 0 means that we'll send a "mousemove" event to every single pixel in
     * between the start location and the end location; that can be very slow, and may cause some
     * browsers to force the JavaScript to timeout.
     * </p>
     * <p>
     * If the mouse speed is greater than the distance between the two dragged objects, we'll just
     * send one "mousemove" at the start location and then one final one at the end location.
     * </p>
     *
     * @param pixels the number of pixels between "mousemove" events
     */
    @Override
    public void setMouseSpeed(String pixels) {
        System.err.println("Sorry, no support for playing with mouse speed");
    }

    /**
     * Returns the number of pixels between "mousemove" events during dragAndDrop commands
     * (default=10).
     *
     * @return the number of pixels between "mousemove" events during dragAndDrop commands
     * (default=10)
     */
    @Override
    public Number getMouseSpeed() {
        System.err.println("Sorry, no support for playing with mouse speed");
        return -1;
    }

    /**
     * Gives focus to the currently selected window
     */
    @Override
    public void windowFocus() {
        driver.switchTo().defaultContent();
    }

    /**
     * Moves the text cursor to the specified position in the given input element or textarea. This
     * method will fail if the specified element isn't an input element or textarea.
     *
     * @param locator  an <a href="#locators">element locator</a> pointing to an input element or
     *                 textarea
     * @param position the numerical position of the cursor in the field; position should be 0 to move
     *                 the position to the beginning of the field. You can also set the cursor to -1 to move it
     */
    @Override
    public void setCursorPosition(String locator, String position) {
        System.err.println("Sorry, no support for playing with cursor as of now");
    }

    /**
     * Get the relative index of an element to its parent (starting from 0). The comment node and
     * empty text node will be ignored.
     *
     * @param locator an <a href="#locators">element locator</a> pointing to an element
     * @return of relative index of the element to its parent (starting from 0)
     */
    @Override
    public Number getElementIndex(String locator) {
        System.err.println("Sorry, no support for playing with position of child as of now");
        return -1;
    }

    /**
     * Check if these two elements have same parent and are ordered siblings in the DOM. Two same
     * elements will not be considered ordered.
     *
     * @param locator1 an <a href="#locators">element locator</a> pointing to the first element
     * @param locator2 an <a href="#locators">element locator</a> pointing to the second element
     * @return true if element1 is the previous sibling of element2, false otherwise
     */
    @Override
    public boolean isOrdered(String locator1, String locator2) {
        System.err.println("Sorry, no support for this, now!");
        return false;
    }

    /**
     * Retrieves the text cursor position in the given input element or textarea; beware, this may not
     * work perfectly on all browsers.
     * <p>
     * Specifically, if the cursor/selection has been cleared by JavaScript, this command will tend to
     * return the position of the last location of the cursor, even though the cursor is now gone from
     * the page. This is filed as <a href="http://jira.openqa.org/browse/SEL-243">SEL-243</a>.
     * </p>
     * This method will fail if the specified element isn't an input element or textarea, or there is
     * no cursor in the element.
     *
     * @param locator an <a href="#locators">element locator</a> pointing to an input element or
     *                textarea
     * @return the numerical position of the cursor in the field
     */
    @Override
    public Number getCursorPosition(String locator) {
        System.err.println("Sorry, no support for playing with cursor position");
        return -1;
    }

    /**
     * Returns the specified expression.
     * <p>
     * This is useful because of JavaScript preprocessing. It is used to generate commands like
     * assertExpression and waitForExpression.
     * </p>
     *
     * @param expression the value to return
     * @return the value passed in
     */
    @Override
    public String getExpression(String expression) {
        Object o = ((JavascriptExecutor)driver).executeScript(expression);
        return String.format("%s",o);
    }

    /**
     * Temporarily sets the "id" attribute of the specified element, so you can locate it in the
     * future using its ID rather than a slow/complicated XPath. This ID will disappear once the page
     * is reloaded.
     *
     * @param locator    an <a href="#locators">element locator</a> pointing to an element
     * @param identifier a string to be used as the ID of the specified element
     */
    @Override
    public void assignId(String locator, String identifier) {
        WebElement element = element(locator);
        String script = String.format("return arguments[0].setAttribute('id', '%s');", identifier);
        ((JavascriptExecutor)driver).executeScript(script, element);
    }

    /**
     * Specifies whether Selenium should use the native in-browser implementation of XPath (if any
     * native version is available); if you pass "false" to this function, we will always use our
     * pure-JavaScript xpath library. Using the pure-JS xpath library can improve the consistency of
     * xpath element locators between different browser vendors, but the pure-JS version is much
     * slower than the native implementations.
     *
     * @param allow boolean, true means we'll prefer to use native XPath; false means we'll only use
     *              JS XPath
     */
    @Override
    public void allowNativeXpath(String allow) {
        System.err.println("Sorry, no support for playing with XPath either!");
    }

    /**
     * Specifies whether Selenium will ignore xpath attributes that have no value, i.e. are the empty
     * string, when using the non-native xpath evaluation engine. You'd want to do this for
     * performance reasons in IE. However, this could break certain xpaths, for example an xpath that
     * looks for an attribute whose value is NOT the empty string.
     * <p>
     * The hope is that such xpaths are relatively rare, but the user should have the option of using
     * them. Note that this only influences xpath evaluation when using the ajaxslt engine (i.e. not
     * "javascript-xpath").
     *
     * @param ignore boolean, true means we'll ignore attributes without value at the expense of xpath
     *               "correctness"; false means we'll sacrifice speed for correctness.
     */
    @Override
    public void ignoreAttributesWithoutValue(String ignore) {
        System.err.println("Sorry, no support for playing with fire");
    }

    /**
     * Runs the specified JavaScript snippet repeatedly until it evaluates to "true". The snippet may
     * have multiple lines, but only the result of the last line will be considered.
     * <p>
     * Note that, by default, the snippet will be run in the runner's test window, not in the window
     * of your application. To get the window of your application, you can use the JavaScript snippet
     * <code>selenium.browserbot.getCurrentWindow()</code>, and then run your JavaScript in there
     * </p>
     *
     * @param script  the JavaScript snippet to run
     * @param timeout a timeout in milliseconds, after which this command will return with an error
     */
    @Override
    public void waitForCondition(String script, String timeout) {
        System.err.println("Sorry, no support for playing with ideas of polling, not now, pls!");
    }

    /**
     * Specifies the amount of time that Selenium will wait for actions to complete.
     * <p>
     * Actions that require waiting include "open" and the "waitFor*" actions.
     * </p>
     * The default timeout is 30 seconds.
     *
     * @param timeout a timeout in milliseconds, after which the action will return with an error
     */
    @Override
    public void setTimeout(String timeout) {
        this.timeout = TypeUtility.castInteger( timeout, this.timeout );
    }

    /**
     * Waits for a new page to load.
     * <p>
     * You can use this command instead of the "AndWait" suffixes, "clickAndWait", "selectAndWait",
     * "typeAndWait" etc. (which are only available in the JS API).
     * </p>
     * <p>
     * Selenium constantly keeps track of new pages loading, and sets a "newPageLoaded" flag when it
     * first notices a page load. Running any other Selenium command after turns the flag to false.
     * Hence, if you want to wait for a page to load, you must wait immediately after a Selenium
     * command that caused a page-load.
     * </p>
     *
     * @param timeout a timeout in milliseconds, after which this command will return with an error
     */
    @Override
    public void waitForPageToLoad(String timeout) {
        this.timeout = TypeUtility.castInteger( timeout, this.timeout );
    }

    /**
     * Waits for a new frame to load.
     * <p>
     * Selenium constantly keeps track of new pages and frames loading, and sets a "newPageLoaded"
     * flag when it first notices a page load.
     * </p>
     * <p>
     * See waitForPageToLoad for more information.
     *
     * @param frameAddress FrameAddress from the server side
     * @param timeout      a timeout in milliseconds, after which this command will return with an error
     */
    @Override
    public void waitForFrameToLoad(String frameAddress, String timeout) {
        System.err.println("Sorry, no support for playing with speed of frame load, no no no!");
    }

    /**
     * Returns true if a cookie with the specified name is present, or false otherwise.
     *
     * @param name the name of the cookie
     * @return true if a cookie with the specified name is present, or false otherwise.
     */
    @Override
    public boolean isCookiePresent(String name) {
        Set<Cookie> cookies = driver.manage().getCookies();
        for ( Cookie c : cookies ){
            if ( c.getName().equals(name)){
                return true ;
            }
        }
        return false ;
    }

    /**
     * Create a new cookie whose path and domain are same with those of current page under test,
     * unless you specified a path for this cookie explicitly.
     *
     * @param nameValuePair name and value of the cookie in a format "name=value"
     * @param optionsString options for the cookie. Currently supported options include 'path',
     *                      'max_age' and 'domain'. the optionsString's format is
     *                      "path=/path/, max_age=60, domain=.foo.com". The order of options are irrelevant, the
     *                      unit of the value of 'max_age' is second. Note that specifying a domain that isn't a
     */
    @Override
    public void createCookie(String nameValuePair, String optionsString) {
        String[] pair = nameValuePair.split("=");
        Cookie cookie = new Cookie(pair[0],pair[1]);
        // TODO : option string later... dude...
        driver.manage().addCookie(cookie);
    }

    /**
     * Calls deleteCookie with recurse=true on all cookies visible to the current page. As noted on
     * the documentation for deleteCookie, recurse=true can be much slower than simply deleting the
     * cookies using a known domain/path.
     */
    @Override
    public void deleteAllVisibleCookies() {
        driver.manage().deleteAllCookies();
    }

    /**
     * Sets the threshold for browser-side logging messages; log messages beneath this threshold will
     * be discarded. Valid logLevel strings are: "debug", "info", "warn", "error" or "off". To see the
     * browser logs, you need to either show the log window in GUI mode, or enable browser-side
     * logging in Selenium RC.
     *
     * @param logLevel one of the following: "debug", "info", "warn", "error" or "off"
     */
    @Override
    public void setBrowserLogLevel(String logLevel) {
        System.err.println("Sorry, no support for playing with browser logs");
    }

    /**
     * Defines a new function for Selenium to locate elements on the page. For example, if you define
     * the strategy "foo", and someone runs click("foo=blah"), we'll run your function, passing you
     * the string "blah", and click on the element that your function returns, or throw an
     * "Element not found" error if your function returns null.
     * <p>
     * We'll pass three arguments to your function:
     * <ul>
     * <li>locator: the string the user passed in</li>
     * <li>inWindow: the currently selected window</li>
     * <li>inDocument: the currently selected document</li>
     * </ul>
     * The function must return null if the element can't be found.
     *
     * @param strategyName       the name of the strategy to define; this should use only letters [a-zA-Z]
     *                           with no spaces or other punctuation.
     * @param functionDefinition a string defining the body of a function in JavaScript. For example:
     */
    @Override
    public void addLocationStrategy(String strategyName, String functionDefinition) {
        System.err.println("Sorry, no support for playing with locators");
    }

    /**
     * Executes a command rollup, which is a series of commands with a unique name, and optionally
     * arguments that control the generation of the set of commands. If any one of the rolled-up
     * commands fails, the rollup is considered to have failed. Rollups may also contain nested
     * rollups.
     *
     * @param rollupName the name of the rollup command
     * @param kwargs     keyword arguments string that influences how the rollup expands into commands
     */
    @Override
    public void rollup(String rollupName, String kwargs) {
        System.err.println("Sorry, I do not think anyone ever used it!");
    }

    /**
     * Loads script content into a new script tag in the Selenium document. This differs from the
     * runScript command in that runScript adds the script tag to the document of the AUT, not the
     * Selenium document. The following entities in the script content are replaced by the characters
     * they represent:
     * <p>
     * &lt; &gt; &amp;
     * <p>
     * The corresponding remove command is removeScript.
     *
     * @param scriptContent the Javascript content of the script to add
     * @param scriptTagId   (optional) the id of the new script tag. If specified, and an element with
     */
    @Override
    public void addScript(String scriptContent, String scriptTagId) {
        System.err.println("Sorry, no support for playing with JS like this!");
    }

    /**
     * Removes a script tag from the Selenium document identified by the given id. Does nothing if the
     * referenced tag doesn't exist.
     *
     * @param scriptTagId the id of the script element to remove.
     */
    @Override
    public void removeScript(String scriptTagId) {
        System.err.println("Sorry, no support for playing with JS like this!");
    }

    /**
     * Allows choice of one of the available libraries.
     *
     * @param libraryName name of the desired library Only the following three can be chosen:
     *                    <ul>
     *                    <li>"ajaxslt" - Google's library</li>
     *                    <li>"javascript-xpath" - Cybozu Labs' faster library</li>
     *                    <li>"default" - The default library. Currently the default library is "ajaxslt" .</li>
     *                    </ul>
     *                    If libraryName isn't one of these three, then no change will be made.
     */
    @Override
    public void useXpathLibrary(String libraryName) {
        System.err.println("Sorry, no support for playing with XPath like this");
    }

    /**
     * Writes a message to the status bar and adds a note to the browser-side log.
     *
     * @param context the message to be sent to the browser
     */
    @Override
    public void setContext(String context) {
        System.err.println("Sorry, no support for things as such : we are not hackers!");
    }

    /**
     * Capture a PNG screenshot. It then returns the file as a base 64 encoded string.
     *
     * @return The base 64 encoded string of the screen shot (PNG file)
     */
    @Override
    public String captureScreenshotToString() {
        String ss = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BASE64);
        return ss;
    }

    /**
     * Returns the network traffic seen by the browser, including headers, AJAX requests, status
     * codes, and timings. When this function is called, the traffic log is cleared, so the returned
     * content is only the traffic seen since the last call.
     *
     * @param type The type of data to return the network traffic as. Valid values are: json, xml, or
     *             plain.
     * @return A string representation in the defined type of the network traffic seen by the browser.
     */
    @Override
    public String captureNetworkTraffic(String type) {
        System.err.println("Sorry, no support for things as such : we are not hackers!");
        return "";
    }

    /**
     * Tells the Selenium server to add the specificed key and value as a custom outgoing request
     * header. This only works if the browser is configured to use the built in Selenium proxy.
     *
     * @param key   the header name.
     * @param value the header value.
     */
    @Override
    public void addCustomRequestHeader(String key, String value) {
        System.err.println("Sorry, no support for things as such");
    }

    /**
     * Kills the running Selenium Server and all browser sessions. After you run this command, you
     * will no longer be able to send commands to the server; you can't remotely start the server once
     * it has been stopped. Normally you should prefer to run the "stop" command, which terminates the
     * current browser session, rather than shutting down the entire server.
     */
    @Override
    public void shutDownSeleniumServer() {
        System.err.println("Sorry, no support for things as such");
    }

    /**
     * Retrieve the last messages logged on a specific remote control. Useful for error reports,
     * especially when running multiple remote controls in a distributed environment. The maximum
     * number of log messages that can be retrieve is configured on remote control startup.
     *
     * @return The last N log messages as a multi-line string.
     */
    @Override
    public String retrieveLastRemoteControlLogs() {
        System.err.println("Sorry, no support for things as such");
        return "";
    }

    /**
     * Simulates a user pressing a key (without releasing it yet) by sending a native operating system
     * keystroke. This function uses the java.awt.Robot class to send a keystroke; this more
     * accurately simulates typing a key on the keyboard. It does not honor settings from the
     * shiftKeyDown, controlKeyDown, altKeyDown and metaKeyDown commands, and does not target any
     * particular HTML element. To send a keystroke to a particular element, focus on the element
     * first before running this command.
     *
     * @param keycode an integer keycode number corresponding to a java.awt.event.KeyEvent; note that
     *                Java keycodes are NOT the same thing as JavaScript keycodes!
     */
    @Override
    public void keyDownNative(String keycode) {
        System.err.println("Sorry, no support for events as such");
    }

    /**
     * Simulates a user releasing a key by sending a native operating system keystroke. This function
     * uses the java.awt.Robot class to send a keystroke; this more accurately simulates typing a key
     * on the keyboard. It does not honor settings from the shiftKeyDown, controlKeyDown, altKeyDown
     * and metaKeyDown commands, and does not target any particular HTML element. To send a keystroke
     * to a particular element, focus on the element first before running this command.
     *
     * @param keycode an integer keycode number corresponding to a java.awt.event.KeyEvent; note that
     *                Java keycodes are NOT the same thing as JavaScript keycodes!
     */
    @Override
    public void keyUpNative(String keycode) {
        System.err.println("Sorry, no support for events as such");
    }

    /**
     * Simulates a user pressing and releasing a key by sending a native operating system keystroke.
     * This function uses the java.awt.Robot class to send a keystroke; this more accurately simulates
     * typing a key on the keyboard. It does not honor settings from the shiftKeyDown, controlKeyDown,
     * altKeyDown and metaKeyDown commands, and does not target any particular HTML element. To send a
     * keystroke to a particular element, focus on the element first before running this command.
     *
     * @param keycode an integer keycode number corresponding to a java.awt.event.KeyEvent; note that
     *                Java keycodes are NOT the same thing as JavaScript keycodes!
     */
    @Override
    public void keyPressNative(String keycode) {
        System.err.println("Sorry, no support for events as such");
    }

    /**
     * Zooms out the view port by one unit
     */
    public void zoomOut(){ zoomOut(1); }
}
