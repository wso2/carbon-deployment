package org.wso2.carbon.webapp.mgt.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.webapp.mgt.WebappsConstants;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Map;
import java.util.Set;

/**
 * Created by sagara on 7/29/14.
 */
public class ApplicationDescriptorContainerInitializer implements ServletContainerInitializer {

    //private static final Log log = LogFactory.getLog(ApplicationDescriptorContainerInitializer.class);

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        System.out.println("===================================" + ctx.getClass());
       // log.debug("onStartup started");
        MetadataReader metadataReader = MetadataReader.getInstance();
        Map<String, Object> metadata = metadataReader.readApplicationDescriptor(ctx);
        System.out.println(metadata);
        ctx.setAttribute(WebappsConstants.APPLICATION_META_DATA, metadata);

        System.out.println(MetadataUtil.getAttributevalue("feature.index.sub-feature.sub-sub-feature.as.version", ctx));
        System.out.println(MetadataUtil.getAttributevalue("feature.index.sub-feature.sub-sub-feature.version", ctx));


    }


}
