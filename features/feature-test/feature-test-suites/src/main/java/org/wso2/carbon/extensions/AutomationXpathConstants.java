/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.extensions;

/**
 * This class is for xpath constants for automation.xml file
 */
public class AutomationXpathConstants {
    public static final String CONTEXT_XPATH_ADMIN_USER_PASSWORD = "//%s/tenant[@domain='%s']/admin/user/password";
    public static final String CONTEXT_XPATH_USERS_NODE = "//%s/tenant[@domain='%s']/users";
    public static final String CONTEXT_XPATH_USER_NODE = "//%s/tenant[@domain='%s']/users/user";
    public static final String CONTEXT_XPATH_ADMIN_USER_USERNAME = "//%s/tenant[@domain='%s']/admin/user/userName";
    public static final String CONTEXT_XPATH_TENANT_USER_USERNAME = "//%s/tenant[@domain='%s']/users/user[@key='%s']/userName";
    public static final String CONTEXT_XPATH_TENANT_USER_PASSWORD = "//%s/tenant[@domain='%s']/users/user[@key='%s']/password";
    public static final String CONTEXT_XPATH_PRODUCT_GROUP = "//productGroup";
    public static final String CONTEXT_XPATH_CLUSTERING_ENABLED = "clusteringEnabled";
    public static final String CONTEXT_XPATH_TENANTS_NODE = "//tenants";
    public static final String CONTEXT_XPATH_TENANTS = "tenants";
    public static final String CONTEXT_XPATH_SUPER_TENANT = "superTenant";
    public static final String CONTEXT_XPATH_DOMAIN = "domain";
    public static final String CONTEXT_XPATH_KEY = "key";
    public static final String CONTEXT_XPATH_NAME = "name";
    public static final String CONTEXT_XPATH_TYPE = "type";

}
