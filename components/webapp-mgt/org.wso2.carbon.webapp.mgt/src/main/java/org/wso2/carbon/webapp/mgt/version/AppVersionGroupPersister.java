/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.webapp.mgt.version;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.core.persistence.metadata.ArtifactMetadataException;
import org.wso2.carbon.core.persistence.metadata.ArtifactMetadataManager;
import org.wso2.carbon.core.persistence.metadata.ArtifactType;
import org.wso2.carbon.core.persistence.metadata.DeploymentArtifactMetadataFactory;
import org.wso2.carbon.webapp.mgt.WebappsConstants;

public class AppVersionGroupPersister {


    public static void persistWebappGroupMetadata(String webappName, String previousName, AxisConfiguration axisConfig) throws
            ArtifactMetadataException, AxisFault {

        ArtifactType type = new ArtifactType(WebappsConstants.WEBAPP_FILTER_PROP, WebappsConstants.WEBAPP_GROUP_METADATA_DIR);
        ArtifactMetadataManager manager = DeploymentArtifactMetadataFactory.getInstance(axisConfig).
                getMetadataManager();

        manager.setParameter(webappName, type,
                WebappsConstants.WEB_APP_VERSION_DEFAULT_WEBAPP, previousName, true);

    }

    public static String readWebappGroupMetadata(String webappName, AxisConfiguration axisConfig) throws
            ArtifactMetadataException, AxisFault {

        ArtifactType type = new ArtifactType(WebappsConstants.WEBAPP_FILTER_PROP, WebappsConstants.WEBAPP_GROUP_METADATA_DIR);
        ArtifactMetadataManager manager = DeploymentArtifactMetadataFactory.getInstance(axisConfig).
                getMetadataManager();

        return manager.loadParameter(webappName, type, WebappsConstants.WEB_APP_VERSION_DEFAULT_WEBAPP);

    }
}
