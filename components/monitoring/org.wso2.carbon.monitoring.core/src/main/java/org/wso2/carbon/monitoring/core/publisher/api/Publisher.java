package org.wso2.carbon.monitoring.core.publisher.api;


public interface Publisher {


	void publish(WebappMonitoringEvent e);

	void publish(ConnectorMonitoringEvent e);

	void publish(WebappResourceMonitoringEvent e);
}
