/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.ejbservices.util;

public class EJBConstants {
    private EJBConstants() { }

    public static final String ROOT = "/repository/components/";
    public static final String EJB_SERVICES = ROOT + "org.wso2.carbon.ejbservices/";
    public static final String APP_SERVERS = EJB_SERVICES + "app.servers/";
    public static final String CONFIGURATIONS = EJB_SERVICES + "configurations/";

    public static final class AppServerProperties {
        public static final String PROVIDER_URL = "providerURL";
        public static final String JNDI_CONTEXT_CLASS = "jndiContextClass";
        public static final String USER_NAME = "userName";
        public static final String PASSWORD = "password";
        public static final String APP_SERVER_TYPE = "appServerType";
    }

    public static final class ConfigProperties {
        public static final String SERVICE_NAME = "serviceName";
        public static final String PROVIDER_URL = "providerURL";
        public static final String JNDI_CONTEXT_CLASS = "jndiContextClass";
        public static final String USER_NAME = "userName";
        public static final String PASSWORD = "password";
        public static final String BEAN_JNDI_NAME = "beanJNDIName";
        public static final String REMOTE_INTERFACE = "remoteInterface";
        public static final String APP_SERVER_TYPE = "appServerType";
    }

    public static final class ComponentConfig {
        public static final String ELE_EJB_APP_SERVERS = "EJBApplicationServers";
        public static final String ELE_EJB_APP_SERVER = "EJBApplicationServer";
        public static final String ELE_PROVIDER_URL = "ProviderURL";
        public static final String ELE_JNDI_CONTEXT_CLASS = "JNDIContextClass";
        public static final String ELE_ID = "Id";
        public static final String ELE_NAME = "Name";
        public static final String EJB_APP_SERVERS = ELE_EJB_APP_SERVERS;
    }
}
