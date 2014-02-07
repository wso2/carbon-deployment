/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.repomanager.axis2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.server.admin.service.ServerAdmin;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Axis2 repository manager Admin service
 */
@SuppressWarnings("unused")
public class Axis2RepoManager extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(Axis2RepoManager.class);
    private static final String RELOAD_REQUIRED = "reload.required";
    private static final String DIRECTORIES_LABEL = "dirs";
    private static final String FILES_LIST_LABEL = "filelist";
    private static final String DIRECTORY_NAME_LABEL = "dirname";
    private static final String FILE_NAME_LABEL = "filename";
    private static final String LIB_DIR = "lib";

    public DirectoryStructureMetaData getDirectoryStructure() throws JSONException {
        String repo = getAxisConfig().getRepository().getPath();

        File folder = new File(repo);
        JSONObject obj = new JSONObject();
        traverseParent(obj, folder, folder.getName());
        obj.put(DIRECTORY_NAME_LABEL, folder.getName());

        Boolean reloadRequiredProp = (Boolean) getConfigContext().getProperty(RELOAD_REQUIRED);
        boolean reloadRequired = (reloadRequiredProp != null) ? reloadRequiredProp : false;
        return new DirectoryStructureMetaData(obj.toString(), reloadRequired);
    }

    /**
     * Function that generates the folder structure
     * Note that this method only returns common artifacts that are located in the "lib" directory
     *
     * @param obj    JSONObject object that represents the folder to be traversed recursively
     * @param folder Actual java.io.File object representing the folder to be traversed
     * @param parent The Axis2 Repository directory path
     * @throws JSONException Thrown if the key if null while adding values to JSONObject using put method
     *
     The repository is traversed assuming that the directory structure will be as follows;
        |-server => [repository_parent]
        |-axis2services => [services_directory]
        |   |-lib => [lib directory]
        |   |   |-dependency1.jar
        |   |   |-dependency2.jar
        |   |-service1.aar
        |   |-service2.aar
        |-axis2modules
        |   |-lib
        |   |   |-dependency1.jar
        |   |   |-dependency2.jar
        |   |-module1.mar
        |   |-module2.mar
        |-carbonapps
        |   |-xxx
        |   |   |-xxx
        |   |   |-zzz
        |   |-xxx
        |   |-zzz
        |-xxxxxx
     */
    private void traverseParent(JSONObject obj, File folder, String parent) throws JSONException {
        JSONArray dirs = new JSONArray();
        JSONArray files = new JSONArray();

        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() && file.getName().equals(LIB_DIR);
            }
        };

        // Check if parent directory has a lib directory, add it explicitly if it doesn't.
        // NOTE: we cannot use the traverseServiceDir() method to travers the parent's lib directory
        // because it will add a lib within the parent's lib, assuming that this it is a service directory.
        if (folder.listFiles(fileFilter).length == 0) {
            JSONObject tempLib = new JSONObject();
            tempLib.put(DIRECTORIES_LABEL, new JSONArray());
            tempLib.put(FILES_LIST_LABEL, new JSONArray());
            tempLib.put(DIRECTORY_NAME_LABEL, LIB_DIR);
            dirs.put(tempLib);
        } else {
            JSONObject tempLibDir = new JSONObject();
            JSONArray libFilesArr = new JSONArray();
            File[] contents = folder.listFiles(fileFilter)[0].listFiles();
            for (File file : contents) {
                libFilesArr.put(new JSONObject().put(FILE_NAME_LABEL, file.getName()));
            }
            tempLibDir.put(DIRECTORIES_LABEL, new JSONArray());
            tempLibDir.put(FILES_LIST_LABEL, libFilesArr);
            tempLibDir.put(DIRECTORY_NAME_LABEL, LIB_DIR);
            dirs.put(tempLibDir);
        }


        File[] folderItems = folder.listFiles();
        for (File file : folderItems) {
            if (file.isDirectory() && !(file.getName().equalsIgnoreCase(LIB_DIR)) && !isMetaDataFile(file)) {
                dirs.put(new JSONObject().put(DIRECTORY_NAME_LABEL, file.getName()));
                traverseServiceDir(dirs.getJSONObject(dirs.length() - 1), file);
            } else if (file.isFile() && !isMetaDataFile(file)) {
                files.put(new JSONObject().put(FILE_NAME_LABEL, file.getName()));
            }
        }

        obj.put(DIRECTORIES_LABEL, dirs);
        obj.put(FILES_LIST_LABEL, files);

    }

    /**
     * Traverse the chile directory
     * @param obj JSONObject object that represents the chile directory
     * @param folder The java.io.File object representing the child directory to be traversed
     * @throws JSONException Thrown if an error occurs when manipulating the JSON array
     */
    private void traverseServiceDir(JSONObject obj, File folder) throws JSONException {
       	
        JSONArray dirs = new JSONArray();

        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() && file.getName().equals(LIB_DIR);
            }
        };

        if (folder.listFiles(fileFilter).length == 0) {
            JSONObject tempLib = new JSONObject();
            tempLib.put(DIRECTORIES_LABEL, new JSONArray());
            tempLib.put(FILES_LIST_LABEL, new JSONArray());
            tempLib.put(DIRECTORY_NAME_LABEL, LIB_DIR);
            dirs.put(tempLib);
        } else {
            JSONObject tempLibDir = new JSONObject();
            JSONArray libFilesArr = new JSONArray();
            File[] contents = folder.listFiles(fileFilter)[0].listFiles();
            for (File file : contents) {
                libFilesArr.put(new JSONObject().put(FILE_NAME_LABEL, file.getName()));
            }
            tempLibDir.put(DIRECTORIES_LABEL, new JSONArray());
            tempLibDir.put(FILES_LIST_LABEL, libFilesArr);
            tempLibDir.put(DIRECTORY_NAME_LABEL, LIB_DIR);
            dirs.put(tempLibDir);
        }

        obj.put(DIRECTORIES_LABEL, dirs);
        obj.put(FILES_LIST_LABEL, new JSONArray());

    }

    /**
     * @param artifactUploadDataList List containing the Axis2 archive files that are to be uploaded
     * @param fileUploadDir          The name of the directory to upload the atrifacts
     * @return The status of the artifact upload process
     * @throws java.io.IOException Thrown if an error occurs while uploading the files
     */
    public boolean uploadArtifact(Axis2ArtifactUploadData[] artifactUploadDataList,
                                  String fileUploadDir) throws IOException {
        final String dir = fileUploadDir;
        String fileName = null;
        File destDir;

        try {
            File repoFile = new File(getAxisConfig().getRepository().getPath());

            if (CarbonUtils.isURL(repoFile.getAbsolutePath())) {
                throw new AxisFault("Uploading services to URL repo is not supported.");
            }

            if (repoFile.getName().equals(fileUploadDir)) {
                destDir = new File(repoFile.getAbsolutePath() + File.separator + LIB_DIR);
            } else {
                FileFilter filter = new FileFilter() {
                    public boolean accept(File file) {
                        return file.isDirectory() && file.getName().equals(dir);
                    }
                };
                File[] temp = repoFile.listFiles(filter);
                if (temp.length == 0) {
                    throw new AxisFault("The directory you selected is not valid.");
                } else {
                    destDir = new File(temp[0].getAbsolutePath() + File.separator + LIB_DIR);
                }
            }

            if (!destDir.exists()) {
                if (!destDir.mkdirs()) {
                    log.error("Could not create lib JAR upload directory " +
                              destDir.getAbsolutePath());
                    throw new AxisFault("Could not upload artifacts to " + fileUploadDir);
                }
            }

            for (Axis2ArtifactUploadData uploadData : artifactUploadDataList) {
                fileName = uploadData.getFileName();
                writeToFileSystem(destDir.getAbsolutePath(), fileName, uploadData.getDataHandler());
            }
            getConfigContext().setProperty(RELOAD_REQUIRED, Boolean.TRUE);
        } catch (IOException e) {
            String msg = "Error occurred while uploading the service " + fileName;
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }

        return true;
    }

    public boolean deleteLib(String libPath) throws AxisFault {
        boolean deletionStatus;
        File libPathFile = new File(getAxisConfig().getRepository().getPath() + libPath);

        if (!libPathFile.exists()) {
            log.error("The file " + libPathFile.getAbsolutePath() + " does not exist");
            throw new AxisFault("The file " + libPathFile.getName() + " does not exist");
        } else {
            if (!libPathFile.canWrite()) {
                log.error("The file " + libPathFile.getAbsolutePath() + " is write protected");
                throw new AxisFault("The file " + libPathFile.getName() + " is write protected");
            } else {
                deletionStatus = libPathFile.delete();
                getConfigContext().setProperty(RELOAD_REQUIRED, Boolean.TRUE);            
            }
        }

        return deletionStatus;
    }

    /**
     * This method is used to restart the Axis2 server after an artifact has been uploaded.
     * This method is invoked via the management console.
     *
     * @return The status of the Axis2 server restart success/failure
     * @throws org.apache.axis2.AxisFault If restarting the server in case of a super tenant
     * reload request fails
     */
    public boolean restartAxis2Server() throws AxisFault {
        if (CarbonContext.getCurrentContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
            // If a super tenant initiates a reload repo, it is equivalent to a restart server request
            try {
                new ServerAdmin().restart();
            } catch (Exception e) {
                String msg = "Could not reload super tenant Axis2 repo. Restart failed.";
                log.error(msg, e);
                throw new AxisFault(msg, e);
            }
        } else {
            reloadTenantAxis2Repo();
        }
        return true;
    }

    private void reloadTenantAxis2Repo() {
        final ConfigurationContext tenantConfigContext = getConfigContext();
        Runnable runnable = new Runnable() {
            public void run() {
                TenantAxisUtils.terminateTenantConfigContext(tenantConfigContext);
            }
        };
        tenantConfigContext.setProperty(RELOAD_REQUIRED, Boolean.FALSE);
        Thread reloadThread = new Thread(runnable);
        try {
            reloadThread.join();
        } catch (InterruptedException e) {
            log.warn("Axis2 repo reload thread was interrupted", e);
        }
        reloadThread.start();
    }

    private void writeToFileSystem(String path, String fileName, DataHandler dataHandler)
            throws IOException {
        File destFile = new File(path, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destFile);
            dataHandler.writeTo(fos);
            fos.flush();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("Failed to close FileOutputStream for file " + fileName, e);
                }
            }
        }
    }

    /**
     * Downloads the relevant artifacts from the repository
     * @param filePath path to the artifacts that should be downloaded, relative to the tenants repository
     * @return the datahandler corresponding to the artifact that should be downloaded 
     */
    public DataHandler downloadArtifact(String filePath) {
        String resourcePath = getAxisConfig().getRepository().getPath() + filePath;
        FileDataSource datasource = new FileDataSource(new File(resourcePath));
        DataHandler handler = new DataHandler(datasource);

        return handler;
    }
    
	/**
	 * Make sure current directory or file is not a meta-data file of a
	 * application such as SVN, IDES etc. This will return true if the file or
	 * directory start with "." e.g - .svn, .git, .project
	 * 
	 * @param file
	 * @return
	 */
	private boolean isMetaDataFile(File file) {
		if (file.getName().startsWith(".")) {
			return true;
		}
		return false;

	}

}
