/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.protobuf.registry.utils;

/*
 * This class keeps constants for corresponding pbs xml elements
 */
public class ServerConfigXMLConstants {
	
	public final static String ENABLE_PBS = "enablePbsServer";
	public final static String PBSCONFIG = "pbsconfig";
	public final static String SERVER_SETTINGS = "serverSettings";
	public final static String HOST_NAME = "hostName";
	public final static String SERVER_PORT = "serverPort";
	public final static String SERVER_CALL_EXECUTOR_THREADPOOL = "serverCallExecutorThreadPool";
	public final static String CORE_POOL_SIZE = "corePoolSize";
	public final static String MAX_POOL_SIZE = "maxPoolSize";
	public final static String TIMEOUT_EXECUTOR_THREADPOOL = "timeoutExecutorThreadPool";
	public final static String TIMEOUT_EXECUTOR_THREADPOOL_KEEP_ALIVE_TIME = "keepAliveTime";
	public final static String TIMEOUT_CHECKER_THREADPOOL = "timeoutCheckerThreadPool";
	public final static String TIMEOUT_PERIOD = "period";
	public final static String TRANSPORT_SETTINGS = "transportSettings";
	public final static String ACCEPTORS = "acceptors";
	public final static String POOL_SIZE = "poolSize";
	public final static String SO_SNDBUF = "SO_SNDBUF";
	public final static String SO_RCVBUF = "SO_RCVBUF";
	public final static String CHANNEL_HANDLERS = "channelHandlers";
	public final static String TCP_NODELAY = "TCP_NODELAY";
	public final static String ENABLE_SSL = "enableSSL";

}