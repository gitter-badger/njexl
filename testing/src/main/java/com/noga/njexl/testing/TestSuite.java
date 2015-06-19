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

package com.noga.njexl.testing;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

import java.io.File;
import java.util.ArrayList;

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

    public static class Feature {

        @XStreamAsAttribute
        public String name ;

        @XStreamAsAttribute
        public String base ;

        @XStreamAsAttribute
        public String method ;

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

        public Feature(){
            name = "" ;
            base = "" ;
            method = "" ;
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
        public ArrayList<Feature> features;

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
