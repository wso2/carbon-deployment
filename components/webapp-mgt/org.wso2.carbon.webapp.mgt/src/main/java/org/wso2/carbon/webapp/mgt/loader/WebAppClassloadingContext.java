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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contains information about the class loading behaviour of a webapp.
 */
public class WebAppClassloadingContext {

    private boolean parentFirst = false;

    private String[] delegatedPackages;
    private String[] delegatedPackageStems;
    private String[] excludeedPackages;
    private String[] excludeedPackageStems;
    private String[] delegatedResources;
    private String[] delegatedResourceStems;
    private String[] excludeedResources;
    private String[] excludeedResourceStems;
    private boolean noExcludedPackages = true;
    private boolean noExcludedResources = true;


    private String[] repositories;

    private boolean delegateAllPackages = false;
    private boolean delegateAllResources = false;

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
        List<String> delegatedPackageList = new ArrayList<>();
        List<String> delegatedPackageStemList = new ArrayList<>();
        List<String> excludedPackageList = new ArrayList<>();
        List<String> excludedPackageStemList = new ArrayList<>();

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

        if (excludedPackageList.size() > 0 || excludedPackageStemList.size() > 0) {
            noExcludedPackages = false;
        }

        if (!noExcludedPackages) {
            excludeedPackages = excludedPackageList.toArray(new String[excludedPackageList.size()]);
            excludeedPackageStems = excludedPackageStemList.toArray(new String[excludedPackageStemList.size()]);

        }

        if (!delegateAllPackages) {
            delegatedPackages = delegatedPackageList.toArray(new String[delegatedPackageList.size()]);
            delegatedPackageStems = delegatedPackageStemList.toArray(new String[delegatedPackageStemList.size()]);
        }
    }


    public String[] getProvidedRepositories() {
        if(repositories!=null){
            int length = repositories.length;
            String[] repos = new String[length];
            System.arraycopy(repositories,0,repos,0,length);
            return repos;
        }
        return null;
        //return repositories;
    }

    public void setProvidedRepositories(String[] repositories) {
        if(repositories!=null){
            int length = repositories.length;
            this.repositories = new String[length];
            System.arraycopy(repositories,0,this.repositories,0,length);
        }
        //this.repositories = repositories;
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

    public boolean isExcludedResources(String name) {
        if (noExcludedResources) {
            return false;
        }

        if (name == null)
            return false;

        for (String excludedRes : excludeedResourceStems) {
            if (name.startsWith(excludedRes)) {
                return true;
            }
        }

        for (String excludedRes : excludeedResources) {
            if (name.equals(excludedRes)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDelegatedResource(String name) {
        if (delegateAllResources) {
            return true;
        }

        if (name == null)
            return false;

        for (String delegatedRes : delegatedResourceStems) {
            if (name.startsWith(delegatedRes)) {
                return true;
            }
        }

        for (String delegatedRes : delegatedResources) {
            if (name.equals(delegatedRes)) {
                return true;
            }
        }
        return false;
    }

    public void setDelegatedResources(String[] delegatedResourceList) {
        List<String> delegatedResList = new ArrayList<>();
        List<String> delegatedResStemList = new ArrayList<>();
        Collection<String> excludedResList = new ArrayList<>();
        Collection<String> excludedResStemList = new ArrayList<>();

        for (String resourceName : delegatedResourceList) {
            // Detect excluded package or delegated package.
            if (resourceName.startsWith("!")) {
                // Remove the "!" part.
                resourceName = resourceName.substring(1);
                if (resourceName.endsWith("/*")) {
                    excludedResStemList.add(resourceName.substring(0, resourceName.length() - 2));
                } else {
                    excludedResList.add(resourceName);
                }

            } else {

                if (resourceName.equals("*")) {
                    delegateAllResources = true;
                } else if (resourceName.endsWith("/*")) {
                    delegatedResStemList.add(resourceName.substring(0, resourceName.length() - 2));
                } else {
                    delegatedResList.add(resourceName);
                }

            }
        }

        if (excludedResList.size() > 0 || excludedResStemList.size() > 0) {
            noExcludedResources = false;
        }

        if (!noExcludedResources) {
            excludeedResources = excludedResList.toArray(new String[excludedResList.size()]);
            excludeedResourceStems = excludedResStemList.toArray(new String[excludedResStemList.size()]);

        }

        if (!delegateAllResources) {
            delegatedResources = delegatedResList.toArray(new String[delegatedResList.size()]);
            delegatedResourceStems = delegatedResStemList.toArray(new String[delegatedResStemList.size()]);
        }

    }
}
