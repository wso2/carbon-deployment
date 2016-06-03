/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.webapp.mgt.config;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 * This class unmarshals an XML input stream.
 */
public class JAXBUnmarshaller {

    /**
     * Unmarshals an XML InputStream
     *
     * @param stream an xml input stream
     * @return The WebAppConfigData object created from the input stream
     */
    public static WebAppConfigurationData unmarshall(InputStream stream)
            throws JAXBException, IOException, SAXException {
        String xmlString = IOUtils.toString(stream, WebAppConfigurationConstants.ENCODING);

        XMLValidator.validateXML(xmlString);

        JAXBContext jaxbContext = JAXBContext.newInstance(WebAppConfigurationData.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        return (WebAppConfigurationData) jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(xmlString)));
    }

}
