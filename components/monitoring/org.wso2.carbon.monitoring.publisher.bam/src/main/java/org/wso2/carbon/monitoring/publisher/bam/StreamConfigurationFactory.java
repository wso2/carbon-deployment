package org.wso2.carbon.monitoring.publisher.bam;

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
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class reads the repository/conf/etc/bam-publisher.xml file and create StreamConfigurationContext
 */
public class StreamConfigurationFactory {

	public static final String CONNECTOR_DATA_STREAM_NAME = "monitoring.connector";
	public static final String WEBAPP_RESOURCE_STREAM_NAME = "monitoring.webapp.resource";
	public static final String HTTP_DATA_STREAM_NAME = "monitoring.webapp.calls";

	private static DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	private static XPath xPath = XPathFactory.newInstance().newXPath();

	private static Log log = LogFactory.getLog(StreamConfigurationFactory.class);
	private static Document document;
	private static String xPathBase = "//bamPublisher/streams/stream[@id='%s']/%s";

	private static ConcurrentHashMap<String, StreamConfigContext> configCache = new ConcurrentHashMap<String, StreamConfigContext>();

	static {

		try {
			String bamConfigPath = CarbonUtils.getEtcCarbonConfigDirPath() +
			                       File.separator + "bam-publisher.xml";
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			document = builder.parse(bamConfigPath);

		} catch (ParserConfigurationException e) {
			log.error("could not load the bam-publisher configuration", e);
		} catch (SAXException e) {
			log.error("could not load the bam-publisher configuration", e);
		} catch (IOException e) {
			log.error("could not load the bam-publisher configuration", e);
		}
	}

	/**
	 * <bamPublisher>
	 * <streams>
	 * <stream name="ConnectorDataStream">
	 * <enabled>true</enabled>
	 * <nickName></nickName>
	 * <streamVersion></streamVersion>
	 * <username></username>
	 * <password></password>
	 * <receiverUrl></receiverUrl>
	 * <description></description>
	 * </stream>
	 * </streams>
	 * </bamPublisher>
	 */
	public static StreamConfigContext getConnectorStreamConfiguration(String id) {
		if (configCache.containsKey(id)) {
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
		return streamConfigContext;

	}

	private static String getValue(String id, String property) {
		String xPathQuery = String.format(xPathBase, id, property);
		try {
			return xPath.compile(xPathQuery).evaluate(document);
		} catch (XPathExpressionException e) {
			log.error("Error while evaluating XPath " + xPathQuery, e);
		}
		return null;
	}

}

