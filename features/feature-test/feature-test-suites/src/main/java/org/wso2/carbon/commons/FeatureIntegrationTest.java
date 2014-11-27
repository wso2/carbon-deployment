/*
 *
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.extensions.LoginLogoutClient;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;
import java.util.Calendar;


/*
*
* Class to have common methods to all TestCase classes.
* This Class has to be extended by all TestCase classes.
*
* */

public class FeatureIntegrationTest {
    private static final Log log = LogFactory.getLog(FeatureIntegrationTest.class);
    protected AutomationContext automationContext;
    protected String sessionCookie;
    protected String backendURL;
    protected String webAppURL;
    protected LoginLogoutClient loginLogoutClient;
    private static int SERVICE_DEPLOYMENT_DELAY = 90 * 1000;


    /**
     * Create AutomationContext,LoginLogoutClient,sessionCookie,backendURL and webAppURL
     * with only super tenant admin.
     * @throws Exception
     */
    protected void init() throws Exception {
        automationContext = new AutomationContext("CARBON_DEPLOYMENT", TestUserMode.SUPER_TENANT_ADMIN);
        loginLogoutClient = new LoginLogoutClient(automationContext);
        sessionCookie = loginLogoutClient.login();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        webAppURL = automationContext.getContextUrls().getWebAppURL();
    }

    /**
     * Create AutomationContext,LoginLogoutClient,sessionCookie,backendURL and webAppURL
     * with multiple user modes
     * @param testUserMode multiple user modes to create Automation context
     * @throws Exception
     */
    protected void init(TestUserMode testUserMode) throws Exception {
        automationContext = new AutomationContext("CARBON_DEPLOYMENT", testUserMode);
        loginLogoutClient = new LoginLogoutClient(automationContext);
        sessionCookie = loginLogoutClient.login();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        webAppURL = automationContext.getContextUrls().getWebAppURL();
    }

    /**
     * Create AutomationContext,LoginLogoutClient,sessionCookie,backendURL and webAppURL
     * with domainKey and userKey
     * @param domainKey
     * @param userKey
     * @throws Exception
     */
    protected void init(String domainKey, String userKey) throws Exception {
        automationContext = new AutomationContext("CARBON_DEPLOYMENT","carbon_deployment", domainKey, userKey);
        loginLogoutClient = new LoginLogoutClient(automationContext);
        sessionCookie = loginLogoutClient.login();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        webAppURL = automationContext.getContextUrls().getWebAppURL();
    }


    protected String getServiceUrl(String serviceName) throws XPathExpressionException {
        return automationContext.getContextUrls().getServiceUrl() + "/" + serviceName;
    }

    /**
     * Check whether service is deployed oor not
     * @param serviceName
     * @return
     * @throws RemoteException
     */
    protected boolean isServiceDeployed(String serviceName) throws RemoteException {
        return isServiceDeployed(backendURL,
                                 sessionCookie, serviceName);
    }

    /**
     * Check whether service is deployed oor not
     * @param backEndUrl
     * @param sessionCookie
     * @param serviceName
     * @return
     * @throws RemoteException
     */
    public static boolean isServiceDeployed(String backEndUrl, String sessionCookie,
                                            String serviceName)
            throws RemoteException {
        log.info("waiting " + SERVICE_DEPLOYMENT_DELAY + " millis for Service deployment " + serviceName);
        boolean isServiceDeployed = false;
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        Calendar startTime = Calendar.getInstance();
        long time;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < SERVICE_DEPLOYMENT_DELAY) {
            if (adminServiceService.isServiceExists(serviceName)) {
                isServiceDeployed = true;
                log.info(serviceName + " Service Deployed in " + time + " millis");
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        return isServiceDeployed;
    }

    /**
     * Delete the service
     * @param serviceName
     * @throws RemoteException
     */
    protected void deleteService(String serviceName) throws RemoteException {
        ServiceAdminClient adminServiceService =
                new ServiceAdminClient(backendURL, sessionCookie);
        if (ServiceDeploymentUtil.isFaultyService(backendURL,
                                                  sessionCookie, serviceName)) {
            adminServiceService.deleteFaultyServiceByServiceName(serviceName);
        } else if (ServiceDeploymentUtil.isServiceExist(backendURL,
                                                        sessionCookie, serviceName)) {
            adminServiceService.deleteService(new String[]{adminServiceService.getServiceGroup(serviceName)});
        }
        ServiceDeploymentUtil.isServiceDeleted(backendURL, sessionCookie, serviceName);
    }

}
