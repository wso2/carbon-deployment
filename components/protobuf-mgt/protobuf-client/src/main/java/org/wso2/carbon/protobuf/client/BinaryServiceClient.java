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

package org.wso2.carbon.protobuf.client;

import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;

/*
 * This class keeps an RPC Channel and RPC Controller alive which is created by Activator.
 * Any class can get this channel and controller from OSGI run time and use these to
 * communicate with Binary Services Server which is running on WSO2 Application Server.
 */
public class BinaryServiceClient {
	
	private RpcChannel channel;
	private RpcController controller;

	protected BinaryServiceClient(RpcChannel channel, RpcController controller) {

		this.channel = channel;
		this.controller = controller;
	}

	public RpcChannel getRpcChannel() {
		return channel;
	}
	
	public RpcController getRpcController() {
		return controller;
	}
}