package org.wso2.carbon.jaxws.webapp.mgt;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.webapp.mgt.*;

import java.io.File;

public class JaxwsWebappAdmin extends WebappAdmin {

    public boolean uploadWebapp(WebappUploadData[] webappUploadDataList) throws AxisFault {
        return super.uploadWebapp(webappUploadDataList);
    }

    protected String getWebappDeploymentDirPath(String webappType) {
        return getAxisConfig().getRepository().getPath() + File.separator +
                JaxwsWebappConstants.JAX_WEBAPP_DEPLOYMENT_DIR;
    }

    protected boolean isWebappRelevant(WebApplication webapp, String webappType) {
        // Check the filter..
        String filterProp = (String) webapp.getProperty(WebappsConstants.WEBAPP_FILTER);
        // If this is a JAX webapp, return true..
        if (filterProp != null &&
                JaxwsWebappConstants.JAX_WEBAPP_FILTER_PROP.equals(filterProp)) {
            return true;
        }
        return false;
    }

}
