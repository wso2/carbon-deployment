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
package org.wso2.carbon.repomanager.axis2.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.repomanager.axis2.stub.Axis2RepoManagerJSONExceptionException;
import org.wso2.carbon.repomanager.axis2.stub.Axis2RepoManagerStub;
import org.wso2.carbon.repomanager.axis2.stub.types.Axis2ArtifactUploadData;
import org.wso2.carbon.repomanager.axis2.stub.types.DirectoryStructureMetaData;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.activation.DataHandler;
import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

public class Axis2RepoManagerClient {

    private Axis2RepoManagerStub axis2repoManagerStub;

    public Axis2RepoManagerClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        String RepoManagerServiceEPR = serverURL + "Axis2RepoManager";
        axis2repoManagerStub = new Axis2RepoManagerStub(ctx, RepoManagerServiceEPR);
        ServiceClient client = axis2repoManagerStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(10000);
    }

    public Axis2RepoManagerClient(String cookie, ServletConfig config, HttpSession session)
            throws AxisFault {
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext ctx = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String repoManagerServiceEPR = serverURL + "Axis2RepoManager";
        axis2repoManagerStub = new Axis2RepoManagerStub(ctx, repoManagerServiceEPR);
        ServiceClient client = axis2repoManagerStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(10000);
    }

    public void uploadArtifacts(Axis2ArtifactUploadData[] artifactUploadData, String fileUploadDir) throws java.lang.Exception {
        axis2repoManagerStub.uploadArtifact(artifactUploadData, fileUploadDir);
    }

    public DirectoryStructureMetaData getDirs()
            throws RemoteException, Axis2RepoManagerJSONExceptionException {
        return axis2repoManagerStub.getDirectoryStructure();
    }

    public boolean deleteLibs(String libPath) throws RemoteException {
        return axis2repoManagerStub.deleteLib(libPath);
    }

    public boolean restartAxis2Server() throws RemoteException {
        return axis2repoManagerStub.restartAxis2Server();
    }

    public void downloadArtifact(String filePath, HttpServletResponse response, String filename)
            throws
            IOException {
        ServletOutputStream out = response.getOutputStream();
        DataHandler dataHandler = axis2repoManagerStub.downloadArtifact(filePath);
        if (dataHandler != null) {
            response.setHeader("Content-Disposition", "fileName=" + filename);
            response.setContentType(dataHandler.getContentType());
            InputStream in = dataHandler.getDataSource().getInputStream();
            int nextChar;
            while ((nextChar = in.read()) != -1) {
                out.write((char) nextChar);
            }
            out.flush();
            out.close();
            in.close();
        } else {
            out.write("The requested dependency archive was not found on the server".getBytes());
        }

    }

}
