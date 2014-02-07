/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.jarservices;

/**
 *
 */
public class UploadArtifactsResponse {
    private String primaryResourceFilePath;
    private String resourcesDirPath;
    private boolean isWsdlProvided;
    private Service[] services;

    public Service[] getServices() {
        return services;
    }

    public void setServices(Service[] services) {
        this.services = services;
    }

    public String getPrimaryResourceFilePath() {
        return primaryResourceFilePath;
    }

    public void setPrimaryResourceFilePath(String primaryResourceFilePath) {
        this.primaryResourceFilePath = primaryResourceFilePath;
    }

    public String getResourcesDirPath() {
        return resourcesDirPath;
    }

    public void setResourcesDirPath(String resourcesDirPath) {
        this.resourcesDirPath = resourcesDirPath;
    }

    public boolean isWsdlProvided() {
        return isWsdlProvided;
    }

    public void setWsdlProvided(boolean wsdlProvided) {
        isWsdlProvided = wsdlProvided;
    }
}
