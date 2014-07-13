package org.wso2.carbon.monitoring.collector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.monitoring.api.MonitoringEvent;
import org.wso2.carbon.monitoring.jmx.Result;

import javax.management.Attribute;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by chamil on 7/11/14.
 */
public class CollectorUtil {

	private Log log;

	public CollectorUtil() {
		log = LogFactory.getLog(CollectorUtil.class);
	}

	public CollectorUtil(Log log) {
		this.log = log;
	}

	public Result getResultByCorrelator(List<Result> results, String correlator) {
		for (Result result : results) {
			if (StringUtils.equals(result.getCorrelator(), correlator)) {
				return result;
			}
		}

		return null;
	}

	public void mapResultAttributesToPoJo(Result connector, Object event) {
		List<Attribute> attributeList = connector.getAttributes().asList();
		for (Attribute attribute : attributeList) {
			setFieldValue(event, attribute);
		}
	}

	public void mapMetaData(MonitoringEvent event){
		String serverAddress = "-";
		String serverName = "-";

		try {
			InetAddress ip = InetAddress.getLocalHost();
			serverAddress = ip.getHostAddress();
			serverName = ip.getHostName();
		} catch (UnknownHostException ignored) {

		}

		event.setServerAddress(serverAddress);
		event.setServerName(serverName);
		event.setClusterDomain(getClusterDomain());
		event.setClusterSubDomain(getClusterSubDomain());
	}

	public void setFieldValue(Object event, Attribute attribute) {
		Class<?> clazz = event.getClass();
		Field field;
		try {
			field = clazz.getDeclaredField(attribute.getName());
		} catch (NoSuchFieldException e) {
			log.warn(attribute.getName() + " not found as a field", e);
			return;
		}

		try {
			field.setAccessible(true);
			if (field.getType().equals(attribute.getValue().getClass())) {
				field.set(event, attribute.getValue());
			} else {
				log.warn("Type mismatch occurred. field = " + field.getName() + " expected = " + field.getType() + ", found = " + attribute.getValue().getClass());
			}
		} catch (IllegalAccessException e) {
			log.warn(field.getName() + " accessing failed.", e);
		}
	}

	private String getClusterDomain() {
		return "-";
	}

	private String getClusterSubDomain() {
		return "-";
	}
}
