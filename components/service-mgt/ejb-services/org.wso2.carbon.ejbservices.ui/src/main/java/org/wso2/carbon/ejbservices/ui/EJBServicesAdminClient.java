package org.wso2.carbon.ejbservices.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jasper.tagplugins.jstl.core.Catch;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.ejbservices.stub.EJBProviderAdminStub;
import org.wso2.carbon.ejbservices.stub.types.carbon.EJBAppServerData;
import org.wso2.carbon.ejbservices.stub.types.carbon.EJBProviderData;
import org.wso2.carbon.ejbservices.stub.types.carbon.UploadedFileItem;
import org.wso2.carbon.ejbservices.stub.types.carbon.WrappedAllConfigurations;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnusedDeclaration")
public class EJBServicesAdminClient {
    private static final Log log = LogFactory.getLog(EJBServicesAdminClient.class);
    private EJBProviderAdminStub stub;

    public EJBServicesAdminClient(javax.servlet.ServletContext servletContext,
                                  javax.servlet.http.HttpSession httpSession) throws Exception {
        try {
            ConfigurationContext cc = (ConfigurationContext)
                    servletContext.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) httpSession.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String serverURL = CarbonUIUtil.getServerURL(servletContext, httpSession);
            stub = new EJBProviderAdminStub(cc, serverURL + "EJBProviderAdmin");
            Options options = stub._getServiceClient().getOptions();
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            options.setManageSession(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public EJBServicesAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        stub = new EJBProviderAdminStub(ctx, serverURL + "EJBProviderAdmin");
        Options options = stub._getServiceClient().getOptions();
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        //Increase the time out when sending large attachments
        options.setTimeOutInMilliSeconds(10000);
        options.setManageSession(true);
    }

    public String uploadService(DataHandler dataHandler, String fileName, String fileType)
            throws RemoteException {
        UploadedFileItem uploadedFileItem = new UploadedFileItem();
        uploadedFileItem.setDataHandler(dataHandler);
        uploadedFileItem.setFileName(fileName);
        uploadedFileItem.setFileType(fileType);
        return stub.uploadService(uploadedFileItem);
    }

    public EJBAppServerData[] getAppServerNameList() throws Exception {
        try {
            return stub.getAppServerNameList();
        } catch (Exception e) {
            String msg = "Failed to get Application Server Name List. " +
                         "Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }


    public EJBAppServerData[] getEJBAppServerConfigurations() throws Exception {
        try {
            return stub.getEJBAppServerConfigurations();
        } catch (Exception e) {
            String msg = "Failed to get Application Server Configurations. " +
                         "Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public void addApplicationServer(String providerURL, String jndiContextClass, String userName,
                                     String password, String appServerType) throws Exception {
        try {
            stub.addApplicationServer(providerURL, jndiContextClass, userName, password, appServerType);
        } catch (Exception e) {
            String msg = "Failed to add Application Server. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public String[] getClassNames(String archiveId) throws Exception {
        try {
            return stub.getClassNames(archiveId);
        } catch (Exception e) {
            String msg = "Failed to get Class names. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public void createAndDeployEJBService(String archiveId, String serviceName,
                                          String[] serviceClasses,
                                          String jnpProviderUrl, String beanJNDIName,
                                          String remoteInterface) throws Exception {
        try {
            stub.createAndDeployEJBService(archiveId, serviceName, serviceClasses, jnpProviderUrl,
                                           beanJNDIName, remoteInterface);
        } catch (Exception e) {
            String msg = "Failed to Deploy EJB Service. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }

    }

    public EJBProviderData[] getEJBConfigurations() throws Exception {

        try {
            return stub.getEJBConfigurations();
        } catch (Exception e) {
            String msg = "Failed to get EJB Configurations. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public WrappedAllConfigurations getAllConfigurations() throws Exception {
        try {
            return stub.getAllConfigurations();
        } catch (Exception e) {
            String msg = "Failed to get All EJB Configurations. Backend service may be unavailable";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

    public Map<String, String> getServiceParameters(String serviceName) throws Exception {
        //TODO update ejb configuration in registry also
        try {
            String[] parameters = stub.getServiceParameters(serviceName);
            Map<String, String> paramMap = new HashMap<String, String>();
            for (String parameter : parameters) {
                OMElement paramEle = AXIOMUtil.stringToOM(parameter);
                String name = paramEle.getAttributeValue(new QName("", "name"));
                String value = paramEle.getText();
                paramMap.put(name, value);
            }
            return paramMap;
        } catch (Exception e) {
            String msg = "Failed to get Service parameters. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public void setServiceParameters(String serviceName, String remoteInterfaceName,
                                     String beanJndiName,
                                     String jndiUser, String jndiPassword, String providerUrl,
                                     String serviceType,
                                     String jndiContextClass, String serviceClass)
            throws Exception {
        try {
            String[] parameters = new String[7];
            parameters[0] = getParam("remoteInterfaceName", remoteInterfaceName);
            parameters[1] = getParam("beanJndiName", beanJndiName);
            parameters[2] = getParam("jndiUser", jndiUser);
            parameters[3] = getParam("jndiPassword", jndiPassword);
            parameters[4] = getParam("providerUrl", providerUrl);
            parameters[5] = getParam("serviceType", serviceType);
            parameters[6] = getParam("jndiContextClass", jndiContextClass);
//            parameters[7] = getParam("ServiceClass", serviceClass);
            stub.setServiceParameters(serviceName, parameters);
        } catch (Exception e) {
            String msg = "Failed to update Service parameters. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    private String getParam(String name, String value) {
        return "<parameter name=\"" + name + "\" locked=\"false\">" + value + "</parameter>";
    }

    public void deleteEJBConfiguration(String serviceName) throws Exception {
        try {
            stub.deleteEJBConfiguration(serviceName);
        } catch (Exception e) {
            String msg = "Failed to delete EJB Configuration. Backend service may be " +
                         "unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public boolean testAppServerConnection(String providerURL,
                                        String jndiContextClass, String userName, String password)
            throws Exception {
        boolean isSuccessful = false;
        try {
            isSuccessful = stub.testAppServerConnection(providerURL, jndiContextClass,  userName,  password);
        } catch (Exception e){
            log.error("Failed to connect to the given Application Server.", e);
            throw e;
        }
        return isSuccessful;
    }
}
