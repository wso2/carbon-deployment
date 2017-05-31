/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.deployment.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.LifecycleEvent;
import org.wso2.carbon.deployment.engine.LifecycleListener;
import org.wso2.carbon.deployment.engine.config.DeploymentConfiguration;
import org.wso2.carbon.deployment.engine.config.DeploymentNotifierConfig;
import org.wso2.carbon.deployment.notifier.internal.DataHolder;
import org.wso2.carbon.deployment.notifier.internal.DeploymentNotificationMessage;
import org.wso2.carbon.deployment.notifier.internal.JMSConnectionFactory;

import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Optional;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Sends a notification whenever an artifact deployment/undeployment happens.
 * Default mode is publishing via JMS to a JMS topic.
 *
 * This sends a serialized {@link DeploymentNotificationMessage}
 * object instance as the message.
 *
 * @since 5.0.0
 */
public class DeploymentNotifierLifecycleListener implements LifecycleListener {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentNotifierLifecycleListener.class);
    private Optional<JMSConnectionFactory> jmsConnectionFactory = Optional.empty();

    private DeploymentNotifierConfig config;

    String serverId;

    public DeploymentNotifierLifecycleListener() {
        try {
            ConfigProvider configProvider = DataHolder.getInstance().getConfigProvider();
            if (configProvider != null) {
                config = configProvider.getConfigurationObject(DeploymentConfiguration.class).getDeploymentNotifier();
            }
        } catch (ConfigurationException e) {
            logger.error("Fail to load deployment configuration");
        }

        serverId = DataHolder.getInstance().getCarbonRuntime().getConfiguration().getId();

        if (config.isJmsPublishingEnabled()) {
            jmsConnectionFactory = Optional.of(getConnectionFactory());
        }
    }

    /**
     *
     * Publishes the artifact deployment status.
     *
     * @param event The lifecycle event. The Artifact object and
     *              the currently triggered lifecycle event is stored
     */
    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        if (!config.isJmsPublishingEnabled()) {
            return;
        }

        //pooled connection is returned back to the pool hence AutoCloseable is not possible
        JMSConnectionFactory.JMSPooledConnectionHolder pooledConnectionHolder = null;
        try {
            logger.debug("Invoked DeploymentNotifierLifecycleListener");
            if (LifecycleEvent.STATE.AFTER_START_EVENT.equals(event.getState()) ||
                    LifecycleEvent.STATE.AFTER_UPDATE_EVENT.equals(event.getState())) {
                String deploymentStatusMessage = createDeploymentStatusMessage(event);
                pooledConnectionHolder = jmsConnectionFactory.get().getConnectionFromPool();

                MessageProducer producer = pooledConnectionHolder.getProducer();
                Session session = pooledConnectionHolder.getSession();

                Message jmsMessage = session.createTextMessage(deploymentStatusMessage);
                producer.send(jmsMessage);
            }
        } catch (JMSException | JAXBException e) {
            //exception in here shouldn't disrupt the artifact deployment flow.
            logger.error("Error while publishing deployment status via JMS.", e);
        } finally {
            if (pooledConnectionHolder != null) {
                jmsConnectionFactory.get().returnPooledConnection(pooledConnectionHolder);
            }

        }
    }

    private String createDeploymentStatusMessage(LifecycleEvent event) throws JAXBException {
        Artifact artifact = event.getArtifact();

        DeploymentNotificationMessage message = new DeploymentNotificationMessage(artifact,
                event.getTimestamp());
        message.setArtifactKey(artifact.getKey());
        //deployer writers are expected to over-ride #toString method as specified in javadocs.
        message.setArtifactType(artifact.getType().get().toString());
        message.setLifecycleState(event.getState());
        message.setCurrentDeploymentResult(event.getDeploymentResult());
        message.setServerId(serverId);
        message.setTraceContent(event.getTraceContent());

        event.getProperties().putAll(config.getStaticMessageContent());
        message.setProperties(event.getProperties());

        return convertToXml(message, DeploymentNotificationMessage.class);
    }

    private JMSConnectionFactory getConnectionFactory() {
        String destinationJNDIName = config.getDestinationJNDIName();
        String destinationType = config.getDestinationType();
        String javaNamingProviderUrl = config.getJavaNamingProviderURL();
        String javaNamingFactoryInitial = config.getJavaNamingFactoryInitial();

        String connectionFactoryJNDIName = config.getConnectionFactoryJNDIName();
        Optional<String> username = config.getJmsUsername();
        Optional<String> password = config.getJmsPassword();

        Hashtable<String, String> properties = new Hashtable<>();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, javaNamingFactoryInitial);
        properties.put(Context.PROVIDER_URL, javaNamingProviderUrl);

        properties.put(Constants.PARAM_DESTINATION, destinationJNDIName);
        properties.put(Constants.PARAM_DEST_TYPE, destinationType);
        properties.put(Constants.PARAM_CONFAC_JNDI_NAME, connectionFactoryJNDIName);

        if (username.isPresent() && password.isPresent()) {
            properties.put(Constants.PARAM_JMS_USERNAME, username.get());
            properties.put(Constants.PARAM_JMS_PASSWORD, password.get());
        }

        return new JMSConnectionFactory(
                properties,
                connectionFactoryJNDIName,
                destinationJNDIName);

    }

    public String convertToXml(Object source, Class... type) throws JAXBException {
        String result;
        StringWriter sw = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(type);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(source, sw);
        result = sw.toString();

        return result;
    }

}
