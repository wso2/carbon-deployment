/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.webapp.mgt;

import org.apache.catalina.Session;
import org.apache.catalina.session.PersistentManagerBase;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CarbonTomcatSessionPersistentManager which ensures that one tenant's sessions cannot be accessed
 * by other tenants. Super tenant should be able to access all sessions
 * <p/>
 * PersistentManager has the capability to swap active (but idle) sessions out to a persistent
 * storage mechanism, as well as to save all sessions across a normal restart of Tomcat.
 * The actual persistent storage mechanism used is selected by your choice of a
 * Store element nested inside the Manager element - this is required for use of PersistentManager.
 */
public class CarbonTomcatSessionPersistentManager extends PersistentManagerBase {
    private static final List<String> allowedClasses = new ArrayList<String>();

    // Tenant ID of the tenant who owns this Tomcat Session Manager
    private int ownerTenantId;

    static {
        allowedClasses.add("org.apache.catalina.session.PersistentManagerBase");
        allowedClasses.add("org.apache.catalina.session.ManagerBase");
        allowedClasses.add("org.apache.catalina.connector.Request");
        allowedClasses.add("org.apache.catalina.session.StandardManager");
    }

    public CarbonTomcatSessionPersistentManager() {
    }

    public CarbonTomcatSessionPersistentManager(int ownerTenantId) {
        this.ownerTenantId = ownerTenantId;
    }

    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "CarbonTomcatSessionPersistentManager/1.0";

    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    protected static final String name = "CarbonTomcatSessionPersistentManager";

    /**
     * Return descriptive information about this Manager implementation and
     * the corresponding version number, in the format
     * <code>description/version</code>.
     */
    @Override
    public String getInfo() {
        return info;
    }

    /**
     * Return the descriptive short name of this Manager implementation.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Max number of concurrent active sessions
     *
     * @return The highest number of concurrent active sessions
     */
    @Override
    public int getMaxActive() {
        checkAccess();
        return super.getMaxActive();
    }

    /**
     * Returns the number of active sessions
     *
     * @return number of sessions active
     */
    @Override
    public int getActiveSessions() {
        checkAccess();
        return super.getActiveSessions();
    }

    /**
     * Gets the number of sessions that have expired.
     *
     * @return Number of sessions that have expired
     */
    @Override
    public long getExpiredSessions() {
        checkAccess();
        return super.getExpiredSessions();
    }

    /**
     * Returns the number of session creations that failed due to maxActiveSessions
     * @return rejected count
     */
    @Override
    public int getRejectedSessions() {
        checkAccess();
        return super.getRejectedSessions();
    }

    /**
     * Return the default maximum inactive interval (in seconds) for Sessions created by this Manager.
     *
     * @return Inactive time in minutes
     */
    @Override
    public int getMaxInactiveInterval() {
        checkAccess();
        return super.getMaxInactiveInterval();
    }

    /**
     * Sets the longest time (in seconds) that an expired session had been alive.
     * @param sessionMaxAliveTime sessionMaxAliveTime Longest time (in seconds) that an expired session had been alive.
     */
    @Override
    public void setSessionMaxAliveTime(int sessionMaxAliveTime) {
        checkAccess();
        super.setSessionMaxAliveTime(sessionMaxAliveTime);
    }

    /**
     * Gets the average time (in seconds) that expired sessions had been alive.
     *
     * @return Average time (in seconds) that expired sessions had been alive.
     */
    @Override
    public int getSessionAverageAliveTime() {
        checkAccess();
        return super.getSessionAverageAliveTime();
    }

    /**
     * Return the set of active Sessions associated with this Manager.
     * If this Manager has no active Sessions, a zero-length array is returned.
     *
     * @return Session objects associated with this Manager as a result set
     */
    @Override
    public Session[] findSessions() {
        checkAccess();
        return super.findSessions();
    }

    /**
     * Return the active Session, associated with this Manager, with the specified session id (if any);
     * otherwise return <code>null</code>.
     *
     * @param id The session id for the session to be returned
     * @return associated session
     * @throws java.io.IOException           if an input/output error occurs while processing this request
     * @throws IllegalStateException if a new session cannot be instantiated for any reason
     */
    @Override
    public Session findSession(String id) throws IOException {
        checkAccess();
        return super.findSession(id);
    }

    /**
     *
     */
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
        if (System.getProperty("java.vm.name").contains("IBM") && allowedClasses.contains(callingClass)) {
            return;
        }

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantId != MultitenantConstants.SUPER_TENANT_ID && tenantId != ownerTenantId) {
            throw new SecurityException("Illegal access attempt by  tenant[" + tenantId +
                                        "] to sessions owned by tenant[" + ownerTenantId + "]");
        }
    }

    /**
     * Sets the tenantID of the tenant who owns this Tomcat Session Manager
     * @param ownerTenantId Relevant tenantID which is associated to CarbonTomcatSessionPersistentManager
     */
    public void setOwnerTenantId(int ownerTenantId) {
        CarbonUtils.checkSecurity();
        this.ownerTenantId = ownerTenantId;
    }
}
