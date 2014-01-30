package org.wso2.carbon.webapp.mgt.sync;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.webapp.mgt.WebApplication;
import org.wso2.carbon.webapp.mgt.WebApplicationsHolder;
import org.wso2.carbon.webapp.mgt.WebappsConstants;
import org.wso2.carbon.webapp.mgt.WebappsConstants.ApplicationOpType;

import java.util.Map;
import java.util.UUID;

/**
 * ClusterMessage for sending a Application status synchronization request
 */
public class ApplicationSynchronizeRequest extends ClusteringMessage {
    private transient static final Log log = LogFactory.getLog(ApplicationSynchronizeRequest.class);
    private int tenantId;
    private String tenantDomain;
    private UUID messageId;
    private ApplicationOpType operation;
    private String[] webappFileNames;

    public ApplicationSynchronizeRequest() {
    }

    public ApplicationSynchronizeRequest(int tenantId, String tenantDomain, UUID messageId,
                                         ApplicationOpType applicationOpType, String[] webappFileNames) {
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.messageId = messageId;
        this.operation = applicationOpType;
        this.webappFileNames = webappFileNames;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public void execute(ConfigurationContext configContext) throws ClusteringFault {
        if (log.isDebugEnabled()) {
            log.debug("Received [" + this + "] ");
        }
        // Run only if the tenant is loaded
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID ||
                TenantAxisUtils.getTenantConfigurationContexts(configContext).get(tenantDomain) != null) {
            if (log.isDebugEnabled()) {
                log.debug("Going to synchronize Application status for tenant: TID - " + tenantId + " TD - " + tenantDomain);
            }
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext privilegedCarbonContext =
                        PrivilegedCarbonContext.getThreadLocalCarbonContext();
                privilegedCarbonContext.setTenantId(tenantId);
                privilegedCarbonContext.setTenantDomain(tenantDomain);

                ConfigurationContext tenantConfigurationContext;
                if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                    tenantConfigurationContext = configContext;
                } else {
                    tenantConfigurationContext = TenantAxisUtils.getTenantConfigurationContexts(configContext).get(tenantDomain);
                }
                WebApplicationsHolder webappsHolder = (WebApplicationsHolder)
                        tenantConfigurationContext.getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER);
                switch (operation) {
                    case STOP:
                        stopApplications(webappsHolder);
                        break;
                    case START:
                        startApplications(tenantConfigurationContext, webappsHolder);
                        break;
                    case RELOAD:
                        reloadApplications(webappsHolder);
                        break;
                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Tenant is not loaded. TID - " + tenantId + " TD - " + tenantDomain);
            }
        }
    }

    private void startApplications(ConfigurationContext configContext, WebApplicationsHolder webappsHolder) {
        Map<String, WebApplication> stoppedWebapps = webappsHolder.getStoppedWebapps();
        Deployer webappDeployer =
                ((DeploymentEngine) configContext.getAxisConfiguration().getConfigurator()).
                        getDeployer(WebappsConstants.WEBAPP_DEPLOYMENT_FOLDER,
                                WebappsConstants.WEBAPP_EXTENSION);
        for (String webappFileName : webappFileNames) {
            WebApplication webapp = stoppedWebapps.get(webappFileName);
            if (webapp != null) {
                try {
                    boolean started = webapp.start();
                    if (started) {
                        String startedWebappFileName = webapp.getWebappFile().getName();
                        stoppedWebapps.remove(startedWebappFileName);
                        Map<String, WebApplication> startedWebapps = webappsHolder.getStartedWebapps();
                        startedWebapps.put(startedWebappFileName, webapp);
                    }
                } catch (CarbonException e) {
                    String msg = "Cannot start Application " + webapp;
                    log.error(msg, e);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No stopped webapp " + webappFileName + "found for tenant:" + tenantDomain);
                }
            }
        }
    }

    private void reloadApplications(WebApplicationsHolder webappsHolder) {
        for (String webappFileName : webappFileNames) {
            webappsHolder.getStartedWebapps().get(webappFileName).reload();
        }
    }

    private void stopApplications(WebApplicationsHolder webappsHolder) {
        Map<String, WebApplication> startedWebapps = webappsHolder.getStartedWebapps();
        for (String webappFileName : webappFileNames) {
            try {
                WebApplication webApplication = startedWebapps.get(webappFileName);
                if (webApplication != null) {
                    webappsHolder.stopWebapp(webApplication);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No started webapp " + webappFileName + "found for tenant:" + tenantDomain);
                    }
                }
            } catch (CarbonException e) {
                log.error("Error occurred while undeploying Applications", e);
            }
        }
    }

    public ClusteringCommand getResponse() {
        return null;
    }

    @Override
    public String toString() {
        return "ApplicationSynchronizeRequest{" +
                "tenantId=" + tenantId +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", messageId=" + messageId +
                '}';
    }
}

