/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.jarservices.ui.fileupload;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.jarservices.stub.JarServiceCreatorAdminStub;
import org.wso2.carbon.jarservices.stub.JarUploadExceptionException;
import org.wso2.carbon.jarservices.stub.DuplicateServiceGroupExceptionException;
import org.wso2.carbon.jarservices.stub.DuplicateServiceExceptionException;
import org.wso2.carbon.jarservices.stub.types.UploadArtifactsResponse;
import org.wso2.carbon.jarservices.stub.types.Resource;
import org.wso2.carbon.jarservices.stub.types.Service;

import java.rmi.RemoteException;

public class JarServiceAdminClient {
    private static final Log log = LogFactory.getLog(JarServiceAdminClient.class);

    private JarServiceCreatorAdminStub stub;

    public JarServiceAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        String epr = serverURL + "JarServiceCreatorAdmin";
        stub = new JarServiceCreatorAdminStub(ctx, epr);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
    }

    public UploadArtifactsResponse upload(String serviceGroupName,
                                          Resource wsdl,
                                          Resource[] resources)
            throws JarUploadExceptionException, DuplicateServiceGroupExceptionException,
                   RemoteException {
        return stub.upload(serviceGroupName, wsdl, resources);
    }

    public Service[] getClassMethods(String directoryPath, Service[] classes)
            throws DuplicateServiceExceptionException, RemoteException {
        return stub.getClassMethods(directoryPath, classes);
    }

    public void createAndDeployService(String directoryPath,
                                       String serviceHierarchy,
                                       String serviceGroupName,
                                       Service[] methods)
            throws DuplicateServiceExceptionException, DuplicateServiceGroupExceptionException,
                   RemoteException {
        stub.createAndDeployService(directoryPath, serviceHierarchy, serviceGroupName, methods);
    }
}
