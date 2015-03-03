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
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.extensions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.configurations.AutomationConfiguration;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.commons.admin.clients.TenantManagementServiceClient;
import org.wso2.carbon.commons.admin.clients.UserManagementClient;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Add users and tenants defined in automation.xml to the carbon server.
 */
public class UserPopulator {

    private static final Log log = LogFactory.getLog(UserPopulator.class);
    private List<String> tenantList;
    private List<String> rolesList;
    private AutomationContext automationContext;
    private List<RemovableData> removableDataList = new ArrayList<RemovableData>();


    public UserPopulator(String productGroupName, String instanceName)
            throws XPathExpressionException {

        this.automationContext = new AutomationContext(productGroupName,
                                                       instanceName,
                                                       TestUserMode.SUPER_TENANT_ADMIN);
        this.tenantList = getTenantList();
        this.rolesList = getRolesList();
    }

    /**
     * Populate Tenants, Users and Roles
     *
     * @throws Exception - Thrown if user population fails
     */
    public void populateUsers() throws Exception {
        // login as carbon super to add tenants
        LoginLogoutClient loginLogoutUtil = new LoginLogoutClient(automationContext);
        String sessionCookie = loginLogoutUtil.login();
        String backendURL = automationContext.getContextUrls().getBackEndUrl();
        TenantManagementServiceClient tenantManagementServiceClient =
                new TenantManagementServiceClient(backendURL, sessionCookie);

        for (String tenant : tenantList) {
            RemovableData removableData = new RemovableData();
            removableData.setTenant(tenant);
            // add tenant, if the tenant is not the Super tenant
            String tenantType = AutomationXpathConstants.CONTEXT_XPATH_SUPER_TENANT;

            if (!tenant.equals(FrameworkConstants.SUPER_TENANT_DOMAIN_NAME)) {
                tenantType = AutomationXpathConstants.CONTEXT_XPATH_TENANTS;
                String tenantAdminUserName = getTenantAdminUsername(tenantType, tenant);
                char[] tenantAdminPassword = getTenantAdminPassword(tenantType, tenant);

                tenantManagementServiceClient
                        .addTenant(tenant, tenantAdminPassword, tenantAdminUserName,
                                   FrameworkConstants.TENANT_USAGE_PLAN_DEMO);

                log.info("Added new tenant : " + tenant);
                // if new tenant added -> need to remove from the system at the end of the test
                removableData.setNewTenant(true);
                // login as newly added tenant
                sessionCookie = login(tenantAdminUserName, tenant, tenantAdminPassword, backendURL,
                                      UrlGenerationUtil.getManagerHost(
                                              automationContext.getInstance()));
            }
            removableData.setTenantType(tenantType);

            UserManagementClient userManagementClient =
                    new UserManagementClient(backendURL, sessionCookie);

            // add roles to the tenant
            addRoles(userManagementClient, removableData);
            // populate users of the current tenant and add roles
            addTenantUsers(tenantType, tenant, userManagementClient, removableData);
            // collect RemovableData
            removableDataList.add(removableData);
        }
    }


    /**
     * Delete Tenants, Users and Roles
     *
     * @throws Exception - Throws if user/tenant deletion fails
     */
    public void deleteUsers() throws Exception {
        String backendURL = automationContext.getContextUrls().getBackEndUrl();

        for (RemovableData removableData : removableDataList) {
            if (removableData.isNewTenant()) {
                LoginLogoutClient loginLogoutUtil = new LoginLogoutClient(automationContext);
                String sessionCookie = loginLogoutUtil.login();
                // remove tenant
                TenantManagementServiceClient tenantManagementServiceClient =
                        new TenantManagementServiceClient(backendURL, sessionCookie);

                tenantManagementServiceClient.deleteTenant(removableData.getTenant());
                log.info("Tenant was deleted successfully - " + removableData.getTenant());

            } else {
                String sessionCookie = login(
                        getTenantAdminUsername(removableData.getTenantType(),
                                               removableData.getTenant()), removableData.getTenant(),
                        getTenantAdminPassword(removableData.getTenantType(),
                                               removableData.getTenant()), backendURL,
                        UrlGenerationUtil.getManagerHost(automationContext.getInstance()));

                UserManagementClient userManagementClient =
                        new UserManagementClient(backendURL, sessionCookie);

                for (String user : removableData.getNewUsers()) {
                    // remove users
                    boolean isTenantUserExist = userManagementClient.getUserList().contains(user);
                    if (isTenantUserExist) {
                        userManagementClient.deleteUser(user);
                        log.info("User was deleted successfully - " + user);
                    }
                }
                for (String role : removableData.getNewRoles()) {
                    // remove roles
                    if (userManagementClient.roleNameExists(role)) {
                        userManagementClient.deleteRole(role);
                        log.info("Role was deleted successfully - " + role);
                    }
                }
            }
        }
    }

