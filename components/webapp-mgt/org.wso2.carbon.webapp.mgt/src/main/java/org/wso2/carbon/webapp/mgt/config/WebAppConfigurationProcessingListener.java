/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.webapp.mgt.config;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.webapp.mgt.DataHolder;
import org.wso2.carbon.webapp.mgt.utils.WebAppConfigurationUtils;

import java.io.IOException;
import java.util.List;

/**
 * This class listens to Lifecycle events of web apps and processes the wso2as-web.xml config file
 * of a web application.
 */
public class WebAppConfigurationProcessingListener implements LifecycleListener {
    private static final Log log = LogFactory.getLog(WebAppConfigurationProcessingListener.class);

    /**
     * Overrides the lifecycleEvent method of the interface LifecycleListener
     * Catches lifecycle events of a web app and if the before_start event
     * is being fired, reads the wso2as-web.xml config file and stores config data
     *
     * @param lifecycleEvent includes events starting from before_start
     */
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if (Lifecycle.BEFORE_START_EVENT.equals(lifecycleEvent.getType())) {
            String webAppFilePath;
            Object source = lifecycleEvent.getSource();
            if (source instanceof StandardContext) {
                StandardContext context = (StandardContext) source;

                try {
                    webAppFilePath = WebAppConfigurationUtils.getWebAppFilePath(context);

                    WebAppConfigurationData configData = WebAppConfigurationReader.retrieveWebConfigData(context);

                    WebAppConfigurationService service = DataHolder.getWebAppConfigurationService();

                    if (service != null) {
                        if (configData != null) {
                            service.addConfiguration(webAppFilePath, configData);

                            if (log.isDebugEnabled()) {
                                log.debug("Configuration data stored for " + webAppFilePath);

                                log.debug("single-sign-on:" + configData.isSingleSignOnEnabled());
                                log.debug("streamID:" + configData.getStatisticsPublisher().getStreamId());
                                log.debug("enabled:" + configData.getStatisticsPublisher().isEnabled());
                                log.debug("parentFirst:" + configData.isParentFirst());
                                List<String> environments = configData.getEnvironments();
                                for (String env : environments) {
                                    log.debug("environment:" + env);
                                }
                                log.debug("web-service-discovery:" + configData.isWebServiceDiscoveryEnabled());
                                log.debug("is-managed-api:" + configData.getRestWebServices().isManagedApi());

                            }
                        } else {
                            log.error("Could not store configuration data. Data object is null");
                        }

                    }
                } catch (IOException e) {
                    log.error("Error while reading configuration file. " + e.getMessage(), e);
                }
            }
        }
    }

}
