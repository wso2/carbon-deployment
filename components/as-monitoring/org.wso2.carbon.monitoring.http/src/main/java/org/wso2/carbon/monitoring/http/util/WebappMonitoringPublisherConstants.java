/*
* Copyright 2004,2013 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.monitoring.http.util;

import org.wso2.carbon.core.RegistryResources;

public class WebappMonitoringPublisherConstants {

	public static final String CLOUD_DEPLOYMENT_PROP = "IsCloudDeployment";

	public static final String SERVER_CONFIG_BAM_URL = "BamServerURL";

	public static final String DEFAULT_BAM_SERVER_URL = "tcp://127.0.0.1:7611";

	// Registry persistence related constants
	public static final String WEBAPP_MONITORING_REG_PATH = RegistryResources.COMPONENTS
	                                                        + "org.wso2.carbon.monitoring.http/webapp_monitoring/";
	public static final String ENABLE_WEBAPP_MONITORING = "EnableWebappMonitoring";

	public static final String WEBAPP_COMMON_REG_PATH = RegistryResources.COMPONENTS
	                                                    + "org.wso2.carbon.monitoring.http/common/";

	public static final String WEBAPP_PROPERTIES_REG_PATH = RegistryResources.COMPONENTS
	                                                        + "org.wso2.carbon.monitoring.http/properties";

	public static final String ENABLE_MONITORING = "enable.statistics";
	public static final String UID_REPLACE_CHAR = "..";
	public static final String UID_REPLACE_CHAR_REGEX = "\\.\\.";
	public static final String X_FORWARDED_FOR = "X-Forwarded-For";
	public static final String UNKNOWN = "unknown";
	public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
	public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
	public static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
	public static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
	public static final String USER_AGENT = "user-agent";
	public static final String HTTP_ACCEPT_LANGUAGE = "HTTP_ACCEPT_LANGUAGE";
	public static final String REFERRER = "Referer";
	public static final String ANNONYMOUS_TENANT = "anonymous.tenant";
	public static final String ANNONYMOUS_USER = "anonymous.user";

	public static final String STREAM_NAME = "bam_http_monitoring";
	public static final String VERSION = "1.0.0";
	public static final String NICK_NAME = "WebappDataAgent";
	public static final String DISCRIPTION = "Publish webapp monitoring events";

	public static final String MONITORING_DATA_PUBLISHING = "MonitoringDataPublishing";
	public static final String ENABLE = "enable";
	public static final String BAMXML = "bam.xml";

}
