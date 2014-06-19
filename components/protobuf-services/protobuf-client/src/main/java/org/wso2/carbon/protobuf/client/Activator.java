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

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.protobuf.client.utils.ProtobufClientConfig;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.RpcSSLContext;
import com.googlecode.protobuf.pro.duplex.client.DuplexTcpClientPipelineFactory;
import com.googlecode.protobuf.pro.duplex.client.RpcClientConnectionWatchdog;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.logging.CategoryPerServiceLogger;
import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerPipelineFactory;
import com.googlecode.protobuf.pro.duplex.timeout.RpcTimeoutChecker;
import com.googlecode.protobuf.pro.duplex.timeout.RpcTimeoutExecutor;
import com.googlecode.protobuf.pro.duplex.timeout.TimeoutChecker;
import com.googlecode.protobuf.pro.duplex.timeout.TimeoutExecutor;
import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;

/*
 * This class starts an RPC Client which makes a TCP connection to 
 * the Binary Services Server that is running on WSO2 Application Server 
 * and keeps the connection alive until bundle is shut down.
 * 
 * It reads configuration information such as server name, server port, thread pool sizes etc,
 * from pbs xml which should be placed inside repository/config/etc directory of WSO2 ESB.
 * 
 * If WSO2 AS is not running when starting WSO2 ESB, RPC Client will not be started up.
 * 
 */
public class Activator implements BundleActivator {

	private static Logger log = LoggerFactory.getLogger(BinaryServiceClient.class);

	private static RpcClientChannel channel = null;
	private static RpcController controller = null;

	public void start(BundleContext bundleContext) {

		log.info("Starting Binary Service ESB Client...");

		//load configuration information from pbs xml
		ProtobufClientConfig clientConfig = new ProtobufClientConfig();

		//if start up failed due to some errors in pbs xml
		if (clientConfig.isStartUpFailed()) {
			log.info("PBS Client StartUp Failed...");
			return;
		}

		//if Binary Service ESB Client is not enabled in pbs xml
		if (!clientConfig.isEnablePbs()) {
			log.debug("Binary Services ESB Client is not enabled");
			return;
		}

		//client information
		PeerInfo client =
		                  new PeerInfo(clientConfig.getClientHostName(),
		                               clientConfig.getClientPort());
		//server information
		PeerInfo server =
		                  new PeerInfo(clientConfig.getServerHostName(),
		                               clientConfig.getServerPort());

		//It works with netty to construct TCP Channel
		DuplexTcpClientPipelineFactory clientFactory = new DuplexTcpClientPipelineFactory();
		clientFactory.setClientInfo(client);

		//if SSL encryption is enabled
		if (clientConfig.isEnableSSL()) {
			RpcSSLContext sslCtx = new RpcSSLContext();
			sslCtx.setKeystorePassword(clientConfig.getKeystorePassword());
			sslCtx.setKeystorePath(clientConfig.getKeystorePath());
			sslCtx.setTruststorePassword(clientConfig.getTruststorePassword());
			sslCtx.setTruststorePath(clientConfig.getTruststorePath());

			try {
				sslCtx.init();
			} catch (Exception e) {
				log.error("Couldn't create SSL Context : " + e.getLocalizedMessage());
				log.info("SSL not enanbled");
			}

			clientFactory.setSslContext(sslCtx);
		}

		//client will terminate after waiting this much of time
		clientFactory.setConnectResponseTimeoutMillis(10000);

		RpcTimeoutExecutor timeoutExecutor =
		                                     new TimeoutExecutor(
		                                                         clientConfig.getTimeoutExecutorCorePoolSize(),
		                                                         clientConfig.getTimeoutExecutorMaxPoolSize());
		RpcTimeoutChecker checker = new TimeoutChecker();
		checker.setTimeoutExecutor(timeoutExecutor);
		checker.startChecking(clientFactory.getRpcClientRegistry());

		// setup a RPC event listener - it just logs what happens
		RpcConnectionEventNotifier rpcEventNotifier = new RpcConnectionEventNotifier();
		RpcConnectionEventListener listener = new RpcConnectionEventListener() {

			@Override
			public void connectionReestablished(RpcClientChannel clientChannel) {
				log.info("connectionReestablished " + clientChannel);
			}

			@Override
			public void connectionOpened(RpcClientChannel clientChannel) {
				log.info("connectionOpened " + clientChannel);
			}

			@Override
			public void connectionLost(RpcClientChannel clientChannel) {
				log.info("connectionLost " + clientChannel);
			}

			@Override
			public void connectionChanged(RpcClientChannel clientChannel) {
				log.info("connectionChanged " + clientChannel);
			}
		};
		rpcEventNotifier.setEventListener(listener);
		clientFactory.registerConnectionEventListener(rpcEventNotifier);

		//creates netty bootstrap
		Bootstrap bootstrap = new Bootstrap();

		EventLoopGroup workers =
		                         new NioEventLoopGroup(
		                                               clientConfig.getChannelHandlersPoolSize(),
		                                               new RenamingThreadFactoryProxy(
		                                                                              "workers",
		                                                                              Executors.defaultThreadFactory()));

		bootstrap.group(workers);
		bootstrap.handler(clientFactory);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 0);
		bootstrap.option(ChannelOption.SO_SNDBUF, clientConfig.getChannelHandlersSendBufferSize());
		bootstrap.option(ChannelOption.SO_RCVBUF, clientConfig.getChannelHandlersRecieveBufferSize());

		//to shut down the channel gracefully
		CleanShutdownHandler shutdownHandler = new CleanShutdownHandler();
		shutdownHandler.addResource(checker);
		shutdownHandler.addResource(timeoutExecutor);
		shutdownHandler.addResource(bootstrap.group());

		try {
			//connect with server
			channel = clientFactory.peerWith(server, bootstrap);
			controller = channel.newRpcController();
			
			// Register Binary Service Client as an OSGi service
			BinaryServiceClient binaryServiceClient = new BinaryServiceClient(channel, controller);
			bundleContext.registerService(BinaryServiceClient.class.getName(), binaryServiceClient, null);
			
		} catch (IOException e) {
			//can happen if Address is already in use and so on
			String msg = "IOException " + e.getLocalizedMessage();
			log.error(msg);
		}

	}

	public void stop(BundleContext bundleContext) {
		//shut down the channel
		channel.close();
		log.info("RPC Client Shutting Down...");
	}
}
