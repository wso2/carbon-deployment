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
package org.wso2.carbon.commons.admin.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.commons.utils.AuthenticateStubUtil;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Provides client for invoking UserMgtAdminService
 * Can be used for user management operation
 */
public class UserManagementClient {
    private static final int LIMIT = 100;
    private final Log log = LogFactory.getLog(UserManagementClient.class);
    private UserAdminStub userAdminStub;

    public UserManagementClient(String backendURL, String sessionCookie) throws AxisFault {
        String serviceName = "UserAdmin";
        String endPoint = backendURL + serviceName;
        userAdminStub = new UserAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, userAdminStub);
    }

    /**
     * This method is to add users
     *
     * @param userName    - user name to be added
     * @param password    - password to be given
     * @param roles       - user roles to be added
     * @param profileName - profile name to be added
     * @throws RemoteException             - Error when adding user
     * @throws UserAdminUserAdminException - Error when adding user
     */
    public void addUser(String userName, String password, String[] roles, String profileName)
            throws RemoteException, UserAdminUserAdminException {
        userAdminStub.addUser(userName, password, roles, null, profileName);
    }

    /**
     * This method is to delete users
     *
     * @param userName - user name of the user who has to be deleted
     * @throws RemoteException             - Error when deleting user
     * @throws UserAdminUserAdminException - Error when deleting user
     */
    public void deleteUser(String userName) throws RemoteException, UserAdminUserAdminException {
        userAdminStub.deleteUser(userName);
    }

    /**
     * Get all users
     *
     * @return String HashSet - List of users
     * @throws RemoteException             - Error when calling listUsers method
     * @throws UserAdminUserAdminException - Error when calling listUsers method
     */
    public HashSet<String> getUserList() throws RemoteException, UserAdminUserAdminException {
        return new HashSet<String>(Arrays.asList(userAdminStub.listUsers("*", LIMIT)));
    }

    public boolean roleNameExists(String roleName)
            throws Exception {
        FlaggedName[] roles = userAdminStub.getAllRolesNames(roleName, LIMIT);
        for (FlaggedName role : roles) {
            if (role.getItemName().equals(roleName)) {
                log.info("Role name " + roleName + " already exists");
                return true;
            }
        }
        return false;
    }

    public void deleteRole(String roleName) throws RemoteException, UserAdminUserAdminException {
        userAdminStub.deleteRole(roleName);
    }


    public void addRole(String roleName, String[] userList, String[] permissions) throws
                                                                                  RemoteException,
                                                                                  UserAdminUserAdminException {
        userAdminStub.addRole(roleName, userList, permissions, false);
    }
}
