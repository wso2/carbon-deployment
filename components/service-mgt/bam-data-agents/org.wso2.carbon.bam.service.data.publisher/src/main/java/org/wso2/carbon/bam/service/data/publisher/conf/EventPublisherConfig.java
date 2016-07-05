package org.wso2.carbon.bam.service.data.publisher.conf;


import org.wso2.carbon.databridge.agent.DataPublisher;

public class EventPublisherConfig {

    DataPublisher dataPublisher;

    public DataPublisher getDataPublisher() {
        return dataPublisher;
    }

    public void setDataPublisher(DataPublisher dataPublisher) {
        this.dataPublisher = dataPublisher;
    }

}
