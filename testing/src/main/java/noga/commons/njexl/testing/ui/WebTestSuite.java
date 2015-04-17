package noga.commons.njexl.testing.ui;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import noga.commons.njexl.testing.TestSuite;
import java.util.ArrayList;

/**
 * Created by noga on 15/04/15.
 */

@XStreamAlias("testSuite")
public class WebTestSuite extends TestSuite{

    @XStreamAsAttribute
    public XSelenium.BrowserType browserType;

    @XStreamAlias("webApp")
    public static class WebApplication extends Application{

        @XStreamAsAttribute
        public String url;

        public WebApplication(){
            url = "" ;
        }
    }

    public WebApplication webApp;

    public WebTestSuite(){
        browserType = XSelenium.BrowserType.FIREFOX ;
        webApp = new WebApplication();
    }

    public static WebTestSuite loadFrom(String file) throws Exception{
        return loadFrom(WebTestSuite.class , file);
    }

}
