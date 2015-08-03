/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.webapp.ext.cxf.interceptor;

import org.apache.cxf.frontend.WSDLGetInterceptor;
import org.apache.cxf.frontend.WSDLGetUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.webapp.ext.cxf.Constants;

import java.util.Map;

public class HostNameSupportedWSDLGetInterceptor extends WSDLGetInterceptor {

    private String hostName;

    public HostNameSupportedWSDLGetInterceptor() {

    }

    public HostNameSupportedWSDLGetInterceptor(String hostName) {
        this.hostName = hostName;

    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Document getDocument(WSDLGetUtils utils, Message message, String base, Map<String, String> params,
                                String ctxUri) {
        Document document =
                utils.getDocument(message, base, params, ctxUri, message.getExchange().getEndpoint().getEndpointInfo());

        Node rootNode = document.getFirstChild();
        if (rootNode != null && rootNode instanceof Element) {
            Element rootEle = (Element) rootNode;
            NodeList serviceEles = rootEle.getElementsByTagNameNS(Constants.WSDL_NS, Constants.WSDL_SERVICE);
            if (serviceEles.getLength() > 0) {
                for (int i = 0; i < serviceEles.getLength(); i++) {
                    Element service = (Element) serviceEles.item(i);
                    NodeList portEles = service.getElementsByTagNameNS(Constants.WSDL_NS, Constants.WSDL_PORT);
                    if (portEles.getLength() > 0) {
                        for (int j = 0; j < portEles.getLength(); j++) {
                            Element port = (Element) portEles.item(j);
                            Element address =
                                    (Element) port.getElementsByTagNameNS(Constants.NO_NS, Constants.WSDL_ADDRESS)
                                                  .item(0);
                            String location = getLocation(address.getAttribute(Constants.WSDL_LOCATION));
                            if (location != null && !"".equals(location)) {
                                address.setAttribute(Constants.WSDL_LOCATION, location);
                            }

                        }
                    }
                }
            }

        }

        return document;
    }

    private String getLocation(String locationAttr) {
        if (!locationAttr.startsWith("http://") && !locationAttr.startsWith("https://")) {
            return null;
        }
        String part = locationAttr.substring(locationAttr.indexOf("://") + 3);
        int idx = part.indexOf(":");
        String host;
        if (idx != -1) {
        	host = part.substring(0, idx);
        	String port = part.substring(idx, part.indexOf("/"));
        } else {
        	host = part.substring(0, part.indexOf("/"));
        } 
        String location = locationAttr.replace(host, hostName);
//        location = location.replace(port, "");
        return location;
    }
}
