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
package org.wso2.carbon.repomanager.axis2.ui.fileupload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.repomanager.axis2.ui.Axis2RepoManagerClient;
import org.wso2.carbon.repomanager.axis2.stub.types.Axis2ArtifactUploadData;
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

/**
 * Class that is used to manage the Axis2 repository.
 * This class communicates with the Axis2RepoManager Admin service to upload and delete artifacts
 */
public class Axis2RepoArtifactUploadExecutor extends AbstractFileUploadExecutor {

    private static final String[] ALLOWED_FILE_EXTENSIONS = new String[]{".jar"};

    private static final Log log = LogFactory.getLog(Axis2RepoArtifactUploadExecutor.class);

    public boolean execute(HttpServletRequest request,
                           HttpServletResponse response) throws CarbonException, IOException {
        
        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        List<FileItemData> fileItemDataArray;
        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "File uploading failed. No files are specified.";
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                                                response, "../" + webContext + "/repomanager-axis2/upload.jsp");
            return false;
        } else {
            // Retrieve the set of FileItemData
            fileItemDataArray = fileItemsMap.get("libFileName");
        }

        String fileUploadDirName;
        Map<String, ArrayList<java.lang.String>> formFieldsMap = getFormFieldsMap();
        if (formFieldsMap == null || formFieldsMap.isEmpty()) {
            String msg = "File uploading failed. No Directory specified to upload files.";
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                                                response, "../" + webContext + "/repomanager-axis2/upload.jsp");
            return false;
        } else {
            // Retrieve the path to upload artifacts
            fileUploadDirName = formFieldsMap.get("dirName").get(0);
        }

        Axis2RepoManagerClient repoManagerClient =
                new Axis2RepoManagerClient(configurationContext, serverURL, cookie);
        String msg;
        
        List<Axis2ArtifactUploadData> uploadDataList = new ArrayList<Axis2ArtifactUploadData>();
        try {
            for (FileItemData fileItemData : fileItemDataArray) {
                String filename = getFileName(fileItemData.getFileItem().getName());
                checkServiceFileExtensionValidity(filename, ALLOWED_FILE_EXTENSIONS);

                if (!filename.endsWith(".jar")) {
                    throw new CarbonException("File with extension " + filename + " is not supported!");
                }

                Axis2ArtifactUploadData uploadData = new Axis2ArtifactUploadData();
                uploadData.setFileName(filename);
                uploadData.setDataHandler(fileItemData.getDataHandler());
                uploadDataList.add(uploadData);
            }

            repoManagerClient.
                    uploadArtifacts(uploadDataList.toArray(new Axis2ArtifactUploadData[uploadDataList.size()]),
                                    fileUploadDirName);

            request.getSession().setAttribute("uploadStatus", "successful");
            //response.setContentType("text/html; charset=utf-8");
            response.sendRedirect("../" + webContext + "/repomanager-axis2/index.jsp");
            /*msg = "Files have been uploaded successfully. " +
                  "Restart the Axis2 server in order to load the newly uploaded libraries.";
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request,
                                                response, "../" + webContext + "/repomanager-axis2/index.jsp");*/
            return true;
        } catch (Exception e) {
            msg = "File upload failed. " + e.getMessage();
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                                                response, "../" + webContext + "/repomanager-axis2/index.jsp");
        }
        return false;
    }
}
