package noga.commons.njexl.testing.ui;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import noga.commons.njexl.testing.TestSuite;
import noga.commons.njexl.testing.Utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by noga on 15/04/15.
 */

@XStreamAlias("testSuite")
public class WebTestSuite extends TestSuite{

    @XStreamAsAttribute
    public XSelenium.BrowserType browserType;

    @XStreamAlias("webApp")
    public static class WebApplication{

        @XStreamAsAttribute
        public boolean enabled;

        @XStreamAsAttribute
        public String name;

        @XStreamAsAttribute
        public String build;

        @XStreamAsAttribute
        public String url;

        @XStreamAsAttribute
        public String scriptDir;

        @XStreamImplicit(itemFieldName = "feature")
        public ArrayList<BaseFeature> features;

        public WebApplication(){
            features = new ArrayList<>();
            enabled = true ;
            name= "";
            build = "" ;
            url = "" ;
            scriptDir ="" ;
        }
    }

    public ArrayList<WebApplication> tests;

    public WebTestSuite(){
        browserType = XSelenium.BrowserType.FIREFOX ;
        tests = new ArrayList<>();
    }

    public static WebTestSuite loadFrom(String xmlFile) throws Exception{
        XStream xStream = new XStream(new PureJavaReflectionProvider());
        xStream.alias("testSuite", WebTestSuite.class);

        xStream.autodetectAnnotations(true);
        String xml = Utils.readToEnd(xmlFile);
        String location = new File(xmlFile).getCanonicalPath();
        location = location.replace('\\','/');
        String dir = location.substring(0, location.lastIndexOf("/"));
        // do the magical relocation here
        xml = Utils.relocatePathInXml(dir, xml);
        WebTestSuite obj = (WebTestSuite)xStream.fromXML(xml);
        obj.location = location ;
        return  obj;
    }
}
