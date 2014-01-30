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
package org.wso2.carbon.ejbservices.service.util;

@SuppressWarnings("UnusedDeclaration")
public class EJBProviderData {
    private String providerURL;
    private String jndiContextClass;
    private String userName;
    private String password;
    private String beanJNDIName;
    private String remoteInterface;
    private String serviceName;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getBeanJNDIName() {
        return beanJNDIName;
    }

    public void setBeanJNDIName(String beanJNDIName) {
        this.beanJNDIName = beanJNDIName;
    }

    public String getJndiContextClass() {
        return jndiContextClass;
    }

    public void setJndiContextClass(String jndiContextClass) {
        this.jndiContextClass = jndiContextClass;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProviderURL() {
        return providerURL;
    }

    public void setProviderURL(String providerURL) {
        this.providerURL = providerURL;
    }

    public String getRemoteInterface() {
        return remoteInterface;
    }

    public void setRemoteInterface(String remoteInterface) {
        this.remoteInterface = remoteInterface;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
