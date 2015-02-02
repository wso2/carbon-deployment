package org.wso2.carbon.webapp.mgt.sync;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.webapp.mgt.WebApplication;
import org.wso2.carbon.webapp.mgt.WebApplicationsHolder;
import org.wso2.carbon.webapp.mgt.WebapplicationHelper;
import org.wso2.carbon.webapp.mgt.WebappsConstants;
import org.wso2.carbon.webapp.mgt.WebappsConstants.ApplicationOpType;
import org.wso2.carbon.webapp.mgt.utils.WebAppUtils;

import java.util.List;
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
    private List<WebapplicationHelper> webapplicationHelperList;

    public ApplicationSynchronizeRequest() {
    }

    public ApplicationSynchronizeRequest(int tenantId, String tenantDomain, UUID messageId,
                                         ApplicationOpType applicationOpType, List<WebapplicationHelper> webapplicationHelperList) {
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.messageId = messageId;
        this.operation = applicationOpType;
        this.webapplicationHelperList = webapplicationHelperList;
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
                Map<String,WebApplicationsHolder> webApplicationsHolderMap = WebAppUtils.getAllWebappHolders(configContext);
                switch (operation) {
                    case STOP:
                        stopApplications(webApplicationsHolderMap);
                        break;
                    case START:
                        startApplications(tenantConfigurationContext, webApplicationsHolderMap);
                        break;
                    case RELOAD:
                        reloadApplications(webApplicationsHolderMap);
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

    private void startApplications(ConfigurationContext configContext, Map<String,WebApplicationsHolder> webApplicationsHolderMap) {
        for(WebApplicationsHolder webApplicationsHolder:webApplicationsHolderMap.values()){
            Map<String, WebApplication> stoppedWebapps = webApplicationsHolder.getStoppedWebapps();
            Deployer webappDeployer =
                    ((DeploymentEngine) configContext.getAxisConfiguration().getConfigurator()).
                            getDeployer(webApplicationsHolder.getWebappsDir().getName(),
                                    WebappsConstants.WEBAPP_EXTENSION);
            for(WebapplicationHelper webapplicationHelper:webapplicationHelperList){
                  WebApplication webApplication = stoppedWebapps.get(webapplicationHelper.getWebappName());
                  if(webApplication!=null && (webapplicationHelper.getHostName()).equals(webApplication.getHostName())){
                      try {
                          boolean started = webApplication.start();
                          if (started) {
                              String startedWebappFileName = webApplication.getWebappFile().getName();
                              stoppedWebapps.remove(startedWebappFileName);
                              Map<String, WebApplication> startedWebapps = webApplicationsHolder.getStartedWebapps();
                              startedWebapps.put(startedWebappFileName, webApplication);
                          }
                      } catch (CarbonException e) {
                          String msg = "Cannot start Application " + webApplication;
                          log.error(msg, e);
                      }
                  } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No stopped webapp " + webapplicationHelper.getWebappName() + "found for tenant:" + tenantDomain);
                    }
                  }
            }
        }

    }

    private void reloadApplications(Map<String,WebApplicationsHolder> webApplicationsHolderMap) {
        for(WebApplicationsHolder webApplicationsHolder:webApplicationsHolderMap.values()){
            for(WebapplicationHelper webapplicationHelper:webapplicationHelperList){
                WebApplication webApplication = webApplicationsHolder.getStartedWebapps().get(webapplicationHelper.getWebappName());
                if(webApplication!=null && (webapplicationHelper.getHostName()).equals(webApplication.getHostName())){
                    webApplication.reload();
                }
            }
        }
    }

    private void stopApplications(Map<String,WebApplicationsHolder> webApplicationsHolderMap) {
        for(WebApplicationsHolder webApplicationsHolder:webApplicationsHolderMap.values()){
            Map<String, WebApplication> startedWebapps = webApplicationsHolder.getStartedWebapps();
            for(WebapplicationHelper webapplicationHelper:webapplicationHelperList){
                try{
                    WebApplication webApplication = startedWebapps.get(webapplicationHelper.getWebappName());
                    if(webApplication!=null &&  (webapplicationHelper.getHostName()).equals(webApplication.getHostName())){
                        webApplicationsHolder.stopWebapp(webApplication);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("No started webapp " + webapplicationHelper.getWebappName() + "found for tenant:" + tenantDomain);
                        }
                    }
                } catch (CarbonException e) {
                    log.error("Error occurred while undeploying Applications", e);
                }
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

