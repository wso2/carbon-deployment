/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.bam.webapp.stat.publisher.publish;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.webapp.stat.publisher.data.WebappStatEvent;
import org.wso2.carbon.bam.webapp.stat.publisher.util.WebappStatisticsPublisherConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;

/**
 * Class ues to publish Webapp events to a Super Tenant stream to capture stats for aPaaS.
 * If the enable.metering is set to true stats will be published to a common stream irrespective of the tenant settings.
 */

public class GlobalWebappEventPublisher {

    private static Log log = LogFactory.getLog(GlobalWebappEventPublisher.class);

    private static final String UsageEventStream = "org.wso2.carbon.appserver.webapp.stats";

    private static final String UsageEventStreamVersion = "1.0.0";

    private static DataPublisher dataPublisher;

    private static final String BamAgentPasswordAlias = "Bam.Agent.ConnectionPassword";

    private static final String GlobalPublisherEl = "GlobalPublisher";
    private static final String BamAgentUsernameEl = "username";

    public static void createGlobalEventStream(OMElement bamConfig) {
        SecretResolver secretResolver = SecretResolverFactory.create(bamConfig, false);
        String username = "";
        String password = "";
        String url = ServerConfiguration.getInstance().
                getProperties(WebappStatisticsPublisherConstants.SERVER_CONFIG_BAM_URL)[0];

        for (Iterator childElements = bamConfig.getChildElements(); childElements.hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            if (element.getLocalName().equals(GlobalPublisherEl)) {
                username = element.getFirstChildWithName(new QName(BamAgentUsernameEl)).getText().trim();
                break;
            }
        }

        if (secretResolver != null && secretResolver.isInitialized() && secretResolver.isTokenProtected(BamAgentPasswordAlias)) {
            password = secretResolver.resolve(BamAgentPasswordAlias);
        }

        try {
            dataPublisher = new DataPublisher(url, username, password);
        } catch (DataEndpointAgentConfigurationException | DataEndpointException | DataEndpointAuthenticationException |
                DataEndpointConfigurationException | TransportException e) {
            log.error("Error occurred while sending the event", e);
        }

    }

    public static void publish(WebappStatEvent webappStatEvent) {
        List<Object> correlationData = webappStatEvent.getCorrelationData();
        List<Object> metaData = webappStatEvent.getMetaData();
        List<Object> payLoadData = webappStatEvent.getEventData();

        dataPublisher.tryPublish(UsageEventStream, System.currentTimeMillis(), getObjectArray(metaData),
                getObjectArray(correlationData),
                getObjectArray(payLoadData));

    }

    private static Object[] getObjectArray(List<Object> list) {
        if (list.size() > 0) {
            return list.toArray();
        }
        return null;
    }

}
