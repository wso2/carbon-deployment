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
package org.wso2.carbon.deployment.notifier.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.LifecycleListener;
import org.wso2.carbon.deployment.notifier.DeploymentNotifierLifecycleListener;
import org.wso2.carbon.kernel.CarbonRuntime;

/**
 * This service component is responsible for initializing the DeploymentEngine and listening for deployer registrations.
 *
 * @since 5.0.0
 */
@Component(
        name = "DeploymentNotifierListenerComponent",
        immediate = true
)
public class DeploymentNotifierListenerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentNotifierListenerComponent.class);

    /**
     * This is the activation method of DeploymentNotifierListenerComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of the carbon core bundle used service registration, etc.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    public void start(BundleContext bundleContext) throws Exception {
        LifecycleListener deploymentCoordinationLifecycleListener = new DeploymentNotifierLifecycleListener();
        bundleContext.registerService(LifecycleListener.class, deploymentCoordinationLifecycleListener, null);
        logger.debug("Registered DeploymentNotifierLifecycleListener.");
    }

    /**
     * This is the deactivation method of DeploymentNotifierListenerComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */

    @Deactivate
    public void stop() throws Exception {
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

}
