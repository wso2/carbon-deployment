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

package org.wso2.carbon.protobuf.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.protobuf.registry.utils.ServerConfig;

import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.RpcSSLContext;
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
 * This class starts an RPC server and register its registry as an OSGI service
 * for binary services.
 * 
 * It reads configuration information from pbs xml which should be placed
 * inside AS's components/repository/lib directory.
 */

public class Activator implements BundleActivator {

	private static Logger log = LoggerFactory.getLogger(BinaryServiceRegistry.class);

	static DuplexTcpServerPipelineFactory serverFactory;

	public void start(BundleContext bundleContext) {

		log.info("Starting Binary Service Server...");

		ServerConfig serverConfig = new ServerConfig();

		if (serverConfig.isStartUpFailed()) {
			log.info("Binary Service Server StartUp Failed...");
			return;
		}

		if (!serverConfig.isEnablePbs()) {
			log.debug("Binary Service Server is not enabled in pbs xml");
			return;
		}

		// server information
		PeerInfo serverInfo = new PeerInfo(serverConfig.getHostName(), serverConfig.getServerPort());

		RpcServerCallExecutor executor = new ThreadPoolCallExecutor(serverConfig.getServerCallExecutorCorePoolSize(), serverConfig.getServerCallExecutorMaxPoolSize(), serverConfig.getServerCallExecutorMaxPoolTimeout(), TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000), Executors.defaultThreadFactory());

		serverFactory = new DuplexTcpServerPipelineFactory(serverInfo);

		serverFactory.setRpcServerCallExecutor(executor);

		// if SSL encryption is enabled
		if (serverConfig.isEnableSSL()) {
			RpcSSLContext sslCtx = new RpcSSLContext();
			sslCtx.setKeystorePassword(serverConfig.getKeystorePassword());
			sslCtx.setKeystorePath(serverConfig.getKeystorePath());
			sslCtx.setTruststorePassword(serverConfig.getTruststorePassword());
			sslCtx.setTruststorePath(serverConfig.getTruststorePath());

			try {
				sslCtx.init();
			} catch (Exception e) {
				log.error("Couldn't create SSL Context : " + e.getLocalizedMessage());
				log.info("SSL not enanbled");
			}

			serverFactory.setSslContext(sslCtx);
		}

		RpcTimeoutExecutor timeoutExecutor = new TimeoutExecutor(serverConfig.getTimeoutExecutorCorePoolSize(), serverConfig.getTimeoutExecutorMaxPoolSize());
		RpcTimeoutChecker timeoutChecker = new TimeoutChecker();
		timeoutChecker.setTimeoutExecutor(timeoutExecutor);
		timeoutChecker.startChecking(serverFactory.getRpcClientRegistry());

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
		serverFactory.registerConnectionEventListener(rpcEventNotifier);

		//Binary Services Server Logger
		CategoryPerServiceLogger logger = new CategoryPerServiceLogger();
		logger.setLogRequestProto(serverConfig.isLogReqProto());
		logger.setLogResponseProto(serverConfig.isLogResProto());
		logger.setLogEventProto(serverConfig.isLogEventProto());
		
		if(!serverConfig.isLogEventProto() && !serverConfig.isLogReqProto() && !serverConfig.isLogResProto()){
			serverFactory.setLogger(null);
		} else {
			serverFactory.setLogger(logger);
		}
		
		// Configure the server.
		ServerBootstrap bootstrap = new ServerBootstrap();
		NioEventLoopGroup boss = new NioEventLoopGroup(serverConfig.getAcceptorsPoolSize(), new RenamingThreadFactoryProxy("boss", Executors.defaultThreadFactory()));
		NioEventLoopGroup workers = new NioEventLoopGroup(serverConfig.getChannelHandlersPoolSize(), new RenamingThreadFactoryProxy("worker", Executors.defaultThreadFactory()));
		bootstrap.group(boss, workers);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.option(ChannelOption.SO_SNDBUF, serverConfig.getAcceptorsSendBufferSize());
		bootstrap.option(ChannelOption.SO_RCVBUF, serverConfig.getAcceptorsRecieveBufferSize());
		bootstrap.childOption(ChannelOption.SO_RCVBUF, serverConfig.getChannelHandlersRecieveBufferSize());
		bootstrap.childOption(ChannelOption.SO_SNDBUF, serverConfig.getChannelHandlersSendBufferSize());
		bootstrap.option(ChannelOption.TCP_NODELAY, serverConfig.isTCP_NODELAY());
		bootstrap.childHandler(serverFactory);
		bootstrap.localAddress(serverInfo.getPort());

		CleanShutdownHandler shutdownHandler = new CleanShutdownHandler();
		shutdownHandler.addResource(boss);
		shutdownHandler.addResource(workers);
		shutdownHandler.addResource(executor);
		shutdownHandler.addResource(timeoutChecker);
		shutdownHandler.addResource(timeoutExecutor);

		// Bind and start to accept incoming connections.
		bootstrap.bind();

		log.info("Serving " + serverInfo);

		// Register Binary Service Registry as an OSGi service
		BinaryServiceRegistry binaryServiceRegistry = new BinaryServiceRegistry(serverFactory);
		bundleContext.registerService(BinaryServiceRegistry.class.getName(), binaryServiceRegistry, null);

	}

	public void stop(BundleContext bundleContext) {
		log.info("Binary Service Server Shutting Down...");
	}
}
