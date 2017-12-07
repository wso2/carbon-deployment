/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.deployment.notifier.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.encoder.Encode;
import org.wso2.carbon.deployment.notifier.Constants;

import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;

/**
 * Miscallaneous methods used for the JMS transport.
 *
 * Borrowed generously from Apache Axi2 JMS implementation.
 */
public class JMSUtils {

    private static final Log log = LogFactory.getLog(JMSUtils.class);

    public static <T> T lookup(Context context, Class<T> clazz, String name) throws NamingException {

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
     * Return the JMS destination with the given destination name looked up from the context.
     * Borrowed generiously from axis2 jms transport implementation
     *
     * @param context the Context to lookup
     * @param destinationName name of the destination to be looked up
     * @param destinationType type of the destination to be looked up
     * @return the JMS destination, or null if it does not exist
     */
    public static Destination lookupDestination(Context context, String destinationName,
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

    /**
     * This is a JMS spec independent method to create a Session. Please be cautious when
     * making any changes
     *
     * @param connection the JMS Connection
     * @param transacted should the session be transacted?
     * @param ackMode    the ACK mode for the session
     * @param jmsSpec11  should we use the JMS 1.1 API?
     * @param isQueue    is this Session to deal with a Queue?
     * @return a Session created for the given information
     * @throws javax.jms.JMSException on errors, to be handled and logged by the caller
     */
    public static Session createSession(Connection connection, boolean transacted, int ackMode,
            boolean jmsSpec11, Boolean isQueue) throws JMSException {

        if (jmsSpec11 || isQueue == null) {
            return connection.createSession(transacted, ackMode);

        } else {
            if (isQueue) {
                return ((QueueConnection) connection).createQueueSession(transacted, ackMode);
            } else {
                return ((TopicConnection) connection).createTopicSession(transacted, ackMode);
            }
        }
    }

    /**
     * This is a JMS spec independent method to create a Connection. Please be cautious when
     * making any changes
     *
     * @param conFac    the ConnectionFactory to use
     * @param user      optional user name
     * @param pass      optional password
     * @param jmsSpec11 should we use JMS 1.1 API ?
     * @param isQueue   is this to deal with a Queue?
     * @return a JMS Connection as requested
     * @throws javax.jms.JMSException on errors, to be handled and logged by the caller
     */
    public static Connection createConnection(ConnectionFactory conFac,
            String user, String pass, boolean jmsSpec11,
            Boolean isQueue,
            boolean isDurable, String clientID)
            throws JMSException {

        Connection connection = null;
        if (log.isDebugEnabled()) {
            log.debug(getEncodedString("Creating a " + (isQueue ? "Queue" : "Topic") +
                    "Connection using credentials : (" + user + "/" + pass + ")"));
        }

        if (jmsSpec11 || isQueue == null) {
            if (user != null && pass != null) {
                connection = conFac.createConnection(user, pass);
            } else {
                connection = conFac.createConnection();
            }
            if (isDurable) {
                connection.setClientID(clientID);
            }

        } else {
            QueueConnectionFactory qConFac = null;
            TopicConnectionFactory tConFac = null;
            if (isQueue) {
                qConFac = (QueueConnectionFactory) conFac;
            } else {
                tConFac = (TopicConnectionFactory) conFac;
            }

            if (user != null && pass != null) {
                if (qConFac != null) {
                    connection = qConFac.createQueueConnection(user, pass);
                } else if (tConFac != null) {
                    connection = tConFac.createTopicConnection(user, pass);
                }
            } else {
                if (qConFac != null) {
                    connection = qConFac.createQueueConnection();
                } else if (tConFac != null) {
                    connection = tConFac.createTopicConnection();
                }
            }
            if (isDurable) {
                connection.setClientID(clientID);
            }
        }
        return connection;
    }

    /**
     * This is a JMS spec independent method to create a MessageProducer. Please be cautious when
     * making any changes
     *
     * @param session     JMS session
     * @param destination the Destination
     * @param isQueue     is the Destination a queue?
     * @param jmsSpec11   should we use JMS 1.1 API ?
     * @return a MessageProducer to send messages to the given Destination
     * @throws javax.jms.JMSException on errors, to be handled and logged by the caller
     */
    public static MessageProducer createProducer(
            Session session, Destination destination, Boolean isQueue, boolean jmsSpec11)
            throws JMSException {

        if (jmsSpec11 || isQueue == null) {
            return session.createProducer(destination);
        } else {
            if (isQueue) {
                return ((QueueSession) session).createSender((Queue) destination);
            } else {
                return ((TopicSession) session).createPublisher((Topic) destination);
            }
        }
    }

    private static String getEncodedString(String str) {
        String cleanedString = str.replace('\n', '_').replace('\r', '_');
        cleanedString = Encode.forHtml(cleanedString);
        if (!cleanedString.equals(str)) {
            cleanedString += " (Encoded)";
        }
        return cleanedString;
    }
}
