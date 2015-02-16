/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.webapp.mgt.ext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.webapp.mgt.internal.APIDataHolder;

import java.util.Map;

/**
 * //todo
 */
public class ASAPIStartupObserver implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(ASAPIStartupObserver.class);

    @Override
    public void completingServerStartup() {
        //nothing to do here for the moment
    }

    @Override
    public void completedServerStartup() {
        //todo
        if(log.isDebugEnabled()) {
            log.debug("Publishing APIs at startup completion.");
        }
        Map<String, Map<String, String>> apiMap = APIDataHolder.getInstance().getInitialAPIInfoMap();

        if(log.isDebugEnabled()) {
            log.debug("Going to publish ".concat(String.valueOf(apiMap.size())).concat(" APIs.") );
        }

        for (String apiKey : apiMap.keySet()) {
            //todo
        }
    }
}
