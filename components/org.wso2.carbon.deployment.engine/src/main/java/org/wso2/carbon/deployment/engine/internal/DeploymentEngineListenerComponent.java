/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.deployment.engine.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.DeploymentService;
import org.wso2.carbon.deployment.engine.LifecycleListener;
import org.wso2.carbon.deployment.engine.config.DeploymentConfiguration;
import org.wso2.carbon.deployment.engine.exception.DeployerRegistrationException;
import org.wso2.carbon.deployment.engine.exception.DeploymentEngineException;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.kernel.startupresolver.StartupServiceUtils;

/**
 * This service component is responsible for initializing the DeploymentEngine and listening for deployer registrations.
 *
 * @since 5.0.0
 */
@Component(
        name = "org.wso2.carbon.deployment.engine.internal.DeploymentEngineListenerComponent",
        immediate = true,
        property = {
                "componentName=" + DeploymentEngineListenerComponent.COMPONENT_NAME
        }
)
@SuppressWarnings("unused")
public class DeploymentEngineListenerComponent implements RequiredCapabilityListener {
    public static final String COMPONENT_NAME = "carbon-deployment-service";
    private static final Logger logger = LoggerFactory.getLogger(DeploymentEngineListenerComponent.class);

    private DeploymentEngine deploymentEngine;
    private ServiceRegistration serviceRegistration;


    public DeploymentEngineListenerComponent() {
        deploymentEngine = new DeploymentEngine();
    }

    /**
     * This is the activation method of DeploymentEngineComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of the carbon core bundle used service registration, etc.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */

    @Activate
    public void start(BundleContext bundleContext) throws Exception {
        DataHolder.getInstance().setBundleContext(bundleContext);
    }

    /**
     * This is the deactivation method of DeploymentEngineComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */

    @Deactivate
    public void stop() throws Exception {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    /**
     * The is a dependency of DeploymentEngineComponent for deployer registrations from other bundles.
     * This is the bind method that gets called for deployer instance registrations that satisfy the policy.
     *
     * @param deployer the deployer instances that are registered as services.
     */
    @Reference(
            name = "carbon.deployer.service",
            service = Deployer.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDeployer"
    )
    protected void registerDeployer(Deployer deployer) {
        try {
            deploymentEngine.registerDeployer(deployer);
            StartupServiceUtils.updateServiceCache(COMPONENT_NAME, Deployer.class);
        } catch (DeployerRegistrationException e) {
            logger.error("Error while adding deployer to the deployment engine", e);
        }
    }

    /**
     * This is the unbind method for the above reference that gets called for deployer instance un-registrations.
     *
     * @param deployer the deployer instances that are un-registered.
     */
    protected void unregisterDeployer(Deployer deployer) {
        try {
            deploymentEngine.unregisterDeployer(deployer);
        } catch (DeploymentEngineException e) {
            logger.error("Error while removing deployer from deployment engine", e);
        }
    }

    /**
     * This is a dependency of DeploymentEngineComponent for deployment listener registrations from other bundles.
     * This is the bind method that gets called for deployment listener instance registrations that satisfy the policy.
     *
     * @param listener the deployer instances that are registered as services.
     */
    @Reference(
            name = "carbon.deployment.listener.service",
            service = LifecycleListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDeploymentListener"
    )
    protected void registerDeploymentListener(LifecycleListener listener) {
        logger.debug("Received LifecycleListener {} ", listener.getClass().getName());
        deploymentEngine.registerDeploymentLifecycleListener(listener);
        StartupServiceUtils.updateServiceCache(COMPONENT_NAME, LifecycleListener.class);
    }

    /**
     * This is the unbind method for the above reference that gets called for deployer instance un-registrations.
     *
     * @param listener the deployer instances that are un-registered.
     */
    protected void unregisterDeploymentListener(LifecycleListener listener) {
        deploymentEngine.unregisterDeploymentLifecycleListener(listener);
    }

    /**
     * Get the CarbonRuntime service.
     * This is the bind method that gets called for CarbonRuntime service registration that satisfy the policy.
     *
     * @param carbonRuntime the CarbonRuntime service that is registered as a service.
     */
    @Reference(
            name = "carbon.runtime.service",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterCarbonRuntime"
    )
    protected void registerCarbonRuntime(CarbonRuntime carbonRuntime) {
        DataHolder.getInstance().setCarbonRuntime(carbonRuntime);
    }

    /**
     * This is the unbind method for the above reference that gets called for CarbonRuntime instance un-registrations.
     *
     * @param carbonRuntime the CarbonRuntime service that get unregistered.
     */
    protected void unregisterCarbonRuntime(CarbonRuntime carbonRuntime) {
        DataHolder.getInstance().setCarbonRuntime(null);
    }

    /**
     * Get the ConfigProvider service.
     * This is the bind method that gets called for ConfigProvider service registration that satisfy the policy.
     *
     * @param configProvider the ConfigProvider service that is registered as a service.
     */
    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(configProvider);
    }

    /**
     * This is the unbind method for the above reference that gets called for ConfigProvider instance un-registrations.
     *
     * @param configProvider the ConfigProvider service that get unregistered.
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(null);
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        try {
            // Initialize deployment engine and scan it
            DeploymentConfiguration deploymentConfiguration = DataHolder.getInstance().getConfigProvider()
                    .getConfigurationObject(DeploymentConfiguration.class);
            DataHolder.getInstance().setDeploymentConfiguration(deploymentConfiguration);

            logger.debug("Starting Carbon Deployment Engine");
            deploymentEngine.start(deploymentConfiguration.getServerRepositoryLocation(),
                                   deploymentConfiguration.getRuntimeRepositoryLocation());

            // Add deployment engine to the data holder for later usages/references of this object
            OSGiServiceHolder.getInstance().setCarbonDeploymentEngine(deploymentEngine);

            // Register DeploymentService
            DeploymentService deploymentService = new CarbonDeploymentService(deploymentEngine);
            BundleContext bundleContext = DataHolder.getInstance().getBundleContext();
            serviceRegistration = bundleContext.registerService(DeploymentService.class.getName(),
                    deploymentService, null);

            logger.debug("Carbon Deployment Engine is successfully started");
        } catch (DeploymentEngineException e) {
            String msg = "Could not initialize carbon deployment engine";
            logger.error(msg, e);
        } catch (ConfigurationException e) {
            String msg = "Fail to load deployment configuration";
            logger.error(msg, e);
        }
    }
}
