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

package org.wso2.carbon.protobuf.registry.utils;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.protobuf.registry.BinaryServiceRegistry;
import org.xml.sax.SAXException;

/*
 * This class reads server configuration information from pbs xml
 * pbs xml should be places inside AS's components/repository/lib directory
 */
public class ServerConfig {

	private static Logger log = LoggerFactory.getLogger(BinaryServiceRegistry.class);

	private static String pbsxmlPath = System.getProperty(CarbonBaseConstants.CARBON_HOME)+File.separator+"repository"+File.separator+"conf"+File.separator+"etc"+File.separator+"pbs.xml";

	private static String pbsSchemaPath = System.getProperty(CarbonBaseConstants.CARBON_HOME)+File.separator+"repository"+File.separator+"conf"+File.separator+"etc"+File.separator+"pbsSchema.xsd";

	public ServerConfig() {
		init();
	}

	private boolean startUpFailed = false;
	private boolean enablePbs;
	private String hostName;
	private int serverPort;
	private int serverCallExecutorCorePoolSize;
	private int serverCallExecutorMaxPoolSize;
	private int serverCallExecutorMaxPoolTimeout;

	public int getServerCallExecutorMaxPoolTimeout() {
		return serverCallExecutorMaxPoolTimeout;
	}

	private int timeoutExecutorCorePoolSize;
	private int timeoutExecutorMaxPoolSize;
	private int timeoutExecutorPoolKeepAliveTime;

	private int timeoutCheckerCorePoolSize;
	private int timeoutPeriod;

	private int acceptorsPoolSize;
	private int acceptorsSendBufferSize;
	private int acceptorsRecieveBufferSize;

	private int channelHandlersPoolSize;
	private int channelHandlersSendBufferSize;
	private int channelHandlersRecieveBufferSize;

	private boolean TCP_NODELAY;

	private boolean enableSSL;
	private String keystorePassword;
	private String keystorePath;
	private String truststorePassword;
	private String truststorePath;
	
	private boolean logReqProto;
	private boolean logResProto;
	private boolean logEventProto;

	public boolean isLogReqProto() {
		return logReqProto;
	}

	public boolean isLogResProto() {
		return logResProto;
	}

	public boolean isLogEventProto() {
		return logEventProto;
	}

	public boolean isEnablePbs() {
		return enablePbs;
	}

	public boolean isStartUpFailed() {
		return startUpFailed;
	}

	public String getHostName() {
		return hostName;
	}

	public int getServerPort() {
		return serverPort;
	}

	public int getServerCallExecutorCorePoolSize() {
		return serverCallExecutorCorePoolSize;
	}

	public int getServerCallExecutorMaxPoolSize() {
		return serverCallExecutorMaxPoolSize;
	}

	public int getTimeoutExecutorCorePoolSize() {
		return timeoutExecutorCorePoolSize;
	}

	public int getTimeoutExecutorMaxPoolSize() {
		return timeoutExecutorMaxPoolSize;
	}

	public int getTimeoutExecutorPoolKeepAliveTime() {
		return timeoutExecutorPoolKeepAliveTime;
	}

	public int getTimeoutCheckerCorePoolSize() {
		return timeoutCheckerCorePoolSize;
	}

	public int getTimeoutPeriod() {
		return timeoutPeriod;
	}

	public int getAcceptorsPoolSize() {
		return acceptorsPoolSize;
	}

	public int getAcceptorsSendBufferSize() {
		return acceptorsSendBufferSize;
	}

	public int getAcceptorsRecieveBufferSize() {
		return acceptorsRecieveBufferSize;
	}

	public int getChannelHandlersPoolSize() {
		return channelHandlersPoolSize;
	}

	public int getChannelHandlersSendBufferSize() {
		return channelHandlersSendBufferSize;
	}

	public int getChannelHandlersRecieveBufferSize() {
		return channelHandlersRecieveBufferSize;
	}

	public boolean isTCP_NODELAY() {
		return TCP_NODELAY;
	}

	public boolean isEnableSSL() {
		return enableSSL;
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}

	public String getKeystorePath() {
		return keystorePath;
	}

	public String getTruststorePassword() {
		return truststorePassword;
	}

	public String getTruststorePath() {
		return truststorePath;
	}

	/*
	 * Getting server configuration information from pbsconfig.xml
	 */

	private void init() {

		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			// Leave off validation, and turn off namespaces
			factory.setValidating(false);
			factory.setNamespaceAware(false);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(pbsxmlPath));

			// if( !(isValidXML(doc, pbsSchemaPath))) {
			// this.startUpFailed = true;
			// return;
			// }

			doc.getDocumentElement().normalize();

			this.enablePbs = Boolean.parseBoolean(doc.getElementsByTagName(ServerConfigXMLConstants.ENABLE_PBS).item(0).getTextContent());

			// return if pbs is not needed
			if (!this.enablePbs) {
				return;
			}

