package org.wso2.carbon.webapp.mgt.utils;

import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardWrapper;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.webapp.mgt.DataHolder;
import org.wso2.carbon.webapp.mgt.WebApplication;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebAppUtils {

    /**
     * This util method is used to check if the given application is a Jax-RS/WS app
     *
     * @param webApplication application object
     * @return relevant servlet mapping of the cxf servlet if its a Jax-RS/WS application.
     *         Null, if its not a Jax-RS/WS application.
     */
    public static String checkJaxApplication(WebApplication webApplication) {
        for (Container container : webApplication.getContext().findChildren()) {
            if (((StandardWrapper) container).getServletClass().equals(
                    "org.apache.cxf.transport.servlet.CXFServlet"))
                return (((StandardWrapper) container).findMappings())[0];
            else if (((StandardWrapper) container).getServletName().toLowerCase().contains("cxf") || "JAXServlet".equals(((StandardWrapper) container).getServletName())) {
                return (((StandardWrapper) container).findMappings())[0];
            }
        }
        return null;
    }

    public static boolean validateWebappFileName(String filename){
        Pattern pattern = Pattern.compile(".*[\\]\\[!\"$%&'()*+,/:;<=>?@~{|}^`].*");
        Matcher matcher = pattern.matcher(filename);
        boolean isMatch = matcher.matches();
        return isMatch;
    }

    public static String getWebappsBaseDir(String webappFilePath){
        String baseDir = webappFilePath.substring(0,webappFilePath.lastIndexOf(File.separator));
        return baseDir;
    }
    public static String getMatchingHostname(String filePath){
        CarbonTomcatService carbonTomcatService = DataHolder.getCarbonTomcatService();
        Container[] childHosts = carbonTomcatService.getTomcat().getEngine().findChildren();
        for(Container host:childHosts){
            Host vhost = (Host)host;
            String appBase = vhost.getAppBase();
            if(appBase.endsWith(File.separator)){
                appBase = appBase.substring(0, appBase.lastIndexOf(File.separator));
            }
            if(appBase.equals(filePath)){
                return vhost.getName();
            }
        }
        return "";
    }

    public static String getWebappKey(File webappFile){
        String baseDir = getWebappsBaseDir(webappFile.getAbsolutePath());
        String hostname = getMatchingHostname(baseDir);
        return hostname+":"+webappFile.getName();
    }
}
