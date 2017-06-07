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

/**
 * Constants for deployment notifier.
 *
 * @since 5.1.0
 *
 */
public final class Constants {

    public static final String DESTINATION_TYPE_TOPIC = "topic";

    public static final String QUEUE_PREFIX = "queue.";

    public static final String TOPIC_PREFIX = "topic.";

    /**
     * The Parameter name indicating the JMS connection factory JNDI name.
     */
    public static final String PARAM_CONFAC_JNDI_NAME = "connectionFactoryJNDIName";

    /**
     * The parameter indicating the JMS API specification to be used.
     * if this is "1.1" the JMS 1.1 API would be used, else the JMS 1.0.2B
     */
    public static final String PARAM_JMS_SPEC_VER = "jmsSpecVersion";

    /**
     * The username to use when obtaining a JMS Connection.
     */
    public static final String PARAM_JMS_USERNAME = "jmsUserName";
    /**
     * The password to use when obtaining a JMS Connection.
     */
    public static final String PARAM_JMS_PASSWORD = "jmsPassword";

    /**
     * The Service level Parameter name indicating the JMS destination for requests of a service.
     */
    public static final String PARAM_DESTINATION = "destinationJNDIName";

    /**
     * The Service level Parameter name indicating the destination type for requests.
     */
    public static final String PARAM_DEST_TYPE = "destinationType";



}
