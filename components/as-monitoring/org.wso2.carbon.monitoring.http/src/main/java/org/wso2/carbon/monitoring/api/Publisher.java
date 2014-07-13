package org.wso2.carbon.monitoring.api;

/**
 * Created by chamil on 6/26/14.
 */
public interface Publisher {


	void publish(WebappMonitoringEvent e);

	void publish(ConnectorMonitoringEvent e);

	void publish(WebappResourceMonitoringEvent e);
}
