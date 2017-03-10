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
package org.wso2.carbon.deployment.engine.osgi;

import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.DeploymentService;
import org.wso2.carbon.deployment.engine.LifecycleListener;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;
import javax.inject.Inject;

/**
 * Carbon Deployment Engine OSGi Test case.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class CarbonDeploymentEngineOSGiTest {

    public static final String DEPLOYMENT_YAML = "deployment.yaml";

    @Inject
    private BundleContext bundleContext;

    @Inject
    private DeploymentService deploymentService;

    private static String artifactPath;

    static {
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get("../../").toString();
        }
        Path testResourceDir = Paths.get(basedir, "src", "test", "resources");
        artifactPath = Paths.get(testResourceDir.toString(), "deployment", "text-files", "sample1.txt").toString();
    }

    @Test
    public void testRegisterDeployer() {
        ServiceRegistration serviceRegistration = bundleContext.registerService(Deployer.class.getName(),
                new CustomDeployer(), null);
        ServiceReference reference = bundleContext.getServiceReference(Deployer.class.getName());
        Assert.assertNotNull(reference, "Custom Deployer Service Reference is null");
        CustomDeployer deployer = (CustomDeployer) bundleContext.getService(reference);
        Assert.assertNotNull(deployer, "Custom Deployer Service is null");
        serviceRegistration.unregister();
        reference = bundleContext.getServiceReference(Deployer.class.getName());
        Assert.assertNull(reference, "Custom Deployer Service Reference should be unregistered and null");

        //register faulty deployers
        CustomDeployer deployer1 = new CustomDeployer();
        deployer1.setArtifactType(null);
        bundleContext.registerService(Deployer.class.getName(), deployer1, null);

        CustomDeployer deployer2 = new CustomDeployer();
        deployer2.setLocation(null);
        bundleContext.registerService(Deployer.class.getName(), deployer2, null);
    }

    @Test
    public void testRegisterLifecycleListener() throws InvalidSyntaxException {
        ServiceRegistration serviceRegistration = bundleContext.registerService(LifecycleListener.class.getName(),
                new CustomLifecycleListener(), null);
        ServiceReference[] references = bundleContext.getServiceReferences(LifecycleListener.class.getName(), null);
        Assert.assertNotNull(references, "The CustomLifecycleListener service reference cannot be found.");

        Optional<ServiceReference> reference = Stream.of(references).filter(this::isCustomLC).findFirst();


        Assert.assertTrue(reference.isPresent(), "The CustomLifecycleListener is not registered.");

        LifecycleListener listener = (LifecycleListener) bundleContext.getService(reference.get());
        Assert.assertNotNull(listener, "The CustomLifecycleListener is not registered.");

        serviceRegistration.unregister();
        references = bundleContext.getServiceReferences(LifecycleListener.class.getName(), null);
        reference = Stream.of(references).filter(this::isCustomLC).findFirst();
        Assert.assertFalse(reference.isPresent(), "The CustomLifecycleListener service un-registration failed.");
    }

    private boolean isCustomLC(ServiceReference serviceReference) {
        String listenerClass = bundleContext.getService(serviceReference).getClass().getName();
        return listenerClass.equals("org.wso2.carbon.deployment.engine.osgi.CustomLifecycleListener");
    }

    @Test(dependsOnMethods = {"testRegisterDeployer"})
    public void testDeploymentService() throws CarbonDeploymentException {
        Assert.assertNotNull(deploymentService);
        CustomDeployer customDeployer = new CustomDeployer();
        bundleContext.registerService(Deployer.class.getName(), customDeployer, null);
        bundleContext.registerService(LifecycleListener.class.getName(), new CustomLifecycleListener(), null);

        //undeploy
        try {
            deploymentService.undeploy(artifactPath, new ArtifactType<>("unknown"));
        } catch (CarbonDeploymentException e) {
            Assert.assertTrue(e.getMessage().contains("Unknown artifactType"));
        }
        try {
            deploymentService.undeploy("fake.path", customDeployer.getArtifactType());
        } catch (CarbonDeploymentException e) {
            Assert.assertEquals(e.getMessage(), "Cannot find artifact with key : fake.path to undeploy");
        }
        try {
            deploymentService.undeploy(artifactPath, customDeployer.getArtifactType());
        } catch (CarbonDeploymentException e) {
            Assert.assertEquals(e.getMessage(), "Cannot find artifact with key : " + artifactPath + " to undeploy");
        }
        //deploy
        try {
            deploymentService.deploy("fake.path", customDeployer.getArtifactType());
        } catch (CarbonDeploymentException e) {
            Assert.assertTrue(e.getMessage().contains("Error wile copying artifact"));
        }
        try {
            deploymentService.deploy(artifactPath, new ArtifactType<>("unknown"));
        } catch (CarbonDeploymentException e) {
            Assert.assertTrue(e.getMessage().contains("Unknown artifactType"));
        }
        deploymentService.deploy(artifactPath, customDeployer.getArtifactType());

        //redeploy - this does not do anything for the moment.
        deploymentService.redeploy(artifactPath, customDeployer.getArtifactType());
    }

}
