package org.wso2.carbon.webapp.mgt;

import javax.activation.DataHandler;

/**
 *  Represents a Webapp data set which is uploaded from the Management Console
 */
public class WebappUploadData {

    private String fileName;
    private String version;
    private DataHandler dataHandler;
    private String hostName;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
