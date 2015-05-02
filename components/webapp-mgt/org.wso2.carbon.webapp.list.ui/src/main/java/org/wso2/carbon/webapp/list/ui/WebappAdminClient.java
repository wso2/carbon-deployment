/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.webapp.list.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.webapp.mgt.stub.WebappAdminStub;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.SessionsWrapper;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.VhostHolder;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappMetadata;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappUploadData;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappsWrapper;

import javax.activation.DataHandler;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Client which communicates with the WebappAdmin service
 */
public class WebappAdminClient {
    public static final String BUNDLE = "org.wso2.carbon.webapp.list.ui.i18n.Resources";
    public static final int MILLISECONDS_PER_MINUTE = 60 * 1000;
    private static final Log log = LogFactory.getLog(WebappAdminClient.class);
    private ResourceBundle bundle;
    public WebappAdminStub stub;

    public WebappAdminClient(String cookie,
                             String backendServerURL,
                             ConfigurationContext configCtx,
                             Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "WebappAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new WebappAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        option.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
    }

    public WebappsWrapper getPagedWebappsSummary(String webappSearchString,
                                                 String webappState, String webappType,
                                                 int pageNumber) throws AxisFault {
        try {
            return stub.getPagedWebappsSummary(webappSearchString, webappState, webappType,
                    pageNumber);
        } catch (RemoteException e) {
            handleException("cannot.get.webapp.data", e);
        }
        return null;
    }

    public WebappMetadata getStartedWebapp(String webappFileName, String hostName) throws AxisFault {
        try {
            return stub.getStartedWebapp(webappFileName, hostName);
        } catch (RemoteException e) {
            handleException("cannot.get.started.webapp.data", e);
        }
        return null;
    }

    public WebappMetadata getStoppedWebapp(String webappFileName, String hostName) throws AxisFault {
        try {
            return stub.getStoppedWebapp(webappFileName, hostName);
        } catch (RemoteException e) {
            handleException("cannot.get.stopped.webapp.data", e);
        }
        return null;
    }

    public void deleteAllStartedWebapps() throws AxisFault {
        try {
            stub.deleteAllStartedWebapps();
        } catch (RemoteException e) {
            handleException("cannot.delete.webapps", e);
        }
    }

    public void deleteAllStoppedWebapps() throws AxisFault {
        try {
            stub.deleteAllStoppedWebapps();
        } catch (RemoteException e) {
            handleException("cannot.delete.webapps", e);
        }
    }

    public void deleteStartedWebapps(String[] webappkey) throws AxisFault {
        try {
            stub.deleteStartedWebapps(webappkey);
        } catch (RemoteException e) {
            handleException("cannot.delete.webapps", e);
        }
    }

    public void deleteStoppedWebapps(String[] webappFileNames) throws AxisFault {
        try {
            stub.deleteStoppedWebapps(webappFileNames);
        } catch (RemoteException e) {
            handleException("cannot.delete.webapps", e);
        }
    }

    public void deleteAllWebapps(String[] webappkey) throws AxisFault {
        try {
            stub.deleteAllWebApps(webappkey);
        } catch (RemoteException e) {
            handleException("cannot.delete.webapps", e);
        }
    }

    public WebappsWrapper getPagedFaultyWebappsSummary(String webappSearchString,
                                                       String webappType,
                                                       int pageNumber) throws AxisFault {
        try {
            return stub.getPagedFaultyWebappsSummary(webappSearchString, webappType, pageNumber);
        } catch (RemoteException e) {
            handleException("cannot.get.webapp.data", e);
        }
        return null;
    }

    public void deleteFaultyWebapps(String[] webappkey) throws AxisFault {
        try {
            stub.deleteFaultyWebapps(webappkey);
        } catch (RemoteException e) {
            handleException("cannot.delete.all.faulty.webapps", e);
        }
    }

    public void deleteAllFaultyWebapps() throws AxisFault {
        try {
            stub.deleteAllFaultyWebapps();
        } catch (RemoteException e) {
            handleException("cannot.delete.all.faulty.webapps", e);
        }
    }

    public void reloadAllWebapps() throws AxisFault {
        try {
            stub.reloadAllWebapps();
        } catch (RemoteException e) {
            handleException("cannot.reload.webapps", e);
        }
    }

    public void reloadWebapps(String[] webappFileNames) throws AxisFault {
        try {
            stub.reloadWebapps(webappFileNames);
        } catch (RemoteException e) {
            handleException("cannot.reload.webapps", e);
        }
    }

    public void stopAllWebapps() throws AxisFault {
        try {
            stub.stopAllWebapps();
        } catch (RemoteException e) {
            handleException("cannot.stop.webapps", e);
        }
    }

    public void stopWebapps(String[] webappFileNames) throws AxisFault {
        try {
            stub.stopWebapps(webappFileNames);
        } catch (RemoteException e) {
            handleException("cannot.stop.webapps", e);
        }
    }

    public void startAllWebapps() throws AxisFault {
        try {
            stub.startAllWebapps();
        } catch (RemoteException e) {
            handleException("cannot.start.webapps", e);
        }
    }

    public void startWebapps(String[] webappFileNames) throws AxisFault {
        try {
            stub.startWebapps(webappFileNames);
        } catch (RemoteException e) {
            handleException("cannot.start.webapps", e);
        }
    }

