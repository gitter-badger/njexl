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
import com.noga.njexl.lang.extension.dataaccess.DataMatrix;
import com.noga.njexl.lang.extension.oop.ScriptClassBehaviour;
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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Created by noga on 24/02/15.
 */
public class XSelenium extends DefaultSelenium implements Eventing , TestAssert.AssertionEventListener {

    public static final String SELENIUM_VAR = "selenium" ;
    public static final String SELENIUM_NS = "sel" ;


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

    public String screenShotDir(){
        return screenShotDir ;
    }

    public void screenShotDir(String dir){
        screenShotDir = dir;
    }

    String getScreenShotFile(){
        String file = screenShotDir + "/" + System.currentTimeMillis() + ".png" ;
        return file ;
    }

    @Override
    public void onAssertion(TestAssert.AssertionEvent assertionEvent) {
        boolean screenShot = ((TestAssert)assertionEvent.getSource()).hasError();
        if ( screenShot ){
            captureScreenshot(getScreenShotFile());
        }
    }

    int timeout = 10000; // 10 sec should be good

    public int getTimeout(){
        return timeout ;
    }

    public void setTimeout(int ms){
        timeout = ms ;
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
                throw new Error("Can not have element : " + locator );
            }
            try{
                Thread.sleep(100);
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
                throw new Error("Did not disappear element : " + locator );
            }
            try{
                Thread.sleep(100);
            }catch (Exception e){}
        }
    }

    public static final String APPEAR = "@@" ;

    public static final String DISAPPEAR = "$$" ;

    @Override
    public void after(Event event) {
        if ( DISAPPEAR.equals(event.pattern) && event.args.length > 1 ){
            waitForDisappear( event.args[0].toString() );
        }
    }

    @Override
    public void before(Event event) {
        if ( APPEAR.equals(event.pattern) && event.args.length > 1 ){
            waitForAppear(event.args[0].toString());
        }
    }

    public enum BrowserType{
        FIREFOX,
        HTML_UNIT,
        CHROME,
        SAFARI
    }

    public static XSelenium  selenium(String baseUrl, String browserType){
        BrowserType type = Enum.valueOf(BrowserType.class,browserType);
        WebDriver driver = null;
        switch (type){
            case HTML_UNIT:
                //set javascript support : with chrome mode on
                driver = new HtmlUnitDriver(BrowserVersion.CHROME);
                ((HtmlUnitDriver)driver).setJavascriptEnabled(true);
                break;
            case CHROME:
                break;
            case SAFARI:
                break;
            case FIREFOX:
            default:
                driver = new FirefoxDriver();
                break;
        }
        CommandProcessor commandProcessor = new WebDriverCommandProcessor(baseUrl,driver);
        return new XSelenium(commandProcessor);
    }

    public XSelenium(String serverHost, int serverPort, String browserStartCommand, String browserURL) {
        super(serverHost, serverPort, browserStartCommand, browserURL);
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

    WebDriver driver;

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

    void selectItem(Select select, String optionLocator) {
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

    void deSelectItem(Select select, String optionLocator) {
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
        By by = getByFromLocator(locator);
        return driver.findElement(by).isDisplayed();
    }

    @Override
    public boolean isEditable(String locator) {
        By by = getByFromLocator(locator);
        return driver.findElement(by).isEnabled();
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

    public Object js(String script,Object... args){
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        return executor.executeScript(script, args);
    }

    public Object jsa(String script,Object... args){
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        return executor.executeAsyncScript(script, args);
    }

    @Override
    public void captureEntirePageScreenshot(String filename, String kwargs) {
        this.captureScreenshot(filename);
    }

    @Override
    public String captureEntirePageScreenshotToString(String kwargs) {
       return ((TakesScreenshot)driver).getScreenshotAs(OutputType.BASE64);
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

    public boolean download(String url, String file){
        return Utils.copyFileFromUrl(url,file);
    }

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

    public DataSource dataSource(){
        return ProviderFactory.dataSource( driver.getCurrentUrl());
    }

    public DataMatrix table(String tableIndex) throws Exception {
        return DataMatrix.loc2matrix(tableIndex);
    }
}
