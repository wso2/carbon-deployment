/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.ejbservices.component.xml;

import org.wso2.carbon.utils.component.xml.builder.ComponentConfigBuilder;
import org.wso2.carbon.utils.component.xml.config.ComponentConfig;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ejbservices.util.EJBConstants;
import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class EJBAppServerConfigBuilder extends ComponentConfigBuilder {
    public ComponentConfig[] build(OMElement omElement) throws CarbonException {
        List<EJBAppServerConfig> appServerConfigList = new ArrayList<EJBAppServerConfig>();
        for (Iterator iterator = omElement.getChildrenWithName(
                new QName(NS_WSO2CARBON, EJBConstants.ComponentConfig.ELE_EJB_APP_SERVER));
             iterator.hasNext();) {
            OMElement configElement = (OMElement) iterator.next();

            //id element
            OMElement idElement = configElement.getFirstChildWithName(
                    new QName(NS_WSO2CARBON, EJBConstants.ComponentConfig.ELE_ID));
            String id = null;
            if (idElement != null) {
                id = idElement.getText().trim();
            }

            if (id == null) {
                throw new CarbonException("Mandatory attribute EJBApplicationServer/Id entry " +
                        "does not exist or is empty in the component.xml");
            }

            //name element
            OMElement nameElement = configElement.getFirstChildWithName(
                    new QName(NS_WSO2CARBON, EJBConstants.ComponentConfig.ELE_NAME));
            String name = null;
            if (nameElement != null) {
                name = nameElement.getText().trim();
            }

            if (name == null) {
                throw new CarbonException("Mandatory attribute EJBApplicationServer/Name entry " +
                        "does not exist or is empty in the component.xml");
            }

            //ProviderURL element
            OMElement providerURLElement = configElement.getFirstChildWithName(
                    new QName(NS_WSO2CARBON, EJBConstants.ComponentConfig.ELE_PROVIDER_URL));
            String providerURL = null;
            if (providerURLElement != null) {
                providerURL = providerURLElement.getText().trim();
            }

            if (providerURL == null) {
                throw new CarbonException("Mandatory attribute EJBApplicationServer/ProviderURL " +
                                          "entry does not exist or is empty in the component.xml");
            }

            //JNDIContextClass element
            OMElement contextClassElement = configElement.getFirstChildWithName(
                    new QName(NS_WSO2CARBON, EJBConstants.ComponentConfig.ELE_JNDI_CONTEXT_CLASS));
            String jndiContextClass = null;
            if (contextClassElement != null) {
                jndiContextClass = contextClassElement.getText().trim();
            }

            if (jndiContextClass == null) {
                throw new CarbonException("Mandatory attribute EJBApplicationServer/" +
                                          "JNDIContextClass entry does not exist or is empty in " +
                                          "the component.xml");
            }

            EJBAppServerConfig appServerConfig = new EJBAppServerConfig();
            appServerConfig.setId(id);
            appServerConfig.setName(name);
            appServerConfig.setProviderURL(providerURL);
            appServerConfig.setJndiContextClass(jndiContextClass);

            appServerConfigList.add(appServerConfig);
        }

        if(appServerConfigList.size() == 0){
            return null;
        } else {
            EJBAppServerConfig[] appServerConfigs =
                    new EJBAppServerConfig[appServerConfigList.size()];
            return appServerConfigList.toArray(appServerConfigs);
        }
    }

    public String getLocalNameOfComponentConfigElement() {
        return "EJBApplicationServers";
    }
}