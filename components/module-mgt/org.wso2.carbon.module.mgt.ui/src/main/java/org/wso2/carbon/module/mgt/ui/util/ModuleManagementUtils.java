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
package org.wso2.carbon.module.mgt.ui.util;

import org.wso2.carbon.module.mgt.stub.ModuleAdminServiceModuleMgtExceptionException;
import org.wso2.carbon.module.mgt.stub.types.ModuleMgtException;
import org.wso2.carbon.ui.CarbonUIMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

public class ModuleManagementUtils {

    public static final String RESOURCE_BUNDLE = "org.wso2.carbon.module.mgt.ui.i18n.Resources";

    public static ModuleMgtException getModuleMgtException(ModuleAdminServiceModuleMgtExceptionException e) {
        if (e.getFaultMessage() != null) {
            return e.getFaultMessage().getModuleMgtException();
        }
        return null;
    }

    public static void handleModuleMgtErrors(ModuleAdminServiceModuleMgtExceptionException e, HttpServletRequest req,
                                             HttpServletResponse res, String warnPage)
            throws IOException {

        //Page tobe redirected in the case of warning
        String warnRedirect = warnPage == null ? "index.jsp" : warnPage;
        CarbonUIMessage carbonMessage;
        String key;

        ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE, req.getLocale());
        if (e.getFaultMessage() == null) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, req, e);
            res.sendRedirect("../admin/error.jsp");
            return;
        }

        ModuleMgtException moduleMgtException = ModuleManagementUtils.getModuleMgtException(e);

        if (moduleMgtException == null) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, req, e);
            res.sendRedirect("../admin/error.jsp");
            return;
        }

        if (moduleMgtException.getLevel() == ModuleManagementConstants.WARNING) {
            key = moduleMgtException.getKey();
            carbonMessage = new CarbonUIMessage(resourceBundle.getString(key), CarbonUIMessage.WARNING);
            req.getSession().setAttribute(CarbonUIMessage.ID, carbonMessage);
            res.sendRedirect(warnRedirect);
            return;
        } else if (moduleMgtException.getLevel() == ModuleManagementConstants.ERROR) {
            key = moduleMgtException.getKey();
            carbonMessage = new CarbonUIMessage(resourceBundle.getString(key), CarbonUIMessage.ERROR);
            req.getSession().setAttribute(CarbonUIMessage.ID, carbonMessage);
            res.sendRedirect("../admin/error.jsp");
            return;
        }
    }

}
