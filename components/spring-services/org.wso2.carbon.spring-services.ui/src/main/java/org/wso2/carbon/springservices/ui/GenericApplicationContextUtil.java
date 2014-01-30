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

package org.wso2.carbon.springservices.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.classloader.MultiParentClassLoader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
/*
 *
 */

public class GenericApplicationContextUtil {
    public static GenericApplicationContext getSpringApplicationContext(
            InputStream applicationContxtInStream, String springBeansFilePath) throws AxisFault {
        GenericApplicationContext appContext = new GenericApplicationContext();

        XmlBeanDefinitionReader xbdr = new XmlBeanDefinitionReader(appContext);
        xbdr.setValidating(false);
        xbdr.loadBeanDefinitions(new InputStreamResource(applicationContxtInStream));
        appContext.refresh();
        return appContext;
    }

    public static GenericApplicationContext getSpringApplicationContextClassPath(
            ClassLoader classLoader, String relPath) throws AxisFault {
        GenericApplicationContext appContext = new GenericApplicationContext();
        appContext.setClassLoader(classLoader);
        ClassLoader prevCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            XmlBeanDefinitionReader xbdr = new XmlBeanDefinitionReader(appContext);
            xbdr.setValidating(false);
            InputStream in = classLoader.getResourceAsStream(relPath);
            if (in == null) {
                throw new AxisFault("Spring context cannot be located for AxisService");
            }
            xbdr.loadBeanDefinitions(new InputStreamResource(in));
            appContext.refresh();
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        } finally {
            // Restore
            Thread.currentThread().setContextClassLoader(prevCl);
        }
        return appContext;
    }

    public static GenericApplicationContext getSpringApplicationContext(
            String applicationContxtFilePath, ClassLoader springBeansClassLoader) throws AxisFault {
        GenericApplicationContext appContext = new GenericApplicationContext();

        appContext.setClassLoader(springBeansClassLoader);

        ClassLoader prevCl = Thread.currentThread().getContextClassLoader();

        try {
            // Save the class loader so that you can restore it later
            Thread.currentThread()
                    .setContextClassLoader(
                            new MultiParentClassLoader(new URL[]{}, new ClassLoader[]{
                                    springBeansClassLoader, prevCl}));
            XmlBeanDefinitionReader xbdr = new XmlBeanDefinitionReader(appContext);
            xbdr.setValidating(false);
            xbdr.loadBeanDefinitions(new InputStreamResource(
                    new FileInputStream(new File(applicationContxtFilePath))));
            appContext.refresh();
        } catch (FileNotFoundException e) {
            throw AxisFault.makeFault(e);
        } finally {
            // Restore
            Thread.currentThread().setContextClassLoader(prevCl);
        }

        return appContext;
    }

    public static GenericApplicationContext getSpringApplicationContext(
            String applicationContxtFilePath, String springBeansFilePath) throws AxisFault {
        try {
            return getSpringApplicationContext(
                    new FileInputStream(new File(applicationContxtFilePath)), springBeansFilePath);
        } catch (FileNotFoundException e) {
            throw AxisFault.makeFault(e);
        }
    }
}
