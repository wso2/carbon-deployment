package org.wso2.carbon.protobuf.client.utils;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.protobuf.client.BinaryServiceClient;
import org.xml.sax.SAXException;

public class ProtobufClientConfig {

	private static Logger log = LoggerFactory.getLogger(BinaryServiceClient.class);

	private static String pbsxmlPath = System.getProperty(CarbonBaseConstants.CARBON_HOME) +
	                                   "/repository/conf/etc/pbs.xml";

	public ProtobufClientConfig() {
		init();
	}

	private boolean startUpFailed = false;
	private boolean enablePbs;
	private String serverHostName;
	private int serverPort;
	private String clientHostName;
	private int clientPort;

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

	public int getChannelHandlersPoolSize() {
		return channelHandlersPoolSize;
	}

	public void setChannelHandlersPoolSize(int channelHandlersPoolSize) {
		this.channelHandlersPoolSize = channelHandlersPoolSize;
	}

	public int getChannelHandlersSendBufferSize() {
		return channelHandlersSendBufferSize;
	}

	public void setChannelHandlersSendBufferSize(int channelHandlersSendBufferSize) {
		this.channelHandlersSendBufferSize = channelHandlersSendBufferSize;
	}

	public int getChannelHandlersRecieveBufferSize() {
		return channelHandlersRecieveBufferSize;
	}

	public void setChannelHandlersRecieveBufferSize(int channelHandlersRecieveBufferSize) {
		this.channelHandlersRecieveBufferSize = channelHandlersRecieveBufferSize;
	}

	private boolean TCP_NODELAY;

	private boolean enableSSL;
	private String keystorePassword;
	private String keystorePath;
	private String truststorePassword;
	private String truststorePath;

	public static Logger getLog() {
		return log;
	}

	public static void setLog(Logger log) {
		ProtobufClientConfig.log = log;
	}

	public static String getPbsxmlPath() {
		return pbsxmlPath;
	}

	public static void setPbsxmlPath(String pbsxmlPath) {
		ProtobufClientConfig.pbsxmlPath = pbsxmlPath;
	}

	public boolean isStartUpFailed() {
		return startUpFailed;
	}

	public void setStartUpFailed(boolean startUpFailed) {
		this.startUpFailed = startUpFailed;
	}

	public boolean isEnablePbs() {
		return enablePbs;
	}

	public void setEnablePbs(boolean enablePbs) {
		this.enablePbs = enablePbs;
	}

	public String getServerHostName() {
		return serverHostName;
	}

	public void setServerHostName(String serverHostName) {
		this.serverHostName = serverHostName;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getClientHostName() {
		return clientHostName;
	}

	public void setClientHostName(String clientHostName) {
		this.clientHostName = clientHostName;
	}

	public int getClientPort() {
		return clientPort;
	}

	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}

	public int getTimeoutExecutorCorePoolSize() {
		return timeoutExecutorCorePoolSize;
	}

	public void setTimeoutExecutorCorePoolSize(int timeoutExecutorCorePoolSize) {
		this.timeoutExecutorCorePoolSize = timeoutExecutorCorePoolSize;
	}

	public int getTimeoutExecutorMaxPoolSize() {
		return timeoutExecutorMaxPoolSize;
	}

	public void setTimeoutExecutorMaxPoolSize(int timeoutExecutorMaxPoolSize) {
		this.timeoutExecutorMaxPoolSize = timeoutExecutorMaxPoolSize;
	}

	public int getTimeoutExecutorPoolKeepAliveTime() {
		return timeoutExecutorPoolKeepAliveTime;
	}

	public void setTimeoutExecutorPoolKeepAliveTime(int timeoutExecutorPoolKeepAliveTime) {
		this.timeoutExecutorPoolKeepAliveTime = timeoutExecutorPoolKeepAliveTime;
	}

	public int getTimeoutCheckerCorePoolSize() {
		return timeoutCheckerCorePoolSize;
	}

	public void setTimeoutCheckerCorePoolSize(int timeoutCheckerCorePoolSize) {
		this.timeoutCheckerCorePoolSize = timeoutCheckerCorePoolSize;
	}

	public int getTimeoutPeriod() {
		return timeoutPeriod;
	}

	public void setTimeoutPeriod(int timeoutPeriod) {
		this.timeoutPeriod = timeoutPeriod;
	}

	public int getAcceptorsPoolSize() {
		return acceptorsPoolSize;
	}

	public void setAcceptorsPoolSize(int acceptorsPoolSize) {
		this.acceptorsPoolSize = acceptorsPoolSize;
	}

	public int getAcceptorsSendBufferSize() {
		return acceptorsSendBufferSize;
	}

	public void setAcceptorsSendBufferSize(int acceptorsSendBufferSize) {
		this.acceptorsSendBufferSize = acceptorsSendBufferSize;
	}

	public int getAcceptorsRecieveBufferSize() {
		return acceptorsRecieveBufferSize;
	}

