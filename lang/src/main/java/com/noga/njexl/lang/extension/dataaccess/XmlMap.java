/*
*Copyright [2016] [Nabarun Mondal]
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

package com.noga.njexl.lang.extension.dataaccess;

import com.noga.njexl.lang.JexlArithmetic;
import com.noga.njexl.lang.ObjectContext;
import com.noga.njexl.lang.extension.TypeUtility;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by noga on 02/04/15.
 */
public class XmlMap {

    public static String toJSON(Object obj){
        if ( obj == null ) return "" ;
        if ( obj instanceof Map ){
            StringBuffer sBuf = new StringBuffer( "{" );
            Set set = ((Map) obj).entrySet() ;
            if ( !set.isEmpty() ){
                Iterator iterator = set.iterator() ;
                Object entry = iterator.next() ;
                String key = "\"" +  String.valueOf ( ((Map.Entry)entry).getKey() ) + "\"" ;
                String value = toJSON( ((Map.Entry)entry).getValue() );
                sBuf.append(key).append(" : ").append(value);
                while ( iterator.hasNext() ){
                    entry = iterator.next() ;
                    key = "\"" +  String.valueOf ( ((Map.Entry)entry).getKey() ) + "\"" ;
                    value = toJSON( ((Map.Entry)entry).getValue() );
                    sBuf.append(",").append(key).append(" : ").append(value);
                }
            }
            sBuf.append("}");
            return sBuf.toString();
        }
        if ( obj instanceof List ){
            StringBuffer sBuf = new StringBuffer( "[" );
            List l = ((List) obj) ;
            if ( !l.isEmpty() ){
                Iterator iterator = l.iterator() ;
                Object entry = iterator.next() ;
                String value = toJSON( entry );
                sBuf.append(value);
                while ( iterator.hasNext() ){
                    entry = iterator.next() ;
                    value = toJSON( entry) ;
                    sBuf.append(",").append(value);
                }
            }
            sBuf.append("]");
            return sBuf.toString();
        }
        if ( obj.getClass().isArray() ){
            StringBuffer sBuf = new StringBuffer( "[" );
            int size  = Array.getLength( obj ) ;
            int i = 0 ;
            if ( size != 0  ){
                Object entry = Array.get(obj,i++) ;
                String value = toJSON( entry );
                sBuf.append(value);
                while ( i < size ){
                    entry = Array.get(obj,i++) ;
                    value = toJSON( entry) ;
                    sBuf.append(",").append(value);
                }
            }
            sBuf.append("]");
            return sBuf.toString();
        }
        if ( obj instanceof CharSequence ){
            // TODO : must escape double quotes ...
            return "\"" + String.valueOf(obj) + "\"" ;
        }
        return String.valueOf(obj);
    }


    public static String getFirstLevelTextContent(Node node) {
        NodeList list = node.getChildNodes();
        StringBuilder textContent = new StringBuilder();
        for (int i = 0; i < list.getLength(); ++i) {
            Node child = list.item(i);
            if (child.getNodeType() == Node.TEXT_NODE)
                textContent.append(child.getTextContent());
        }
        return textContent.toString();
    }

    public static String jsonDict(Map map) {
        String tmp = "";
        for (Object k : map.keySet()) {
            String prop = k.toString();
            String value = map.get(k).toString();
            value = value.replaceAll("\n", "\\\\n");
            value = value.replaceAll("\r", "\\\\r");
            String pair = String.format("\"%s\" : \"%s\"", prop, value);
            tmp = tmp + pair + ",";
        }
        if (!tmp.isEmpty()) {
            int l = tmp.length();
            tmp = tmp.substring(0, l - 1);
        }
        tmp = "{" + tmp + "}";
        return tmp;
    }

    public static String jsonProp(String prop, String value) {
        value = value.replaceAll("\n", "\\\\n");
        value = value.replaceAll("\r", "\\\\r");
        String pair = String.format("\"%s\" : \"%s\"", prop, value);
        return pair;
    }

    public static class XmlElement {

        public static final XPath X_PATH = XPathFactory.newInstance().newXPath();

        // the dom root
        public XmlMap root;

        // the attributes
        public HashMap<String, String> attr;
        // the name
        public String name;
        // the namespace
        public String ns;
        // the prefix
        public String prefix;

        // the elements
        public ArrayList<XmlElement> children;
        // the text
        public String text;
        // parent node of me
        public XmlElement parent;
        // this is actually me
        public Node node;

        public XmlElement(Node n, XmlElement p) {
            node = n;
            parent = p;
            name = n.getNodeName();
            ns = "";
            if (n.getNamespaceURI() != null) {
                ns = n.getNamespaceURI();
            }
            prefix = "";
            if (n.getPrefix() != null) {
                prefix = n.getPrefix();
            }

            attr = new HashMap<>();
            if (n.hasAttributes()) {
                NamedNodeMap map = n.getAttributes();
                int len = map.getLength();
                for (int i = 0; i < len; i++) {
                    Node a = map.item(i);
                    String name = a.getNodeName();
                    String value = a.getTextContent();
                    attr.put(name, value);
                }
            }
            text = getFirstLevelTextContent(node);
            children = new ArrayList<>();
        }

        protected void populate(XmlMap root) {
            if (parent != null) {
                parent.children.add(this);
            }
            this.root = root;
            this.root.nodes.put(node, this);
            NodeList nodeList = node.getChildNodes();
            int count = nodeList.getLength();
            for (int i = 0; i < count; i++) {
                Node c = nodeList.item(i);
                if (c.getNodeType() == Node.ELEMENT_NODE) {
                    XmlElement child = new XmlElement(c, this);
                    child.populate(root);
                }
            }
        }

        @Override
        public String toString() {
            return json();
        }

