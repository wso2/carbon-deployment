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
package org.wso2.carbon.aarservices;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServiceUploader extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(ServiceUploader.class);

    public String uploadService(AARServiceData[] serviceDataList) throws Exception {

        String fileName = null;
        try {
            // First lets filter for jar resources
            AxisConfiguration axisConfig = getAxisConfig();
            String repo = axisConfig.getRepository().getPath();

            if (CarbonUtils.isURL(repo)) {
                throw new AxisFault("Uploading services to URL repo is not supported ");
            }

            // Composing the proper location to copy the artifact
            // TODO we need to get this from AxisConfiguration as we can set a
            // custom directory in
            // the deployer configuration
            String servicesDir = axisConfig.getParameter(DeploymentConstants.SERVICE_DIR_PATH)
                    .getValue().toString();


            for (AARServiceData serviceData : serviceDataList) {
                fileName = serviceData.getFileName();
                StringBuffer destDir = new StringBuffer();
                // create the hierarchical folders before deploying
                if (serviceData.getServiceHierarchy() != null) {
                    destDir.append(repo).append(File.separator).append(servicesDir);
                    String[] hierarchyParts = serviceData.getServiceHierarchy().split("/");
                    for (String part : hierarchyParts) {
                        destDir.append(File.separator).append(part);
                        File hierarchyDir = new File(destDir.toString());
                        if (!hierarchyDir.exists() && !hierarchyDir.mkdirs()){
                            log.warn("Could not create hierarchy directory " + hierarchyDir);
                        }
                    }
                }

                writeToFileSystem(destDir.toString(), fileName, serviceData.getDataHandler());

            }

        } catch (IOException e) {
            String msg = "Error occured while uploading the service " + fileName;
            log.error(msg, e);
            throw new Exception("Failed to upload the service archive " + fileName, e);
        }

        return "successful";
    }

    private void writeToFileSystem(String path,
                                   String fileName,
                                   DataHandler dataHandler) throws Exception {
        File destFile = new File(path, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destFile);
            dataHandler.writeTo(fos);
            fos.flush();
            fos.close();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    private String prepareHierarchy(String serviceHierarchy) {
        if (serviceHierarchy.startsWith("/")) {
            serviceHierarchy = serviceHierarchy.substring(1);
        }
        if (serviceHierarchy.endsWith("/")) {
            serviceHierarchy = serviceHierarchy.substring(0, serviceHierarchy.length() - 1);
        }
        return serviceHierarchy;
    }

}
