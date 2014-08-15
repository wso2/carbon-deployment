/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.webapp.mgt.metadata;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.webapp.mgt.WebappsConstants;

import javax.servlet.ServletContext;
import java.util.Map;

public class MetadataUtil {

    public static Object getAttributevalue(String attributeName, ServletContext context) {

        Map<String, Object> metadata = (Map<String, Object>) context.getAttribute(WebappsConstants.APPLICATION_META_DATA);
        Object obj = metadata.get(attributeName);
        return obj;
    }


}
