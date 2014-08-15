package org.wso2.carbon.webapp.mgt.metadata;

import org.w3c.dom.*;
import org.wso2.carbon.webapp.mgt.WebappsConstants;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sagara on 7/29/14.
 */
public class MetadataReader {

    private Map<String, Object> metadata;


    private MetadataReader() {

        metadata = new HashMap<String, Object>();


    }


    public static MetadataReader getInstance() {

        return new MetadataReader();

    }


    public Map<String, Object> readApplicationDescriptor(ServletContext servletContext) {

        Document appDesc = loadApplicationDescriptor(servletContext);
        if (appDesc != null) {
            processDocument(appDesc);
        }
        return metadata;
    }

    private void processDocument(Document document){
        Element applicationEle = document.getDocumentElement();
        if (WebappsConstants.APPLICATION_ELE.equals(applicationEle.getTagName())) {
            NodeList elements = applicationEle.getChildNodes();
            for (int i = 0; i < elements.getLength(); i++) {
                Node node = elements.item(i);
                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    String elementName = node.getNodeName();
                    processNode(elementName, (Element) node);

                }
            }

        } else {

            throw  new RuntimeException("Unexepted element in application.xml file");
        }

    }

    private void processNode(String elementName, Element element) {

        String key = elementName;
        NamedNodeMap namedNodeMap = element.getAttributes();
        //If 'id' attribute present append to node name to make unique key
        Node idNode = namedNodeMap.getNamedItem(WebappsConstants.APPLICATION_ID_ATTR);
        if (idNode != null) {
            String attrValue = ((Attr) idNode).getValue();
            if (attrValue != null && !"".equals(attrValue)) {
                key = key + "." + attrValue;
            }
        }
        processAttributes(key, namedNodeMap);
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (Node.ELEMENT_NODE == node.getNodeType()) {
                processNode(key + "." + node.getNodeName(), (Element) node);
            }

        }


    }

    private void processAttributes(String nodeName, NamedNodeMap namedNodeMap) {

        if (namedNodeMap != null) {
            for (int i = 0; i < namedNodeMap.getLength(); i++) {
                Attr attr = (Attr) namedNodeMap.item(i);
                String attrName = attr.getName();
                //Don't add "id" attribute
                if (!WebappsConstants.APPLICATION_ID_ATTR.equals(attrName)) {
                    metadata.put(nodeName  + "." + attrName, attr.getValue());
                }
            }
        }
    }


    private Document loadApplicationDescriptor(ServletContext servletContext) {

        //Load "META-INF/application.xml" file.
        InputStream in = servletContext.getResourceAsStream(WebappsConstants.APPLICATION_DESCRIPTOR_FILE);
        //Proceed if file exists.
        if (in != null) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                return builder.parse(in);
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;

    }


}
