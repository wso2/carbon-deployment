package org.wso2.carbon.webapp.deployer.internal;

import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.utils.component.xml.config.DeployerConfig;
import org.wso2.carbon.utils.deployment.Axis2DeployerProvider;
import org.wso2.carbon.webapp.mgt.DataHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VirtualHostDeployerProvider implements Axis2DeployerProvider {

    private List<DeployerConfig> deployerConfigs = new ArrayList<DeployerConfig>();

    public VirtualHostDeployerProvider() {
        CarbonTomcatService carbonTomcatService = DataHolder.getCarbonTomcatService();
        Container[] virtualhosts = carbonTomcatService.getTomcat().getEngine().findChildren();
        for (org.apache.catalina.Container vhost : virtualhosts) {
            Host childHost = (Host) vhost;
            String directory = getDirectoryName(childHost.getAppBase());

            deployerConfigs.add(getDeployerConfig(directory, ".war"));
            deployerConfigs.add(getDeployerConfig(directory, null));
        }
    }

    @Override
    public List<DeployerConfig> getDeployerConfigs() {
        return deployerConfigs;
    }

    private DeployerConfig getDeployerConfig(String directoryName, String extension) {
        DeployerConfig deployerConfig = new DeployerConfig();
        deployerConfig.setClassStr("org.wso2.carbon.webapp.deployer.WebappDeployer");
        deployerConfig.setDirectory(directoryName);
        deployerConfig.setExtension(extension);

        return deployerConfig;
    }

    public String getDirectoryName(String appBase) {
        String basedir;
        if (appBase.endsWith(File.separator)) {
            basedir = appBase.substring(0, appBase.lastIndexOf(File.separator));
        } else {
            basedir = appBase;
        }

        return basedir.substring(basedir.lastIndexOf(File.separator) + 1, basedir.length());
    }
}