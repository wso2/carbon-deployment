/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.deployment.engine.osgi.utils;

import org.ops4j.pax.exam.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.repositories;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * This class contains Utility methods to configure PAX-EXAM container.
 *
 * @since 5.0.0
 */
public class OSGiTestUtils {

    private static final Logger logger = LoggerFactory.getLogger(OSGiTestUtils.class);

    /**
     * Returns an array of default PAX-EXAM options.
     *
     * @return array of Options
     */
    public static Option[] getDefaultPaxOptions() {
        return options(
                repositories("http://maven.wso2.org/nexus/content/groups/wso2-public"),
                systemProperty("carbon.home").value(System.getProperty("carbon.home")),
                systemProperty(Constants.START_TIME).value(System.getProperty(Constants.START_TIME))
        );
    }

    /**
     * Returns a merged array of user specified options and default options.
     *
     * @param options custom options.
     * @return a merged array.
     */
    public static Option[] getDefaultPaxOptions(Option[] options) {
        return Stream.concat(Arrays.stream(getDefaultPaxOptions()), Arrays.stream(options))
                .toArray(Option[]::new);
    }

    /**
     * Set the environment prior to tests.
     */
    public static void setEnv() {
        setStartupTime();
        copyCarbonYAML();
        copyLog4jXMLFile();
        copyLaunchPropertiesFile();
        copyDeploymentYmlFile();
        copyDeploymentFile();
    }

    /**
     * Set the startup time to calculate the server startup time.
     */
    private static void setStartupTime() {
        if (System.getProperty(Constants.START_TIME) == null) {
            System.setProperty(Constants.START_TIME, System.currentTimeMillis() + "");
        }
    }

    /**
     * Replace the existing carbon.yml file with populated carbon.yml file.
     */
    private static void copyCarbonYAML() {
        copy(Paths.get("src", "test", "resources", "conf", "carbon.yml"),
                Paths.get(System.getProperty("carbon.home"),
                        "conf", "carbon.yml"));
    }

    /**
     * Replace the existing log4j2.xml file with populated log4j2.xml file.
     */
    private static void copyLog4jXMLFile() {
        copy(Paths.get("src", "test", "resources", "conf", "log4j2.xml"),
                Paths.get("conf", "log4j2.xml"));
    }

    /**
     * Replace the existing launch.properties file with populated launch.properties file.
     */
    private static void copyLaunchPropertiesFile() {
        copy(Paths.get("src", "test", "resources", "conf", "osgi", "launch.properties"),
                Paths.get("conf", "osgi", "launch.properties"));
    }

    /**
     * Copy deployment.yaml file
     */
    private static void copyDeploymentYmlFile() {
        copy(Paths.get("src", "test", "resources", "conf", "deployment.yaml"),
                Paths.get("conf", "deployment.yaml"));
    }

    /**
     * Replace the existing "README.txt file with populated "README.txt file.
     */
    private static void copyDeploymentFile() {
        copy(Paths.get("src", "test", "resources", "deployment", "README.txt"),
                Paths.get("deployment", "README.txt"));
    }

    /**
     * Copy files.
     *
     * @param src  Path for source
     * @param dest Path for destination
     */
    public static void copy(Path src, Path dest) {
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }

        src = Paths.get(basedir).resolve(src);
        dest = Paths.get(System.getProperty("carbon.home")).resolve(dest);

        createOutputFolderStructure(dest.toString());
        try {
            try (FileInputStream inputStr = new FileInputStream(src.toAbsolutePath().toString());
                 FileOutputStream outputStr = new FileOutputStream(dest.toAbsolutePath().toString())) {
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStr.read(buf)) > 0) {
                    outputStr.write(buf, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            logger.error("error occurred while copying the file", e);
        }
    }


    /**
     * Create the directory structure.
     *
     * @param destFileLocation
     */
    private static void createOutputFolderStructure(String destFileLocation) {
        File destFile = new File(destFileLocation);
        File parentFolder = destFile.getParentFile();
        parentFolder.mkdirs();
    }
}
