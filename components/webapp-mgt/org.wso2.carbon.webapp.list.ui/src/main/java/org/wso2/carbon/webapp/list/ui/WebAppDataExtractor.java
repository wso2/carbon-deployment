/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.webapp.list.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WebAppDataExtractor {

    private Map<String, String> jaxWSMap = new HashMap<String, String>();
    private Map<String, String> jaxRSMap = new HashMap<String, String>();
    private String serviceListPath = "";
    private String cxfConfigFileLocation = "WEB-INF/cxf-servlet.xml";
    private String jaxservletUrlPattern = "services";

    private static final Log log = LogFactory.getLog(WebAppDataExtractor.class);

    public Map<String, String> getJaxWSMap() {
        return jaxWSMap;
    }

    public void setJaxWSMap(Map<String, String> jaxWSMap) {
        this.jaxWSMap = jaxWSMap;
    }

    public void getServletXML(InputStream inputStream) {
        jaxWSMap.clear();
        jaxRSMap.clear();
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        try {
            ZipEntry entry;

            HashMap<String, byte[]> map = new HashMap<String, byte[]>();
            while ((entry = zipInputStream.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buff = new byte[1024];
                int count;

                while ((count = zipInputStream.read(buff)) != -1) {
                    baos.write(buff, 0, count);
                }

                String filename = entry.getName();
                byte[] bytes = baos.toByteArray();
                map.put(filename, bytes);
            }

            String webXmlString = stripNonValidXMLCharacters(new String(map.get("WEB-INF/web.xml")));

            processWebXml(webXmlString);

            String configFile = "";

            if (map.containsKey(cxfConfigFileLocation)) {
                configFile = new String(map.get(cxfConfigFileLocation));
            } else {
                return;
            }

            configFile = stripNonValidXMLCharacters(configFile);
            OMElement element = AXIOMUtil.stringToOM(configFile);

            Iterator<OMElement> iterator = element.getChildrenWithName(new QName(
                    "http://cxf.apache.org/jaxws", "endpoint"));
            while (iterator.hasNext()) {
                OMElement temp = iterator.next();
                jaxWSMap.put(temp.getAttribute(new QName("id"))
                        .getAttributeValue(),
                        temp.getAttribute(new QName("address"))
                                .getAttributeValue());

            }

            iterator = element.getChildrenWithName(new QName(
                    "http://cxf.apache.org/jaxws", "server"));
            while (iterator.hasNext()) {
                OMElement temp = iterator.next();
                jaxWSMap.put(temp.getAttribute(new QName("id"))
                        .getAttributeValue(),
                        temp.getAttribute(new QName("address"))
                                .getAttributeValue());

            }

            iterator = element.getChildrenWithName(new QName(
                    "http://cxf.apache.org/jaxrs", "server"));
            while (iterator.hasNext()) {
                OMElement temp = iterator.next();
                jaxRSMap.put(temp.getAttribute(new QName("id"))
                        .getAttributeValue(),
                        temp.getAttribute(new QName("address"))
                                .getAttributeValue());

            }

            setServiceListPath(processServiceListPathWebXml(webXmlString));

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            try {
                zipInputStream.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }

    private static String stripNonValidXMLCharacters(String in) {
        StringBuilder out = new StringBuilder(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in)))
            return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught
            // here; it should not happen.
            if ((current == 0x9) || (current == 0xA) || (current == 0xD)
                    || ((current >= 0x20) && (current <= 0xD7FF))
                    || ((current >= 0xE000) && (current <= 0xFFFD))
                    || ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }

    public List getWSDLs(String serverURL) {
        List<String> list = new ArrayList<String>();
        Iterator<String> iterator = jaxWSMap.keySet().iterator();
        while (iterator.hasNext()) {
            list.add(serverURL + jaxservletUrlPattern + jaxWSMap.get(iterator.next()) + "?wsdl");
        }
        if (list.size() == 0) {
            return null;
        }
        return list;
    }

    public List getWADLs(String serverURL) {
        List<String> list = new ArrayList<String>();
        Iterator<String> iterator = jaxRSMap.keySet().iterator();
        while (iterator.hasNext()) {
            list.add(serverURL + jaxservletUrlPattern + jaxRSMap.get(iterator.next()) + "?_wadl");
        }
        if (list.size() == 0) {
            return null;
        }
        return list;
    }

    public String getServiceListPath() {
        return serviceListPath;
    }
    
    private void setServiceListPath(String serviceListPathInitParam) {
        boolean emptyAddress = false;
        for(String address : jaxRSMap.values()) {
            if ("/".equals(address)) {
                emptyAddress = true;
            }
        }

        for(String address : jaxWSMap.values()) {
            if ("/".equals(address)) {
                emptyAddress = true;
            }
        }

        if(emptyAddress) {
            serviceListPath = !"".equals(serviceListPathInitParam) ? serviceListPathInitParam : "/services";
        } else {
            serviceListPath = serviceListPathInitParam;
        }
    }

    /**
     * This method reads the web.xml file and seach for whether cxf configuration file is included
     *
     * @param stream stream
     * @return cxf file localtion
     */

    private void processWebXml(String stream) {

        try {
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(stream));

            DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document doc = b.parse(is); //doc.getDomConfig().setParameter();

            XPath xPath = XPathFactory.newInstance().newXPath();
            String configLocationParam = xPath.evaluate(
                    "/web-app/servlet/init-param[param-name[contains(text(),'config-location')]]/param-value/text()",
                    doc.getDocumentElement());
            if(configLocationParam == null || configLocationParam == ""){
                configLocationParam = xPath.evaluate(
                        "/web-app/context-param[param-name[contains(text(),'contextConfigLocation')]]/param-value/text()",
                        doc.getDocumentElement());
            }
            String cxfServletName = xPath.evaluate(
                    "/web-app/servlet[servlet-class[contains(text(),'org.apache.cxf.transport.servlet.CXFServlet')]]/servlet-name/text()",
                    doc.getDocumentElement());
            String jaxservletUrlPattern = xPath.evaluate(
                    "/web-app/servlet-mapping[servlet-name/text()=\"" + cxfServletName + "\"]/url-pattern/text()",
                    doc.getDocumentElement());

            if (!"".equals(configLocationParam) && configLocationParam != null ) {
                cxfConfigFileLocation = configLocationParam;
            } else {
                cxfConfigFileLocation = "WEB-INF/cxf-servlet.xml";
            }
            if (!"".equals(jaxservletUrlPattern) && jaxservletUrlPattern != null ) {
                if (jaxservletUrlPattern.endsWith("/*")) {
                    jaxservletUrlPattern = jaxservletUrlPattern.substring(0, jaxservletUrlPattern.length() - 2);
                }
                if (jaxservletUrlPattern.startsWith("/")) {
                    jaxservletUrlPattern = jaxservletUrlPattern.substring(1);
                }
                this.jaxservletUrlPattern = jaxservletUrlPattern;
            } else {
                this.jaxservletUrlPattern = "services";
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * This method reads the web.xml file and seach for whether service-list-path init param is set
     *
     * @param stream stream
     * @return cxf file localtion
     */

    private String processServiceListPathWebXml(String stream) {

        try {
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(stream));

            DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document doc = b.parse(is);

            XPath xPath = XPathFactory.newInstance().newXPath();
            String serviceListPathParam = xPath.evaluate(
                    "/web-app/servlet/init-param[param-name[contains(text(), 'service-list-path')]]/param-value/text()",
                    doc.getDocumentElement());

            if (!"".equals(serviceListPathParam) && serviceListPathParam != null ) {
                return serviceListPathParam;
            } else {
                return "";
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return "";
        }
    }


}
