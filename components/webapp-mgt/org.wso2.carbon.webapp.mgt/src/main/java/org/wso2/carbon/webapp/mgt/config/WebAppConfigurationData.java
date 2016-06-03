/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.webapp.mgt.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class holds configuration information of a web application.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "wso2as-web")
public class WebAppConfigurationData {

    @XmlElement(name = "single-sign-on")
    private boolean singleSignOn;

    @XmlElement(name = "statistics-publisher")
    private StatisticsPublisher statisticsPublisher;

    @XmlElement(name = "classloading")
    private Classloading classloading;

    @XmlElement(name = "web-service-discovery")
    private boolean webServiceDiscovery;

    @XmlElement(name = "rest-web-services")
    private RestWebServices restWebServices;

    public boolean isSingleSignOnEnabled() {
        return singleSignOn;
    }

    public void setSingleSignOnEnabled(boolean value) {
        singleSignOn = value;
    }

    public StatisticsPublisher getStatisticsPublisher() {
        return statisticsPublisher;
    }

    public void setStatisticsPublisher(StatisticsPublisher value) {
        this.statisticsPublisher = value;
    }

    public Classloading getClassloading() {
        return classloading;
    }

    public void setClassloading(Classloading value) {
        this.classloading = value;
    }

    public boolean isWebServiceDiscoveryEnabled() {
        return webServiceDiscovery;
    }

    public RestWebServices getRestWebServices() {
        return restWebServices;
    }

    public void setRestWebServices(RestWebServices value) {
        this.restWebServices = value;
    }

    public boolean isParentFirst() {
        Classloading classloading = getClassloading();
        if (classloading != null) {
            return getClassloading().isParentFirst();
        }
        return false;
    }

    public void setParentFirst(boolean parentFirst) {
        if (getClassloading() == null) {
            classloading = new Classloading();
        }
        classloading.setParentFirst(parentFirst);
    }

    public List<String> getEnvironments() {
        Classloading classloading = getClassloading();
        List<String> envList = null;
        if (classloading != null) {
            Classloading.Environments environments = classloading.getEnvironments();
            if (environments != null) {
                envList = classloading.getEnvironments().getEnvironment();
            }
        }
        return envList;
    }

    public void setEnvironments(List<String> environments) {
        if (classloading != null) {
            if (classloading.getEnvironments() != null) {
                classloading.getEnvironments().setEnvironment(environments);
            } else {
                classloading.setEnvironments(new Classloading.Environments());
                classloading.getEnvironments().setEnvironment(environments);
            }
        } else {
            classloading = new Classloading();
            classloading.setEnvironments(new Classloading.Environments());
            classloading.getEnvironments().setEnvironment(environments);
        }
    }

    public boolean isManagedAPI() {
        RestWebServices restWebServices = getRestWebServices();
        if (restWebServices != null) {
            return restWebServices.isManagedApi();
        }
        return false;
    }

    public void setIsManagedAPI(boolean value) {
        RestWebServices restWebServices = getRestWebServices();
        if (restWebServices != null) {
            restWebServices.setIsManagedApi(value);
        } else {
            restWebServices = new RestWebServices();
            restWebServices.setIsManagedApi(value);
            this.restWebServices = restWebServices;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "",
            propOrder = { "parentFirst", "environments" })
    public static class Classloading {

        @XmlElement(name = "parent-first")
        private boolean parentFirst;

        @XmlElement(name = "environments")
        private Environments environments;

        private boolean isParentFirst() {
            return parentFirst;
        }

        private void setParentFirst(boolean value) {
            this.parentFirst = value;
        }

        private Environments getEnvironments() {
            return environments;
        }

        private void setEnvironments(Environments env) {
            environments = env;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "",
                propOrder = { "environment" })
        public static class Environments {

            @XmlElement(name = "environment",
                    required = true)
            private List<String> environment;

            private List<String> getEnvironment() {
                if (environment == null) {
                    environment = new ArrayList<>();
                }
                return environment;
            }

            private void setEnvironment(List<String> environmentList) {
                environment = environmentList;
            }

        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "",
            propOrder = { "isManagedApi" })
    public static class RestWebServices {

        @XmlElement(name = "is-managed-api")
        private boolean isManagedApi;

        public boolean isManagedApi() {
            return isManagedApi;
        }

        public void setIsManagedApi(boolean value) {
            isManagedApi = value;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "",
            propOrder = { "enabled", "streamId" })
    public static class StatisticsPublisher {
        @XmlElement(name = "enabled",
                required = true)
        private boolean enabled;
        @XmlElement(name = "stream-id",
                required = true)
        private String streamId;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean value) {
            enabled = value;
        }

        public String getStreamId() {
            return streamId;
        }

    }

}
