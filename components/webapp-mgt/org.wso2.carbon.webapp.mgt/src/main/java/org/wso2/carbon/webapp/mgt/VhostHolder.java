package org.wso2.carbon.webapp.mgt;

import org.wso2.carbon.webapp.mgt.utils.WebAppUtils;

import java.util.List;

public class VhostHolder {

    private List<String> vhosts;


    public List<String> getVhosts() {
        return WebAppUtils.getVhostNames();
    }
}
