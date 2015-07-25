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

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class XMLValidator {

    /**
     * Validates a given XML file against a given schema
     *
     * @param schemaPath  Path to wso2-web schema
     * @param inputStream FileInputStream of configuration file
     * @throws IOException
     * @throws SAXException
     */
    public static void validateXML(String schemaPath, FileInputStream inputStream) throws IOException, SAXException {
        Source schemaFile = new StreamSource(new File(schemaPath));
        Source xmlFile = new StreamSource(inputStream);

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaFile);

        Validator validator = schema.newValidator();
        validator.validate(xmlFile);

    }
}
