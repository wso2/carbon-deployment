package org.wso2.carbon.webapp.mgt;


import java.io.Serializable;

public class WebapplicationHelper implements Serializable {

    private String hostName;
    private String webappName;

    public WebapplicationHelper(String hostName, String webappName){
        this.hostName = hostName;
        this.webappName = webappName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getWebappName() {
        return webappName;
    }

    public void setWebappName(String webappName) {
        this.webappName = webappName;
    }
}
