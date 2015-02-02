package org.wso2.carbon.javaee.tomee.openjpa;


import org.apache.openejb.log.LoggerCreator;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.LogFactoryAdapter;

public class JULOpenJPALogFactory extends LogFactoryAdapter {
    @Override
    protected Log newLogAdapter(final String channel) {
        return new ASJULOpenJPALog(new LoggerCreator(channel));
    }
}
