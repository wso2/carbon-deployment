package org.wso2.carbon.monitoring.api;

/**
 * Created by chamil on 6/26/14.
 */
public interface StatCollector extends Runnable{
	void setPublisher(Publisher publisher);

}
