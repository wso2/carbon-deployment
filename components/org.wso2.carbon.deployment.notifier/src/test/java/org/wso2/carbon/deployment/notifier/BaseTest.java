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
package org.wso2.carbon.deployment.notifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * BaseTest class for deployment tests.
 *
 * @since 5.0.0
 */
public class BaseTest {

    /**
     * Basedir for all file I/O.
     */
    public static String basedir;
    private static final String OS_NAME_KEY = "os.name";
    private static final String WINDOWS_PARAM = "Windows";

    static {
        basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = new File(".").getAbsolutePath();
        }
    }

    protected String testDir = "src" + File.separator + "test" + File.separator;
    protected String testResourceDir = testDir + "resources";

    /**
     * @param testName
     */
    public BaseTest(String testName) {
        testDir = new File(basedir, testDir).getAbsolutePath();
        testResourceDir = new File(basedir, testResourceDir).getAbsolutePath();
    }

    public File getTestResourceFile(String relativePath) {
        return new File(testResourceDir, relativePath);
    }

    public InputStream getTestResource(String relativePath) {
        File testResource = getTestResourceFile(relativePath);
        try {
            return new FileInputStream(testResource);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("The '" + testResource.getAbsolutePath() +
                    "' file does not exist. Verify that the 'basedir' System property " +
                    "is pointing to the root of the project", e);
        }
    }

    /**
     * Get the path of a provided resource.
     *
     * @param resourcePaths path strings to the location of the resource
     * @return path of the resources
     */
    public Optional<Path> getResourcePath(String... resourcePaths) {
        URL resourceURL = BaseTest.class.getClassLoader().getResource("");
        if (resourceURL != null) {
            String resourcePath = resourceURL.getPath();
            if (resourcePath != null) {
                resourcePath = System.getProperty(OS_NAME_KEY).contains(WINDOWS_PARAM) ?
                        resourcePath.substring(1) : resourcePath;
                return Optional.ofNullable(Paths.get(resourcePath, resourcePaths));
            }
        }
        return Optional.empty(); // Resource do not exist
    }
}
