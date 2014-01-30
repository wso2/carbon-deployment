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
package org.wso2.carbon.aarservices.ui.fileupload;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.aarservices.stub.types.carbon.AARServiceData;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceFileUploadExecutor extends AbstractFileUploadExecutor {

    private static final String[] ALLOWED_FILE_EXTENSIONS = new String[]{".aar"};

    public boolean execute(HttpServletRequest request, HttpServletResponse response)
            throws CarbonException, IOException {


        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "File uploading failed. No files are specified";
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                                                response, getContextRoot(request) + "/" + webContext + "/aarservices/index.jsp");
            return false;
        }

        ServiceUploaderClient serviceUploaderClient = new ServiceUploaderClient(configurationContext, serverURL, cookie);
        String msg;

        // Retrieve the set of Service Hierarchies
        Map<String, ArrayList<java.lang.String>> formFieldsMap = getFormFieldsMap();
        List<String> serviceHierarchies = formFieldsMap.get("serviceHierarchy");

        // Retrieve the set of FileItemData
        List<FileItemData> fileItems = fileItemsMap.get("aarFilename");

        List<AARServiceData> serviceDataList = new ArrayList<AARServiceData>();
        try {

            for (int i = 0; i < serviceHierarchies.size(); i++) {
                String filename = getFileName(fileItems.get(i).getFileItem().getName());
                checkServiceFileExtensionValidity(filename, ALLOWED_FILE_EXTENSIONS);

                if (!filename.endsWith(".aar")) {
                    throw new CarbonException("File with extension " + getFileName(fileItems.get(i).getFileItem().getName())
                                              + " is not supported!");
                } else {
                    AARServiceData serviceData = new AARServiceData();
                    serviceData.setFileName(filename);
                    serviceData.setServiceHierarchy(serviceHierarchies.get(i));
                    serviceData.setDataHandler(fileItems.get(i).getDataHandler());
                    serviceDataList.add(serviceData);
                }
            }

            serviceUploaderClient.uploadService(serviceDataList.toArray(new AARServiceData[serviceDataList.size()]));

            response.setContentType("text/html; charset=utf-8");
            msg = "Files have been uploaded "
                  + "successfully. Please refresh this page in a while to see "
                  + "the status of the created Axis2 service";
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request,
                                                response, getContextRoot(request) + "/" + webContext + "/service-mgt/index.jsp");
            return true;
        } catch (Exception e) {
            msg = "File upload failed. " + e.getMessage();
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                                                response, getContextRoot(request) + "/" + webContext + "/aarservices/index.jsp");
        }
        return false;
    }
}
