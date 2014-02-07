/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.springservices;

import org.apache.axis2.AxisFault;
import org.apache.axis2.ServiceObjectSupplier;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.i18n.Messages;
import org.springframework.context.ApplicationContext;

public class GenericApplicationContextSupplier implements ServiceObjectSupplier {
    public static final String SERVICE_SPRING_BEANNAME = "SpringBeanName";
    public static final String APPLICATION_CONTEXT_LOCATION = "SpringContextLocation";


    public Object getServiceObject(AxisService axisService) throws AxisFault {
        try {
            // Get the Spring Context based on file location
            ApplicationContext aCtx;
            Parameter springContextLocation = axisService.
                    getParameter(APPLICATION_CONTEXT_LOCATION);
            if (springContextLocation != null) {
                String springContextLocationValue =
                        ((String) springContextLocation.getValue()).trim();
                if (springContextLocationValue != null) {
                    // ApplicationContextHolder implements Spring interface ApplicationContextAware
                    aCtx = GenericApplicationContextUtil
                            .getSpringApplicationContext(axisService, springContextLocationValue);
                } else {
                    throw new AxisFault(
                            Messages.getMessage("paramIsNotSpecified",
                                                "springContextLocationValue"));
                }


            } else {
                throw new AxisFault(
                        Messages.getMessage("paramIsNotSpecified", "APPLICATION_CONTEXT_LOCATION"));
            }

            // Name of spring aware bean to be injected, taken from services.xml
            // via 'SERVICE_SPRING_BEANNAME ' . The Bean and its properties are pre-configured
            // as normally done in a spring type of way and subsequently loaded by Spring.
            // Axis2 just assumes that the bean is configured and is in the classloader.
            Parameter implBeanParam = axisService.getParameter(SERVICE_SPRING_BEANNAME);
            String beanName = ((String) implBeanParam.getValue()).trim();
            if (beanName != null) {
                if (aCtx == null) {
                    throw new Exception("Axis2 Can't find Spring's ApplicationContext");
                } else if (aCtx.getBean(beanName) == null) {
                    throw new Exception("Axis2 Can't find Spring Bean: " + beanName);
                }
                return aCtx.getBean(beanName);
            } else {
                throw new AxisFault(
                        Messages.getMessage("paramIsNotSpecified", "SERVICE_SPRING_BEANNAME"));
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }

    }
}
