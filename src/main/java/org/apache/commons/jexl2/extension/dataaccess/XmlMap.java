package org.apache.commons.jexl2.extension.dataaccess;

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
                    String name = a.getLocalName();
                    String value = a.getTextContent();
                    attr.put(name,value);
                }
            }
            text = getFirstLevelTextContent(node);
            child = new ArrayList<>();
        }

    }

    public XmlElement root;

    public Document doc;

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
}
