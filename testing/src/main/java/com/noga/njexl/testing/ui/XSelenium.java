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
import com.noga.njexl.lang.extension.TypeUtility;
import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.testing.TestAssert;
import com.noga.njexl.testing.Utils;
import com.noga.njexl.testing.dataprovider.DataSource;
import com.noga.njexl.testing.dataprovider.ProviderFactory;
import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.webdriven.WebDriverCommandProcessor;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Created by noga on 24/02/15.
 */
public class XSelenium extends DefaultSelenium implements Eventing , TestAssert.AssertionEventListener {

    public static final String SELENIUM_VAR = "selenium" ;

    public static final String SELENIUM_NS = "sel" ;

    public static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    public static final boolean IS_MAC = OS_NAME.startsWith("mac ");

    public static final boolean IS_WIN = OS_NAME.startsWith("win");

    public static final String CHROME_DRIVER_PATH = "CHROME_DRIVER" ;

    public static final String OPERA_DRIVER_PATH = "OPERA_DRIVER" ;


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

    @Override
    public void after(Event event) {
        if ( DISAPPEAR.equals(event.pattern) && event.args.length > 0 ){
            waitForDisappear(event.args[0].toString());
        }
    }

    @Override
    public void before(Event event) {
        if ( APPEAR.equals(event.pattern) && event.args.length > 0 ){
            waitForAppear(event.args[0].toString());
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
     * @param baseUrl the base url
     * @param browserType the type of the browser
     * @return an @{XSelenium} object
     */
    public static XSelenium  selenium(String baseUrl, String browserType){
        BrowserType type = Enum.valueOf(BrowserType.class,browserType);
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
        CommandProcessor commandProcessor = new WebDriverCommandProcessor(baseUrl,driver);
        return new XSelenium(commandProcessor);
    }

    public XSelenium(CommandProcessor processor) {
        super(processor);
        driver = ((WebDriverCommandProcessor) processor).getWrappedDriver();
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
        element.sendKeys(value);
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

    /**
     * Highlights and flashes the locator boundary
     * @param locator the element which needs to be highlighted
     * @param args 0 is color (red) , 1 is width(3) , 2 is no of flashes(5), 3 is delay(1sec)
     */
    public void highlight(String locator, String... args){
        WebElement element = element(locator);
        highlight(element,args);
    }

    public void highlight(WebElement element, String... args) {

        String lineWidth = "3" ;
        String color = "red" ;
        int times = 5 ;
        int delay = 1000;
        if ( args.length > 0 ){
            color = args[0];
            if ( args.length > 1 ){
                lineWidth = args[1];
                if ( args.length > 2 ){
                    times = TypeUtility.castInteger(args[2],2);
                    if ( args.length > 3 ){
                        delay = TypeUtility.castInteger(args[3],1000);
                    }
                }
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
    public DataMatrix table(String tableIndex) throws Exception {
        DataSource ds = dataSource();
        if ( ds == null ){ throw  new Exception("Data Source Can not be Initialized!") ;}
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
     * Zooms out the view port by one unit
     */
    public void zoomOut(){ zoomOut(1); }
}
