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
 * This class listens to Lifecycle events of web apps and processes the wso2-web.xml config file
 * of a web application
 */
public class WebAppConfigurationListener implements LifecycleListener {
    /**
     * Overrides the lifecycleEvent method of the interface LifecycleListener
     * Catches lifecycle events of a web app and if the before_start event
     * is being fired, reads the wso2-web.xml config file and stores config data
     *
     * @param lifecycleEvent includes events starting from before_start
     */
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if (Lifecycle.BEFORE_START_EVENT.equals(lifecycleEvent.getType())) {
            Object source = lifecycleEvent.getSource();
            if (source instanceof StandardContext) {
                StandardContext context = (StandardContext) source;

                WebAppConfigurationData configData = WebAppConfigurationReader.retrieveWebConfigData(context);

                //   TODO:Store config data somewhere

                //1. Store in context - not possible as data gets cleared when the web app stops
                //2. Store in WebApplication object - WebApplication gets created at deployment but config
                //   data is needed in between. Therefore need to create the WebApplication here which doesnt look nice
                //3. Create new Classes, Config data and holder

                //Even though we go for the 3rd option still we've got a problem because
                //ASGlobalLifecycleListener needs these config data at the before_init event,
                //but before_init is not captured here
            }
        }
    }

}
