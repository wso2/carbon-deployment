/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.module.mgt.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.module.mgt.ModuleMgtException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ModuleAdminMessageReceiver extends RPCMessageReceiver {

    private static Log log = LogFactory.getLog(ModuleAdminMessageReceiver.class);

    protected void handleInvocationTargetException(InvocationTargetException e, Method method) throws AxisFault {
        String msg = null;
        Throwable cause = e.getCause();
        if (cause != null) {
            msg = cause.getMessage();
        }
        if (msg == null) {
            msg = "Exception occurred while trying to invoke service method " +
                    (method != null ? method.getName() : "null");
        }
        if (cause instanceof ModuleMgtException) {
            log.debug(msg, cause);
            AxisFault axisFault = new AxisFault(msg, e);
            axisFault.setDetail(getExceptionDetails((ModuleMgtException)cause));
            throw axisFault;
        } else if (cause instanceof AxisFault) {
            log.debug(msg, cause);
            throw (AxisFault) cause;
        }
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    private OMElement getExceptionDetails(ModuleMgtException e) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://service.mgt.module.carbon.wso2.org", "modmgt");
        OMNamespace nsXsd = factory.createOMNamespace("http://mgt.module.carbon.wso2.org/xsd", "modxsd");
        OMElement moduleEx = factory.createOMElement("ModuleMgtException", ns);
        OMElement moduleWrap = factory.createOMElement("ModuleMgtException", ns);
        OMElement key = factory.createOMElement("key", nsXsd);
        OMElement level = factory.createOMElement("level", nsXsd);
        key.setText(e.getKey());
        level.setText(String.valueOf(e.getLevel()));
        moduleEx.addChild(key);
        moduleEx.addChild(level);
        moduleWrap.addChild(moduleEx);

        return moduleWrap;

    }

}
