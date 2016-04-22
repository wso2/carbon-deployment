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
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.DeploymentConfigurationProvider;
import org.wso2.carbon.deployment.engine.LifecycleEvent;
import org.wso2.carbon.deployment.engine.LifecycleListener;
import org.wso2.carbon.deployment.engine.config.DeploymentNotifierConfig;
import org.wso2.carbon.deployment.notifier.internal.DataHolder;
import org.wso2.carbon.deployment.notifier.internal.DeploymentNotificationMessage;

import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
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

    private DeploymentNotifierConfig config;

    private Session session;
    private MessageProducer producer;

    String serverId;

    public DeploymentNotifierLifecycleListener() {
        config = DeploymentConfigurationProvider.
                getDeploymentConfiguration().getDeploymentNotifier();
        serverId = DataHolder.getInstance().getCarbonRuntime().getConfiguration().getId();
    }

    /**
     * todo javadocs
     * @param event The lifecycle event. The Artifact object and
     *              the currently triggered lifecycle event is stored
     */
    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        try {
            if (!config.isJmsPublishingEnabled()) {
                return;
            }

            logger.debug("Invoked DeploymentNotifierLifecycleListener");
            if (LifecycleEvent.STATE.AFTER_START_EVENT.equals(event.getState()) ||
                    LifecycleEvent.STATE.AFTER_UPDATE_EVENT.equals(event.getState())) {
                String deploymentStatusMessage = createDeploymentStatusMessage(event);
                initConnectionFactory(); //todo

                Message jmsMessage = session.createTextMessage(deploymentStatusMessage);
                producer.send(jmsMessage);
            }
        } catch (JMSException | JAXBException | NamingException e) {
            //exception in here shouldn't disrupt the artifact deployment flow.
            logger.error("Error while publishing deployment status via JMS.", e);
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

    private void initConnectionFactory() throws NamingException, JMSException {
        DeploymentNotifierConfig config = DeploymentConfigurationProvider.
                getDeploymentConfiguration().getDeploymentNotifier();

        String destinationJNDIName = config.getDestinationJNDIName();
        String destinationType = config.getDestinationType();
        String javaNamingProviderUrl = config.getJavaNamingProviderURL();
        String javaNamingFactoryInitial = config.getJavaNamingFactoryInitial();

        String connectionFactoryJNDIName = config.getConnectionFactoryJNDIName();
        Optional<String> username = config.getJmsUsername();
        Optional<String> password = config.getJmsPassword();

        Hashtable<String, String> properties = new Hashtable<>();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, javaNamingFactoryInitial);
        properties.put(Context.PROVIDER_URL, javaNamingProviderUrl); //todo take destination type into account

        InitialContext context = new InitialContext(properties);
        ConnectionFactory connectionFactory = lookup(context, ConnectionFactory.class, connectionFactoryJNDIName);
        Destination destination = lookupDestination(context, destinationJNDIName, destinationType);

        Connection connection;
        if (username.isPresent() && password.isPresent()) {
            //todo check the affect of doing this for each deployment. close after message is sent
            connection = connectionFactory.createConnection(username.get(), password.get());
        } else {
            connection = connectionFactory.createConnection();
        }
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        producer = session.createProducer(destination);

    }

    private <T> T lookup(Context context, Class<T> clazz, String name) throws NamingException {

        Object object = context.lookup(name);
        try {
            return clazz.cast(object);
        } catch (ClassCastException ex) {
            // Instead of a ClassCastException, throw an exception with some
            // more information.
            if (object instanceof Reference) {
                Reference ref = (Reference) object;
                throw new NamingException("JNDI failed to de-reference Reference with name " +
                        name + "; is the factory " + ref.getFactoryClassName() +
                        " in your classpath?");
            } else {
                throw new NamingException("JNDI lookup of name " + name + " returned a " +
                        object.getClass().getName() + " while a " + clazz + " was expected");
            }
        }

    }

    /**
     * Return the JMS destination with the given destination name looked up from the context
     * Borrowed generiously from axis2 jms transport implementation
     *
     * @param context the Context to lookup
     * @param destinationName name of the destination to be looked up
     * @param destinationType type of the destination to be looked up
     * @return the JMS destination, or null if it does not exist
     */
    public Destination lookupDestination(Context context, String destinationName,
            String destinationType) throws NamingException {
        try {
            return lookup(context, Destination.class, destinationName);
        } catch (NameNotFoundException e) {
            Properties initialContextProperties = new Properties();
            if (context.getEnvironment() != null) {
                if (context.getEnvironment().get(Context.INITIAL_CONTEXT_FACTORY) != null) {
                    initialContextProperties.put(Context.INITIAL_CONTEXT_FACTORY,
                            context.getEnvironment().get(Context.INITIAL_CONTEXT_FACTORY));
                }
                if (context.getEnvironment().get(Context.PROVIDER_URL) != null) {
                    initialContextProperties
                            .put(Context.PROVIDER_URL, context.getEnvironment().get(Context.PROVIDER_URL));
                }
            }
            if (Constants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType)) {
                initialContextProperties.put(Constants.TOPIC_PREFIX + destinationName, destinationName);
            } else {
                initialContextProperties.put(Constants.QUEUE_PREFIX + destinationName, destinationName);
            }
            InitialContext initialContext = new InitialContext(initialContextProperties);
            try {
                return lookup(initialContext, Destination.class, destinationName);
            } catch (NamingException e1) {
                return lookup(context, Destination.class,
                        (Constants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType) ?
                                "dynamicTopics/" :
                                "dynamicQueues/") + destinationName);
            }
        }
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
