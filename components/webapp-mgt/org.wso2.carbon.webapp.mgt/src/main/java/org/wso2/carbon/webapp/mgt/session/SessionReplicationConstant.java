/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.webapp.mgt.session;

public final class SessionReplicationConstant {
	private SessionReplicationConstant() {

	}

	/**
	 * Session replication init params
	 */
	public static final String HZ_CLUSTER_NAME = "_web_8d201e19-aead-4359-8dca-d99f5d869991_WSO2";
	public static final boolean HZ_SESSION_COOKIE_SECURE = false;
	public static final boolean HZ_SESSION_COOKIE_HTTP_ONLY = false;
	public static final boolean HZ_STICKY_SESSION = true;
	public static final boolean HZ_SHUTDOWN_ON_DESTROY = true;
	public static final boolean HZ_DIFERRED_WRITE = false;
	public static final String HZ_SESSION_TTL_SECONDS= null;
	public static final String HZ_SESSION_COOKIE_DOMAIN= null;
	public static final String HZ_INSTANCE_NAME="default";
	public static final String HZ_COOKIE_NAME="hazelcast.sessionId";
	public static final boolean HZ_DEBUG=true;
	public static final String USER_CLIENT="false";



}
