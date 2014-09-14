/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.as.monitoring.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class reads the repository/conf/etc/bam-publisher.xml file and create StreamConfigurationContext
 */
public class StreamConfigurationReader {


    public static final String CONNECTOR_DATA_STREAM_NAME = "monitoring.connector";
    public static final String WEBAPP_RESOURCE_STREAM_NAME = "monitoring.webapp.resource";
    public static final String HTTP_DATA_STREAM_NAME = "monitoring.webapp.calls";
    public static final String CONFIG_XML_FILE = "bam-publisher.xml";
    public static final String COULD_NOT_LOAD_CONFIGURATION = "could not load the " + CONFIG_XML_FILE + " configuration";
    private static final Log LOG = LogFactory.getLog(StreamConfigurationReader.class);
    private static final String XPATH_BASE = "//bamPublisher/streams/stream[@id='%s']/%s";
    private static volatile StreamConfigurationReader streamConfigurationReader;
    private DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    private XPath xPath = XPathFactory.newInstance().newXPath();
    private Document document;
    private Map<String, StreamConfigContext> configCache = new ConcurrentHashMap<String, StreamConfigContext>();

    /**
     * Instantiation not needed. Private constructor to avoid.
     */
    private StreamConfigurationReader() throws BAMPublisherConfigurationException {
        try {
            String bamConfigPath = CarbonUtils.getEtcCarbonConfigDirPath() +
                                   File.separator + CONFIG_XML_FILE;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Config File Path = " + bamConfigPath);
            }
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            document = builder.parse(bamConfigPath);

        } catch (ParserConfigurationException e) {
            throw new BAMPublisherConfigurationException(COULD_NOT_LOAD_CONFIGURATION, e);
        } catch (SAXException e) {
            throw new BAMPublisherConfigurationException(COULD_NOT_LOAD_CONFIGURATION, e);
        } catch (IOException e) {
            throw new BAMPublisherConfigurationException(COULD_NOT_LOAD_CONFIGURATION, e);
        }
    }

    /**
     * @return
     * @throws BAMPublisherConfigurationException
     */
    public static StreamConfigurationReader getInstance()
            throws BAMPublisherConfigurationException {
        if (streamConfigurationReader == null) {
            synchronized (StreamConfigurationReader.class) {
                if (streamConfigurationReader == null) {
                    streamConfigurationReader = new StreamConfigurationReader();
                    LOG.debug("StreamConfigurationReader successfully parsed the config XML.");
                }
            }
        }

        return streamConfigurationReader;
    }

    /**
     * Read the properties for a given stream configuration.
     * The stream configuration comes in the the following format.
     * <pre>
     * {@code <bamPublisher>
     *     <streams>
     *         <stream name="ConnectorDataStream">
     *         <enabled>true</enabled>
     *         <nickName></nickName>
     *         <streamVersion></streamVersion>
     *         <username></username>
     *         <password></password>
     *         <receiverUrl></receiverUrl>
     *         <description></description>
     *         </stream>
     *      </streams>
     * </bamPublisher>
     * }
     * </pre>
     */
    public StreamConfigContext getStreamConfiguration(String id)
            throws BAMPublisherConfigurationException {
        if (configCache.containsKey(id)) {
            LOG.debug("StreamConfiguration returning from the Cache.");
            return configCache.get(id);
        }

        StreamConfigContext streamConfigContext = new StreamConfigContext();

        String value = getValue(id, "enabled");
        streamConfigContext.setEnabled(Boolean.parseBoolean(value));

        streamConfigContext.setStreamName(getValue(id, "streamName"));
        streamConfigContext.setNickName(getValue(id, "nickName"));
        streamConfigContext.setStreamVersion(getValue(id, "streamVersion"));
        streamConfigContext.setUsername(getValue(id, "username"));
        streamConfigContext.setPassword(getValue(id, "password"));
        streamConfigContext.setReceiverUrl(getValue(id, "receiverUrl"));
        streamConfigContext.setDescription(getValue(id, "description"));

        configCache.put(id, streamConfigContext);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Configuration for Stream " + id + " is loaded to the cache successfully.");
        }
        return streamConfigContext;

    }

    private String getValue(String id, String property)
            throws BAMPublisherConfigurationException {
        String xPathQuery = String.format(XPATH_BASE, id, property);
        try {
            return xPath.compile(xPathQuery).evaluate(document);
        } catch (XPathExpressionException e) {
            final String message = "'" + property + "' of Stream '" + id + "' cannot be read from " + CONFIG_XML_FILE + ".";
            throw new BAMPublisherConfigurationException(message, e);
        }
    }

}