			NodeList serverSettingsList = doc.getElementsByTagName(ServerConfigXMLConstants.SERVER_SETTINGS);
			for (int temp = 0; temp < serverSettingsList.getLength(); temp++) {

				Node nNode = serverSettingsList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					this.hostName = eElement.getElementsByTagName(ServerConfigXMLConstants.HOST_NAME).item(0).getTextContent();
					log.debug("Host Name					:" + this.hostName);
					this.serverPort = Integer.parseInt(eElement.getElementsByTagName(ServerConfigXMLConstants.SERVER_PORT).item(0).getTextContent());
					log.debug("Server Port				:" + this.serverPort);

					this.enableSSL = Boolean.parseBoolean(eElement.getElementsByTagName(ServerConfigXMLConstants.ENABLE_SSL).item(0).getTextContent());
					log.debug("Enable SSL					:" + this.enableSSL);

					if (this.enableSSL) {
						ServerConfiguration configuration = ServerConfiguration.getInstance();
						this.keystorePath = configuration.getFirstProperty("Security.KeyStore.Location");
						this.keystorePassword = configuration.getFirstProperty("Security.KeyStore.Password");
						this.truststorePath = configuration.getFirstProperty("Security.TrustStore.Location");
						this.truststorePassword = configuration.getFirstProperty("Security.TrustStore.Password");
					}

					NodeList callExecutorList = eElement.getElementsByTagName(ServerConfigXMLConstants.SERVER_CALL_EXECUTOR_THREADPOOL);
					Element callExecutorElements = (Element) callExecutorList.item(0);
					this.serverCallExecutorCorePoolSize = Integer.parseInt(callExecutorElements.getElementsByTagName(ServerConfigXMLConstants.CORE_POOL_SIZE).item(0).getTextContent());
					log.debug("Server Call Executor Core Pool Size	:" + this.serverCallExecutorCorePoolSize);

					this.serverCallExecutorMaxPoolSize = Integer.parseInt(callExecutorElements.getElementsByTagName(ServerConfigXMLConstants.MAX_POOL_SIZE).item(0).getTextContent());
					log.debug("Server Call Executor Max Pool Size		:" + this.serverCallExecutorMaxPoolSize);

					this.serverCallExecutorMaxPoolTimeout = Integer.parseInt(callExecutorElements.getElementsByTagName(ServerConfigXMLConstants.SERVER_CALL_EXECUTOR_MAX_THREADPOOL_TIMEOUT).item(0).getTextContent());
					log.debug("Server Call Executor Max Pool Timeout		:" + this.serverCallExecutorMaxPoolTimeout);

					NodeList timeoutExecutorList = eElement.getElementsByTagName(ServerConfigXMLConstants.TIMEOUT_EXECUTOR_THREADPOOL);
					Element timeoutExecutorElements = (Element) timeoutExecutorList.item(0);
					this.timeoutExecutorCorePoolSize = Integer.parseInt(timeoutExecutorElements.getElementsByTagName(ServerConfigXMLConstants.CORE_POOL_SIZE).item(0).getTextContent());
					log.debug("Timeout Executor Core Pool Size		:" + this.timeoutExecutorCorePoolSize);

					this.timeoutExecutorMaxPoolSize = Integer.parseInt(timeoutExecutorElements.getElementsByTagName(ServerConfigXMLConstants.MAX_POOL_SIZE).item(0).getTextContent());

					log.debug("Timeout Executor Max Pool Size		:" + this.timeoutExecutorMaxPoolSize);

					this.timeoutExecutorPoolKeepAliveTime = Integer.parseInt(timeoutExecutorElements.getElementsByTagName(ServerConfigXMLConstants.TIMEOUT_EXECUTOR_THREADPOOL_KEEP_ALIVE_TIME).item(0).getTextContent());

					log.debug("Timeout Executor KeepAliveTime		:" + this.timeoutExecutorPoolKeepAliveTime);

					NodeList timeoutCheckerList = eElement.getElementsByTagName(ServerConfigXMLConstants.TIMEOUT_CHECKER_THREADPOOL);
					Element timeoutCheckerElements = (Element) timeoutCheckerList.item(0);
					this.timeoutCheckerCorePoolSize = Integer.parseInt(timeoutCheckerElements.getElementsByTagName(ServerConfigXMLConstants.CORE_POOL_SIZE).item(0).getTextContent());
					log.debug("Timeout Checker Core Pool Size		:" + this.timeoutCheckerCorePoolSize);

					this.timeoutPeriod = Integer.parseInt(timeoutCheckerElements.getElementsByTagName(ServerConfigXMLConstants.TIMEOUT_PERIOD).item(0).getTextContent());
					log.debug("Timeout Checker Timeout Period		:" + this.timeoutPeriod);

					NodeList loggerList = eElement.getElementsByTagName(ServerConfigXMLConstants.LOGGER);
					Element loggerElements = (Element) loggerList.item(0);
					this.logReqProto = Boolean.parseBoolean(loggerElements.getElementsByTagName(ServerConfigXMLConstants.LOG_REQ_PROTO).item(0).getTextContent());
					log.debug("Log Request Proto ?		:" + this.logReqProto);

					this.logResProto = Boolean.parseBoolean(loggerElements.getElementsByTagName(ServerConfigXMLConstants.LOG_RES_PROTO).item(0).getTextContent());

					log.debug("Log Response Proto ?		:" + this.logResProto);

					this.logEventProto = Boolean.parseBoolean(loggerElements.getElementsByTagName(ServerConfigXMLConstants.LOG_EVENT_PROTO).item(0).getTextContent());

					log.debug("Log Event Proto ?		:" + this.logEventProto);

					
				}
			}

			NodeList transportSettingsList = doc.getElementsByTagName(ServerConfigXMLConstants.TRANSPORT_SETTINGS);
			for (int temp = 0; temp < transportSettingsList.getLength(); temp++) {

				Node nNode = transportSettingsList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					NodeList acceptorsList = eElement.getElementsByTagName(ServerConfigXMLConstants.ACCEPTORS);
					Element acceptorsElements = (Element) acceptorsList.item(0);
					this.acceptorsPoolSize = Integer.parseInt(acceptorsElements.getElementsByTagName(ServerConfigXMLConstants.POOL_SIZE).item(0).getTextContent());
					log.debug("Acceptors Pool Size			:" + this.acceptorsPoolSize);

					this.acceptorsSendBufferSize = Integer.parseInt(acceptorsElements.getElementsByTagName(ServerConfigXMLConstants.SO_SNDBUF).item(0).getTextContent());
					log.debug("Acceptors Send Buffer Size			:" + this.acceptorsSendBufferSize);

					this.acceptorsRecieveBufferSize = Integer.parseInt(acceptorsElements.getElementsByTagName(ServerConfigXMLConstants.SO_RCVBUF).item(0).getTextContent());
					log.debug("Acceptors Recieve Buffer Size		:" + this.acceptorsRecieveBufferSize);

					NodeList channelHandlersList = eElement.getElementsByTagName(ServerConfigXMLConstants.CHANNEL_HANDLERS);
					Element channelHandlersElements = (Element) channelHandlersList.item(0);
					this.channelHandlersPoolSize = Integer.parseInt(channelHandlersElements.getElementsByTagName(ServerConfigXMLConstants.POOL_SIZE).item(0).getTextContent());
					log.debug("ChannelHandlers Pool Size			:" + this.channelHandlersPoolSize);

					this.channelHandlersSendBufferSize = Integer.parseInt(channelHandlersElements.getElementsByTagName(ServerConfigXMLConstants.SO_SNDBUF).item(0).getTextContent());
					log.debug("ChannelHandlers Send Buffer Size		:" + this.channelHandlersSendBufferSize);

					this.channelHandlersRecieveBufferSize = Integer.parseInt(channelHandlersElements.getElementsByTagName(ServerConfigXMLConstants.SO_RCVBUF).item(0).getTextContent());
					log.debug("ChannelHandlers Recieve Buffer Size	:" + this.channelHandlersRecieveBufferSize);

					this.TCP_NODELAY = Boolean.parseBoolean(eElement.getElementsByTagName(ServerConfigXMLConstants.TCP_NODELAY).item(0).getTextContent());
					log.debug("TCP_NODELAY				:" + this.TCP_NODELAY);

				}
			}

			String msg = "PBS Server is running { server host/port : " + this.getHostName() + "@" + this.getServerPort() + " }";
			log.info(msg);

		} catch (ParserConfigurationException e) {
			this.startUpFailed = true;
			String msg = "The underlying parser does not support the requested features. " + e.getLocalizedMessage();
			log.info(msg);
		} catch (FactoryConfigurationError e) {
			this.startUpFailed = true;
			String msg = "Error occurred obtaining SAX Parser Factory. " + e.getLocalizedMessage();
			log.info(msg);
		} catch (SAXException e) {
			this.startUpFailed = true;
			String msg = "Error while SAX Parsing. " + e.getLocalizedMessage();
			log.info(msg);
		} catch (IOException e) {
			this.startUpFailed = true;
			String msg = "Could not found " + pbsxmlPath + "  " + e.getLocalizedMessage();
			log.info(msg);
			log.info("Couldn't start RPC Server");
		}
	}

	private boolean isValidXML(Document document, String schemaPath) {

		SchemaFactory schemaFactory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);

		try {

			Schema schema = schemaFactory.newSchema(new StreamSource(schemaPath));
			Validator validator = schema.newValidator();
			validator.validate(new DOMSource(document));

		} catch (SAXException e) {

			String msg = "PBS Schema Validation failed! " + e.getLocalizedMessage();
			log.info(msg);
			return false;

		} catch (IOException e) {

			String msg = "PBS Schema Validation failed! " + e.getLocalizedMessage();
			log.info(msg);
			return false;
		}

		// arrives here only if the validation was successful
		String msg = "PBS Schema Validation is successful!";
		log.info(msg);
		return true;

	}
}