    private void addTenantUsers(String tenantType, String tenant,
                                UserManagementClient userManagementClient,
                                RemovableData removableData) throws Exception {
        List<String> userList = getUserList(tenant);

        for (String tenantUser : userList) {
            String tenantUserUsername = getTenantUserUsername(tenantType, tenant, tenantUser);
            boolean isTenantUserExist = userManagementClient.getUserList().contains(
                    tenantUserUsername);
            if (!isTenantUserExist) {
                String[] rolesToBeAdded = new String[]{FrameworkConstants.ADMIN_ROLE};
                List<String> userRoles = new ArrayList<String>();
                NodeList roleList = automationContext.getConfigurationNodeList(
                        String.format(AutomationXpathConstants.CONTEXT_XPATH_TENANT_USER_ROLES, tenantType,
                                      tenant, tenantUser));
                if (roleList != null && roleList.item(0) != null) {
                    roleList = roleList.item(0).getChildNodes();
                    for (int i = 0; i < roleList.getLength(); i++) {
                        String role = roleList.item(i).getTextContent();
                        if (userManagementClient.roleNameExists(role)) {
                            userRoles.add(role);
                        } else {
                            log.warn("Role is not exist : " + role);
                        }
                    }
                    if (userRoles.size() > 0) {
                        rolesToBeAdded = userRoles.toArray(new String[userRoles.size()]);
                    }
                }

                userManagementClient.addUser(tenantUserUsername, String.valueOf(
                        getTenantUserPassword(tenantType, tenant, tenantUser)), rolesToBeAdded, null);

                log.info("User - " + tenantUser + " created in tenant domain of " + " " + tenant);
                // if new user added for existing tenant -> need to remove from the system at the
                // end of the test
                if (!removableData.isNewTenant()) {
                    removableData.setNewUser(tenantUserUsername);
                }
            } else {
                log.info(tenantUser + " is already in " + tenant);
            }
        }
    }

    private String getTenantUserUsername(String tenantType, String tenant, String tenantUser)
            throws XPathExpressionException {
        return automationContext.getConfigurationValue(
                String.format(AutomationXpathConstants.CONTEXT_XPATH_TENANT_USER_USERNAME, tenantType, tenant, tenantUser));
    }


    private char[] getTenantUserPassword(String tenantType, String tenant, String tenantUser)
            throws XPathExpressionException {
        return automationContext.getConfigurationValue(String.format(
                AutomationXpathConstants.CONTEXT_XPATH_TENANT_USER_PASSWORD, tenantType, tenant,
                tenantUser)).toCharArray();
    }

    /**
     * This method is to login to the server and return sessionCookie as a String
     *
     * @param userName   - login username
     * @param domain     - login domain
     * @param password   - login password
     * @param backendUrl - backend url of the server
     * @param hostName   - host name of the server
     * @return - sessionCookie of the login as a String
     * @throws RemoteException - Error while calling AuthenticatorClient login method
     * @throws LoginAuthenticationExceptionException - Error while calling AuthenticatorClient
     * @throws XPathExpressionException - Error while getting configuration node list from AutomationContext
     */
    protected String login(String userName, String domain, char[] password, String backendUrl,
                           String hostName)
            throws RemoteException, LoginAuthenticationExceptionException,
                   XPathExpressionException {
        AuthenticatorClient loginClient = new AuthenticatorClient(backendUrl);

        if (!domain.equals(AutomationConfiguration.getConfigurationValue(FrameworkConstants.SUPER_TENANT_DOMAIN_NAME))) {
            userName += "@" + domain;
        }
        return loginClient.login(userName, password, hostName);
    }

    /**
     * This method is to get all the users as list for a tenant domain
     *
     * @param tenantDomain - Tenant domain
     * @return - User list for this tenant
     * @throws XPathExpressionException - Error while getting configuration node list from AutomationContext
     */
    public List<String> getUserList(String tenantDomain) throws XPathExpressionException {
        //according to the automation xml the super tenant no has to be accessed explicitly
        List<String> userList = new ArrayList<String>();
        AutomationContext automationContext = new AutomationContext();
        int numberOfUsers;
        String superTenantReplacement = AutomationXpathConstants.CONTEXT_XPATH_TENANTS;

        if (tenantDomain.equals(FrameworkConstants.SUPER_TENANT_DOMAIN_NAME)) {
            superTenantReplacement = AutomationXpathConstants.CONTEXT_XPATH_SUPER_TENANT;
        }

        numberOfUsers =
                automationContext.getConfigurationNodeList(String.format(
                        AutomationXpathConstants.CONTEXT_XPATH_USER_NODE,
                        superTenantReplacement, tenantDomain)).getLength();

        for (int i = 0; i < numberOfUsers; i++) {
            String userKey = automationContext.getConfigurationNodeList(
                    String.format(AutomationXpathConstants.CONTEXT_XPATH_USERS_NODE, superTenantReplacement,
                                  tenantDomain)).item(0).getChildNodes().item(i).getAttributes().
                    getNamedItem(AutomationXpathConstants.CONTEXT_XPATH_KEY).getNodeValue();

            userList.add(userKey);
        }
        return userList;
    }

