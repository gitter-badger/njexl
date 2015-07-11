/*
*Copyright [2015] [Nabarun Mondal]
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

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by noga on 02/04/15.
 */
public class XmlMap {

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

    public static String jsonDict(Map map){
        String tmp = "" ;
        for ( Object k : map.keySet() ) {
            String prop = k.toString() ;
            String value = map.get(k).toString() ;
            value = value.replaceAll("\n", "\\\\n");
            value = value.replaceAll("\r", "\\\\r");
            String pair = String.format("\"%s\" : \"%s\"", prop, value);
            tmp = tmp + pair + "," ;
        }
        if ( !tmp.isEmpty() ) {
            int l = tmp.length();
            tmp = tmp.substring(0, l - 1);
        }
        tmp = "{" + tmp + "}" ;
        return tmp;
    }

    public static String jsonProp(String prop, String value){
        value = value.replaceAll("\n", "\\\\n");
        value = value.replaceAll("\r", "\\\\r");
        String pair = String.format("\"%s\" : \"%s\"", prop,value);
        return pair ;
    }

    public static class XmlElement {
        // the attributes
        public HashMap<String,String> attr;
        // the name
        public String name;
        // the namespace
        public String ns;
        // the prefix
        public String prefix;

        // the elements
        public ArrayList<XmlElement> child;
        // the text
        public String text;
        // parent node of me
        public XmlElement parent;
        // this is actually me
        public Node node;

        public XmlElement(Node n, XmlElement p){
            node = n ;
            parent = p ;
            name = n.getNodeName();
            ns = "";
            if ( n.getNamespaceURI()!= null ){
                ns = n.getNamespaceURI();
            }
            prefix = "" ;
            if ( n.getPrefix() != null ){
                prefix = n.getPrefix();
            }

            attr = new HashMap<>();
            if ( n.hasAttributes() ){
                NamedNodeMap map =  n.getAttributes();
                int len = map.getLength();
                for ( int i = 0 ; i < len; i++){
                    Node a = map.item(i);
                    String name = a.getNodeName();
                    String value = a.getTextContent();
                    attr.put(name,value);
                }
            }
            text = getFirstLevelTextContent(node);
            child = new ArrayList<>();
        }

        @Override
        public String toString(){
            return json();
        }

        /**
         * Converts this to JSON String
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
            if ( !child.isEmpty() ){
                int i = 0 ;
                String ej = child.get(i).json();
                builder.append(ej);
                i++;
                for ( ; i < child.size(); i++ ){
                    builder.append(",\n");
                    ej = child.get(i).json();
                    builder.append(ej);
                }
            }
            builder.append(" ]");
            builder.append( " }" );
            return builder.toString();
        }
    }

    public final XmlElement root;

    public final Document doc;

    public static XmlMap file2xml(String file) throws Exception{
        byte[] arr = Files.readAllBytes(new File(file).toPath());
        String text = new String(arr);
        return string2xml(text);
    }

    public static XmlMap string2xml(String text) throws Exception{
        // this is funny ...
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(text.getBytes("UTF-8")));
        doc.getDocumentElement().normalize();
        return new XmlMap(doc);
    }

    protected void populate(Node n,XmlElement parent){
        XmlElement me = new XmlElement(n,parent);
        parent.child.add(me);
        NodeList nodeList = n.getChildNodes();
        int count = nodeList.getLength();
        for ( int i = 0 ; i < count; i++) {
            Node c = nodeList.item(i);
            if ( c.getNodeType() == Node.ELEMENT_NODE) {
                populate(c,me);
            }
        }
    }

    public XmlMap(Document doc){
        this.doc = doc ;
        root = new XmlElement(doc.getDocumentElement(),null);
        NodeList nodeList = doc.getDocumentElement().getChildNodes();
        int count = nodeList.getLength();
        for ( int i = 0 ; i < count; i++) {
            Node c = nodeList.item(i);
            if ( c.getNodeType() == Node.ELEMENT_NODE ) {
                populate(c,root);
            }
        }
    }

    /**
     * Converts this to JSON String
     * @return json string for this xml
     */
    public String json(){
       return root.json();
    }

    @Override
    public String toString(){
        return json();
    }
}
