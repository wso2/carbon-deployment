/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
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

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.notifier.Constants;
import org.wso2.carbon.deployment.notifier.DeploymentNotifierException;

import java.util.Hashtable;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Encapsulate a JMS Connection factory.
 * <p>
 * JMS Connection Factory definitions, allows JNDI properties as well as other service
 * level parameters to be defined, and re-used by each service that binds to it
 * <p>
 * When used for sending messages out, the JMSConnectionFactory'ies are able to cache
 * a Connection, Session or Producer
 * <p>
 *
 * Borrowed generously from Apache Axi2 JMS implementation.
 */
public class JMSConnectionFactory {

    private static final Logger log = LoggerFactory.getLogger(JMSConnectionFactory.class);

    /**
     * The list of parameters from the deployment.yml
     */
    private Hashtable<String, String> parameters = new Hashtable<>();
    private String name;

    /**
     * The cached InitialContext reference
     */
    private Context context = null;
    /**
     * The JMS ConnectionFactory this definition refers to
     */
    private ConnectionFactory conFactory = null;

    /**
     * The Shared Destination
     */
    private Destination sharedDestination = null;

    private int maxConnections;

    private GenericObjectPool connectionPool;

    private String destinationName;

    /**
     * Digest a JMS CF definition and construct.
     * Set max concurrent connections to be 5 if unspecified.
     */
    public JMSConnectionFactory(Hashtable<String, String> parameters, String name, String destination) {
        this(parameters, name, destination, 5);
    }

    /**
     * Digest a JMS CF definition  'Parameter' and construct
     */
    @SuppressWarnings("unchecked")
    public JMSConnectionFactory(Hashtable<String, String> parameters, String name, String destination,
            int maxConcurrentConnections) {
        this.parameters = (Hashtable<String, String>) parameters.clone();
        this.name = name;
        this.destinationName = destination;

        if (maxConcurrentConnections > 0) {
            this.maxConnections = maxConcurrentConnections;
        }

        try {
            context = new InitialContext(parameters);
            conFactory = JMSUtils
                    .lookup(context, ConnectionFactory.class, parameters.get(Constants.PARAM_CONFAC_JNDI_NAME));
            log.info("JMS ConnectionFactory : " + name + " initialized");

        } catch (NamingException e) {
            throw new DeploymentNotifierException("Cannot acquire JNDI context, JMS Connection factory : " +
                    parameters.get(Constants.PARAM_CONFAC_JNDI_NAME) +
                    " or default destinationName : " +
                    parameters.get(Constants.PARAM_DESTINATION) +
                    " for JMS CF : " + name + " using : " + parameters, e);
        }

        createConnectionPool();
    }

    // need to initialize
    private void createConnectionPool() {
        GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
        poolConfig.minEvictableIdleTimeMillis = 3000;
        poolConfig.maxWait = 3000;
        poolConfig.maxActive = maxConnections;
        poolConfig.maxIdle = maxConnections;
        poolConfig.minIdle = 0;
        poolConfig.numTestsPerEvictionRun = Math.max(1, maxConnections / 10);
        poolConfig.timeBetweenEvictionRunsMillis = 5000;
        this.connectionPool = new GenericObjectPool(new PoolableJMSConnectionFactory(), poolConfig);

    }

