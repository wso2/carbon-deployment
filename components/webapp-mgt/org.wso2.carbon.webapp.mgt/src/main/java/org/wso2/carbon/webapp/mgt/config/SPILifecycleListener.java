package org.wso2.carbon.webapp.mgt.config;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.ContextConfig;


/**
 * Created by sagara on 8/19/14.
 */
public class SPILifecycleListener implements LifecycleListener {

    @Override
    public void lifecycleEvent(LifecycleEvent event) {

        if(event.getType() == Lifecycle.CONFIGURE_START_EVENT && event.getSource() instanceof StandardContext){

            Context context = (Context) event.getSource();
            LifecycleListener defaultContextConfig = null;
            for(Object obj : context.getApplicationLifecycleListeners()){
                  if( obj instanceof ContextConfig){
                      defaultContextConfig = (LifecycleListener)obj;
                      break;
                  }
            }

            context.removeLifecycleListener(defaultContextConfig);
            CarbonContextConfig carbonContextConfig = new CarbonContextConfig();
            context.addLifecycleListener(carbonContextConfig);

        }

    }
}
