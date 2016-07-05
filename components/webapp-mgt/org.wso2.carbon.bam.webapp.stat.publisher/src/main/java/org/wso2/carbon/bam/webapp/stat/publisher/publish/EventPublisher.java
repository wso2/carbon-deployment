/*
* Copyright 2004,2013 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.bam.webapp.stat.publisher.publish;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.EventPublisherConfig;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.InternalEventingConfigData;
import org.wso2.carbon.bam.webapp.stat.publisher.data.WebappStatEvent;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.util.List;

/*
* Purpose of this class is to publish the event to BAM server.
*/
public class EventPublisher {

    private static Log log = LogFactory.getLog(EventPublisher.class);

    public void publish(WebappStatEvent webappStatEvent, InternalEventingConfigData configData) {
        List<Object> correlationData = webappStatEvent.getCorrelationData();
        List<Object> metaData = webappStatEvent.getMetaData();
        List<Object> payLoadData = webappStatEvent.getEventData();

        String key = null;
        EventPublisherConfig eventPublisherConfig = null;

        StreamDefinition streamDef = null;
        key = configData.getUrl() + "_" + configData.getUserName() + "_" +
                configData.getPassword();
        eventPublisherConfig = WebappAgentUtil.getEventPublisherConfig(key);
        streamDef = configData.getStreamDefinition();

        try {
            if (eventPublisherConfig == null) {
                synchronized (EventPublisher.class) {
                    eventPublisherConfig = WebappAgentUtil.getEventPublisherConfig(key);
                    if (null == eventPublisherConfig) {
                        eventPublisherConfig = new EventPublisherConfig();
                        DataPublisher dataPublisher = new DataPublisher(configData.getUrl(),
                                configData.getUserName(),
                                configData.getPassword());
                        eventPublisherConfig.setDataPublisher(dataPublisher);
                        WebappAgentUtil.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                    }
                }
            }

            DataPublisher dataPublisher = eventPublisherConfig.getDataPublisher();

            dataPublisher.tryPublish(streamDef.getStreamId(), System.currentTimeMillis(), getObjectArray(metaData),
                    getObjectArray(correlationData),
                    getObjectArray(payLoadData));
        } catch (DataEndpointConfigurationException | DataEndpointException | DataEndpointAgentConfigurationException |
                DataEndpointAuthenticationException | TransportException e) {
            log.error("Error occurred while sending the event", e);
        }
    }

    private Object[] getObjectArray(List<Object> list) {
        if (list.size() > 0) {
            return list.toArray();
        }
        return null;
    }
}
