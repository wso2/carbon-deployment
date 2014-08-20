/*
* Copyright 2004,2013 The Apache Software Foundation.
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
package org.wso2.carbon.javaee.tomee;

import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomee.catalina.TomcatLoader;
import org.apache.tomee.catalina.TomcatWebAppBuilder;

import java.util.Properties;

public class ASTomcatLoader extends TomcatLoader {

    public void initialize(Properties properties) throws Exception {

        //Since we initialize TomcatWebAppBuilder early, we need to set the jdbc pool here.
        //TWAB does some heavy operations which require the jdbc pool.
        setJdbcPool();

        //Set our own TWAB. Why we need this? To set our own Tomcat context LifeCycleListener
        TomcatWebAppBuilder tomcatWebAppBuilder = (TomcatWebAppBuilder)
                SystemInstance.get().getComponent(WebAppBuilder.class);
        if (tomcatWebAppBuilder == null) {
            tomcatWebAppBuilder = new ASTomcatWebAppBuilder();
            tomcatWebAppBuilder.start();
            SystemInstance.get().setComponent(WebAppBuilder.class, tomcatWebAppBuilder);
        }
        super.initialize(properties);
    }

    /**
     * Snippet taken from TomcatWebAppBuilder#initialize method.
     */
    protected void setJdbcPool() {
        // set tomcat pool
        try {// in embedded mode we can easily remove it so check we can use it before setting it
            final Class<?> creatorClass = TomcatLoader.class.getClassLoader().loadClass("org.apache.tomee.jdbc.TomEEDataSourceCreator");
            SystemInstance.get().setProperty(ConfigurationFactory.OPENEJB_JDBC_DATASOURCE_CREATOR, creatorClass.getName());
        } catch (Throwable ignored) {
            // will use the defaul tone
        }



    }
}
