package org.wso2.carbon.webapp.mgt;

import org.wso2.carbon.webapp.mgt.utils.WebAppUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nipuni
 * Date: 6/30/14
 * Time: 11:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class VhostHolder {

    private List<String> vhosts;

    public List<String> getVhosts(){
        return WebAppUtils.getVhostNames();
    }


}