    public SessionsWrapper getActiveSessionsInWebapp(String webappFileName,
                                                     int pageNumber, String hostName) throws AxisFault {
        try {
            return stub.getActiveSessions(webappFileName, pageNumber, hostName);
        } catch (RemoteException e) {
            handleException("cannot.get.active.sessions", e);
        }
        return null;
    }

    public void expireSessionsInWebapps(String[] webappKeySet) throws AxisFault {
        try {
            stub.expireSessionsInWebapps(webappKeySet);
        } catch (RemoteException e) {
            handleException("cannot.expire.all.sessions.in.webapps", e);
        }
    }

    public void expireSessionsInWebapp(String webappFileName,
                                       float maxSessionLifetimeMinutes) throws AxisFault {
        try {
            // We have to send session life time in milliseconds to the BE
            long maxSessionLifetimeMillis = (long) (maxSessionLifetimeMinutes *
                    MILLISECONDS_PER_MINUTE);
            stub.expireSessionsInWebapp(webappFileName, maxSessionLifetimeMillis);
        } catch (RemoteException e) {
            handleException("cannot.expire.all.sessions.in.webapps", e);
        }
    }

    public void expireSessionsInWebapp(String webappFileName,
                                       String[] sessionIDs, String hostName) throws AxisFault {
        try {
            stub.expireSessions(webappFileName, sessionIDs, hostName);
        } catch (RemoteException e) {
            handleException("cannot.expire.all.sessions.in.webapps", e);
        }
    }

    public void expireSessionsInAllWebapps() throws AxisFault {
        try {
            stub.expireSessionsInAllWebapps();
        } catch (RemoteException e) {
            handleException("cannot.expire.all.sessions.in.webapps", e);
        }
    }

    public void expireAllSessionsInWebapp(String webappFileName) throws AxisFault {
        try {
            stub.expireAllSessions(webappFileName);
        } catch (RemoteException e) {
            handleException("cannot.expire.all.sessions.in.webapps", e);
        }
    }

    public void uploadWebapp(WebappUploadData[] webappUploadDataList) throws AxisFault {
        try {
            stub.uploadWebapp(webappUploadDataList);
        } catch (RemoteException e) {
            handleException("cannot.upload.webapps", e);
        }
    }

    public String findWebappState(String webappFileName, String hostName) throws AxisFault {
        try {
            if (getStartedWebapp(webappFileName, hostName) != null) {
                return "started";
            } else if (getStoppedWebapp(webappFileName, hostName) != null) {
                return "stopped";
            }
        } catch (RemoteException e) {
            handleException("cannot.get.webapp.state", e);
        }
        return null;
    }

    private void handleException(String msgKey, Exception e) throws AxisFault {
        String msg = bundle.getString(msgKey);
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public void downloadWarFileHandler(String fileName, String hostName, String webappType,
                                       HttpServletResponse response) throws AxisFault {
        try {
            ServletOutputStream out = response.getOutputStream();
            DataHandler handler = stub.downloadWarFileHandler(fileName, hostName, webappType);
            if (handler != null) {
                if ("jaggeryWebapp".equals(webappType)) {
                    if (!fileName.endsWith(".zip")) {
                        fileName = fileName.concat(".zip");
                    }
                } else if (!fileName.endsWith(".war")) {
                    fileName = fileName.concat(".war");
                }
                response.setHeader("Content-Disposition", "fileName=" + fileName);
                response.setContentType(handler.getContentType());
                InputStream in = handler.getDataSource().getInputStream();
                int nextChar;
                while ((nextChar = in.read()) != -1) {
                    out.write((char) nextChar);
                }
                out.flush();
                in.close();
            } else {
                out.write("The requested webapp was not found on the server".getBytes());
            }
        } catch (RemoteException e) {
            handleException("error.downloading.war", e);
        } catch (IOException e) {
            handleException("error.downloading.war", e);
        }
    }

    public InputStream getWarFileInputStream(String fileName, String hostName, String webappType) throws AxisFault {
        InputStream inputStream = null;
        try {

            DataHandler handler = stub.downloadWarFileHandler(fileName, hostName, webappType);
            if (handler != null) {

                inputStream = handler.getDataSource().getInputStream();

            } else {

            }
        } catch (RemoteException e) {
            handleException("error.downloading.war", e);
        } catch (IOException e) {
            handleException("error.downloading.war", e);
        }
        return inputStream;
    }

    public void setBamConfig(String fileName, String value, String hostName) {
        if (value.contains("1")) {
            value = "true";
        } else {
            value = "false";
        }

        try {
            stub.setBamConfiguration(fileName, value, hostName);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot set bam configuration to the back end.");
            }
        }
    }

    public Boolean getBamConfig(String fileName, String hostName) {
        try {
            return Boolean.parseBoolean(stub.getBamConfiguration(fileName, hostName));
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot recieve bam configuration from the back end.");
            }
            return null;
        }
    }

    public void changeDefaultAppVersion(String appFileName, String appContext, String hostName) throws AxisFault {
        try {
            stub.changeDefaultAppVersion(appContext, appFileName, hostName);
        } catch (Exception e) {
            handleException("cannot.make.default.version", e);
        }
    }

    public VhostHolder getVhostHolder() throws RemoteException {
        return stub.getVhostHolder();
    }
}
