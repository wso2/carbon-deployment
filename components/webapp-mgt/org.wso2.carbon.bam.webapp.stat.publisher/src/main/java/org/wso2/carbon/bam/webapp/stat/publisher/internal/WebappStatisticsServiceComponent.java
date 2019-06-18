package org.wso2.carbon.bam.webapp.stat.publisher.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.EventPublisherConfig;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.InternalEventingConfigData;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.RegistryPersistenceManager;
import org.wso2.carbon.bam.webapp.stat.publisher.data.CarbonDataHolder;
import org.wso2.carbon.bam.webapp.stat.publisher.publish.GlobalWebappEventPublisher;
import org.wso2.carbon.bam.webapp.stat.publisher.publish.WebappAgentUtil;
import org.wso2.carbon.bam.webapp.stat.publisher.util.TenantEventConfigData;
import org.wso2.carbon.bam.webapp.stat.publisher.util.WebappStatisticsPublisherConstants;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.bam.webapp.stat.publisher ", 
         immediate = true)
public class WebappStatisticsServiceComponent {

    private static boolean publishingEnabled;

    private static Log log = LogFactory.getLog(WebappStatisticsServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        if ("true".equals(System.getProperty("metering.enabled"))) {
            GlobalWebappEventPublisher.createGlobalEventStream(getPublishingConfig());
            WebappAgentUtil.setGlobalPublishingEnabled(true);
        }
        checkPublishingEnabled();
        WebappAgentUtil.setPublishingEnabled(publishingEnabled);
        if (publishingEnabled) {
            try {
                BundleContext bundleContext = context.getBundleContext();
                bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), new WebappStatisticsAxis2ConfigurationContextObserver(), null);
                new RegistryPersistenceManager().load();
                // if adding the valve programmatically, it can be done here.
                log.info("BAM webapp statistics data publisher bundle is activated");
            } catch (Throwable t) {
                log.error("Failed to activate BAM webapp statistics data publisher bundle", t);
            }
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) throws DataEndpointException {
        if (log.isDebugEnabled()) {
            log.debug("BAM service statistics data publisher bundle is deactivated");
        }
        Map<Integer, InternalEventingConfigData> tenantSpecificEventConfig = TenantEventConfigData.getTenantSpecificEventingConfigData();
        for (Map.Entry<Integer, InternalEventingConfigData> entry : tenantSpecificEventConfig.entrySet()) {
            InternalEventingConfigData configData = entry.getValue();
            String key = configData.getUrl() + "_" + configData.getUserName() + "_" + configData.getPassword();
            EventPublisherConfig eventPublisherConfig = WebappAgentUtil.getEventPublisherConfig(key);
            if (null != eventPublisherConfig) {
                if (null != eventPublisherConfig.getDataPublisher()) {
                    eventPublisherConfig.getDataPublisher().shutdownWithAgent();
                }
            }
        }
    }

    /*
    *  Checks weather web apps statistics publishing is enabled in the <WSO2 Application Server home>/repository/conf/etc/bam.xml
    */
    private void checkPublishingEnabled() {
        OMElement bamConfig = getPublishingConfig();
        if (null != bamConfig) {
            OMElement servicePublishElement = bamConfig.getFirstChildWithName(new QName(WebappStatisticsPublisherConstants.WEBAPPDATAPUBLISHING));
            if (null != servicePublishElement) {
                if (servicePublishElement.getText().trim().equalsIgnoreCase(WebappStatisticsPublisherConstants.ENABLE)) {
                    publishingEnabled = true;
                    log.info("BAM Web Apps Statistics Publishing is enabled");
                } else {
                    log.info("BAM Web Apps Statistics Publishing is disabled");
                    publishingEnabled = false;
                }
            } else {
                publishingEnabled = false;
            }
        } else {
            log.warn("Invalid " + WebappStatisticsPublisherConstants.BAMXML + ". Disabling service publishing.");
            publishingEnabled = false;
        }
    }

    /*
    *   Reads the <WSO2 Application Server home>/repository/conf/etc/bam.xml
    */
    private OMElement getPublishingConfig() {
        String bamConfigPath = CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + WebappStatisticsPublisherConstants.BAMXML;
        File bamConfigFile = new File(bamConfigPath);
        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            InputStream inputStream = new FileInputStream(bamConfigFile);
            XMLStreamReader reader = xif.createXMLStreamReader(inputStream);
            xif.setProperty("javax.xml.stream.isCoalescing", false);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            return builder.getDocument().getOMDocumentElement();
        } catch (FileNotFoundException e) {
            log.warn("No " + WebappStatisticsPublisherConstants.BAMXML + " is found in " + bamConfigPath);
            return null;
        } catch (XMLStreamException e) {
            log.error("Incorrect format in " + WebappStatisticsPublisherConstants.BAMXML + " file", e);
            return null;
        }
    }

    @Reference(
             name = "config.context.service", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        CarbonDataHolder.setServerConfigContext(configurationContextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        CarbonDataHolder.setServerConfigContext(null);
    }

    @Reference(
             name = "user.realmservice.default", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        CarbonDataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        CarbonDataHolder.setRealmService(null);
    }

    @Reference(
             name = "org.wso2.carbon.registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        try {
            RegistryPersistenceManager.setRegistryService(registryService);
        } catch (Exception e) {
            log.error("Cannot retrieve System Registry", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        RegistryPersistenceManager.setRegistryService(null);
    }

    public static boolean isPublishingEnabled() {
        return publishingEnabled;
    }
}

