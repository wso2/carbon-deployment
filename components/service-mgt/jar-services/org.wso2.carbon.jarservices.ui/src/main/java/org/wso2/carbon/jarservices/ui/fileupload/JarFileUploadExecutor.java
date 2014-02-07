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

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.jarservices.stub.types.Resource;
import org.wso2.carbon.jarservices.stub.types.UploadArtifactsResponse;
import org.wso2.carbon.jarservices.stub.JarUploadExceptionException;
import org.wso2.carbon.jarservices.stub.DuplicateServiceGroupExceptionException;
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

public class JarFileUploadExecutor extends AbstractFileUploadExecutor {

    private static final String[] RESOURCE_FILE_EXTENSIONS = new String[]{".jar"};
    private static final String[] WSDL_FILE_EXTENSIONS = new String[]{".wsdl"};

    public boolean execute(HttpServletRequest request, HttpServletResponse response)
            throws CarbonException, IOException {

        Map<String, ArrayList<String>> formFieldsMap = getFormFieldsMap();
        String serviceGroupName = formFieldsMap.get(JarServicesConstants.SERVICE_GROUP_NAME).get(0);
        String serviceHierarchy = formFieldsMap.get(JarServicesConstants.SERVICE_HIERARCHY).get(0);

        request.getSession().setAttribute(JarServicesConstants.SERVICE_GROUP_NAME, serviceGroupName);
        request.getSession().setAttribute(JarServicesConstants.SERVICE_HIERARCHY, serviceHierarchy);
        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "File upload failed. No files are specified";
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                                                response, getContextRoot(request) + "/" + webContext + "/jarservices/index.jsp");
        }

        try {
            JarServiceAdminClient jarServiceAdminClient = new JarServiceAdminClient(configurationContext, serverURL, cookie);
            List<Resource> resources = new ArrayList<Resource>();
            Resource wsdl = null;
            for (Object o : fileItemsMap.keySet()) {
                String fieldName = (String) o;
                List<FileItemData> fileItemDatas = fileItemsMap.get(fieldName);

                for (FileItemData fileItemData: fileItemDatas){
                    String fileName = getFileName(fileItemData.getFileItem().getName());
                    if (fieldName.equals("resourceFileName")) {
                        checkServiceFileExtensionValidity(fileName, RESOURCE_FILE_EXTENSIONS);
                        Resource resource = new Resource();
                        resource.setDataHandler(fileItemData.getDataHandler());
                        resource.setFileName(fileName);
                        resources.add(resource);
                    } else if (fieldName.equals("wsdlFileName")) {
                        checkServiceFileExtensionValidity(fileName, WSDL_FILE_EXTENSIONS);
                        wsdl = new Resource();
                        wsdl.setDataHandler(fileItemData.getDataHandler());
                        wsdl.setFileName(fileName);
                    }
                }
            }
            UploadArtifactsResponse uploadResponse = jarServiceAdminClient.upload(serviceGroupName,
                                                                                  wsdl,
                                                                                  resources.toArray(new Resource[resources.size()]));
            request.getSession().setAttribute(JarServicesConstants.UPLOAD_ARTIFACTS_RESPONSE, uploadResponse);
            response.sendRedirect(getContextRoot(request) + "/" + webContext + "/jarservices/list_classes.jsp");
            return true;
        } catch (JarUploadExceptionException e) {
            handleException(request, response, CarbonUIMessage.ERROR, webContext,
                            "Error occurred while uploading resource. " +
                            e.getFaultMessage().getJarUploadException().getMessage(),
                            e);
        } catch (DuplicateServiceGroupExceptionException e) {
            handleException(request, response, CarbonUIMessage.WARNING, webContext,
                            "Service group " + serviceGroupName + " already exists. ",
                            e);
        } catch (Exception e) {
            handleException(request, response, CarbonUIMessage.ERROR, webContext,
                            "File upload failed. " + e.getMessage(),
                            e);
        }
        return false;
    }

    private void handleException(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String msgStatus,
                                 String webContext,
                                 String msg,
                                 Exception e) throws IOException {
        log.error(msg, e);
        CarbonUIMessage.sendCarbonUIMessage(msg, msgStatus, request,
                                            response, getContextRoot(request) + "/" + webContext + "/jarservices/index.jsp");
    }
}
