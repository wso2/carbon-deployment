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
package org.wso2.carbon.discovery.cxf.listeners;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.config.Config;
import org.wso2.carbon.discovery.cxf.CXFServiceInfo;
import org.wso2.carbon.discovery.cxf.CxfMessageSender;
import org.wso2.carbon.discovery.cxf.internal.CxfDiscoveryDataHolder;
import org.wso2.carbon.utils.CarbonUtils;

import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TomcatCxfDiscoveryListener implements LifecycleListener {

    private static final String CXF_SERVLET_CLASS = "org.apache.cxf.transport.servlet.CXFServlet";
    private static final String HTTP_PORT = "http";
    private static final String HTTPS_PORT = "https";
    private static final String HOST_NAME_ = "HostName";
    private static final String HOST_NAME_LOCAL = "carbon.local.ip";
    private static final String JAX_WS_PREFIX = "jaxws";
    private static final String JAX_RS_PREFIX = "jaxrs";
    private static final String JAX_WS_NAMESPACE_URI = "http://cxf.apache.org/jaxws";
    private static final String JAX_RS_NAMESPACE_URI = "http://cxf.apache.org/jaxrs";

    private CxfMessageSender cxfMessageSender = new CxfMessageSender();
    private Config config = new Config();

    private static final Log log = LogFactory.getLog(TomcatCxfDiscoveryListener.class);

    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        try {
            String type = lifecycleEvent.getType();
            if (Lifecycle.AFTER_START_EVENT.equals(type) ||
                    Lifecycle.BEFORE_STOP_EVENT.equals(type)) {
                StandardContext context = (StandardContext) lifecycleEvent.getLifecycle();
                String jaxServletMapping = null;

                boolean isJaxWebapp = false;
                Map<String, ? extends ServletRegistration> servletRegs = context.getServletContext().getServletRegistrations();
                for (ServletRegistration servletReg : servletRegs.values()) {
                    if (CXF_SERVLET_CLASS.equals(servletReg.getClassName())) {
                        Object[] mappings = servletReg.getMappings().toArray();
                        jaxServletMapping = mappings.length > 0 ? getServletContextPath((String)mappings[0]) : null;
                        isJaxWebapp = true;
                        break;
                    }
                }

                if (isJaxWebapp) {
                    addConfigData(context.getServletContext());
                    for (CXFServiceInfo serviceBean : getServiceInfo(context, jaxServletMapping)) {
                        if (serviceBean == null) {
                            return;
                        }
                        if (Lifecycle.AFTER_START_EVENT.equals(type)) {
                            cxfMessageSender.sendHello(serviceBean, config);

                        } else if (Lifecycle.BEFORE_STOP_EVENT.equals(type)) {
                            cxfMessageSender.sendBye(serviceBean, config);
                        }
                    }
                }
            }

        } catch (DiscoveryException e) {
            log.error("Error while publishing the services to the discovery service ", e);
        } catch (Throwable e) {
            //Catching throwable since this listener's state shouldn't affect the webapp deployment.
            log.error("Error while publishing the services to the discovery service ", e);
        }
    }

    private String getServletContextPath(String jaxServletPattern) {
        if (!"".equals(jaxServletPattern) && jaxServletPattern != null) {
            if (jaxServletPattern.endsWith("/*")) {
                jaxServletPattern = jaxServletPattern.substring(0, jaxServletPattern.length() - 2);
            }
            if (jaxServletPattern.startsWith("/")) {
                jaxServletPattern = jaxServletPattern.substring(1);
            }
        } else {
            jaxServletPattern = "services";
        }

        return jaxServletPattern;

    }

    /**
     * Get JAX-WS,JAX-RS service info needed to send in the WS-Discovery message
     *
     */
    private List<CXFServiceInfo> getServiceInfo(StandardContext context, String jaxServletMapping)
            throws DiscoveryException {
        String contextPath = context.getServletContext().getContextPath();
        contextPath = contextPath.startsWith("/") ? contextPath.substring(1, contextPath.length()) : contextPath;
        try {
            InputStream configStream = getConfigLocation(context.getServletContext());
            StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(configStream);
            OMElement documentElement = stAXOMBuilder.getDocumentElement(true);
            for (Iterator declaredNamespaces = documentElement.getAllDeclaredNamespaces(); declaredNamespaces
                    .hasNext(); ) {
                OMNamespaceImpl nameSpace = (OMNamespaceImpl) declaredNamespaces.next();
                if (JAX_WS_PREFIX.equals(nameSpace.getPrefix()) && JAX_WS_NAMESPACE_URI
                        .equals(nameSpace.getNamespaceURI())) {
                    return getJaxWsServiceInfo(documentElement, context, jaxServletMapping, contextPath);
                } else if (JAX_RS_PREFIX.equals(nameSpace.getPrefix()) && JAX_RS_NAMESPACE_URI
                        .equals(nameSpace.getNamespaceURI())) {
                    return getJaxRsServiceInfo(documentElement, context, jaxServletMapping, contextPath);
                }

            }

        } catch (XMLStreamException e) {
            log.error("Error processing CXF config file of " + contextPath, e);
        }
        return new ArrayList<CXFServiceInfo>();
    }

    /**
     * This method will add service information needed for the JAX-RS services
     *
     * @param documentElement Document element of configuration file
     * @param context StandardContext of the service
     * @param jaxServletMapping Servlet mapping define in the web.xml
     * @param contextPath Context path of the web app
     * @return List ow available service in the web app
     */

    private List<CXFServiceInfo> getJaxRsServiceInfo(OMElement documentElement, StandardContext context,
            String jaxServletMapping, String contextPath) throws DiscoveryException {
        List<CXFServiceInfo> serviceInfoList = new ArrayList<CXFServiceInfo>();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        QName serverQName = new QName(JAX_RS_NAMESPACE_URI, "server");

        for (Iterator services = documentElement.getChildrenWithName(serverQName); services.hasNext(); ) {
            OMElement server = (OMElement) services.next();
            String endpoint = server.getAttributeValue(new QName("address"));
            String serviceBean = (server.getFirstChildWithName(new QName(JAX_RS_NAMESPACE_URI, "serviceBeans")))
                    .getFirstElement().getAttributeValue(new QName("bean"));
            String endpointInterface = "";
            for (Iterator beanList = documentElement.getChildrenWithName(new QName("bean")); beanList.hasNext(); ) {
                OMElement bean = (OMElement) beanList.next();
                if (serviceBean.equals(bean.getAttributeValue(new QName("id")))) {
                    endpointInterface = bean.getAttributeValue(new QName("class"));
                    break;
                }
            }
            CXFServiceInfo serviceInfo = new CXFServiceInfo();
            serviceInfo.setServiceName(contextPath + "_" + endpoint.substring(1));
            serviceInfo.setType(getType(endpointInterface));
            serviceInfo.setTenantDomain(tenantDomain);
            serviceInfo.setWsdlURI(getWadlUri(context, jaxServletMapping, endpoint));
            serviceInfo.setxAddrs(getXAddrsList(context, jaxServletMapping, endpoint));
            serviceInfoList.add(serviceInfo);
        }
        return serviceInfoList;
    }

    /**
     * This method will add service information needed for the JAX-WS services using @WebService annotation.
     *
     * @param documentElement Document element of configuration file
     * @param context StandardContext of the service
     * @param jaxServletMapping Servlet mapping define in the web.xml
     * @param contextPath Context path of the web app
     * @return List ow available service in the web app
     */
    private List<CXFServiceInfo> getJaxWsServiceInfo(OMElement documentElement, StandardContext context,
            String jaxServletMapping, String contextPath) throws DiscoveryException {
        List<CXFServiceInfo> serviceInfoList = new ArrayList<CXFServiceInfo>();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        QName serviceType = new QName(JAX_WS_NAMESPACE_URI, "server");
        for (Iterator services = documentElement.getChildrenWithName(serviceType); services.hasNext(); ) {
            OMElement server = (OMElement) services.next();
            String endpoint = server.getAttributeValue(new QName("address"));
            String endpointInterface = server.getFirstChildWithName(new QName(JAX_WS_NAMESPACE_URI, "serviceBean"))
                    .getFirstElement().getAttributeValue(new QName("class"));
            CXFServiceInfo serviceInfo = new CXFServiceInfo();
            serviceInfo.setServiceName(contextPath + "_" + endpoint.substring(1));
            serviceInfo.setType(getPortType(context, endpointInterface));
            serviceInfo.setTenantDomain(tenantDomain);
            serviceInfo.setWsdlURI(getWsdlUri(context, jaxServletMapping, endpoint));
            serviceInfo.setxAddrs(getXAddrsList(context, jaxServletMapping, endpoint));
            serviceInfoList.add(serviceInfo);
        }
        return serviceInfoList;
    }

    /**
     *  This method will get the qualified name from the service endpoint interface
     *  by reversing the service end point interface
     *
     * @param serviceEndpointInterface Endpoint interface name
     * @return the qualified name to use as the ws-discovery type,
     */
    private QName getType(String serviceEndpointInterface){
        String targetNamespace = generateTargetNsFromInterfaceName(serviceEndpointInterface);
        String name = serviceEndpointInterface.substring(serviceEndpointInterface.lastIndexOf('.') + 1);

        return new QName(targetNamespace, name);
    }

    /**
     * Get the JAX-WS service port type. A port type is identified by the targetNamespace
     * of the service and the port name.
     */
    private QName getPortType(StandardContext context, String serviceEndpoint) throws DiscoveryException {
        QName seiInfo;
        try {
            Class<?> clazz = context.getServletContext().getClassLoader().loadClass(serviceEndpoint);
            seiInfo = processClazz(clazz, context);
        } catch (ClassNotFoundException e) {
            throw new DiscoveryException(e.getMessage(), e);
        }

        return seiInfo;
    }

    /**
     * Retrieve targetNamespace and name from the @WebService annotation in jax-ws resource class.
     * First read the SEI class, and if any of the elements are empty, then check the SEI interface as well.
     */
    private QName processClazz(Class<?> clazz, StandardContext context)
            throws DiscoveryException, ClassNotFoundException {

        WebService jwsAnnotation = clazz.getAnnotation(javax.jws.WebService.class);
        String targetNamespace = jwsAnnotation.targetNamespace();   // ex. http://apache.org/handlers
        String name = jwsAnnotation.name();
        String endpointInterfaceName = jwsAnnotation.endpointInterface();

        Class<?> endpointInterface;
        if (endpointInterfaceName != null && !endpointInterfaceName.trim().isEmpty()) {
            endpointInterface = context.getServletContext().getClassLoader().loadClass(endpointInterfaceName);
        } else {
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                endpointInterface = interfaces[0];
            } else {

                // Generate targetNamespace as per jax-ws 2.2 specification chapter 3.4
                // where service classes doesn't have a customized endpointInterface, take the
                // service endpointInterface and compute the target namespace according chapter 3.2
                if (targetNamespace == null || targetNamespace.trim().isEmpty()) {
                    targetNamespace = generateTargetNsFromInterfaceName(clazz.getName());
                }

                if (name == null || name.trim().isEmpty()) {
                    String tmpInterfaceName = clazz.getName();
                    name = tmpInterfaceName.substring(tmpInterfaceName.lastIndexOf('.') + 1);
                }

                return new QName(targetNamespace, name);
            }
        }

        //if targetNamespace or name is empty,try to extract the info from SEI interface
        WebService eiJwsAnnotation = endpointInterface.getAnnotation(javax.jws.WebService.class);
        if (targetNamespace == null || targetNamespace.trim().isEmpty()) {
            targetNamespace = eiJwsAnnotation.targetNamespace();
        }
        if (name == null || name.trim().isEmpty()) {
            name = eiJwsAnnotation.name();
        }

        if (targetNamespace == null || targetNamespace.trim().isEmpty()) {
            targetNamespace = generateTargetNsFromInterfaceName(endpointInterface.getName());
        }

        if (name == null || name.trim().isEmpty()) {
            String tmpInterfaceName = endpointInterface.getName();
            name = tmpInterfaceName.substring(tmpInterfaceName.lastIndexOf('.') + 1);
        }

        return new QName(targetNamespace, name);
    }

    /**
     * Generate targetNamespace as per jax-ws 2.2 specification chapter 3.2.
     *
     * 1. The package name is tokenize using the “.” character as a delimiter.
     * 2. The order of the tokens is reversed.
     * 3. The value of the targetNamespace attribute is obtained by concatenating "http://"
     * to the list of tokens separated by "." and "/".
     *
     * @param sei fully qualified sei interface name
     * @return targetNamespace
     */
    private String generateTargetNsFromInterfaceName(String sei) {
        String[] arr = sei.substring(0, sei.lastIndexOf('.')).
                split("\\.");
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        for(int i = arr.length-1; i >= 0; i--) {
            sb.append(arr[i]).append(".");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("/");
        return sb.toString();
    }

    private String getWsdlUri(StandardContext context, String jaxServletMapping, String endpoint) {
        return "http://" + getHostname() + ":" + getPortForTransport(HTTP_PORT) +
                context.getServletContext().getContextPath() + "/" + jaxServletMapping + endpoint + "?wsdl";
    }

    private String getWadlUri(StandardContext context, String jaxServletMapping, String endpoint) {
        if (jaxServletMapping.isEmpty()) {
            return "http://" + getHostname() + ":" + getPortForTransport(HTTP_PORT) +
                    context.getServletContext().getContextPath() + endpoint + "?_wadl";
        } else {
            return "http://" + getHostname() + ":" + getPortForTransport(HTTP_PORT) +
                    context.getServletContext().getContextPath() + "/" + jaxServletMapping + endpoint + "?_wadl";
        }
    }

    /**
     * This method will get the hostname if it is defined in the carbon.xml,
     * if not it will return the local port
     *
     * @return host name
     */

    private String getHostname() {
        ServerConfigurationService serverConfigurationService = CxfDiscoveryDataHolder.getInstance()
                .getServerConfigurationService();
        if (serverConfigurationService.getFirstProperty(HOST_NAME_) != null) {
            return serverConfigurationService.getFirstProperty(HOST_NAME_);
        } else {
            return System.getProperty(HOST_NAME_LOCAL);
        }
    }

    /**
     * This method will calculate service endpoints which needs the service
     *
     * @param context StandardContext
     * @param jaxServletMapping Servlet-Mapping defined in the web.xml
     * @param endpoint Service endpoint
     * @return List of endpoint addresses
     */

    private List getXAddrsList(StandardContext context, String jaxServletMapping, String endpoint) {
        List<String> xAddrs = new ArrayList<String>();

        String httpEndpoint = "http://" + getHostname() + ":" + getPortForTransport(HTTP_PORT) +
                context.getServletContext().getContextPath() + "/" + jaxServletMapping + endpoint;
        String httpsEndpoint = "https://" + getHostname() + ":" + getPortForTransport(HTTPS_PORT) +
                context.getServletContext().getContextPath() + "/" + jaxServletMapping + endpoint;

        xAddrs.add(httpEndpoint);
        xAddrs.add(httpsEndpoint);

        return xAddrs;
    }

    /**
     * Get configuration location and load it as input stream, if configuration location not defined
     * it will get it from the default location
     *
     * @param context ServletContext
     * @return input stream of the configuration
     */

    private InputStream getConfigLocation(ServletContext context) {
        String configLocation = context.getInitParameter("config-location");
        if (configLocation == null) {
            InputStream is = null;
            try {
                is = context.getResourceAsStream("/WEB-INF/cxf-servlet.xml");
                if (is != null && is.available() > 0) {
                    configLocation = "/WEB-INF/cxf-servlet.xml";
                }
            } catch (IOException ex) {
                String message = "Configuration location not available for " + context.getContextPath();
                log.error(message, ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }

        return context.getResourceAsStream(configLocation);
    }

    /**
     * Retrieve the added scopes in the <context-param> tag in the web.xml,
     * if there is no context param defined for scope will set the default scope
     *
     * @param scopesString single string with all the available scopes defined in the <param-value>
     */
    private void addScopes(String scopesString) {
        if (scopesString != null && !scopesString.isEmpty()) {
            String[] scopeNames = scopesString.split(" ");
            for (String scope : scopeNames) {
                config.addScope(scope);
            }
        } else {
            config.addScope(DiscoveryConstants.DISCOVERY_DEFAULT_SCOPE);
        }
    }

    /**
     * Retrieve the added Metadata version in the <context-param> tag in the web.xml,
     * if there is no context param defined for MetadataVersion will set the default version
     *
     * @param metaDataVersion version defined in the <param-value>
     */
    private void addMetadataVersion(String metaDataVersion) {
        if (metaDataVersion != null && !metaDataVersion.isEmpty()) {
            config.setMetadataVersion(Integer.parseInt(metaDataVersion));
        } else {
            config.setMetadataVersion(DiscoveryConstants.DISCOVERY_DEFAULT_METADATA_VERSION);
        }
    }

    /**
     * Add the Scopes of the web app and the metadata version
     *
     * @param servletContext ServletContext of the service
     */
    private void addConfigData(ServletContext servletContext) {
        addScopes(servletContext.getInitParameter(DiscoveryConstants.CONFIG_SCOPES));
        addMetadataVersion(servletContext.getInitParameter(DiscoveryConstants.CONFIG_METADATA_VERSION));
    }

    /**
     * This method will return the port of the each transport type
     *
     * @param transport transport type
     * @return port of the transport
     */
    private int getPortForTransport(String transport){
       return CarbonUtils.getTransportPort(CxfDiscoveryDataHolder.getInstance().getMainServerConfigContext(), transport);
    }
}
