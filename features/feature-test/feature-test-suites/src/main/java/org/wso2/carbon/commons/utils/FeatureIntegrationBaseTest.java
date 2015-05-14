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
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.commons.admin.clients.ServiceAdminClient;
import org.wso2.carbon.extensions.LoginLogoutClient;

import javax.xml.xpath.XPathExpressionException;
import java.lang.System;
import java.rmi.RemoteException;

/*
* Base class for feature integration tests classed
* Provide environment initialization and common functionalities
*/
public class FeatureIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(FeatureIntegrationBaseTest.class);
    protected AutomationContext automationContext;
    protected String sessionCookie;
    protected String backendURL;
    protected String webAppURL;
    protected LoginLogoutClient loginLogoutClient;


    /**
     * Create AutomationContext,LoginLogoutClient,sessionCookie,backendURL and webAppURL
     * with only super tenant admin.
     *
     * @throws Exception - Error when reading xml content
     */
    protected void init() throws Exception {
        automationContext = new AutomationContext(FeatureIntegrationConstant.PRODUCT_GROUP_NAME,
                                                  TestUserMode.SUPER_TENANT_ADMIN);

        loginLogoutClient = new LoginLogoutClient(automationContext);
        sessionCookie = loginLogoutClient.login();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        webAppURL = automationContext.getContextUrls().getWebAppURL();
    }

    /**
     * Create AutomationContext,LoginLogoutClient,sessionCookie,backendURL and webAppURL
     * with multiple user modes
     *
     * @param testUserMode - multiple user modes to create Automation context
     * @throws Exception - Error when reading xml content
     */
    protected void init(TestUserMode testUserMode) throws Exception {
        automationContext =
                new AutomationContext(FeatureIntegrationConstant.PRODUCT_GROUP_NAME, testUserMode);
        loginLogoutClient = new LoginLogoutClient(automationContext);
        sessionCookie = loginLogoutClient.login();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        webAppURL = automationContext.getContextUrls().getWebAppURL();
    }

    /**
     * This method is to get service url for a service name
     *
     * @param serviceName - service name
     * @return - constructed service url
     * @throws XPathExpressionException - Error when getting context url
     */
    protected String getServiceUrl(String serviceName) throws XPathExpressionException {
        return automationContext.getContextUrls().getServiceUrl() + "/" + serviceName;
    }

    /**
     * Check whether service is deployed or not
     *
     * @param serviceName - service name
     * @return boolean - true : if service has deployed , false : if not deployed successfully
     * @throws RemoteException - Error when checking service has deployed
     */
    protected boolean isServiceDeployed(String serviceName) throws RemoteException {
        return isServiceDeployed(backendURL, sessionCookie, serviceName);
    }

    /**
     * Check whether service is deployed or not
     *
     * @param backEndUrl    - back end url of the server
     * @param sessionCookie - sessionCookie of the login
     * @param serviceName   - service name
     * @return boolean - true : if service has deployed successfully , false : if not deployed successfully
     * @throws RemoteException - Error when initializing ServiceAdminClient
     */
    public static boolean isServiceDeployed(String backEndUrl, String sessionCookie, String serviceName)
            throws RemoteException {

        log.info("waiting " + FeatureIntegrationConstant.DEPLOYMENT_DELAY_IN_MILLIS +
                 " millis for Service deployment " + serviceName);
        boolean isServiceDeployed = false;
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) < FeatureIntegrationConstant.DEPLOYMENT_DELAY_IN_MILLIS) {
            if (adminServiceService.isServiceExists(serviceName)) {
                isServiceDeployed = true;
                log.info(serviceName + " Service Deployed in " + (System.currentTimeMillis() - startTime) + " millis");
                break;
            }
        }
        return isServiceDeployed;
    }

    /**
     * Delete the service
     *
     * @param serviceName - service name
     * @throws RemoteException - Error when checking service status
     */
    protected void deleteService(String serviceName) throws RemoteException {

        ServiceAdminClient adminServiceService = new ServiceAdminClient(backendURL, sessionCookie);
        if (ServiceDeploymentUtil.isFaultyService(backendURL, sessionCookie, serviceName)) {
            adminServiceService.deleteFaultyServiceByServiceName(serviceName);
        } else if (ServiceDeploymentUtil.isServiceExist(backendURL, sessionCookie, serviceName)) {
            adminServiceService.deleteService(new String[]{adminServiceService.getServiceGroup(serviceName)});
        }
        ServiceDeploymentUtil.isServiceDeleted(backendURL, sessionCookie, serviceName);
    }

}
