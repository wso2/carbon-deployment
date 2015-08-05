/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.javaee.tomee;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.util.ServerInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openejb.classloader.ClassLoaderConfigurer;
import org.apache.openejb.config.QuickJarsTxtParser;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JuliLogStreamFactory;
import org.apache.openejb.util.OpenEjbVersion;
import org.apache.tomee.TomEELogConfigurer;
import org.apache.tomee.catalina.ServerListener;
import org.apache.tomee.loader.TomcatHelper;
import org.wso2.carbon.base.ServerConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AS baked ServerListener. Borrowed large parts of the code from TomEE
 * where TomEE doesn't provide extension points.
 * <p/>
 * Changes can be found between the comments
 * "WSO2 START PATCH" and "WSO2 END PATCH"
 * <p/>
 * In addition, java util logging has been changed to commons logging
 */
public class ASTomEEServerListener extends ServerListener {

	private static final Log log = LogFactory.getLog(ASTomEEServerListener.class.getName());

	//static private boolean listenerInstalled;
	private static final AtomicBoolean listenerInstalled = new AtomicBoolean(false);

	// ###### WSO2 START PATCH ###### //
	//Added the synchronization because of WSAS-2035. Remove when this is fixed properly in TomEE 1.7.3
	static {
		synchronized (System.getProperties()) {
			SystemInstance.get();
		}
	}
	// ###### WSO2 END PATCH ###### //

	public void lifecycleEvent(LifecycleEvent event) {
		// Bootstrap
		install(event);

		// Notify
		SystemInstance.get().fireEvent(event);
	}

	private void install(final LifecycleEvent event) {
		if (Lifecycle.BEFORE_INIT_EVENT.equals(event.getType()) && StandardServer.class.isInstance(event.getSource())) {
			installServerInfo();
		}

		synchronized (listenerInstalled) {

			// only install once
			if (listenerInstalled.get() || !Lifecycle.AFTER_INIT_EVENT.equals(event.getType())) {
				return;
			}
			if (!(event.getSource() instanceof StandardServer)) {
				return;
			}

			try {
				final StandardServer server = (StandardServer) event.getSource();

				TomcatHelper.setServer(server);

				final Properties properties = new Properties();
				System.getProperties().setProperty("openejb.embedder.source", getClass().getSimpleName());
				properties.setProperty("openejb.embedder.source", getClass().getSimpleName());

				// if SystemInstance is already initialized, then return
				if (SystemInstance.isInitialized()) {
					return;
				}

				properties.setProperty("tomee.webapp.classloader.enrichment.skip", "true");

				// set the openejb.loader property to tomcat-system
				properties.setProperty("openejb.loader", "tomcat-system");

				properties.setProperty("openejb.system.apps", "false");
				// Get the value of catalina.home and set it to openejb.home
				String catalinaHome = System.getProperty("catalina.base");
				properties.setProperty("openejb.home", catalinaHome);

				//Sets system property for openejb.home
				System.setProperty("openejb.home", catalinaHome);

				//get the value of catalina.base and set it to openejb.base
				String catalinaBase = System.getProperty("catalina.base");
				properties.setProperty("openejb.base", catalinaBase);

				//Sets system property for openejb.base
				System.setProperty("openejb.base", catalinaBase);

				properties.setProperty("openejb.cxf.jmx", "false");

				// System.setProperty("tomcat.version", "x.y.z.w");
				// System.setProperty("tomcat.built", "mmm dd yyyy hh:mm:ss");
				// set the System properties, tomcat.version, tomcat.built
				final ClassLoader classLoader = ServerListener.class.getClassLoader();
				try {
					final Properties tomcatServerInfo = IO.readProperties(
							classLoader.getResourceAsStream("org/apache/catalina/util/ServerInfo.properties"),
							new Properties());

					String serverNumber = tomcatServerInfo.getProperty("server.number");
					if (serverNumber == null) {
						// Tomcat5 only has server.info
						final String serverInfo = tomcatServerInfo.getProperty("server.info");
						if (serverInfo != null) {
							final int slash = serverInfo.indexOf('/');
							serverNumber = serverInfo.substring(slash + 1);
						}
					}
					if (serverNumber != null) {
						System.setProperty("tomcat.version", serverNumber);
					}

					final String serverBuilt = tomcatServerInfo.getProperty("server.built");
					if (serverBuilt != null) {
						System.setProperty("tomcat.built", serverBuilt);
					}
				} catch (final Throwable e) {
					// no-op
				}

				// manage additional libraries
				if (URLClassLoader.class.isInstance(classLoader)) {
					final URLClassLoader ucl = URLClassLoader.class.cast(classLoader);
					try {
						final Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
						final boolean acc = addUrl.isAccessible();
						try {
							for (final File f : ProvisioningUtil.addAdditionalLibraries()) {
								addUrl(ucl, addUrl, f.toURI().toURL());
							}

							final File globalJaxrsTxt = SystemInstance.get().getConf(QuickJarsTxtParser.FILE_NAME);
							final ClassLoaderConfigurer configurer = QuickJarsTxtParser.parse(globalJaxrsTxt);
							if (configurer != null) {
								for (final URL f : configurer.additionalURLs()) {
									addUrl(ucl, addUrl, f);
								}
							}
						} finally {
							addUrl.setAccessible(acc);
						}
					} catch (final Exception e) {
						log.error(e.getMessage(), e);
					}
				} else {
					// ###### WSO2 START PATCH ###### //
					// Change log level from info to debug
					log.debug("container classloader is not an URL one so can't check provisining: " + classLoader);
					// ###### WSO2 END PATCH ###### //
				}

				// ###### WSO2 START PATCH ###### //
				setServiceManager(properties);
				setOpenJPALogFactory();
				readSystemPropertiesConf();
				ASTomcatLoader loader = new ASTomcatLoader();
				loader.initSystemInstance(properties);
				loader.initialize(properties);
				// ###### WSO2 END PATCH ###### //

				TomEELogConfigurer.configureLogs();

				listenerInstalled.set(true);
			} catch (final Exception e) {
				log.error("TomEE Listener can't start OpenEJB", e);
				// e.printStackTrace(System.err);
			}
		}
	}

