package org.wso2.carbon.webapp.mgt.metadata;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.webapp.mgt.WebappsConstants;

import javax.servlet.ServletContext;
import java.util.Map;

/**
 * Created by sagara on 7/29/14.
 */
public class MetadataUtil {

    public static Object getAttributevalue(String attributeName, ServletContext context) {

        Map<String, Object> metadata = (Map<String, Object>) context.getAttribute(WebappsConstants.APPLICATION_META_DATA);
        Object obj = metadata.get(attributeName);
        return obj;
    }


}
