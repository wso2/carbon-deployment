package org.wso2.carbon.monitoring.publisher.bam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

/**
 * Created by chamil on 7/10/14.
 */
public abstract class PublisherBase {

	private static final Log log = LogFactory.getLog(PublisherBase.class);

	protected StreamConfigContext configContext;
	protected AsyncDataPublisher publisher;

	public PublisherBase() {
		configContext = StreamConfigurationFactory.getConnectorStreamConfiguration(getDataStreamName());
		publisher = createPublisher();
	}

	private AsyncDataPublisher createPublisher() {
		AsyncDataPublisher publisher = new AsyncDataPublisher(configContext.getReceiverUrl(), configContext.getUsername(), configContext.getPassword());
		publisher.addStreamDefinition(createStreamDefinition());
		return publisher;
	}

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

	protected abstract void addPayloadDataAttributes(StreamDefinition definition);

	protected abstract void addMetaDataAttributes(StreamDefinition definition);

	protected abstract String getDataStreamName();

	protected void publish(Event event){
		try {
			publisher.publish(configContext.getStreamName(), configContext.getStreamVersion(), event);
		} catch (AgentException exception) {
			log.warn("Exception occurred while publishing Connector Monitoring Event to BAM", exception);
		}
	}

	protected Integer mapNull(Integer value){
		return (value == null) ? 0 : value;
	}

	protected Long mapNull(Long value){
		return (value == null) ? 0L : value;
	}

	protected String mapNull(String value){
		return (value == null) ? "-" : value;
	}
}
