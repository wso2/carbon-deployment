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

package org.wso2.carbon.as.monitoring.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.as.monitoring.config.BAMPublisherConfigurationException;
import org.wso2.carbon.as.monitoring.config.StreamConfigContext;
import org.wso2.carbon.as.monitoring.config.StreamConfigurationReader;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;


/**
 * Base class for all the BAM Publishers. There can be a separate publisher per each defined data stream.
 */
public abstract class PublisherBase {

    private static final Log LOG = LogFactory.getLog(PublisherBase.class);

    protected StreamConfigContext configContext;
    protected AsyncDataPublisher publisher;

    public PublisherBase() throws BAMPublisherConfigurationException {
        final String streamName = getDataStreamName();
        configContext = StreamConfigurationReader.getInstance().getStreamConfiguration(streamName);
        if (configContext.isEnabled()) {
            publisher = createPublisher();
        }
    }

    /**
     * This method indicates whether this Publisher is in a publishable situation.
     *
     * @return whether this publisher is in a publishable state.
     */
    public boolean isPublishable() {
        return configContext.isEnabled() && publisher != null;
    }

    /**
     * Adds the Payload data attributes to the stream definition.
     *
     * @param definition the Stream definition
     */
    protected abstract void addPayloadDataAttributes(StreamDefinition definition);

    /**
     * Adds meta data attributes to the stream definition.
     *
     * @param definition the Stream definition
     */
    protected abstract void addMetaDataAttributes(StreamDefinition definition);

    /**
     * get the data stream name for data stream. This is the ID for the entry in the bam-publisher.xml.
     *
     * @return
     */
    protected abstract String getDataStreamName();

    protected StreamDefinition createStreamDefinition() throws BAMPublisherConfigurationException {
        StreamDefinition definition;
        try {
            definition = new StreamDefinition(configContext.getStreamName(), configContext.getStreamVersion());
            definition.setDescription(configContext.getDescription());
            definition.setNickName(configContext.getNickName());
            addMetaDataAttributes(definition);
            addPayloadDataAttributes(definition);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stream definition created : " + definition);
            }
        } catch (MalformedStreamDefinitionException e) {
            // This can only occur due to malformed configuration for stream name, version etc...
            // Hence throwing configuration exception
            throw new BAMPublisherConfigurationException("The values in bam-publisher.xml are malformed for " + configContext.getStreamName(), e);
        }
        return definition;
    }

    /**
     * Publish the given event to the data stream.
     *
     * @param event the event
     */
    protected void publish(Event event) throws MonitoringPublisherException {
        if (!configContext.isEnabled()) {
            return;
        }
        try {
            publisher.publish(configContext.getStreamName(), configContext.getStreamVersion(), event);
        } catch (AgentException exception) {
            throw new MonitoringPublisherException("Exception occurred while publishing Connector Monitoring Event to BAM", exception);
        }
    }

    /**
     * Maps null Integers to zero
     *
     * @param value
     * @return
     */
    protected Integer mapNull(Integer value) {
        return (value == null) ? 0 : value;
    }

    /**
     * Maps null Long values to zero
     *
     * @param value
     * @return
     */
    protected Long mapNull(Long value) {
        return (value == null) ? 0L : value;
    }

    /**
     * Map null String to -
     *
     * @param value
     * @return
     */
    protected String mapNull(String value) {
        return (value == null) ? "-" : value;
    }

    private AsyncDataPublisher createPublisher() throws BAMPublisherConfigurationException {
        AsyncDataPublisher asyncPublisher = new AsyncDataPublisher(configContext.getReceiverUrl(), configContext.getUsername(), configContext.getPassword());
        asyncPublisher.addStreamDefinition(createStreamDefinition());
        return asyncPublisher;
    }

}
