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
package org.wso2.carbon.module.mgt.ui.fileupload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.module.mgt.ui.client.ModuleManagementClient;
import org.wso2.carbon.module.mgt.stub.types.ModuleUploadData;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModuleFileUploadExecutor extends AbstractFileUploadExecutor {

    private static final String[] ALLOWED_FILE_EXTENSIONS =
            new String[]{".mar"};

    private static final Log log = LogFactory.getLog(ModuleFileUploadExecutor.class);

    public boolean execute(HttpServletRequest request, HttpServletResponse response)
            throws CarbonException, IOException {


        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "File uploading failed.";
            log.error(msg);
            response.sendRedirect(getContextRoot(request) + "/" + webContext + "/modulemgt/index.jsp?errorMessage=" + msg);
            return false;
        }

        ModuleManagementClient moduleUploaderClient =
                new ModuleManagementClient(configurationContext, serverURL, cookie, true);
        List<ModuleUploadData> moduleUploadDataList = new ArrayList<ModuleUploadData>();

        try {
            for (FileItemData fieldData : fileItemsMap.get("marFilename")) {
                String fileName = getFileName(fieldData.getFileItem().getName());
                checkServiceFileExtensionValidity(fileName, ALLOWED_FILE_EXTENSIONS);
                if (!fileName.endsWith(".mar")) {
                    throw new CarbonException("File with extension " + fileName
                                              + " is not supported!");
                } else {
                    ModuleUploadData tempModuleData = new ModuleUploadData();
                    tempModuleData.setDataHandler(fieldData.getDataHandler());
                    tempModuleData.setFileName(fileName);
                    moduleUploadDataList.add(tempModuleData);
                }
            }

            moduleUploaderClient.uploadService(moduleUploadDataList.toArray(new ModuleUploadData[moduleUploadDataList.size()]));

            response.setContentType("text/html; charset=utf-8");
            response.sendRedirect(getContextRoot(request) + "/" + webContext + "/modulemgt/index.jsp?restart=true");
            return true;
        } catch (Exception e) {
            String msg = "File upload failed. " + e.getMessage();
            log.error(msg, e);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                                                response, getContextRoot(request) + "/" + webContext + "/modulemgt/index.jsp");
        }
        return false;
    }

}
