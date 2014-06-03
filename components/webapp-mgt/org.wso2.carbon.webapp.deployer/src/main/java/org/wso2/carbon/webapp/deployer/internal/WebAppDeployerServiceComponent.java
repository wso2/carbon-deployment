package org.wso2.carbon.webapp.deployer.internal;

import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.utils.component.xml.config.DeployerConfig;
import org.wso2.carbon.utils.deployment.Axis2DeployerProvider;
import org.wso2.carbon.webapp.mgt.DataHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="org.wso2.carbon.webapp.deployer.internal.WebAppDeployerServiceComponent"
 * immediate="true"
 * @scr.reference name="carbon.tomcat.service"
 * interface="org.wso2.carbon.tomcat.api.CarbonTomcatService"
 * cardinality="0..1" policy="dynamic" bind="setCarbonTomcatService"
 * unbind="unsetCarbonTomcatService"
 */
public class WebAppDeployerServiceComponent implements Axis2DeployerProvider {

    private static final Log log = LogFactory.getLog(WebAppDeployerServiceComponent.class);
    private List<DeployerConfig> deployerConfigs;
    private ServiceRegistration registration;


    protected void activate(ComponentContext ctx) {
        CarbonTomcatService carbonTomcatService = DataHolder.getCarbonTomcatService();
        Container[] virtualhosts = carbonTomcatService.getTomcat().getEngine().findChildren();
        deployerConfigs = new ArrayList<DeployerConfig>();
        registration = (ctx.getBundleContext()).registerService(Axis2DeployerProvider.class.getName(), this, null);
        addDeployers(virtualhosts);

    }

    public void addDeployers(Container[] virtualhosts){

        for(org.apache.catalina.Container vhost:virtualhosts){
            Host childHost = (Host)vhost;
            String directory = getDirectoryName(childHost.getAppBase());

            deployerConfigs.add(getDeployerConfig(directory,".war"));
            deployerConfigs.add(getDeployerConfig(directory,null));
        }
    }

    public DeployerConfig getDeployerConfig(String directoryName,String extension){
        DeployerConfig deployerConfig = new DeployerConfig();
        deployerConfig.setClassStr("org.wso2.carbon.webapp.deployer.WebappDeployer");
        deployerConfig.setDirectory(directoryName);
        deployerConfig.setExtension(extension);

        return deployerConfig;
    }

    public String getDirectoryName(String appBase){
        String basedir =  "";
        if(appBase.endsWith("/")){
            basedir = appBase.substring(0, appBase.lastIndexOf(File.separator));
        } else {
            basedir = appBase;
        }
        String directory = basedir.substring(basedir.lastIndexOf(File.separator)+1,basedir.length());

        return directory;
    }

    @Override
    public DeployerConfig[] getDeployerConfigs() {
        DeployerConfig[] deployers = new DeployerConfig[deployerConfigs.size()];
        deployerConfigs.toArray(deployers);
        return deployers;
    }

    protected void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        DataHolder.setCarbonTomcatService(carbonTomcatService);

    }


    protected void unsetCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        DataHolder.setCarbonTomcatService(null);
    }
}
