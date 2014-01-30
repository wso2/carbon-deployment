/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.webapp.mgt;

import org.apache.catalina.Session;
import org.apache.catalina.session.StandardManager;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SessionManager which ensures that one tenant's sessions cannot be accessed by other tenants.
 * Super tenant should be able to access all sessions
 */
public class CarbonTomcatSessionManager extends StandardManager {

    private static final List<String> allowedClasses = new ArrayList<String>();

    static {
        allowedClasses.add("org.apache.catalina.session.ManagerBase");
        allowedClasses.add("org.apache.catalina.connector.Request");
    }

    /**
     *
     * Tenant ID of the tenant who owns this Tomcat Session Manager
     */
    private int ownerTenantId;

    public CarbonTomcatSessionManager(int ownerTenantId) {
        this.ownerTenantId = ownerTenantId;
    }

    @Override
    public int getRejectedSessions() {
        checkAccess();
        return super.getRejectedSessions();
    }

    @Override
    public long getExpiredSessions() {
        checkAccess();
        return super.getExpiredSessions();
    }

    @Override
    public int getMaxInactiveInterval() {
        checkAccess();
        return super.getMaxInactiveInterval();
    }

    @Override
    public Session findSession(String id) throws IOException {
        checkAccess();
        return super.findSession(id);
    }

    @Override
    public Session[] findSessions() {
        checkAccess();
        return super.findSessions();
    }

    @Override
    public int getMaxActive() {
        checkAccess();
        return super.getMaxActive();
    }

    @Override
    public int getSessionAverageAliveTime() {
        checkAccess();
        return super.getSessionAverageAliveTime();
    }

    @Override
    public int getSessionMaxAliveTime() {
        checkAccess();
        return super.getSessionMaxAliveTime();
    }

    @Override
    public int getActiveSessions() {
        checkAccess();
        return super.getActiveSessions();
    }

    private void checkAccess() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        String callingClass = trace[3].getClassName();
        if (allowedClasses.contains(callingClass)) {
            return;
        }
        // When using IBM JDK, the MangerBase and Request is at 5th place in the stack trace,
        // so we have to check that as well
        callingClass = trace[4].getClassName();

        // A security issue may arise with SUN JDK , when sometime the allowed class is found at the
        // 5th place in the stack. The following check will ensure that this will not arise and
        // we allow only IBM JDK to proceed in this if statement
        if (System.getProperty("java.vm.name").contains("IBM") &&
            allowedClasses.contains(callingClass)) {
            return;
        }

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if(tenantId != MultitenantConstants.SUPER_TENANT_ID && tenantId != ownerTenantId) {
            throw new SecurityException("Illegal access attempt by  tenant[" + tenantId +
                                        "] to sessions owned by tenant[" + ownerTenantId + "]");
        }
    }
}
