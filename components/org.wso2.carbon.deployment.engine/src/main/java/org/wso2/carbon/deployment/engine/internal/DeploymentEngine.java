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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.LifecycleEvent;
import org.wso2.carbon.deployment.engine.LifecycleListener;
import org.wso2.carbon.deployment.engine.config.DeploymentConfiguration;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.deployment.engine.exception.DeployerRegistrationException;
import org.wso2.carbon.deployment.engine.exception.DeploymentEngineException;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Deployment Engine of Carbon which manages the deployment/undeployment of artifacts in carbon.
 *
 * @since 5.0.0
 */

public class DeploymentEngine {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentEngine.class);

    /**
     * The repository scanner associated with this engine.
     */
    private RepositoryScanner repositoryScanner;

    /**
     * Server repository directory for this deployment engine.
     */
    private File serverRepositoryDirectory;

    /**
     * Runtime repository directory for this deployment engine.
     */
    private File runtimeRepositoryDirectory;

    /**
     * The map which holds the set of registered deployers with this engine.
     */
    private Map<ArtifactType, Deployer> deployerMap = new ConcurrentHashMap<>();

    /**
     * The list which holds the set of registered lifecycle listeners with this engine.
     * todo need to keep an index to order the listeners?
     */
    private List<LifecycleListener> lifecycleListeners = new ArrayList<>();

    /**
     * A map to hold all currently deployed artifacts.
     */
    private Map<ArtifactType, ConcurrentHashMap<Object, Artifact>> deployedArtifacts = new ConcurrentHashMap<>();

    /**
     * A map to hold faulty artifacts.
     */
    private Map<String, Artifact> faultyArtifacts = new ConcurrentHashMap<>();


    /**
     * Configure and prepare the repository associated with this engine.
     */
    public DeploymentEngine() {
        repositoryScanner = new RepositoryScanner(this);
    }

    /**
     * Starts the Deployment engine to perform Hot deployment and so on.
     * This will start the repository scanner and scheduler task and load artifacts to
     * the deployment engine.
     *
     * @param serverRepositoryDir server deployment repository directory that repository scanner will start scanning.
     * @param runtimeRepositoryDir runtime deployment repository directory that repository scanner will start scanning.
     * @throws DeploymentEngineException when an error occurs while trying to start the deployment engine.
     */
    public void start(String serverRepositoryDir, String runtimeRepositoryDir) throws DeploymentEngineException {
        serverRepositoryDirectory = new File(serverRepositoryDir);
        runtimeRepositoryDirectory = new File(runtimeRepositoryDir);

        if (!serverRepositoryDirectory.exists()) {
            throw new DeploymentEngineException("Cannot find repository : " + serverRepositoryDirectory);
        }
        if (!runtimeRepositoryDirectory.exists()) {
            throw new DeploymentEngineException("Cannot find repository : " + runtimeRepositoryDirectory);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Starting carbon deployment engine for repository : " + serverRepositoryDir);
        }

        //Deploy initial set of artifacts
        repositoryScanner.scan();
        // We need to check and scan the task based on the deployment engine mode of operation
        // Currently there can be two modes
        // 1. Scheduled Mode - where the task runs periodically and trigger deployment
        // 2. Triggered Mode - the deployment has to be triggered externally,
        //    eg : in a worker node we don't need the task to run, but rather when we receive a
        //         cluster msg,  the deployment has to be triggered manually
        //TODO : Need to check whether we need to scan the task when triggered mode is enabled
        startScheduler();
    }


    private void startScheduler() throws DeploymentEngineException {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
        SchedulerTask schedulerTask = new SchedulerTask(repositoryScanner);
        DeploymentConfiguration deploymentConfiguration = DataHolder.getInstance().getDeploymentConfiguration();

        int interval = 15;
        if (deploymentConfiguration != null) {
            interval = deploymentConfiguration.getUpdateInterval();
            logger.debug("Using the specified scheduler update interval of {}", interval);
        } else {
            logger.debug("Using the default deployment scheduler update interval of 15 seconds");
        }
        executorService.scheduleWithFixedDelay(schedulerTask, 0, interval, TimeUnit.SECONDS);
    }

    /**
     * Add and initialize a new Deployer to deployment engine.
     *
     * @param deployer the deployer instance to register
     * @throws DeployerRegistrationException Throwing deployment registration exception
     */
    public void registerDeployer(Deployer deployer) throws DeployerRegistrationException {
        if (deployer == null) {
            throw new DeployerRegistrationException("Failed to add Deployer : Deployer Class Name is null");
        }
        logger.debug("Registering deployer instance {} with deployment engine", deployer.getClass().getName());
        // Try and initialize the deployer
        deployer.init();

        if (deployer.getLocation() == null) {
            throw new DeployerRegistrationException("Failed to add Deployer " + deployer.getClass().getName() +
                    " : missing 'directory' attribute in deployer instance");
        }
        ArtifactType type = deployer.getArtifactType();

        if (type == null) {
            throw new DeployerRegistrationException("Artifact Type for Deployer : " + deployer + " is null");
        }

        Deployer existingDeployer = deployerMap.get(type);
        if (existingDeployer == null) {
            deployerMap.put(type, deployer);
        }
    }

    /**
     * Removes a deployer from the deployment engine configuration.
     *
     * @param deployer the deployer instance to un-register
     * @throws DeploymentEngineException Throwing deployment registration exception
     */
    public void unregisterDeployer(Deployer deployer) throws DeploymentEngineException {
        logger.debug("Un-registering deployer instance {} from deployment engine", deployer.getClass().getName());
        ArtifactType type = deployer.getArtifactType();
        if (type == null) {
            throw new DeploymentEngineException("Artifact Type for Deployer : " + deployer +
                    " is null");
        }

        Deployer existingDeployer = deployerMap.get(type);
        if (existingDeployer != null) {
            deployerMap.remove(type);
        }
    }

    /**
     * Retrieve the deployer from the current deployers map, by giving the associated directory.
     *
     * @param type the artifact type that the deployer is associated with
     * @return Deployer instance
     */
    public Deployer getDeployer(ArtifactType type) {
        return deployerMap.get(type);
    }

    /**
     * Return the registered deployers as a Map.
     *
     * @return registered deployers
     */
    public Map<ArtifactType, Deployer> getDeployers() {
        return this.deployerMap;
    }

    public void registerDeploymentLifecycleListener(LifecycleListener lifecycleListener) {
        logger.debug("Registering deployment listener instance {} with deployment engine",
                lifecycleListener.getClass().getName());

        lifecycleListeners.add(lifecycleListener);
    }

    public void unregisterDeploymentLifecycleListener(LifecycleListener lifecycleListener) {
        logger.debug("Un-registering deployment listener instance {} from deployment engine",
                lifecycleListener.getClass().getName());

        lifecycleListeners.remove(lifecycleListener);
    }

    /**
     * Returns the server repository directory that the deployment engine is registered with.
     * Eg: CARBON_HOME/deployment
     *
     * @return server repository directory
     */
    public File getServerRepositoryDirectory() {
        return serverRepositoryDirectory;
    }

    /**
     * Returns the runtime repository directory that the deployment engine is registered with.
     * Eg: CARBON_HOME/wso2/<runtime-name>/deployment
     *
     * @return runtime repository directory
     */
    public File geRuntimeRepositoryDirectory() {
        return runtimeRepositoryDirectory;
    }

    /**
     * This will return an artifact for given artifactkey and directory from
     * currently deployed artifacts.
     *
     * @param type        type of the artifact
     * @param artifactKey key of an artifact is used to uniquely identify it self within a runtime
     * @return the deployed artifact for given key and type
     */

    public Artifact getDeployedArtifact(ArtifactType type, Object artifactKey) {
        Artifact artifact = null;
        if (deployedArtifacts.get(type) != null) {
            artifact = deployedArtifacts.get(type).get(artifactKey);
        }
        return artifact;
    }

    public Map<ArtifactType, ConcurrentHashMap<Object, Artifact>> getDeployedArtifacts() {
        return deployedArtifacts;
    }

    public Map<String, Artifact> getFaultyArtifacts() {
        return faultyArtifacts;
    }

    /**
     * Deploy the artifacts found in the artifacts to deploy list.
     *
     * @param artifactsToDeploy list of artifacts to deploy
     */
    public void deployArtifacts(List<Artifact> artifactsToDeploy) {
        artifactsToDeploy.forEach(artifactToDeploy -> {
            LifecycleEvent lifecycleEvent = new LifecycleEvent(artifactToDeploy, new Date(),
                    LifecycleEvent.STATE.BEFORE_START_EVENT);
            try {
                fireLifecycleEvent(lifecycleEvent);
                Deployer deployer = getDeployer(artifactToDeploy.getType());
                if (deployer != null) {
                    logger.debug("Deploying artifact {} using {} deployer", artifactToDeploy.getName(),
                            deployer.getClass().getName());
                    Object artifactKey = deployer.deploy(artifactToDeploy);
                    if (artifactKey != null) {
                        artifactToDeploy.setKey(artifactKey);
                        addToDeployedArtifacts(artifactToDeploy);
                    } else {
                        throw new CarbonDeploymentException("Deployed artifact key is null for : " +
                                artifactToDeploy.getName());
                    }
                } else {
                    throw new CarbonDeploymentException("Deployer instance cannot be found for the type : " +
                            artifactToDeploy.getType());
                }

            } catch (CarbonDeploymentException e) {
                logger.error("Error while deploying artifacts", e);
                addToFaultyArtifacts(artifactToDeploy);

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                lifecycleEvent.setTraceContent("Error while deploying artifact. \n" + sw.toString());
                lifecycleEvent.setDeploymentResult(LifecycleEvent.RESULT.FAILED);
            }

            //reuse the previously created object
            lifecycleEvent.setState(LifecycleEvent.STATE.AFTER_START_EVENT);
            fireLifecycleEvent(lifecycleEvent);
        });
    }

    private void fireLifecycleEvent(LifecycleEvent lifecycleEvent) {

        logger.debug("Triggering lifecycle event {} for artifact - {}", lifecycleEvent.getState(),
                lifecycleEvent.getArtifact().getName());
        lifecycleListeners.forEach(lifecycleListener -> lifecycleListener.lifecycleEvent(lifecycleEvent));
    }

    /**
     * Updates the artifacts found in the artifacts to be updated list.
     *
     * @param artifactsToUpdate list of artifacts to update
     */
    public void updateArtifacts(List<Artifact> artifactsToUpdate) {
        artifactsToUpdate.forEach(artifactToUpdate -> {
            LifecycleEvent lifecycleEvent = new LifecycleEvent(artifactToUpdate, new Date(),
                    LifecycleEvent.STATE.BEFORE_UPDATE_EVENT);
            try {
                fireLifecycleEvent(lifecycleEvent);
                Deployer deployer = getDeployer(artifactToUpdate.getType());
                if (deployer != null) {
                    logger.debug("Updating artifact {} using {} deployer", artifactToUpdate.getName(),
                            deployer.getClass().getName());
                    Object artifactKey = deployer.update(artifactToUpdate);
                    if (artifactKey != null) {
                        artifactToUpdate.setKey(artifactKey);
                        addToDeployedArtifacts(artifactToUpdate);
                    } else {
                        throw new CarbonDeploymentException("Deployed artifact key is null for : " +
                                artifactToUpdate.getName());
                    }
                } else {
                    throw new CarbonDeploymentException("Deployer instance cannot be found for the type : " +
                            artifactToUpdate.getType());
                }
            } catch (CarbonDeploymentException e) {
                logger.error("Error while updating artifact", e);
                addToFaultyArtifacts(artifactToUpdate);

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                lifecycleEvent.setTraceContent("Error while updating artifact. \n" + sw.toString());
                lifecycleEvent.setDeploymentResult(LifecycleEvent.RESULT.FAILED);
            }

            //reuse the previously created object
            lifecycleEvent.setState(LifecycleEvent.STATE.AFTER_UPDATE_EVENT);
            fireLifecycleEvent(lifecycleEvent);
        });
    }

    private void addToDeployedArtifacts(Artifact artifact) {
        ConcurrentHashMap<Object, Artifact> artifactMap = deployedArtifacts.
                get(artifact.getType());
        if (artifactMap == null) {
            artifactMap = new ConcurrentHashMap<>();
        }
        artifactMap.put(artifact.getKey(), artifact);
        deployedArtifacts.put(artifact.getType(), artifactMap);
        faultyArtifacts.remove(artifact.getPath());
    }

    private void addToFaultyArtifacts(Artifact artifact) {
        faultyArtifacts.put(artifact.getPath(), artifact);
        //removeFromDeployedArtifacts if it became faulty while undeploying
        removeFromDeployedArtifacts(artifact);
    }

    /**
     * Undeploy the artifacts found in the artifact to be undeployed list.
     *
     * @param artifactsToUndeploy list of artifacts to undeploy
     */
    public void undeployArtifacts(List<Artifact> artifactsToUndeploy) {
        artifactsToUndeploy.forEach(artifactToUnDeploy -> {
            LifecycleEvent lifecycleEvent = new LifecycleEvent(artifactToUnDeploy, new Date(),
                    LifecycleEvent.STATE.BEFORE_STOP_EVENT);
            try {
                fireLifecycleEvent(lifecycleEvent);
                Deployer deployer = getDeployer(artifactToUnDeploy.getType());
                if (deployer != null) {
                    logger.debug("Undeploying artifact {} using {} deployer", artifactToUnDeploy.getName(),
                            deployer.getClass().getName());
                    deployer.undeploy(artifactToUnDeploy.getKey());
                    removeFromDeployedArtifacts(artifactToUnDeploy);
                } else {
                    throw new CarbonDeploymentException("Deployer instance cannot be found for the type : " +
                            artifactToUnDeploy.getType());
                }
            } catch (CarbonDeploymentException e) {
                logger.error("Error while undeploying artifacts", e);
                addToFaultyArtifacts(artifactToUnDeploy);

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                lifecycleEvent.setTraceContent("Error while undeploying artifact. \n" + sw.toString());
                lifecycleEvent.setDeploymentResult(LifecycleEvent.RESULT.FAILED);
            }

            //reuse the previously created object
            lifecycleEvent.setState(LifecycleEvent.STATE.AFTER_STOP_EVENT);
            fireLifecycleEvent(lifecycleEvent);
        });
    }

    private void removeFromDeployedArtifacts(Artifact artifact) {
        Map<Object, Artifact> artifactMap = deployedArtifacts.get(artifact.getType());
        if (artifactMap != null && artifactMap.containsKey(artifact.getKey())) {
            artifactMap.remove(artifact.getKey());
            if (artifactMap.isEmpty()) {
                deployedArtifacts.remove(artifact.getType());
            }
        }
    }
}