	private static void addUrl(final URLClassLoader ucl, final Method addUrl, final URL url)
			throws IllegalAccessException, InvocationTargetException, MalformedURLException {
		if (!addUrl.isAccessible()) { // set it lazily
			addUrl.setAccessible(true);
		}
		addUrl.invoke(ucl, url);
	}

	/**
	 * Set AS specific OpenJPA logger since OpenJPA logging is broken with default implementation.
	 * Borrowed the segment from a static block in JuliLogStreamFactory.
	 */
	protected void setOpenJPALogFactory() {
		try {
			JuliLogStreamFactory.class.getClassLoader()
			                          .loadClass("org.wso2.carbon.javaee.tomee.openjpa.JULOpenJPALogFactory");
			//System.setProperty("openjpa.Log", "org.apache.openejb.openjpa.JULOpenJPALogFactory"); //the default
			System.setProperty("openjpa.Log", "org.wso2.carbon.javaee.tomee.openjpa.JULOpenJPALogFactory");
		} catch (Exception ignored) {
			log.debug(ignored.getMessage(), ignored);
			// no-op: openjpa is not at the classpath so don't trigger it loading with our logger
		}
	}

	protected void setServiceManager(Properties properties) {
		properties.put("openejb.service.manager.class", "org.wso2.carbon.javaee.tomee.osgi.ASServiceManagerExtender");
	}

	private synchronized void installServerInfo() {
		//        if (SystemInstance.get().getOptions().get("tomee.keep-server-info", false)) {
		//            return;
		//        }

		// force static init
		final String value = ServerInfo.getServerInfo();

		Field field = null;
		boolean acc = true;
		try {
			field = ServerInfo.class.getDeclaredField("serverInfo");
			acc = field.isAccessible();
			final int slash = value.indexOf('/');
			field.setAccessible(true);
			final String version = OpenEjbVersion.get().getVersion();
			final String tomeeVersion = (Integer.parseInt(Character.toString(version.charAt(0))) - 3) +
			                            version.substring(1, version.length());
                        // ###### WSO2 START PATCH ###### //
                        // adding wso2 as signature to the page footer
                        final String asVersion = ServerConfiguration.getInstance().getFirstProperty("Version");
                        field.set(null, "WSO2 AS " + asVersion + " (" +
                                  value.substring(0, slash) + " " + value.substring(slash+1) +
                                  "/TomEE " + tomeeVersion + ")");
                        // ###### WSO2 END PATCH ###### //

		} catch (final Exception e) {
			// no-op
		} finally {
			if (field != null) {
				field.setAccessible(acc);
			}
		}
	}

	private void readSystemPropertiesConf() {
		String systemPropertiesPath = new File(System.getProperty("carbon.home")).getAbsolutePath() +
		                              File.separator + "repository" + File.separator + "conf" + File.separator +
		                              "tomee" + File.separator + "system.properties";

		File file = new File(systemPropertiesPath);
		if (!file.exists()) {
			return;
		}

		final Properties systemProperties;
		try {
			systemProperties = IO.readProperties(file);
		} catch (IOException e) {
			return;
		}

		SystemInstance.get().getProperties().putAll(systemProperties);
	}

}
