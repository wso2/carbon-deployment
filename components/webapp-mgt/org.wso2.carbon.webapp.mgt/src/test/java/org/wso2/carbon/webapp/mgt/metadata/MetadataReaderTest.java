package org.wso2.carbon.webapp.mgt.metadata;

import junit.framework.TestCase;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by sagara on 7/29/14.
 */
public class MetadataReaderTest extends TestCase {


    private int status;

    public void testReadApplicationDescriptorExists() {
        //Set flag to 'exists'
        status = 1;
        MetadataReader metadataReader = MetadataReader.getInstance();
        ServletContext dummyCtx = new DummyServletContext();
        Map<String, Object> metadata = metadataReader.readApplicationDescriptor(dummyCtx);
        assertNotNull(metadata);
        System.out.println(metadata);
        assertEquals("true", metadata.get("manged-api.enabled"));
        assertEquals("true", metadata.get("discovery.enabled"));
        assertEquals("false", metadata.get("feature.index.enabled"));
        assertEquals("true", metadata.get("monitring.bam.enabled"));
        assertEquals("false", metadata.get("monitring.cep.enabled"));
        assertEquals("1.0", metadata.get("feature.index.sub-feature.sub-sub-feature.version"));
        assertEquals("x, y", metadata.get("feature.index.sub-feature.sub-sub-feature.as.label"));
        assertEquals("false", metadata.get("feature.index.sub-feature.enabled"));
        assertEquals("2.0", metadata.get("feature.index.sub-feature.sub-sub-feature.as.version"));
    }

    public void testReadApplicationDescriptorNotexists() {
        //Set flag to 'notexists'
        status = 2;
        MetadataReader metadataReader = MetadataReader.getInstance();
        ServletContext dummyCtx = new DummyServletContext();
        Map<String, Object> metadata = metadataReader.readApplicationDescriptor(dummyCtx);
        assertNotNull(metadata);
        assertEquals(0, metadata.size());
        assertEquals(null, metadata.get("discovery.enabled"));

    }

    public void testReadApplicationDescriptorFaulty() {
        //Set flag to 'faulty'
        status = 3;
        MetadataReader metadataReader = MetadataReader.getInstance();
        ServletContext dummyCtx = new DummyServletContext();
        Map<String, Object> metadata;
        try {
            metadata = metadataReader.readApplicationDescriptor(dummyCtx);
            fail("An exception is expected here !");

        } catch (Exception e) {

        }

    }

    public void testReadApplicationDescriptorWrong() {
        //Set flag to 'faulty'
        status = 4;
        MetadataReader metadataReader = MetadataReader.getInstance();
        ServletContext dummyCtx = new DummyServletContext();
        Map<String, Object> metadata;
        try {
            metadata = metadataReader.readApplicationDescriptor(dummyCtx);
            fail("An exception is expected here !");

        } catch (Exception e) {

        }

    }


    /**
     * Dummy implementation of ServletContext interface. according to
     * the Tomcat org.apache.catalina.core.ApplicationContextFacade.
     */
    public class DummyServletContext implements ServletContext {

        @Override
        public InputStream getResourceAsStream(String path) {

            String testPath;
            switch (status) {

                //flag is set to 'notexists'
                case 2:
                    testPath = "src/test/resources/notexists/application.xml";
                    break;

                //flag is set to 'faulty'
                case 3:
                    testPath = "src/test/resources/faulty/application.xml";
                    break;

                //flag is set to 'wrong'
                case 4:
                    testPath = "src/test/resources/wrong/application.xml";
                    break;

                //flag is set to 'exists'
                default:
                    testPath = "src/test/resources/exists/application.xml";

            }

            InputStream in = null;
            try {
                in = new FileInputStream(testPath);
            } catch (Exception e) {
                //Do nothing !
                //This is according to org.apache.catalina.core.ApplicationContext#getResourceAsStream method
            }
            return in;
        }


        @Override
        public ServletContext getContext(String uripath) {
            return null;
        }

        @Override
        public String getContextPath() {
            return null;
        }

        @Override
        public int getMajorVersion() {
            return 0;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public int getEffectiveMajorVersion() {
            return 0;
        }

        @Override
        public int getEffectiveMinorVersion() {
            return 0;
        }

        @Override
        public String getMimeType(String file) {
            return null;
        }

        @Override
        public Set<String> getResourcePaths(String path) {
            return null;
        }

        @Override
        public URL getResource(String path) throws MalformedURLException {
            return null;
        }


        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            return null;
        }

        @Override
        public RequestDispatcher getNamedDispatcher(String name) {
            return null;
        }

        @Override
        public Servlet getServlet(String name) throws ServletException {
            return null;
        }

        @Override
        public Enumeration<Servlet> getServlets() {
            return null;
        }

        @Override
        public Enumeration<String> getServletNames() {
            return null;
        }

        @Override
        public void log(String msg) {

        }

        @Override
        public void log(Exception exception, String msg) {

        }

        @Override
        public void log(String message, Throwable throwable) {

        }

        @Override
        public String getRealPath(String path) {
            return null;
        }

        @Override
        public String getServerInfo() {
            return null;
        }

        @Override
        public String getInitParameter(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return null;
        }

        @Override
        public boolean setInitParameter(String name, String value) {
            return false;
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return null;
        }

        @Override
        public void setAttribute(String name, Object object) {

        }

        @Override
        public void removeAttribute(String name) {

        }

        @Override
        public String getServletContextName() {
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, String className) {
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
            return null;
        }

        @Override
        public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
            return null;
        }

        @Override
        public ServletRegistration getServletRegistration(String servletName) {
            return null;
        }

        @Override
        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, String className) {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
            return null;
        }

        @Override
        public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
            return null;
        }

        @Override
        public FilterRegistration getFilterRegistration(String filterName) {
            return null;
        }

        @Override
        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            return null;
        }

        @Override
        public SessionCookieConfig getSessionCookieConfig() {
            return null;
        }

        @Override
        public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) throws IllegalStateException, IllegalArgumentException {

        }

        @Override
        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            return null;
        }

        @Override
        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            return null;
        }

        @Override
        public void addListener(String className) {

        }

        @Override
        public <T extends EventListener> void addListener(T t) {

        }

        @Override
        public void addListener(Class<? extends EventListener> listenerClass) {

        }

        @Override
        public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
            return null;
        }

        @Override
        public void declareRoles(String... roleNames) {

        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public JspConfigDescriptor getJspConfigDescriptor() {
            return null;
        }
    }

}
