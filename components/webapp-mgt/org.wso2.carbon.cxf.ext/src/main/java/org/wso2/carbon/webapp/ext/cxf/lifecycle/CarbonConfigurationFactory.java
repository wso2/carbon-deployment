/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.webapp.ext.cxf.lifecycle;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.webapp.ext.cxf.Constants;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;


/**
 * Load CarbonConfiguration from carbon.xml and keep it as a static reference.
 */
public class CarbonConfigurationFactory {

    private static CarbonConfiguration carbonConfiguration = null;

    public static CarbonConfiguration getCurrentCarbonConfiguration() {
        if (carbonConfiguration == null) {
            try {
                initCarbonConfiguration();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return carbonConfiguration;

    }

    private static void initCarbonConfiguration() throws Exception {

        String carbonHome = System.getProperty(Constants.CARBON_CONFIG_DIR_PATH);
        if (carbonHome != null) {

            File carbonXml = new File(carbonHome, Constants.CARBON_CONFIG_FILE_NAME);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(new FileInputStream(carbonXml));
            carbonConfiguration = new CarbonConfiguration();


            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = Constants.CARBON_HOSTNAME_ENTRY_XPATH;
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            if (nodeList.getLength() > 0) {
                Element hostName = (Element) nodeList.item(0);
                carbonConfiguration.getParameters().put(Constants.CARBON_HOSTNAME_PARAMETER, hostName.getTextContent());

            }


        }
    }
}
