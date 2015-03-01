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
import org.wso2.carbon.commons.utils.AuthenticateStubUtil;
import org.wso2.carbon.webapp.mgt.stub.WebappAdminStub;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.VersionedWebappMetadata;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappMetadata;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappUploadData;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappsWrapper;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides client for invoking WebAppAdminService
 * Can be used for web app management operation
 */
public class WebAppAdminClient {

    private final Log log = LogFactory.getLog(WebAppAdminClient.class);
    private WebappAdminStub webappAdminStub;
    private int pageNumber;

    public WebAppAdminClient(String backendUrl, String sessionCookie) throws AxisFault {
        String serviceName = "WebappAdmin";
        String endPoint = backendUrl + serviceName;
        webappAdminStub = new WebappAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, webappAdminStub);
        pageNumber = 0;
    }

    /**
     * Upload a war file
     *
     * @param filePath - file path of the war file
     * @throws RemoteException - Error when upload web applications
     */
    public void warFileUploader(String filePath) throws RemoteException, MalformedURLException {
        File file = new File(filePath);
        String fileName = file.getName();
        URL url = new URL("file://" + filePath);
        DataHandler dh = new DataHandler(url);

        WebappUploadData webApp = new WebappUploadData();
        webApp.setFileName(fileName);
        webApp.setDataHandler(dh);
        webappAdminStub.uploadWebapp(new WebappUploadData[]{webApp});

        log.info("Webapp " + fileName + "uploaded successfully");
    }

    /**
     * Get summary of web applications for the following criteria
     *
     * @param searchString - string contain in service name
     * @param webAppType   - web application type
     * @param webAppState  - web appliocation state
     * @param pageNo       - page number
     * @return WebappsWrapper - web applications summary
     * @throws RemoteException - Error when getting paged web apps summary
     */
    public WebappsWrapper getPagedWebappsSummary(String searchString, String webAppType,
                                                 String webAppState, int pageNo) throws RemoteException {
        return webappAdminStub.getPagedWebappsSummary(searchString, webAppType, webAppState, pageNo);
    }

    /**
     * All the web apps in the server which contain the search string
     *
     * @param webAppNameSearchString - web applications search string
     * @return - List of web applications which contain the search string.
     * @throws RemoteException - Error when getting paged web apps summary
     */
    public List<String> getWebApplist(String webAppNameSearchString) throws RemoteException {
        List<String> list = new ArrayList<String>();
        WebappsWrapper wrapper = getPagedWebappsSummary(webAppNameSearchString, "ALL", "ALL", pageNumber);
        VersionedWebappMetadata[] webappGroups = wrapper.getWebapps();

        if (webappGroups != null) {
            for (VersionedWebappMetadata webappGroup : webappGroups) {
                for (WebappMetadata metaData : webappGroup.getVersionGroups()) {
                    list.add(metaData.getWebappFile());
                }
            }
        }
        return list;
    }

    /**
     * Get faulty web applications with the following search criteria
     *
     * @param searchString - web application name contain string
     * @param webAppType   - web application type
     * @param pageNo       - page number
     * @return List of faulty web applications
     * @throws RemoteException - Error when getting paged faulty web apps summary
     */
    public WebappsWrapper getPagedFaultyWebappsSummary(String searchString, String webAppType,
                                                       int pageNo) throws RemoteException {
        return webappAdminStub.getPagedFaultyWebappsSummary(searchString, webAppType, pageNo);
    }

    /**
     * Get faulty web applications for a name contain string.
     *
     * @param webAppNameSearchString - Faulty web application name contain string
     * @return List<String> - List of faulty web applications
     * @throws RemoteException - Error when getting paged faulty web apps summary
     */
    public List<String> getFaultyWebAppList(String webAppNameSearchString) throws RemoteException {
        List<String> list = new ArrayList<String>();
        WebappsWrapper wrapper = getPagedFaultyWebappsSummary(webAppNameSearchString, "ALL", pageNumber);
        VersionedWebappMetadata[] webappGroups = wrapper.getWebapps();

        if (webappGroups != null && webappGroups[0].getVersionGroups() != null) {
            for (WebappMetadata metaData : webappGroups[0].getVersionGroups()) {
                list.add(metaData.getWebappFile());
            }
        }
        return list;
    }

}