	public void setAcceptorsRecieveBufferSize(int acceptorsRecieveBufferSize) {
		this.acceptorsRecieveBufferSize = acceptorsRecieveBufferSize;
	}

	public boolean isTCP_NODELAY() {
		return TCP_NODELAY;
	}

	public void setTCP_NODELAY(boolean tCP_NODELAY) {
		TCP_NODELAY = tCP_NODELAY;
	}

	public boolean isEnableSSL() {
		return enableSSL;
	}

	public void setEnableSSL(boolean enableSSL) {
		this.enableSSL = enableSSL;
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	public String getKeystorePath() {
		return keystorePath;
	}

	public void setKeystorePath(String keystorePath) {
		this.keystorePath = keystorePath;
	}

	public String getTruststorePassword() {
		return truststorePassword;
	}

	public void setTruststorePassword(String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}

	public String getTruststorePath() {
		return truststorePath;
	}

	public void setTruststorePath(String truststorePath) {
		this.truststorePath = truststorePath;
	}

	/*
	 * Getting server configuration information from pbs.xml
	 */

	private void init() {

		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			// Leave off validation, and turn off namespaces
			factory.setValidating(false);
			factory.setNamespaceAware(false);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(pbsxmlPath));

			doc.getDocumentElement().normalize();

			this.enablePbs =
			                 Boolean.parseBoolean(doc.getElementsByTagName(ProtobufClientConfigXMLConstants.ENABLE_PBS)
			                                         .item(0).getTextContent());

			// return if pbs is not needed
			if (!this.enablePbs) {
				return;
			}

			NodeList clientSettingsList =
			                              doc.getElementsByTagName(ProtobufClientConfigXMLConstants.CLIENT_SETTINGS);
			for (int temp = 0; temp < clientSettingsList.getLength(); temp++) {

				Node nNode = clientSettingsList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					this.serverHostName =
					                      eElement.getElementsByTagName(ProtobufClientConfigXMLConstants.SERVER_HOST_NAME)
					                              .item(0).getTextContent();
					log.debug("Server Host Name					:" + this.serverHostName);
					this.serverPort =
					                  Integer.parseInt(eElement.getElementsByTagName(ProtobufClientConfigXMLConstants.SERVER_PORT)
					                                           .item(0).getTextContent());
					log.debug("Server Port				:" + this.serverPort);

					this.clientHostName =
					                      eElement.getElementsByTagName(ProtobufClientConfigXMLConstants.CLIENT_HOST_NAME)
					                              .item(0).getTextContent();
					log.debug("Client Host Name					:" + this.clientHostName);
					this.clientPort =
					                  Integer.parseInt(eElement.getElementsByTagName(ProtobufClientConfigXMLConstants.CLIENT_PORT)
					                                           .item(0).getTextContent());
					log.debug("Client Port				:" + this.clientPort);

					this.enableSSL =
					                 Boolean.parseBoolean(eElement.getElementsByTagName(ProtobufClientConfigXMLConstants.ENABLE_SSL)
					                                              .item(0).getTextContent());
					log.debug("Enable SSL					:" + this.enableSSL);

					if (this.enableSSL) {
						ServerConfiguration configuration = ServerConfiguration.getInstance();
						this.keystorePath =
						                    configuration.getFirstProperty("Security.KeyStore.Location");
						this.keystorePassword =
						                        configuration.getFirstProperty("Security.KeyStore.Password");
						this.truststorePath =
						                      configuration.getFirstProperty("Security.TrustStore.Location");
						this.truststorePassword =
						                          configuration.getFirstProperty("Security.TrustStore.Password");
					}

					NodeList timeoutExecutorList =
					                               eElement.getElementsByTagName(ProtobufClientConfigXMLConstants.TIMEOUT_EXECUTOR_THREADPOOL);
					Element timeoutExecutorElements = (Element) timeoutExecutorList.item(0);
					this.timeoutExecutorCorePoolSize =
					                                   Integer.parseInt(timeoutExecutorElements.getElementsByTagName(ProtobufClientConfigXMLConstants.CORE_POOL_SIZE)
					                                                                           .item(0)
					                                                                           .getTextContent());
					log.debug("Timeout Executor Core Pool Size		:" +
					         this.timeoutExecutorCorePoolSize);

					this.timeoutExecutorMaxPoolSize =
					                                  Integer.parseInt(timeoutExecutorElements.getElementsByTagName(ProtobufClientConfigXMLConstants.MAX_POOL_SIZE)
					                                                                          .item(0)
					                                                                          .getTextContent());

					log.debug("Timeout Executor Max Pool Size		:" + this.timeoutExecutorMaxPoolSize);

					this.timeoutExecutorPoolKeepAliveTime =
					                                        Integer.parseInt(timeoutExecutorElements.getElementsByTagName(ProtobufClientConfigXMLConstants.TIMEOUT_EXECUTOR_THREADPOOL_KEEP_ALIVE_TIME)
					                                                                                .item(0)
					                                                                                .getTextContent());

					log.debug("Timeout Executor KeepAliveTime		:" +
					         this.timeoutExecutorPoolKeepAliveTime);

					NodeList timeoutCheckerList =
					                              eElement.getElementsByTagName(ProtobufClientConfigXMLConstants.TIMEOUT_CHECKER_THREADPOOL);
					Element timeoutCheckerElements = (Element) timeoutCheckerList.item(0);
					this.timeoutCheckerCorePoolSize =
					                                  Integer.parseInt(timeoutCheckerElements.getElementsByTagName(ProtobufClientConfigXMLConstants.CORE_POOL_SIZE)
					                                                                         .item(0)
					                                                                         .getTextContent());
					log.debug("Timeout Checker Core Pool Size		:" + this.timeoutCheckerCorePoolSize);

					this.timeoutPeriod =
					                     Integer.parseInt(timeoutCheckerElements.getElementsByTagName(ProtobufClientConfigXMLConstants.TIMEOUT_PERIOD)
					                                                            .item(0)
					                                                            .getTextContent());
					log.debug("Timeout Checker Timeout Period		:" + this.timeoutPeriod);

				}
			}