    public void returnPooledConnection(JMSPooledConnectionHolder pooledConnection) {
        try {
            this.connectionPool.returnObject(pooledConnection);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Create a new MessageProducer
     *
     * @param session     Session to be used
     * @param destination Destination to be used
     * @return a new MessageProducer
     */
    private MessageProducer createProducer(Session session, Destination destination) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating a new JMS MessageProducer from JMS CF : " + name);
            }

            return JMSUtils.createProducer(session, destination, isQueue(), isJmsSpec11());

        } catch (JMSException e) {
            handleException("Error creating JMS producer from JMS CF : " + name, e);
        }
        return null;
    }

    /**
     * Get cached InitialContext
     *
     * @return cache InitialContext
     */
    @SuppressWarnings("unused")
    public Context getContext() {
        return context;
    }

    /**
     * Lookup a Destination using this JMS CF definitions and JNDI name
     *
     * @return JMS Destination for the given JNDI name or null
     */
    public synchronized Destination getDestination() {
        try {
            if (sharedDestination == null) {
                sharedDestination = JMSUtils
                        .lookupDestination(context, destinationName, parameters.get(Constants.PARAM_DEST_TYPE));
            }
            return sharedDestination;
        } catch (NamingException e) {
            handleException(
                    "Error looking up the JMS destinationName with name " + destinationName + " of type " + parameters
                            .get(Constants.PARAM_DEST_TYPE), e);
        }

        // never executes but keeps the compiler happy
        return null;
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new DeploymentNotifierException(msg, e);
    }

    /**
     * Should the JMS 1.1 API be used? - defaults to yes
     *
     * @return true, if JMS 1.1 api should  be used
     */
    public boolean isJmsSpec11() {
        return parameters.get(Constants.PARAM_JMS_SPEC_VER) == null || "1.1"
                .equals(parameters.get(Constants.PARAM_JMS_SPEC_VER));
    }

    /**
     * Return the type of the JMS CF Destination
     *
     * @return TRUE if a Queue, FALSE for a Topic and NULL for a JMS 1.1 Generic Destination
     */
    public Boolean isQueue() {
        if ("queue".equalsIgnoreCase(parameters.get(Constants.PARAM_DEST_TYPE))) {
            return true;
        } else if ("topic".equalsIgnoreCase(parameters.get(Constants.PARAM_DEST_TYPE))) {
            return false;
        } else {
            throw new DeploymentNotifierException("Invalid " + Constants.PARAM_DEST_TYPE + " : " +
                    parameters.get(Constants.PARAM_DEST_TYPE) + " for JMS CF : " + name);
        }
    }

    public Connection createConnection() {

        Connection connection = null;
        try {
            connection = JMSUtils.createConnection(conFactory, parameters.get(Constants.PARAM_JMS_USERNAME),
                    parameters.get(Constants.PARAM_JMS_PASSWORD), isJmsSpec11(), isQueue(), false, null);

            if (log.isDebugEnabled()) {
                log.debug("New JMS Connection from JMS CF : " + name + " created");
            }
        } catch (JMSException e) {
            handleException("Error acquiring a Connection from the JMS CF : " + name +
                    " using properties : " + parameters, e);
        }
        return connection;
    }

    public JMSPooledConnectionHolder getConnectionFromPool() {
        try {
            return (JMSPooledConnectionHolder) this.connectionPool.borrowObject();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @SuppressWarnings("unused")
    public synchronized void close() {

        try {
            connectionPool.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (context != null) {
            try {
                context.close();
            } catch (NamingException e) {
                log.warn("Error while closing the InitialContext of factory : " + name, e);
            }
        }
    }

    /**
     * Holder class for connections and its session/producer.
     */
    public static class JMSPooledConnectionHolder {
        private Connection connection;
        private Session session;
        private MessageProducer producer;

        public Connection getConnection() {
            return connection;
        }

        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        public Session getSession() {
            return session;
        }

        public void setSession(Session session) {
            this.session = session;
        }

        public MessageProducer getProducer() {
            return producer;
        }

        public void setProducer(MessageProducer producer) {
            this.producer = producer;
        }

    }

    /**
     * JMSConnectionFactory used by the connection pool.
     */
    private class PoolableJMSConnectionFactory implements PoolableObjectFactory {

        @Override
        public Object makeObject() throws Exception {
            Connection con = createConnection();
            try {
                Session session = JMSUtils
                        .createSession(con, false, Session.AUTO_ACKNOWLEDGE, isJmsSpec11(), isQueue());

                MessageProducer producer = createProducer(session, getDestination());

                JMSPooledConnectionHolder entry = new JMSPooledConnectionHolder();
                entry.setConnection(con);
                entry.setSession(session);
                entry.setProducer(producer);
                return entry;
            } catch (JMSException e) {
                log.error(e.getMessage(), e);
                return null;
            }

        }

        @Override
        public void destroyObject(Object o) throws Exception {
            JMSPooledConnectionHolder entry = (JMSPooledConnectionHolder) o;
            entry.getProducer().close();
            entry.getSession().close();
            entry.getConnection().close();

        }

        @Override
        public boolean validateObject(Object o) {
            return false;
        }

        @Override
        public void activateObject(Object o) throws Exception {

        }

        @Override
        public void passivateObject(Object o) throws Exception {

        }

    }

}
