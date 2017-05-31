/*
*  Copyright (c) $today.year, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.deployment.engine.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Optional;
import java.util.Properties;

/**
 * JAXB mapping for deployment notifier configuration.
 *
 * @since 5.1.0
 */
@Configuration(description = "Deployment notifier configuration parameters")
public class DeploymentNotifierConfig {

    @Element(description = "JMS publishing enabled")
    private boolean jmsPublishingEnabled = false;

    @Element(description = "Desitination JNDI name")
    private String destinationJNDIName = "topic0";

    @Element(description = "Destination type")
    private String destinationType = "topic";

    @Element(description = "Java naming factory initial")
    private String javaNamingFactoryInitial = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";

    @Element(description = "Java naming provider URL")
    private String javaNamingProviderURL = "conf/jndi.properties";

    @Element(description = "JMS user name")
    private Optional<String> jmsUsername = Optional.empty();

    @Element(description = "JMS password")
    private Optional<String> jmsPassword = Optional.empty();

    @Element(description = "Connection factory JNDI name")
    private String connectionFactoryJNDIName = "TopicConnectionFactory";

    @Element(description = "Static massage content")
    private Properties staticMessageContent = new Properties();

    public boolean isJmsPublishingEnabled() {
        return jmsPublishingEnabled;
    }

    public String getDestinationJNDIName() {
        return destinationJNDIName;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public Optional<String> getJmsUsername() {
        return jmsUsername;
    }

    public Optional<String> getJmsPassword() {
        return jmsPassword;
    }

    public String getJavaNamingFactoryInitial() {
        return javaNamingFactoryInitial;
    }

    public String getJavaNamingProviderURL() {
        return javaNamingProviderURL;
    }

    public String getConnectionFactoryJNDIName() {
        return connectionFactoryJNDIName;
    }

    public Properties getStaticMessageContent() {
        return Optional.ofNullable(staticMessageContent).orElse(new Properties());
    }
}