    private List<String> getTenantList() throws XPathExpressionException {
        List<String> tenantList = new ArrayList<String>();
        // add carbon.super
        tenantList.add(FrameworkConstants.SUPER_TENANT_DOMAIN_NAME);
        // add other tenants
        NodeList tenantNodeList = automationContext.getConfigurationNodeList(
                AutomationXpathConstants.CONTEXT_XPATH_TENANTS_NODE).item(0).getChildNodes();

        for (int i = 0; i < tenantNodeList.getLength(); i++) {
            tenantList.add(
                    tenantNodeList.item(i).getAttributes()
                            .getNamedItem(AutomationXpathConstants.CONTEXT_XPATH_DOMAIN).getNodeValue()
            );
        }
        return tenantList;
    }

    private char[] getTenantAdminPassword(String tenantType, String tenant)
            throws XPathExpressionException {

        return automationContext.getConfigurationValue(
                String.format(AutomationXpathConstants.CONTEXT_XPATH_ADMIN_USER_PASSWORD,
                              tenantType, tenant)).toCharArray();
    }

    private String getTenantAdminUsername(String tenantType, String tenant)
            throws XPathExpressionException {
        return automationContext.getConfigurationValue(
                String.format(AutomationXpathConstants.CONTEXT_XPATH_ADMIN_USER_USERNAME,
                              tenantType, tenant));
    }

    private List<String> getRolesList() throws XPathExpressionException {
        List<String> roleList = new ArrayList<String>();
        NodeList roleNodeList =
                automationContext.getConfigurationNodeList(AutomationXpathConstants.CONTEXT_XPATH_ROLES_NODE);

        if (roleNodeList != null && roleNodeList.item(0) != null) {
            roleNodeList = roleNodeList.item(0).getChildNodes();
            for (int i = 0; i < roleNodeList.getLength(); i++) {
                roleList.add(roleNodeList.item(i).getAttributes()
                                     .getNamedItem(AutomationXpathConstants.CONTEXT_XPATH_NAME)
                                     .getNodeValue());
            }
        }
        return roleList;
    }

    private void addRoles(UserManagementClient userManagementClient, RemovableData removableData)
            throws Exception {
        for (String role : rolesList) {
            if (!userManagementClient.roleNameExists(role)) {
                List<String> permissions = getPermissionList(role);
                userManagementClient
                        .addRole(role, null, permissions.toArray(new String[permissions.size()]));
                log.info("Added role " + role + " with permissions");
                // if new role added for existing tenant -> need to remove from the system at the
                // end of the test
                if (!removableData.isNewTenant()) {
                    removableData.setNewRole(role);
                }
            }
        }
    }

    private List<String> getPermissionList(String role) throws XPathExpressionException {
        List<String> permissionList = new ArrayList<String>();
        NodeList permissionNodeList = automationContext
                .getConfigurationNodeList(
                        String.format(AutomationXpathConstants.CONTEXT_XPATH_PERMISSIONS_NODE, role));
        if (permissionNodeList != null && permissionNodeList.item(0) != null) {
            permissionNodeList = permissionNodeList.item(0).getChildNodes();
            for (int i = 0; i < permissionNodeList.getLength(); i++) {
                permissionList.add(permissionNodeList.item(i).getTextContent());
            }
        }
        return permissionList;
    }


    /**
     * Class to store data to be removed at the end of the test execution
     */
    private static class RemovableData {
        private String tenant;
        private String tenantType;
        private boolean isNewTenant = false;
        private List<String> newRoles = new ArrayList<String>();
        private List<String> newUsers = new ArrayList<String>();

        public String getTenant() {
            return tenant;
        }

        public void setTenant(String tenant) {
            this.tenant = tenant;
        }

        public String getTenantType() {
            return tenantType;
        }

        public void setTenantType(String tenantType) {
            this.tenantType = tenantType;
        }

        public boolean isNewTenant() {
            return isNewTenant;
        }

        public void setNewTenant(boolean isNewTenant) {
            this.isNewTenant = isNewTenant;
        }

        public List<String> getNewRoles() {
            return newRoles;
        }

        public void setNewRole(String role) {
            this.newRoles.add(role);
        }

        public List<String> getNewUsers() {
            return newUsers;
        }

        public void setNewUser(String user) {
            this.newUsers.add(user);
        }
    }
}