			NodeList transportSettingsList =
			                                 doc.getElementsByTagName(ProtobufClientConfigXMLConstants.TRANSPORT_SETTINGS);
			for (int temp = 0; temp < transportSettingsList.getLength(); temp++) {

				Node nNode = transportSettingsList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					NodeList acceptorsList =
					                         eElement.getElementsByTagName(ProtobufClientConfigXMLConstants.ACCEPTORS);
					Element acceptorsElements = (Element) acceptorsList.item(0);
					this.acceptorsPoolSize =
					                         Integer.parseInt(acceptorsElements.getElementsByTagName(ProtobufClientConfigXMLConstants.POOL_SIZE)
					                                                           .item(0)
					                                                           .getTextContent());
					log.debug("Acceptors Pool Size			:" + this.acceptorsPoolSize);

					this.acceptorsSendBufferSize =
					                               Integer.parseInt(acceptorsElements.getElementsByTagName(ProtobufClientConfigXMLConstants.SO_SNDBUF)
					                                                                 .item(0)
					                                                                 .getTextContent());
					log.debug("Acceptors Send Buffer Size			:" + this.acceptorsSendBufferSize);

					this.acceptorsRecieveBufferSize =
					                                  Integer.parseInt(acceptorsElements.getElementsByTagName(ProtobufClientConfigXMLConstants.SO_RCVBUF)
					                                                                    .item(0)
					                                                                    .getTextContent());
					log.debug("Acceptors Recieve Buffer Size		:" + this.acceptorsRecieveBufferSize);

					NodeList channelHandlersList =
					                               eElement.getElementsByTagName(ProtobufClientConfigXMLConstants.CHANNEL_HANDLERS);
					Element channelHandlersElements = (Element) channelHandlersList.item(0);
					this.channelHandlersPoolSize =
					                               Integer.parseInt(channelHandlersElements.getElementsByTagName(ProtobufClientConfigXMLConstants.POOL_SIZE)
					                                                                       .item(0)
					                                                                       .getTextContent());
					log.debug("ChannelHandlers Pool Size			:" + this.channelHandlersPoolSize);

					this.channelHandlersSendBufferSize =
					                                     Integer.parseInt(channelHandlersElements.getElementsByTagName(ProtobufClientConfigXMLConstants.SO_SNDBUF)
					                                                                             .item(0)
					                                                                             .getTextContent());
					log.debug("ChannelHandlers Send Buffer Size		:" +
					         this.channelHandlersSendBufferSize);

					this.channelHandlersRecieveBufferSize =
					                                        Integer.parseInt(channelHandlersElements.getElementsByTagName(ProtobufClientConfigXMLConstants.SO_RCVBUF)
					                                                                                .item(0)
					                                                                                .getTextContent());
					log.debug("ChannelHandlers Recieve Buffer Size	:" +
					         this.channelHandlersRecieveBufferSize);

					this.TCP_NODELAY =
					                   Boolean.parseBoolean(eElement.getElementsByTagName(ProtobufClientConfigXMLConstants.TCP_NODELAY)
					                                                .item(0).getTextContent());
					log.debug("TCP_NODELAY				:" + this.TCP_NODELAY);

				}
			}
			
			String msg = "PBS Client is running { server host/port : "+this.getServerHostName()+"@"+this.getServerPort()+", client host/port : "+this.getClientHostName()+"@"+this.clientPort+" }";
			log.info(msg);
			
		} catch (ParserConfigurationException e) {
			this.startUpFailed = true;
			String msg =
			             "The underlying parser does not support the requested features. " +
			                     e.getLocalizedMessage();
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
			log.info("Couldn't start PBS Client");
		}
	}
}
