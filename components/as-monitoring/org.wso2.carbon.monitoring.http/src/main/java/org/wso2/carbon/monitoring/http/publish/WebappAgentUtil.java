package org.wso2.carbon.monitoring.http.publish;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.monitoring.http.conf.EventPublisherConfig;
import org.wso2.carbon.monitoring.http.conf.InternalEventingConfigData;
import org.wso2.carbon.monitoring.http.conf.Property;
import org.wso2.carbon.monitoring.http.data.WebappMonitoringEvent;
import org.wso2.carbon.monitoring.http.data.WebappMonitoringEventData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps info on whether the webapp stat publishing is enabled or not and
 * the event publisher configurations which includes the data publisher, stream def etc.
 */
public class WebappAgentUtil {

    private static Log log = LogFactory.getLog(WebappAgentUtil.class);

    private static Map<String,EventPublisherConfig> eventPublisherConfigMap =
            new HashMap<String, EventPublisherConfig>();

    private static boolean isPublishingEnabled = false;

    private static boolean isGlobalPublishingEnabled = false;

    public static void setPublishingEnabled(boolean isPublishingEnabled) {
        WebappAgentUtil.isPublishingEnabled = isPublishingEnabled;
    }

    public static boolean getPublishingEnabled() {
        return isPublishingEnabled;
    }

    public static EventPublisherConfig getEventPublisherConfig(String key) {
        return eventPublisherConfigMap.get(key);
    }

    public static Map<String, EventPublisherConfig> getEventPublisherConfigMap() {
        return eventPublisherConfigMap;
    }

    public static void removeExistingEventPublisherConfigValue(String key) {
        if (eventPublisherConfigMap != null) {
            eventPublisherConfigMap.put(key, null);
        }
    }

    public static WebappMonitoringEvent makeEventList(WebappMonitoringEventData webappMonitoringEventData,
                                      InternalEventingConfigData eventingConfigData) {

        List<Object> correlationData = new ArrayList<Object>();
        List<Object> metaData = new ArrayList<Object>();
        List<Object> eventData = new ArrayList<Object>();

        eventData = addCommonEventData(webappMonitoringEventData, eventData);

        eventData = addStatisticEventData(webappMonitoringEventData, eventData);
        metaData = addStatisticsMetaData(webappMonitoringEventData, metaData);

        if(eventingConfigData != null) {
            metaData = addPropertiesAsMetaData(eventingConfigData, metaData);
        }

        WebappMonitoringEvent publishEvent = new WebappMonitoringEvent(correlationData, metaData, eventData);
        return publishEvent;
    }

    private static List<Object> addPropertiesAsMetaData(InternalEventingConfigData eventingConfigData,
                                                List<Object> metaData) {
        Property[] properties = eventingConfigData.getProperties();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                Property property = properties[i];
                if (property.getKey() != null && !property.getKey().isEmpty()) {
                    metaData.add(property.getValue());
                }
            }
        }

        return metaData;
    }


    private static List<Object> addCommonEventData(WebappMonitoringEventData event, List<Object> eventData) {

        eventData.add(replaceNull(event.getWebappName()));
        eventData.add(replaceNull(event.getWebappVersion()));
        eventData.add(replaceNull(event.getUserId()));
        eventData.add(replaceNull(event.getResourcePath()));
        eventData.add(replaceNull(event.getWebappType()));
        eventData.add(replaceNull(event.getWebappDisplayName()));
        eventData.add(replaceNull(event.getWebappContext()));
        eventData.add(replaceNull(event.getSessionId()));
        eventData.add(replaceNull(event.getHttpMethod()));
        eventData.add(replaceNull(event.getContentType()));
        eventData.add(replaceNull(event.getResponseContentType()));
        eventData.add(replaceNull(event.getRemoteAddress()));
        eventData.add(replaceNull(event.getReferrer()));
        eventData.add(replaceNull(event.getRemoteUser()));
        eventData.add(replaceNull(event.getAuthType()));
        eventData.add(replaceNull(event.getUserAgentFamily()));
        eventData.add(replaceNull(event.getUserAgentVersion()));
        eventData.add(replaceNull(event.getOperatingSystem()));
        eventData.add(replaceNull(event.getOperatingSystemVersion()));
	    eventData.add(replaceNull(event.getDeviceCategory()));
        eventData.add(replaceNull(event.getCountry()));
        eventData.add(event.getTimestamp());
        eventData.add(event.getResponseHttpStatusCode());
        eventData.add(event.getResponseTime());
	    eventData.add(replaceNull(event.getLanguage()));
        eventData.add(event.getRequestSizeBytes());
        eventData.add(event.getResponseSizeBytes());
	    eventData.add(replaceNull(event.getRequestHeader()));
	    eventData.add(replaceNull(event.getResponseHeader()));
	    eventData.add(replaceNull(event.getRequestPayload()));
	    eventData.add(replaceNull(event.getResponsePayload()));


      return eventData;

    }

	private static String replaceNull(String value){
		return (value != null) ? value : "-";
	}

    private static List<Object> addStatisticEventData(WebappMonitoringEventData event, List<Object> eventData) {
        return eventData;
    }

    private static List<Object> addStatisticsMetaData(WebappMonitoringEventData event, List<Object> metaData) {
        metaData.add(replaceNull(event.getServerAddress()));
        metaData.add(replaceNull(event.getServerName()));
	    metaData.add(replaceNull(event.getClusterId()));
        metaData.add(event.getTenantId());
        metaData.add(replaceNull(event.getWebappOwnerTenant()));
        metaData.add(replaceNull(event.getUserTenant()));

        return metaData;
    }

    public static boolean isGlobalPublishingEnabled() {
        return isGlobalPublishingEnabled;
    }

    public static void setGlobalPublishingEnabled(boolean globalPublishingEnabled) {
        isGlobalPublishingEnabled = globalPublishingEnabled;
    }
}