        public NodeList nodes(String expression) throws Exception {
            NodeList nodeList = (NodeList) X_PATH.compile(expression).evaluate(
                    this.node, XPathConstants.NODESET);
            return nodeList;
        }

        public List<XmlElement> elements(String expression) throws Exception {
            NodeList nodeList = nodes(expression);
            ArrayList<XmlElement> elements = new ArrayList<>();
            int size = nodeList.getLength();
            for (int i = 0; i < size; i++) {
                Node node = nodeList.item(i);
                XmlElement e = root.nodes.get(node);
                elements.add(e);
            }
            return elements;
        }

        public Node node(String expression) throws Exception {
            Node node = (Node) X_PATH.compile(expression).evaluate(this.node, XPathConstants.NODE);
            return node;
        }

        public XmlElement element(String expression) throws Exception {
            Node node = node(expression);
            return root.nodes.get(node);
        }

        public boolean exists(String expression) throws Exception {
            boolean exists = (boolean)X_PATH.compile("boolean(" + expression + ")").evaluate(this.node,
                    XPathConstants.BOOLEAN);
            return exists ;
        }

        public String xpath(String expression) throws Exception {
            return xpath(expression,null);
        }

        public String xpath(String expression, String defaultValue) throws Exception {
            boolean exists = exists(expression);
            if ( ! exists ) return defaultValue ;
            String val = (String) X_PATH.compile(expression).evaluate(this.node, XPathConstants.STRING);
            return val;
        }

        /**
         * Converts this to JSON String
         *
         * @return json string for this element
         */
        public String json() {
            StringBuilder builder = new StringBuilder();
            builder.append("{ ");
            String prop = "";
            prop = jsonProp("name", name);
            builder.append(prop).append(" , ");
            prop = jsonProp("ns", ns);
            builder.append(prop).append(", ");
            prop = jsonProp("prefix", prefix);
            builder.append(prop).append(", ");
            prop = jsonProp("text", text);
            builder.append(prop).append(", ");
            // now the attributes
            String attrStrings = jsonDict(attr);
            builder.append("\"attr\" : ").append(attrStrings).append(", ");
            // now the children
            builder.append("\"children\" : [ ");
            if (!children.isEmpty()) {
                int i = 0;
                String ej = children.get(i).json();
                builder.append(ej);
                i++;
                for (; i < children.size(); i++) {
                    builder.append(",\n");
                    ej = children.get(i).json();
                    builder.append(ej);
                }
            }
            builder.append(" ]");
            builder.append(" }");
            return builder.toString();
        }
    }

    public final XmlElement root;

    public final Document doc;

    public final HashMap<Node, XmlElement> nodes;

    public static XmlMap file2xml(String... args) throws Exception {
        if ( args.length == 0 ) return null;
        byte[] arr = Files.readAllBytes(new File(args[0]).toPath());
        String text = new String(arr);
        String encoding = "UTF-8" ;
        if ( args.length > 1 ){
            encoding = args[1];
        }
        return string2xml(text, encoding);
    }

    public static String dict2xml(Map m){
        StringBuffer buf = new StringBuffer();
        for ( Object key : m.keySet() ){
            String elemName = String.valueOf(key) ;
            buf.append("<").append(elemName).append(">");
            String val = obj2xml( m.get(key) );
            buf.append(val);
            buf.append("</").append(elemName).append(">");
        }
        return buf.toString();
    }

    public static String array2xml(Object array){
        int len = Array.getLength(array);
        StringBuffer buf = new StringBuffer();
        for ( int i = 0 ; i < len; i++ ){
            Object o = Array.get(array,i);
            buf.append("<i>") ; // item
            String s = obj2xml(o);
            buf.append(s);
            buf.append("</i>") ; // item
        }
        return buf.toString();
    }

    public static String obj2xml(Object o)  {
        if ( o == null ) return "" ;

        if ( o instanceof Map ){
            return dict2xml((Map)o);
        }else if ( o.getClass().isArray() ){
            return array2xml(o) ;
        }else if ( o instanceof String ){
            return (String)o;
        }
        else if ( o instanceof Number ){
            return String.valueOf(o);
        }
        else if (JexlArithmetic.isTimeLike(o)){
            return String.valueOf(o);
        }
        try {
            o = TypeUtility.makeDict(o);
            return dict2xml((Map)o);
        }catch (Exception e){
            return String.valueOf(o);
        }
    }

    public static String xml(Object o){
        String val = obj2xml(o);
        return "<root>" + val + "</root>" ;
    }

    public static XmlMap string2xml(String... args) throws Exception {
        if ( args.length == 0 ) return null;
        // this is funny ...
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        String encoding = "UTF-8" ;
        if ( args.length > 1 ){
            encoding = args[1];
        }
        Document doc = db.parse(new ByteArrayInputStream(args[0].getBytes(encoding)));
        doc.getDocumentElement().normalize();
        return new XmlMap(doc);
    }

    public XmlMap(Document doc) {
        this.doc = doc;
        root = new XmlElement(doc.getDocumentElement(), null);
        nodes = new HashMap<>();
        root.populate(this);
    }

    public XmlElement element(String expression) throws Exception {
        return root.element(expression);
    }

    public List<XmlElement> elements(String expression) throws Exception {
        return root.elements(expression);
    }

    public String xpath(String expression) throws Exception {
        return xpath(expression,null);
    }

    public String xpath(String expression, String defaultValue) throws Exception {
        return root.xpath(expression,defaultValue);
    }

    public boolean exists(String expression) throws Exception {
        return root.exists(expression);
    }

    /**
     * Converts this to JSON String
     *
     * @return json string for this xml
     */
    public String json() {
        return root.json();
    }

    @Override
    public String toString() {
        return json();
    }
}
