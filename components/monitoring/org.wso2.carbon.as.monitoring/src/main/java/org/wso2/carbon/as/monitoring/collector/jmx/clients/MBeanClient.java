/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.as.monitoring.collector.jmx.clients;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This is the base class for all the other MBean attribute readers. The subclasses can simply define
 * the attribute list, MBean name query & write the logic to extract any specific values from the
 * ObjectName.
 */
public abstract class MBeanClient {

    private static final Log LOG = LogFactory.getLog(MBeanClient.class);
    private static MBeanServer server;


    /**
     * Constructs MBean Client
     */
    public MBeanClient() {
        server = ManagementFactory.getPlatformMBeanServer();
    }


    /**
     * Get the ObjectName query which may contains wildcards which will be used to query all the MBeans
     * That matches to that query.
     *
     * @return the ObjectName query.
     */
    protected abstract String getObjectNameQuery();

    /**
     * List of attribute names that should be read from the MBeans
     *
     * @return Array of attribute names
     */
    protected abstract String[] getAttributeNames();

    /**
     * get the Attributes from the ObjectName of the MBean
     *
     * @param objectName The ObjectName of the MBean
     * @return List of Attributes
     */
    protected abstract AttributeList getPropertiesFromKey(ObjectName objectName);

    /**
     * generate a correlation key to correlate these data with the other MBean data
     *
     * @param objectName
     * @return
     */
    protected abstract String getCorrelationKey(ObjectName objectName);

    /**
     * Read the possible attribute values from the MBean
     *
     * @return List of Result objects
     */
    public List<Result> readPossibleAttributeValues() throws MalformedObjectNameException {
        ObjectName name = new ObjectName(getObjectNameQuery());
        Set<ObjectInstance> instances = server.queryMBeans(name, null);

        List<Result> results = new ArrayList<Result>();
        for (ObjectInstance instance : instances) {
            ObjectName objectName = instance.getObjectName();

            AttributeList attributes = null;
            try {
                attributes = server.getAttributes(objectName, getAttributeNames());
            } catch (InstanceNotFoundException ignored) {
                // Here we put best-effort to grab any attributes.
                // Missing objects are ignored. We need whatever possible.
                if (LOG.isDebugEnabled()) {
                    LOG.debug(objectName + " MBean not found : " + ignored.getMessage(), ignored);
                }
            } catch (ReflectionException ignored) {
                // Here we put best-effort to grab any attributes.
                // erroneous attributes are ignored. We need whatever possible.
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Exception occurred while reading the attributes from " + objectName, ignored);
                }
            }

            if (attributes != null) {
                attributes.addAll(getPropertiesFromKey(objectName));
            }

            Result result = new Result(getCorrelationKey(objectName), attributes);
            results.add(result);
        }

        return results;
    }
}
