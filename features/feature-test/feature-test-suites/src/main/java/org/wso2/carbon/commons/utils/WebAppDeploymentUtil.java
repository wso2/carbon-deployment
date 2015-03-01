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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.commons.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.commons.admin.clients.WebAppAdminClient;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;

/**
 * Provide set of utility methods to webApp deployment
 */
public class WebAppDeploymentUtil {
    private static Log log = LogFactory.getLog(WebAppDeploymentUtil.class);

    /**
     * This method is to check whether application has deployed or not
     *
     * @param backEndUrl     - back end url of the server
     * @param sessionCookie  - sessionCookie of the login
     * @param webAppFileName - web application name
     * @return - boolean for deployed or not
     * @throws RemoteException - Error while calling web app admin client
     */
    public static boolean isWebApplicationDeployed(String backEndUrl, String sessionCookie,
                                                   String webAppFileName) throws RemoteException {

        log.info("waiting " + FeatureIntegrationConstant.DEPLOYMENT_DELAY_IN_MILLIS +
                 " millis for Service deployment " + webAppFileName);

        WebAppAdminClient webAppAdminClient = new WebAppAdminClient(backEndUrl, sessionCookie);
        List<String> webAppList;
        String webAppName = webAppFileName + ".war";

        boolean isWebAppDeployed = false;
        Calendar startTime = Calendar.getInstance();
        long time;
        while (!isWebAppDeployed && (time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) <
               FeatureIntegrationConstant.DEPLOYMENT_DELAY_IN_MILLIS) {
            webAppList = webAppAdminClient.getWebApplist(webAppFileName);
            for (String weApp : webAppList) {
                if (webAppName.equalsIgnoreCase(weApp)) {
                    isWebAppDeployed = true;
                    log.info(webAppFileName + " Web Application deployed in " + time + " millis");
                    break;
                }
            }
        }
        return isWebAppDeployed;
    }

    /**
     * This method is to check whether method has un deployed or not
     *
     * @param backEndUrl     - back end url of the server
     * @param sessionCookie  - sessionCookie of the login
     * @param webAppFileName - web application name
     * @return - boolean for un deployed or not
     * @throws Exception - Error while calling web app admin client
     */
    public static boolean isWebApplicationUnDeployed(String backEndUrl, String sessionCookie,
                                                     String webAppFileName) throws Exception {
        log.info("waiting " + FeatureIntegrationConstant.DEPLOYMENT_DELAY_IN_MILLIS + " millis for webApp undeployment " + webAppFileName);
        WebAppAdminClient webAppAdminClient = new WebAppAdminClient(backEndUrl, sessionCookie);
        List<String> webAppList;

        boolean isWebAppUnDeployed = false;
        Calendar startTime = Calendar.getInstance();
        while ((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()) < FeatureIntegrationConstant.DEPLOYMENT_DELAY_IN_MILLIS) {
            webAppList = webAppAdminClient.getWebApplist(webAppFileName);
            if (webAppList.size() != 0) {
                for (String name : webAppList) {
                    if (webAppFileName.equalsIgnoreCase(name)) {
                        isWebAppUnDeployed = false;
                        log.info(webAppFileName + " -  Web Application not undeployed yet");
                        break;
                    }
                }
            } else {
                return true;
            }
        }
        return isWebAppUnDeployed;
    }

}