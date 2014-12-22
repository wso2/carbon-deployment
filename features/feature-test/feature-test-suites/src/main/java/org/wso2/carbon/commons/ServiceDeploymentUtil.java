/*
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
 */

package org.wso2.carbon.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.rmi.RemoteException;
import java.util.Calendar;

/**
 * This Class is for check service is deployed successfully and deleted successfully.
 */

public class ServiceDeploymentUtil {
    private static int SERVICE_DEPLOYMENT_DELAY = 90 * 1000;
    private static Log log = LogFactory.getLog(ServiceDeploymentUtil.class);

    /**
     * Check whether service is available or not
     *
     * @param backEndUrl    - server back end url
     * @param sessionCookie - login sessionCookie
     * @param serviceName   - service name
     * @return boolean - is service exist or not
     * @throws RemoteException - Error when checking service exist or not
     */
    public static boolean isServiceExist(String backEndUrl, String sessionCookie,
                                         String serviceName)
            throws RemoteException {
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        return adminServiceService.isServiceExists(serviceName);
    }

    /**
     * Check whether this service is faulty service or not
     *
     * @param backEndUrl    - server back end url
     * @param sessionCookie - login sessionCookie
     * @param serviceName   - service name
     * @return boolean - is service faulty or not
     * @throws RemoteException - Error when checking faulty service exist or not
     */
    public static boolean isFaultyService(String backEndUrl, String sessionCookie,
                                          String serviceName)
            throws RemoteException {
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        return adminServiceService.isServiceFaulty(serviceName);
    }

    /**
     * Check whether service has deleted or not
     *
     * @param backEndUrl    - server back end url
     * @param sessionCookie - login sessionCookie
     * @param serviceName   - service name
     * @return boolean - is service deleted or not
     * @throws RemoteException - Error when checking service exist or not
     */
    public static boolean isServiceDeleted(String backEndUrl, String sessionCookie,
                                           String serviceName)
            throws RemoteException {
        log.info("waiting " + SERVICE_DEPLOYMENT_DELAY + " millis for service un-deployment");
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        boolean isServiceDeleted = false;
        Calendar startTime = Calendar.getInstance();
        long time;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < SERVICE_DEPLOYMENT_DELAY) {
            if (!adminServiceService.isServiceExists(serviceName)) {
                isServiceDeleted = true;
                log.info(serviceName + " Service un-deployed in " + time + " millis");
                break;
            }
        }
        return isServiceDeleted;
    }
}
