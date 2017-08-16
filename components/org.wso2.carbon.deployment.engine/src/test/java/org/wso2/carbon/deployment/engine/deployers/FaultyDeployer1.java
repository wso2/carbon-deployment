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
package org.wso2.carbon.deployment.engine.deployers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A Faulty Deployer class to test deployment.
 *
 * @since 5.0.0
 */
public class FaultyDeployer1 implements Deployer {
    private static final Logger logger = LoggerFactory.getLogger(FaultyDeployer1.class);
    /**
     * Has init() been called?.
     */
    public static boolean initCalled;
    /**
     * Set to true if "XML1" has been deployed.
     */
    public static boolean sample1Deployed;
    /**
     * Set to true if "XML1" has been updated.
     */
    public static boolean sample1Updated;

    private URL directoryLocation;
    private ArtifactType artifactType;

    public FaultyDeployer1() {
        artifactType = new ArtifactType<>("txt");
        try {
            directoryLocation = new URL("file:text-files");
        } catch (MalformedURLException e) {
            logger.error("Error while initializing directoryLocation", e);
        }
    }

    public void init() {
        logger.info("Initializing Deployer");
        initCalled = true;
    }

    public String deploy(Artifact artifact) throws CarbonDeploymentException {
        logger.info("Deploying : " + artifact.getName());
        sample1Deployed = false;
        throw new CarbonDeploymentException("Error while deploying : " + artifact.getName());
    }

    public void undeploy(Object key) throws CarbonDeploymentException {
        logger.info("Undeploying : " + key);
        throw new CarbonDeploymentException("Error while Un Deploying : " + key);
    }

    public String update(Artifact artifact) throws CarbonDeploymentException {
        logger.info("Updating : " + artifact.getName());
        sample1Updated = true;
        return artifact.getName();
    }


    public URL getLocation() {
        return directoryLocation;
    }

    public void setLocation(URL directoryLocation) {
        this.directoryLocation = directoryLocation;
    }

    public ArtifactType getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(ArtifactType artifactType) {
        this.artifactType = artifactType;
    }
}
