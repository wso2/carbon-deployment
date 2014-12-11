/*
 *
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
 * /
 */
package org.wso2.carbon.commons;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;

public class UserManagementClient {
    private static final int LIMIT = 100;
    private final Log log = LogFactory.getLog(UserManagementClient.class);
    private final String serviceName = "UserAdmin";
    private UserAdminStub userAdminStub;

    public UserManagementClient(String backendURL, String sessionCookie) throws AxisFault {
        String endPoint = backendURL + serviceName;
        userAdminStub = new UserAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, userAdminStub);
    }

    public void addUser(String userName, String password, String[] roles,
                        String profileName) throws RemoteException, UserAdminUserAdminException {
        userAdminStub.addUser(userName, password, roles, null, profileName);
    }


    public void deleteUser(String userName) throws RemoteException, UserAdminUserAdminException {
        userAdminStub.deleteUser(userName);
    }

    public HashSet<String> getUserList() throws RemoteException, UserAdminUserAdminException {
        return new HashSet<String>(Arrays.asList(userAdminStub.listUsers("*", LIMIT)));
    }
}
