/*
 * Copyright 2004,2014 The Apache Software Foundation.
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

package org.wso2.carbon.monitoring.publisher.bam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

/**
 * Base class for all the BAM Publishers. There can be a separate publisher per each defined data stream.
 */
public abstract class PublisherBase {

	private static final Log log = LogFactory.getLog(PublisherBase.class);

	protected StreamConfigContext configContext;
	protected AsyncDataPublisher publisher;

	public PublisherBase() {
		configContext = StreamConfigurationFactory.getConnectorStreamConfiguration(getDataStreamName());
		if(configContext.isEnabled()) {
			publisher = createPublisher();
		}
	}

	/**
	 * Adds the Payload data attributes to the stream definition
	 * @param definition the Stream definition
	 */
	protected abstract void addPayloadDataAttributes(StreamDefinition definition);

	/**
	 * Adds meta data attributes to the stream defintion
	 * @param definition the Stream definition
	 */
	protected abstract void addMetaDataAttributes(StreamDefinition definition);

	/**
	 * get the data stream name for data stream. This is the ID for the entry in the bam-publisher.xml
	 * @return
	 */
	protected abstract String getDataStreamName();

	protected StreamDefinition createStreamDefinition() {
		StreamDefinition definition = null;
		try {
			definition = new StreamDefinition(configContext.getStreamName(), configContext.getStreamVersion());
			definition.setDescription(configContext.getDescription());
			definition.setNickName(configContext.getNickName());
			addMetaDataAttributes(definition);
			addPayloadDataAttributes(definition);
		} catch (MalformedStreamDefinitionException e) {
			log.error("Malformed Stream Definition", e);
		}
		return definition;
	}

	/**
	 * Publish the given event to the data stream
	 * @param event the event
	 */
	protected void publish(Event event){
		if(!configContext.isEnabled()){
			return;
		}
		try {
			publisher.publish(configContext.getStreamName(), configContext.getStreamVersion(), event);
		} catch (AgentException exception) {
			log.warn("Exception occurred while publishing Connector Monitoring Event to BAM", exception);
		}
	}

	/**
	 * Maps null Integers to zero
	 * @param value
	 * @return
	 */
	protected Integer mapNull(Integer value){
		return (value == null) ? 0 : value;
	}

	/**
	 * Maps null Long values to zero
	 * @param value
	 * @return
	 */
	protected Long mapNull(Long value){
		return (value == null) ? 0L : value;
	}

	/**
	 * Map null String to -
	 * @param value
	 * @return
	 */
	protected String mapNull(String value){
		return (value == null) ? "-" : value;
	}

	private AsyncDataPublisher createPublisher() {
		AsyncDataPublisher publisher = new AsyncDataPublisher(configContext.getReceiverUrl(), configContext.getUsername(), configContext.getPassword());
		publisher.addStreamDefinition(createStreamDefinition());
		return publisher;
	}

}
