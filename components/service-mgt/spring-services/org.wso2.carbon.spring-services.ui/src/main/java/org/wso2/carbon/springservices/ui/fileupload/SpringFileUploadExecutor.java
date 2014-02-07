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
package org.wso2.carbon.springservices.ui.fileupload;

import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.apache.commons.collections.bidimap.TreeBidiMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;


public class SpringFileUploadExecutor extends AbstractFileUploadExecutor {

    public boolean execute(HttpServletRequest request, HttpServletResponse response)
            throws CarbonException, IOException {


        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        Map<String, ArrayList<java.lang.String>> formFieldsMap = getFormFieldsMap();
        String serviceHierarchy = formFieldsMap.get("serviceHierarchy").get(0);

        request.getSession().setAttribute("service.hierarchy", serviceHierarchy);

        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "File uploading failed. No files are specified";
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                    response, getContextRoot(request) + "/carbon/spring/index.jsp");
        }

        String[] uuids = new String[2];
        Map fileResourceMap = (Map) configurationContext.getProperty(
                ServerConstants.FILE_RESOURCE_MAP);
        if (fileResourceMap == null) {
            fileResourceMap = new TreeBidiMap();
            configurationContext.setProperty(ServerConstants.FILE_RESOURCE_MAP, fileResourceMap);
        }

        try {
            for (Object o : fileItemsMap.keySet()) {
                String fieldName = (String) o;
                FileItemData fileItemData = fileItemsMap.get(fieldName).get(0);
                String fileName = getFileName(fileItemData.getFileItem().getName());
                File servicesDir;
                File uploadedFile;
                if (fieldName.trim().equals("springBeans")) {
                    String uuid = generateUUID();
                    String serviceUploadDir = getWorkingDir() +
                            File.separator + "spring" +
                            File.separator + uuid +
                            File.separator;

                    if (fileName.endsWith(".jar")) {

                        servicesDir = new File(serviceUploadDir);
                        if (!servicesDir.mkdirs()) {
                            log.error("Error while creating directories");
                        }
                        uploadedFile = new File(servicesDir, fileName);

                        uuids[0] = uuid;
                        fileResourceMap.put(uuid, uploadedFile.getAbsolutePath());
                        fileItemData.getFileItem().write(uploadedFile);
                    }

                }
                if (fieldName.trim().equals("springContext")) {
                    String uuid = generateUUID();
                    String serviceUploadDir = getWorkingDir() +
                            File.separator + "spring" +
                            File.separator + uuid +
                            File.separator;
                    //Assumed this is the springs applicationContext.xml
                    // This can be safely assumed as, this class has been written only for
                    // uploading this.
                    servicesDir = new File(serviceUploadDir);
                    if (!servicesDir.mkdirs()) {
                        log.error("Error while creating directories");
                    }
                    uploadedFile = new File(servicesDir, fileName);

                    uuids[1] = uuid;
                    fileResourceMap.put(uuid, uploadedFile.getAbsolutePath());
                    fileItemData.getFileItem().write(uploadedFile);

                }

            }
            response.setContentType("text/html; charset=utf-8");
            response.sendRedirect(getContextRoot(request) + "/" + webContext + "/spring/showbeans.jsp?springBeansUUID=" + uuids[0] + "&springContextUUID=" + uuids[1]);
            return true;
        } catch (Exception e) {
            String msg = "File upload failed. " + e.getMessage();
            log.error(msg, e);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                    response, getContextRoot(request) + "/" + webContext + "/spring/index.jsp");
        }
        return false;
    }

}



