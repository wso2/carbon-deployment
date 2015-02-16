/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.webapp.mgt.ext;

import org.apache.axis2.Constants;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scannotation.AnnotationDB;
import org.scannotation.WarUrlFinder;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * //todo
 */
@SuppressWarnings("UnusedDeclaration")
public class APIPublisherLifecycleListener extends CarbonLifecycleListenerBase {

    private static final Log log = LogFactory.getLog(APIPublisherLifecycleListener.class);

    private static final String httpPort = "mgt.transport.http.port";
    private static final String hostName = "carbon.local.ip";

    @Override
    public void lifecycleEvent(StandardContext context, ApplicationInfo applicationInfo) {
        if (applicationInfo != null) {
            log.info(" Started processing application ".concat(context.getName()).concat(" for API creation."));
        } else {
            if (log.isDebugEnabled()) {
                log.debug(" Skipping application ".concat(context.getName()).concat(" from API creation."));
            }
        }

        if (applicationInfo != null && applicationInfo.isManagedApi()) {
            log.info(" Scanning Application : ".concat(context.getName()).concat(", for annotations."));
            scanStandardContext(context);
            //todo build api resource model with the result of the scan

            addApi(context, applicationInfo);
        }
    }

    public void scanStandardContext(StandardContext context) {
        // Set<String> entityClasses = getAnnotatedClassesStandardContext(context, Path.class);
        //todo pack the annotation db with feature
        Set<String> entityClasses = null;

        AnnotationDB db = new AnnotationDB();
        db.addIgnoredPackages("org.apache");
        db.addIgnoredPackages("org.codehaus");
        db.addIgnoredPackages("org.springframework");

        final String path = context.getRealPath("/WEB-INF/classes");
        //TODO follow the above line for "WEB-INF/lib" as well

        URL[] libPath = WarUrlFinder.findWebInfLibClasspaths(context.getServletContext());
        URL classPath = WarUrlFinder.findWebInfClassesPath(context.getServletContext());
        URL[] urls = (URL[]) ArrayUtils.add(libPath, libPath.length, classPath);

        try {
            db.scanArchives(urls);
            entityClasses = db.getAnnotationIndex().get(Path.class.getName());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (entityClasses != null && !entityClasses.isEmpty()) {
            for (String className : entityClasses) {
                try {

                    List<URL> fileUrls = convertToFileUrl(libPath, classPath, context.getServletContext());

                    URLClassLoader cl = new URLClassLoader(fileUrls.toArray(new URL[]{}), this.getClass().getClassLoader());

                    Class<?> clazz = context.getServletContext().getClassLoader().loadClass(className);

                    showAPIinfo(context.getServletContext(), clazz);
                    //cl = null;

                } catch (ClassNotFoundException e) {
                    log.error(e.getStackTrace());
                }
            }
        }
    }

    private List<URL> convertToFileUrl(URL[] libPath, URL classPath, ServletContext context) {

        if ((libPath != null || libPath.length == 0) && classPath == null) {
            return null;
        }

        List<URL> list = new ArrayList<URL>();
        if (classPath != null) {
            list.add(classPath);
        }

        if (libPath != null && libPath.length != 0) {
            final String libBasePath = context.getRealPath("/WEB-INF/lib");
            for (URL lib : libPath) {
                String path = lib.getPath();
                if (path != null) {
                    String fileName = path.substring(path.lastIndexOf(File.separator));
                    if (fileName != null) {
                        try {
                            list.add(new URL("jar:file://" + libBasePath + File.separator + fileName + "!/"));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return list;
    }

    private static Set<String> getAnnotatedClassesStandardContext(
            StandardContext context, Class<?> annotation) {

        AnnotationDB db = new AnnotationDB();
        db.addIgnoredPackages("org.apache");
        db.addIgnoredPackages("org.codehaus");
        db.addIgnoredPackages("org.springframework");

        final String path = context.getRealPath("/WEB-INF/classes");
        //TODO follow the above line for "WEB-INF/lib" as well
        URL resourceUrl = null;
        URL[] urls = null;

        if (path != null) {
            final File fp = new File(path);
            if (fp.exists()) {
                try {
                    resourceUrl = fp.toURI().toURL();
                    urls = new URL[]{new URL(resourceUrl.toExternalForm())};

                    db.scanArchives(urls);
                    return db.getAnnotationIndex().get(annotation.getName());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public void addApi(StandardContext context, ApplicationInfo applicationInfo) {
        log.info(" Creating API for Application : " + context.getName());
        if (applicationInfo != null && applicationInfo.isManagedApi()) {
            if (isAPIProviderReady()) {
                //todo check null
                // if null --> add to map
                APIProvider apiProvider = getAPIProvider();

                /*String username = CarbonContext.getCurrentContext().getUsername();
                if(username == null || username.equals("")) {
                    username = "admin";
                }
                log.info(" ## UserName == " + username);*/

                String provider = "admin";
                //todo (later) get correct provider(username) for tenants

                String apiVersion = applicationInfo.getVerion();

                String apiName = null;
                String apiContext = applicationInfo.getApiName();
                if (!apiContext.startsWith("/")) {
                    apiName = apiContext;
                    apiContext = "/" + apiContext;
                } else {
                    apiName = apiContext.substring(1);
                }


                String apiEndpoint = "http://" + System.getProperty(hostName) + ":" + System.getProperty(httpPort) + apiContext;

                String iconPath = "";
                String documentURL = "";
                String authType = "Any";

                APIIdentifier identifier = new APIIdentifier(provider, apiName, apiVersion);
                try {
                    if (apiProvider.isAPIAvailable(identifier)) {
                        //todo : do nothing ?? update API ?
                        log.info("Skip adding duplicate API " + apiName);
                        return;
                    }
                } catch (APIManagementException e) {
                    log.error("Error while deleting existing API", e);
                }

                API api = createAPIModel(apiProvider, apiContext, apiEndpoint, authType, identifier);

                if (api != null) {
                    try {
                        apiProvider.createProductAPI(api);

                        log.info("Successfully added the API, provider: ".concat(provider).concat(" name: ").
                                concat(apiName).concat(" version:").concat(apiVersion));

                    } catch (APIManagementException e) {
                        log.error("Error while adding API", e);
                    }
                }
            } else {
                //todo add to map to wait until apiProvier is ready
                /*Map<String, String> map = new HashMap<String, String>();
                map.put("testKey", "testValue");
                APIDataHolder.getInstance().getInitialAPIInfoMap().put("test-1", map);*/
            }
        }
    }

    private API createAPIModel(APIProvider apiProvider, String apiContext, String apiEndpoint, String authType, APIIdentifier identifier) {
        API api = null;
        try {
            api = new API(identifier);
            api.setContext(apiContext);
            api.setUrl(apiEndpoint);
            api.setUriTemplates(getURITemplates(apiEndpoint, authType));
            api.setVisibility(APIConstants.API_GLOBAL_VISIBILITY);
            api.addAvailableTiers(apiProvider.getTiers());
            api.setEndpointSecured(false);
            api.setStatus(APIStatus.PUBLISHED);
            api.setTransports(Constants.TRANSPORT_HTTP + "," + Constants.TRANSPORT_HTTPS);

            Set<Tier> tiers = new HashSet<Tier>();
            tiers.add(new Tier(APIConstants.UNLIMITED_TIER));
            api.addAvailableTiers(tiers);
            api.setSubscriptionAvailability(APIConstants.SUBSCRIPTION_TO_ALL_TENANTS);
            api.setResponseCache(APIConstants.DISABLED);

            String endpointConfig = "{\"production_endpoints\":{\"url\":\" " + apiEndpoint + "\",\"config\":null},\"endpoint_type\":\"http\"}";
            api.setEndpointConfig(endpointConfig);
            //todo default version support
            //todo use a constant for ""
            if("".equals(identifier.getVersion())) {
                api.setAsDefaultVersion(Boolean.TRUE);
            }

            //todo add a proper icon
            /* Adding Icon*/
            /*File file = null;
            if (!APIStartupPublisherConstants.API_ICON_PATH_AND_DOCUMENT_URL_DEFAULT.equals(iconPath)) {
                file =new File(iconPath);
                String absolutePath = file.getAbsolutePath();
                Icon icon = new Icon(getImageInputStream(absolutePath), getImageContentType(absolutePath));
                String thumbPath = APIUtil.getIconPath(identifier);
                String thumbnailUrl = provider.addIcon(thumbPath, icon);
                api.setThumbnailUrl(APIUtil.prependTenantPrefix(thumbnailUrl, apiProvider));*/

            /*Set permissions to anonymous role for thumbPath*/
                /*APIUtil.setResourcePermissions(apiProvider, null, null, thumbPath);*/

        } catch (APIManagementException e) {
            log.error("Error while initializing API Provider", e);
        } /*catch (IOException e) {
            log.error("Error while reading image from icon path", e);
        }*/
        return api;
    }

    private Set<URITemplate> getURITemplates(String endpoint, String authType) {
        //todo improve to add sub context paths for uri templates as well.
        //todo This is based on result from App scanning
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        String[] httpVerbs = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};

        if (authType.equals(APIConstants.AUTH_NO_AUTHENTICATION)) {
            for (int i = 0; i < 5; i++) {
                URITemplate template = new URITemplate();
                template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
                template.setHTTPVerb(httpVerbs[i]);
                template.setResourceURI(endpoint);
                template.setUriTemplate("/*");
                uriTemplates.add(template);
            }
        } else {
            for (int i = 0; i < 5; i++) {
                URITemplate template = new URITemplate();
                if (i != 4) {
                    template.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                } else {
                    template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
                }
                template.setHTTPVerb(httpVerbs[i]);
                template.setResourceURI(endpoint);
                template.setUriTemplate("/*");
                uriTemplates.add(template);
            }
        }

        return uriTemplates;
    }

    private static void showAPIinfo(ServletContext context, Class<?> clazz) {

        Path rootCtx = clazz.getAnnotation(Path.class);
        if (rootCtx != null) {
            String root = rootCtx.value();
            log.info("======================== API INFO ======================= ");
            if (context != null) {
                log.info("Application Context root = " + context.getContextPath());
            }
            log.info("API Root  Context = " + root);
            log.info("API Sub Context List ");
            for (Method method : clazz.getDeclaredMethods()) {
                Path path = method.getAnnotation(Path.class);
                if (path != null) {
                    String subCtx = path.value();
                    log.info("  " + root + "/" + subCtx);
                }
            }
        }
        log.info("===================================================== ");
    }

    private APIProvider getAPIProvider() {
        try {
            //todo get current user
            return APIManagerFactory.getInstance().getAPIProvider("admin");
        } catch (APIManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isAPIProviderReady() {
        if (ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService() != null) {
            return true;
        }
        return false;
    }
}
