package com.noga.njexl.testing;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by noga on 16/04/15.
 */
@XStreamAlias("testSuite")
public class TestSuite {

    public String location ;

    @XStreamAsAttribute
    public String version;

    @XStreamAlias("source")
    public static class DataSource{

        public static final String TEST_ID = "#testId#" ;

        public static final String TEST_ENABLE = "#enable#" ;

        @XStreamAsAttribute
        public String name;

        @XStreamAsAttribute
        public String location;

        @XStreamAsAttribute
        public String testIdColumn;

        @XStreamAsAttribute
        public String testEnableColumn;

        public DataSource(){
            name="";
            location="";
            testIdColumn = TEST_ID ;
            testEnableColumn = TEST_ENABLE ;
        }
    }

    public static class ObjectInit{

        @XStreamAsAttribute
        public String name;

        @XStreamAsAttribute
        public String type;

        @XStreamImplicit(itemFieldName = "param")
        public ArrayList<String> params;

        public ObjectInit(){
            name="";
            type="";
            params = new ArrayList<>();
        }
    }

    @XStreamAlias("reporter")
    public static class Reporter extends ObjectInit{}

    public static class BaseFeature{

        @XStreamAsAttribute
        public String name ;

        @XStreamAsAttribute
        public String ds ;

        @XStreamAsAttribute
        public String table ;

        @XStreamAsAttribute
        public String owner ;

        @XStreamAsAttribute
        public boolean enabled ;

        @XStreamAsAttribute
        public String script ;

        @XStreamAsAttribute
        public String beforeScript ;

        @XStreamAsAttribute
        public String afterScript ;

        public BaseFeature(){
            name = "" ;
            ds = "" ;
            table = "" ;
            owner = "" ;
            enabled = true ;
            script = "";
            beforeScript = "" ;
            afterScript = "" ;
        }

    }

    public static class Application{

        @XStreamAsAttribute
        public String name;

        @XStreamAsAttribute
        public String build;

        @XStreamAsAttribute
        public String scriptDir;

        @XStreamAsAttribute
        public String logs;


        @XStreamImplicit(itemFieldName = "feature")
        public ArrayList<BaseFeature> features;

        public Application(){
            features = new ArrayList<>();
            name= "";
            build = "" ;
            scriptDir ="" ;
            logs = "" ;
        }
    }

    public ArrayList<DataSource> dataSources;

    public ArrayList<Reporter> reporters;

    public TestSuite(){
        location = "" ;
        version = "0.1";
        dataSources = new ArrayList<>();
        reporters = new ArrayList<>();
    }

    protected static <T extends TestSuite> T loadFrom(Class c , String xmlFile) throws Exception{

        if ( !TestSuite.class.isAssignableFrom(c)){
            throw new Exception("Sorry pal, [" + c + "] is not a TestSuite!" );
        }
        XStream xStream = new XStream(new PureJavaReflectionProvider());
        xStream.alias("testSuite", c);
        xStream.autodetectAnnotations(true);
        String xml = Utils.readToEnd(xmlFile);
        String location = new File(xmlFile).getCanonicalPath();
        location = location.replace('\\','/');
        String dir = location.substring(0, location.lastIndexOf("/"));
        // do the magical relocation here
        xml = Utils.relocatePathInXml(dir, xml);
        T obj = (T)xStream.fromXML(xml);
        obj.location = location ;
        return  obj;
    }
}
