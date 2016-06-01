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
package org.wso2.carbon.javaee.tomee.osgi;

import org.apache.openejb.server.osgi.ServiceManagerExtender;
import org.osgi.framework.FrameworkUtil;

/**
 * Merger of OpenEJB ServiceManagerExtender and TomEEServiceManager
 *
 */
public class ASServiceManagerExtender extends ServiceManagerExtender {

    public ASServiceManagerExtender() {
        super(FrameworkUtil.getBundle(ASServiceManagerExtender.class).getBundleContext());
    }

    /**
     * TomEE specific accept method
     */
    @Override
    protected boolean accept(final String serviceName) {
        // managed manually or done in a different way in TomEE
        return !"httpejbd".equalsIgnoreCase(serviceName)
                && !"ejbd".equalsIgnoreCase(serviceName)
                && !"ejbds".equalsIgnoreCase(serviceName)
                && !"admin".equalsIgnoreCase(serviceName);
    }

    public void shutdown() {
        super.shutdown();
    }


}
