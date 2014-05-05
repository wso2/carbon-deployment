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

package org.wso2.carbon.webapp.ext.cxf.lifecycle;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.Interceptor;

import java.io.File;


public class CarbonServerLifeCycleUtil {

    public static void replaceInInterceptor(Server server, String target, Interceptor interceptor) {

        Interceptor toRemove = null;
        for (Interceptor i : server.getEndpoint().getInInterceptors()) {
            if (i.getClass().getName().equals(target)) {
                toRemove = i;
            }
        }
        //remove the interceptor
        server.getEndpoint().getInInterceptors().remove(toRemove);
        server.getEndpoint().getInInterceptors().add(interceptor);

    }

    public static void replaceInInterceptor(Server server, String target, String interceptor) {
        try {
            Interceptor in = (Interceptor) Class.forName(interceptor).newInstance();
            replaceInInterceptor(server, target, in);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
