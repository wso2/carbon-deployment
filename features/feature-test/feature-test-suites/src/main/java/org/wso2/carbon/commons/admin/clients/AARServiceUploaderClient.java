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

package org.wso2.carbon.commons.admin.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.aarservices.stub.ExceptionException;
import org.wso2.carbon.aarservices.stub.ServiceUploaderStub;
import org.wso2.carbon.aarservices.stub.types.carbon.AARServiceData;
import org.wso2.carbon.commons.utils.AuthenticateStubUtil;

import javax.activation.DataHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * Provides client to invoke ServiceUploader admin service.
 * Can be used to upload aar files.
 */
public class AARServiceUploaderClient {
    private static final Log log = LogFactory.getLog(AARServiceUploaderClient.class);
    private ServiceUploaderStub serviceUploaderStub;

    /**
     * This Contractor is for authenticating the endpoint url
     *
     * @param backEndUrl    - server back end url
     * @param sessionCookie - sessionCookie of the logged in user
     * @throws AxisFault - Error when initializing ServiceUploaderStub
     */
    public AARServiceUploaderClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String serviceName = "ServiceUploader";
        String endPoint = backEndUrl + serviceName;
        serviceUploaderStub = new ServiceUploaderStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, serviceUploaderStub);
    }

    /**
     * This method is for copying aar files to relevant folder.
     *
     * @param fileName         - Name of the file which has to be copied.
     * @param filePath         - Path of the aar file which has to be copied.
     * @param serviceHierarchy - Service hierarchy
     * @throws ExceptionException    - Error while uploading the aar file
     * @throws RemoteException       - Error while uploading the aar file
     * @throws MalformedURLException - Variable filePath is invalid
     */
    public void uploadAARFile(String fileName, String filePath,String serviceHierarchy)
            throws ExceptionException, RemoteException, MalformedURLException {
        URL url = new URL("file://" + filePath);
        AARServiceData aarServiceData;
        aarServiceData = new AARServiceData();
        aarServiceData.setFileName(fileName);
        aarServiceData.setDataHandler(createDataHandler(url));
        aarServiceData.setServiceHierarchy(serviceHierarchy);
        serviceUploaderStub.uploadService(new AARServiceData[]{aarServiceData});
    }

    /**
     * Create data handler for given url
     *
     * @param url - file path
     * @return DataHandler
     * @throws MalformedURLException - Throws if error occurred while creating data handler.
     */
    private DataHandler createDataHandler(URL url) throws MalformedURLException {
            return new DataHandler(url);
    }
}
