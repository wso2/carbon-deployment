/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.webapp.mgt.ext;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * //todo
 */
public abstract class CarbonLifecycleListenerBase implements LifecycleListener {

    private static final Log log = LogFactory.getLog(CarbonLifecycleListenerBase.class);

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        //todo register LC listeners by default - include in context.xml

        //todo filter LC events - done
        if (Lifecycle.AFTER_START_EVENT.equals(lifecycleEvent.getType())) {
            if (log.isDebugEnabled()) {
                log.debug("Received LC event of type : ".concat(lifecycleEvent.getType()));
            }

            if (lifecycleEvent.getSource() instanceof StandardContext) {
                StandardContext context = (StandardContext) lifecycleEvent.getSource();
                lifecycleEvent(context, getAppInfo(context));
            }
        }
    }

    public abstract void lifecycleEvent(StandardContext context, ApplicationInfo applicationInfo);

    protected void setAppInfo(StandardContext context, ApplicationInfo applicationInfo) {
        context.getServletContext().setAttribute("APP_INFO", applicationInfo);
    }

    protected ApplicationInfo getAppInfo(StandardContext context) {
        return (ApplicationInfo) context.getServletContext().getAttribute("APP_INFO");
    }
}
