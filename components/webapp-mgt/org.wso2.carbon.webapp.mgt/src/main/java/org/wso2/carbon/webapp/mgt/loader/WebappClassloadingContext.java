/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.webapp.mgt.loader;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains information about the class loading behaviour of a webapp.
 */
public class WebappClassloadingContext {
    private static final Log log = LogFactory.getLog(WebappClassloadingContext.class);

    private boolean parentFirst = false;

    private String[] delegatedPackages;
    private String[] delegatedPackageStems;
    private String[] excludeedPackages;
    private String[] excludeedPackageStems;
    private boolean noExcludedPackages = true;

    private String[] repositories;

    private static ClassloadingConfiguration classloadingConfig;
    private boolean delegateAllPackages = false;

    static {
        try {
            classloadingConfig = ClassloadingContextBuilder.buildSystemConfig();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static ClassloadingConfiguration getClassloadingConfig() {
        return classloadingConfig;
    }

    public boolean isDelegatedPackage(String name) {
        if (delegateAllPackages) {
            return true;
        }

        if (name == null)
            return false;

        // Looking up the package
        String packageName;
        int pos = name.lastIndexOf('.');
        if (pos != -1) {
            packageName = name.substring(0, pos);
        } else {
            return false;
        }

        for (String delegatedPkg : delegatedPackageStems) {
            if (packageName.startsWith(delegatedPkg)) {
                return true;
            }
        }

        for (String delegatedPkg : delegatedPackages) {
            if (packageName.equals(delegatedPkg)) {
                return true;
            }
        }
        return false;
    }

    public void setDelegatedPackages(String[] delegatedPkgList) {
        List<String> delegatedPackageList = new ArrayList<String>();
        List<String> delegatedPackageStemList = new ArrayList<String>();
        List<String> excludedPackageList = new ArrayList<String>();
        List<String> excludedPackageStemList = new ArrayList<String>();

        for (String packageName : delegatedPkgList) {
            // Detect excluded package or delegated package.
            if (packageName.startsWith("!")) {
                // Remove the "!" part.
                packageName = packageName.substring(1);
                if (packageName.endsWith(".*")) {
                    excludedPackageStemList.add(packageName.substring(0, packageName.length() - 2));
                } else {
                    excludedPackageList.add(packageName);
                }

            } else {

                if (packageName.equals("*")) {
                    delegateAllPackages = true;
                } else if (packageName.endsWith(".*")) {
                    delegatedPackageStemList.add(packageName.substring(0, packageName.length() - 2));
                } else {
                    delegatedPackageList.add(packageName);
                }

            }
        }

        if(excludedPackageList.size() > 0 || excludedPackageStemList.size() > 0){
            noExcludedPackages = false;
        }

        if(!noExcludedPackages){
            excludeedPackages = excludedPackageList.toArray(new  String[excludedPackageList.size()]);
            excludeedPackageStems = excludedPackageStemList.toArray(new String[excludedPackageStemList.size()]);

        }

        if (!delegateAllPackages) {
            delegatedPackages = delegatedPackageList.toArray(new String[delegatedPackageList.size()]);
            delegatedPackageStems = delegatedPackageStemList.toArray(new String[delegatedPackageStemList.size()]);
        }
    }


    public String[] getProvidedRepositories() {
        return repositories;
    }

    public void setProvidedRepositories(String[] repositories) {
        this.repositories = repositories;
    }

    public boolean isParentFirst() {
        return parentFirst;
    }

    public void setParentFirst(boolean parentFirst) {
        this.parentFirst = parentFirst;
    }

    public boolean isExcludedPackage(String name) {
        if (noExcludedPackages) {
            return false;
        }

        if (name == null)
            return false;

        // Looking up the package
        String packageName;
        int pos = name.lastIndexOf('.');
        if (pos != -1) {
            packageName = name.substring(0, pos);
        } else {
            return false;
        }

        for (String excludedPkg : excludeedPackageStems) {
            if (packageName.startsWith(excludedPkg)) {
                return true;
            }
        }

        for (String excludedPkg : excludeedPackages) {
            if (packageName.equals(excludedPkg)) {
                return true;
            }
        }
        return false;
    }
}
