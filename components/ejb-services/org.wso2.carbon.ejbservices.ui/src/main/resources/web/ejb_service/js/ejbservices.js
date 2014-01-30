var ejbProviderConfigObject = '';
var existingAppServerConfigs = 0;

function setDefaultServerValues(obj, document) {
    var selectedValue = obj[obj.selectedIndex].value;
    if (selectedValue == 'jboss') {
        document.getElementById('providerUrl').value = 'jnp://localhost:1099';
        document.getElementById('jndiContextClass').value = 'org.jnp.interfaces.NamingContextFactory';
    } else if (selectedValue == 'geronimo') {
        document.getElementById('providerUrl').value = 'localhost:4201';
        document.getElementById('jndiContextClass').value = 'org.openejb.client.JNDIContext';
    } else if (selectedValue == 'weblogic') {
        document.getElementById('providerUrl').value = 't3://localhost:7001';
        document.getElementById('jndiContextClass').value = 'weblogic.jndi.WLInitialContextFactory';
    } else if (selectedValue == 'glassfish') {
        document.getElementById('providerUrl').value = 'iiop://localhost:3700';
        document.getElementById('jndiContextClass').value = 'com.sun.appserv.naming.S1ASCtxFactory';
    } else if (selectedValue == 'WebSphere') {
        document.getElementById('providerUrl').value = 'iiop://localhost:2809';
        document.getElementById('jndiContextClass').value =
        'com.ibm.websphere.naming.WsnInitialContextFactory';
    } else {
        document.getElementById('providerUrl').value = '';
        document.getElementById('jndiContextClass').value = '';
    }
}

function ejbProviderStep1DisableFields() {
    var comboToLoad = document.getElementById('existingAppServerConfigurations');
    existingAppServerConfigs = comboToLoad.length;
    //disable all text/combo boxes at the screen load
    var radio2 = document.getElementById('useExistingConfig');
    if (existingAppServerConfigs != 0 && radio2.checked == false) {
        document.getElementById('serverType').disabled = true;
        document.getElementById('providerUrl').disabled = true;
        document.getElementById('jndiContextClass').disabled = true;
        document.getElementById('userName').disabled = true;
        document.getElementById('password').disabled = true;
        document.getElementById('confirmPassword').disabled = true;
        document.getElementById('addApplicationServerButton').disabled = true;

    } else if (existingAppServerConfigs != 0 && radio2.checked == true) {
        toggleEJBAppServerConfigurationEditScreen('existingEJBServer');
    } else if (existingAppServerConfigs == 0) {
        var radio = document.getElementById('addNewConfig');
//        radio.disabled = true;
        document.getElementById('existingAppServerConfigurations').disabled = true;
        if (existingAppServerConfigs == 0) {
            document.getElementById('ejbStep0NextButton').disabled = true;
        }
    } else if (radio2.checked == true) {
        document.getElementById('existingAppServerConfigurations').disabled = true;
    }

}

function toggleEJBAppServerConfigurationEditScreen(value) {
    if (value == 'addNewEJBServer') {
        //disable existing
        document.getElementById('existingAppServerConfigurations').disabled = true;
		//enable add new
        document.getElementById('serverType').disabled = false;
        document.getElementById('providerUrl').disabled = false;
        document.getElementById('jndiContextClass').disabled = false;
        document.getElementById('userName').disabled = false;
        document.getElementById('password').disabled = false;
        document.getElementById('confirmPassword').disabled = false;
        document.getElementById('addApplicationServerButton').disabled = false;
        document.addEJBApplicationServerForm.appServerConfiguration1.checked = false;
        document.addEJBApplicationServerForm.appServerConfiguration2.checked = true;
        document.getElementById('ejbStep0NextButton').disabled = true;
    } else if (value == 'existingEJBServer') {
        //enable existing
        document.getElementById('existingAppServerConfigurations').disabled = false;
		//disable add new
        document.getElementById('serverType').disabled = true;
        document.getElementById('providerUrl').disabled = true;
        document.getElementById('jndiContextClass').disabled = true;
        document.getElementById('userName').disabled = true;
        document.getElementById('password').disabled = true;
        document.getElementById('confirmPassword').disabled = true;
        document.getElementById('addApplicationServerButton').disabled = true;
        document.addEJBApplicationServerForm.appServerConfiguration1.checked = true;
        document.addEJBApplicationServerForm.appServerConfiguration2.checked = false;
        //existingAppServerConfigs = comboToLoad.length;
        var comboToLoad = document.getElementById('existingAppServerConfigurations');
        existingAppServerConfigs = comboToLoad.length;

        if (existingAppServerConfigs > 0) {
            document.getElementById('ejbStep0NextButton').disabled = false;
        }
    }
}

function checkForExistingAppServerConfigurations(providerURL) {
    var combo = document.getElementById('existingAppServerConfigurations');
    var count;
    for (count = 0; count < combo.length; count++) {
        if (providerURL == combo.options[count].value) {
            CARBON.showErrorDialog('Configuration exists for provider URL ' + combo.options[count].value +
                                   '. Please select it from existing configuration section.');
            combo.selectedIndex = count;
            document.getElementById('existingAppServerConfigurations').disabled = false;
			//document.getElementById('appServerConfiguration1').checked = true;
            //document.getElementById('appServerConfiguration2').checked = false;
            return false;
        }
    }
    return true;
}

function setRemoteInterfaceClass(value) {
    document.getElementById('remoteInterface').value = value;
    document.getElementById('serviceName').value = value.substring(value.lastIndexOf(".") + 1) +
                                                   Math.floor(Math.random()*100);
}

//function setHomeInterfaceClass(value) {
//    document.getElementById('homeInterface').value = value;
//}


function arrangeEJBServiceParams(pArr, vArr) {
    var pA = new Array();
    var vA = new Array();

    pA[0] = 'remoteInterfaceName';
    for (var index = 0; index < pArr.length; index++) {
        if (pArr[index] == 'remoteInterfaceName') {
            vA[0] = vArr[index];
            pArr.splice(index, 1);
            vArr.splice(index, 1);
            break;
        }
    }
    pA[1] = 'homeInterfaceName';
    for (var index = 0; index < pArr.length; index++) {
        if (pArr[index] == 'homeInterfaceName') {
            vA[1] = vArr[index];
            pArr.splice(index, 1);
            vArr.splice(index, 1);
            break;
        }
    }
    pA[2] = 'beanJndiName';
    for (var index = 0; index < pArr.length; index++) {
        if (pArr[index] == 'beanJndiName') {
            vA[2] = vArr[index];
            pArr.splice(index, 1);
            vArr.splice(index, 1);
            break;
        }
    }
    pA[3] = 'jndiUser';
    for (var index = 0; index < pArr.length; index++) {
        if (pArr[index] == 'jndiUser') {
            vA[3] = vArr[index];
            pArr.splice(index, 1);
            vArr.splice(index, 1);
            break;
        }
    }

    pA[4] = 'jndiPassword';
    for (var index = 0; index < pArr.length; index++) {
        if (pArr[index] == 'jndiPassword') {
            vA[4] = vArr[index];
            pArr.splice(index, 1);
            vArr.splice(index, 1);
            break;
        }
    }
    for (var index = 0, i = 5; index < pArr.length; index++,i++) {
        pA[i] = pArr[index];
        vA[i] = vArr[index];
    }
    var str = '';
    var label;
    for (var index = 0; index < pA.length; index++) {
        if (pA[index] == 'remoteInterfaceName') {
            label = 'Remote Interface Name';
        }
        /*else if (pA[index] == 'homeInterfaceName') {
            label = 'Home Interface Name';
        }*/
        else if (pA[index] == 'beanJndiName') {
            label = 'Bean JNDI Name';
        }
        else if (pA[index] == 'jndiUser') {
            label = 'JNDI User';
        }
        else if (pA[index] == 'jndiPassword') {
            label = 'JNDI Password';
        }
        else if (pA[index] == 'serviceType') {
            label = 'Service Type';
        }
        else if (pA[index] == 'jndiContextClass') {
            label = 'JNDI Context Class';
        }
        else if (pA[index] == 'providerUrl') {
            label = 'Provider URL';
        }
        else if (pA[index] == 'ServiceClass') {
            label = 'Service Class';
        }
        str = str + '<div style="clear:both;"><label style="width: 25%">' + label + '</label>';
        str = str + '<input type="text" size="50" value="' + vA[index] + '"';
        str += ' id="' + pA[index] + '"';
        str += 'tabindex="' + index + '"';
        if (pA[index] == 'serviceType' || pA[index] == 'ServiceClass') {
            str += ' disabled=true ';
        }
        str = str + '></input></div>';

    }
    document.getElementById('param_div').innerHTML = str;
}



//
//function addApplicationServer() {
//    var serverType = document.getElementById('serverType').value;
//    var providerURL = document.getElementById('providerUrl').value;
//    var jndiContextClass = document.getElementById('jndiContextClass').value;
//    var jndiUserName = document.getElementById('userName').value;
//    var password = document.getElementById('password').value;
//    var confirmPassword = document.getElementById('confirmPassword').value;
//
//    if (checkForExistingAppServerConfigurations(providerURL)) {
//        if (serverType == null || wso2.wsf.Util.trim(serverType) == "") {
//            wso2.wsf.Util.alertWarning("Please select an Application Server type.");
//            return false;
//        }
//
//        if (providerURL == null || wso2.wsf.Util.trim(providerURL) == "") {
//            wso2.wsf.Util.alertWarning("Please enter a valid provider url.");
//            return false;
//        }
//        if (jndiContextClass == null || wso2.wsf.Util.trim(jndiContextClass) == "") {
//            wso2.wsf.Util.alertWarning("Please enter a valid JNDI Context class.");
//            return false;
//        }
//        if (password != null && wso2.wsf.Util.trim(password) != "") {
//            if (jndiUserName == null || wso2.wsf.Util.trim(jndiUserName) == "") {
//                wso2.wsf.Util.alertWarning("Please enter username for the password provided.");
//                return false;
//            }
//            if (password != confirmPassword) {
//                wso2.wsf.Util.alertWarning("Password and re-entered password do not match.");
//                return false;
//            }
//        }
//
//        var ejbAppServer_xml = '';
//        ejbAppServer_xml +=
//        ' <req:providerUrl>' + providerURL + '</req:providerUrl>\n';
//        ejbAppServer_xml +=
//        ' <req:jndiContextClass>' + jndiContextClass +
//        '</req:jndiContextClass>\n';
//        ejbAppServer_xml +=
//        ' <req:userName>' + jndiUserName + '</req:userName>\n';
//        ejbAppServer_xml +=
//        ' <req:password>' + password + '</req:password>\n';
//        ejbAppServer_xml +=
//        ' <req:appServerType>' + serverType + '</req:appServerType>\n';
//
//        /*var body_xml = '<req:addApplicationServerRequest xmlns:req="http://org.apache.axis2/xsd">\n' +
//                       ejbAppServer_xml +
//                    ' </req:addApplicationServerRequest>\n';
//
//          var callURL = serverURL + "/" + "EJBProviderAdmin" + "/" ;*/
//        var callURL =
//                new wso2.wsf.WSRequest(callURL, "addApplicationServer", body_xml, ejbProviderConfigCallback);
//    }
//}
//
//
//
////function addApplicationServerElement() {
////    var combo = document.getElementById('existingAppServerConfigurations');
////    if (combo.length == 0) {
////        wso2.wsf.Util.alertWarning("No existing configurations found.Please add Application " +
////                                   "Server before continuing.");
////        return false;
////    }
////
////	//This is the first reference to this variable.
////    //Reinitialize
////    //    ejbProviderConfigObject = '';
////    var obj = document.getElementById('existingAppServerConfigurations');
//////    ejbProviderConfigObject +=
////    //    ' <req:providerUrl>' + obj[obj.selectedIndex].value + '</req:providerUrl>\n';
////
////    showEJBServiceWizardStep1(obj[obj.selectedIndex].value);
////}
//
//function addEJBDetailsElement() {
//    //var classes = document.getElementsByName("chkClasses");
//    var selectedClasses = '';
//    //var isClassSelected = false;
//    //for (var i = 0; i < classes.length; i++) {
//    //    if (classes[i].checked) {
//    //        isClassSelected = true;
//    //        selectedClasses += ' <req:serviceClasses>' + classes[i].value + '</req:serviceClasses>\n';
//    //    }
//    //}
//
//    var remoteInterfaceClass = '';
//    var homeInterfaceClass = '';
//    var beanJNDIName = document.getElementById('beanJNDIName').value;
//
//    var remoteInterfaces = document.getElementsByName("chkRemoteInterface");
//    for (var a = 0; a < remoteInterfaces.length; a++) {
//        if (remoteInterfaces[a].checked) {
//            remoteInterfaceClass = remoteInterfaces[a].value;
//        }
//    }
//    selectedClasses += ' <req:serviceClasses>' + remoteInterfaceClass + '</req:serviceClasses>\n';
//
//    var homeInterfaces = document.getElementsByName("chkHomeInterface");
//    for (var a = 0; a < homeInterfaces.length; a++) {
//        if (homeInterfaces[a].checked) {
//            homeInterfaceClass = homeInterfaces[a].value;
//        }
//    }
//
//	//validation
//    if (homeInterfaceClass == null || wso2.wsf.Util.trim(homeInterfaceClass) == "") {
//        wso2.wsf.Util.alertWarning("Please enter Home Interface class.");
//        return false;
//    }
//    if (remoteInterfaceClass == null || wso2.wsf.Util.trim(remoteInterfaceClass) == "") {
//        wso2.wsf.Util.alertWarning("Please enter Remote Interface class.");
//        return false;
//    }
//    if (beanJNDIName == null || wso2.wsf.Util.trim(beanJNDIName) == "") {
//        wso2.wsf.Util.alertWarning("Please enter JNDI name of EJB.");
//        return false;
//    }
//
//	//selected classes should come first
//    ejbProviderConfigObject = selectedClasses + ejbProviderConfigObject;
//    ejbProviderConfigObject +=
//    ' <req:beanJNDIName>' + beanJNDIName + '</req:beanJNDIName>\n';
//    ejbProviderConfigObject += ' <req:homeInterface>' + document.getElementById('homeInterface').value +
//                               '</req:homeInterface>\n';
//    ejbProviderConfigObject +=
//    ' <req:remoteInterface>' + document.getElementById('remoteInterface').value +
//    '</req:remoteInterface>\n';
//
//    generateAARForSelectedEJBRemoteInterface();
//    //showEJBServiceWizardStep3();
//}
//
////function addServiceGroupDetailsElementAndDeploy(){
////    ejbProviderConfigObject += ' <req:serviceGroupId>' + document.getElementById('serviceGroupId').value +
////                        '</req:serviceGroupId>\n';
////    ejbProviderConfigObject +=
////    ' <req:addNewServiceGroup>' + document.getElementById('addNewServiceGroup').value +
////    '</req:addNewServiceGroup>\n';
////    generateAARForSelectedEJBRemoteInterface();
////}
//
//
//
////functions for ejb provider archive reader
//function generateAARForSelectedEJBRemoteInterface() {
//    var body_xml = '<req:createAndDeployEJBServiceRequest xmlns:req="http://org.apache.axis2/xsd">\n' +
//                   '<req:archiveId>' + genFileKey + '</req:archiveId>\n' +
//                   ejbProviderConfigObject +
//                   '</req:createAndDeployEJBServiceRequest>\n';
//
//    var callURL = serverURL + "/" + "ServiceAdmin" ;
//    new wso2.wsf.WSRequest(callURL, "createAndDeployEJBService", body_xml, completeAARGenerationCB);
//}
//
//
//function deleteEJBConfiguration(beanJNDIName, jnpProviderUrl) {
//    beanJNDIName = beanJNDIName.replace(/\xA7/g, "'");
//    jnpProviderUrl = jnpProviderUrl.replace(/\xA7/g, "'");
//
//    var deleteIt = confirm("Do you really want to delete EJB configuration for " + beanJNDIName + "@" + jnpProviderUrl + "?");
//    if (deleteIt) {
//        var body_xml = '<req:deleteEJBConfigurationRequest xmlns:req="http://org.apache.axis2/xsd">\n' +
//                       '<req:beanJNDIName>' + beanJNDIName + '</req:beanJNDIName>\n' +
//                       '<req:jnpProviderUrl>' + jnpProviderUrl + '</req:jnpProviderUrl>\n' +
//                       '</req:deleteEJBConfigurationRequest>\n';
//
//        var xsltFileName = "ejb_provider_service.xsl";
//
//        var callURL = serverURL + "/" + "EJBProviderAdmin" + "/" ;
//        new wso2.wsf.WSRequest(callURL, "deleteEJBConfiguration", body_xml, ejbProviderConfigCallback);
//    }
//}
//
//
//function ejbProviderConfigCallback() {
//    ejbProviderConfig();
//}
//
//
//// loading service groups for combo
//function loadServiceGroupsComboBox() {
//    var body_xml = '<ns1:listServiceGroups  xmlns:ns1="http://org.apache.axis2/xsd">\n' +
//                   ' </ns1:listServiceGroups>\n';
//
//    var callURL = serverURL + "/" + SERVICE_GROUP_ADMIN_STRING + "/" + "listServiceGroups";
//    new wso2.wsf.WSRequest(callURL, "listServiceGroups", body_xml, loadServiceGroupListCallback);
//
//}
//
//
//
////load service group list callback.
//function loadServiceGroupListCallback() {
//    var comboToLoad = document.getElementById('serviceGroupSelectBox');
//    var data = this.req.responseXML;
//    var rets = data.getElementsByTagName("return");
//    var len = rets.length;
//    var count;
//
//    for (count = 0; count < len; count++) {
//        comboToLoad.options[count] = new Option(rets[count].getElementsByTagName("group_id").item(0).firstChild.nodeValue);
//        comboToLoad.options[count].value = rets[count].getElementsByTagName("group_id").item(0).firstChild.nodeValue;
//    }
//    comboToLoad.options[count] = new Option('');
//    comboToLoad.options[count].value = '';
//    comboToLoad.selectedIndex = count;
//}
//
//
//function loadAppServersComboBox() {
//    var body_xml = '<ns1:loadAppServersComboBox  xmlns:ns1="http://org.apache.axis2/xsd">\n' +
//                   ' </ns1:loadAppServersComboBox>\n';
//
//    var callURL = serverURL + "/" + "EJBProviderAdmin" + "/" ;
//    new wso2.wsf.WSRequest(callURL, "getAppServerNameList", body_xml, loadAppServerListCallback);
//}
//
//function loadExistingAppServerConfigComboBox() {
//    var body_xml = '<ns1:getEJBAppServerConfigurationsRequest  xmlns:ns1="http://org.apache.axis2/xsd">\n' +
//                   ' </ns1:getEJBAppServerConfigurationsRequest>\n';
//
//    var callURL = serverURL + "/" + "EJBProviderAdmin" + "/" ;
//    //new wso2.wsf.WSRequest(callURL,"getEJBAppServerConfigurations",body_xml,loadAppServerConfigurationListCallback);
//    new wso2.wsf.WSRequest(callURL, "getEJBAppServerConfigurations", body_xml, loadAppServerConfigurationListCallback);
//}
//
//
//function loadAppServerConfigurationListCallback() {
//    var comboToLoad = document.getElementById('existingAppServerConfigurations');
//    var data = this.req.responseXML;
//    var rets = data.getElementsByTagName("return");
//    var len = rets.length;
//    var count;
//    for (count = 0; count < len; count++) {
//        comboToLoad.options[count] = new Option(rets[count].getElementsByTagName("appServerType").item(0).firstChild.nodeValue + ' - ' +
//                                                rets[count].getElementsByTagName("providerURL").item(0).firstChild.nodeValue);
//
//        comboToLoad.options[count].value = rets[count].getElementsByTagName("providerURL").item(0).firstChild.nodeValue;
//    }
//    existingAppServerConfigs = comboToLoad.length;
//    ejbProviderStep1DisableFields();
//}
//
//
//function fileUpload() {
//
//}

//load ejb application server list callback.
//function loadAppServerListCallback() {
//    var comboToLoad = document.getElementById('serverType');
//    var data = this.req.responseXML;
//    var rets = data.getElementsByTagName("return");
//    var len = rets.length;
//    var count;
//
//    for (count = 0; count < len; count++) {
//        comboToLoad.options[count] = new Option(rets[count].getElementsByTagName("serverName").item(0).firstChild.nodeValue);
//        comboToLoad.options[count].value = rets[count].getElementsByTagName("serverId").item(0).firstChild.nodeValue;
//    }
//    comboToLoad.options[count] = new Option('--Application Server--');
//    comboToLoad.options[count].value = '';
//    comboToLoad.selectedIndex = count;
//}


//function setServiceGroupId(obj, document) {
//    var serviceGroupId = '';
//    if (obj.name == 'serviceGroupSelectBox') {
//        if (obj[obj.selectedIndex].value != '') {
//            serviceGroupId = obj[obj.selectedIndex].value;
//        }
//    } else if (obj.name == 'newServiceGroupId') {
//        serviceGroupId = obj.value;
//        document.getElementById('serviceGroupSelectBox').value = '';
//        document.getElementById('addNewServiceGroup').value = 'true';
//    }
//    document.getElementById('serviceGroupId').value = serviceGroupId;
//}
//
//
//function ejbProviderConfig() {
//    var body_xml = '<req:getEJBConfigurationsRequest xmlns:req="http://org.apache.axis2/xsd">\n' +
//                   ' </req:getEJBConfigurationsRequest>\n';
//
//    var callURL = serverURL + "/" + "EJBProviderAdmin" + "/" ;
//    new wso2.wsf.WSRequest(callURL, "getEJBConfigurations", body_xml, function() {
//        wso2.wsf.Util.callbackhelper(this.req.responseXML, "ejb_provider_service.xsl", document.getElementById("divEJBProvider"));
//    });
//}

//function showEJBServiceWizardStep1(providerUrl) {
//    /* var tmpTransformationNode;
//    if (window.XMLHttpRequest && !wso2.wsf.Util.isIE()) {
//        tmpTransformationNode =
//        document.implementation.createDocument("", "ejbDetailsTemplate", null);
//    } else if (window.ActiveXObject) {
//        tmpTransformationNode = new ActiveXObject("Microsoft.XmlDom");
//        var sXml = "<ejbDetailsTemplate></ejbDetailsTemplate>";
//        tmpTransformationNode.loadXML(sXml);
//    }
//    var objDiv = document.getElementById("divEJBProvider_step1");
//    wso2.wsf.Util.processXML(tmpTransformationNode, "ejb_provider_wizard_step1.xsl", objDiv);
//    wso2.wsf.Util.showOnlyOneMain(objDiv);*/
//
//    var obj = document.getElementById('divEJBProvider_step1');
//    location.href = 'ejb_provider_wizard_step1.jsp?providerUrl=' + providerUrl;
//   // obj.innerHTML = '<br><b><i><a href="ejb_provider_wizard_step1.jsp">Faulty Services</a></i><b>';
//    // var data = this.req.responseXML;
//    // var intValue = data.getElementsByTagName("return")[0].firstChild.nodeValue;
//
//    /*if (intValue != '0') {
//        obj.innerHTML = '<br><b><i><a href="ejb_provider_wizard_step1.jsp">('+intValue+') Faulty Services</a></i><b>';
//    }else{
//        obj.innerHTML = '';
//    }
//*/
//}


//function showEJBServiceWizardStep2() {
//    var tmpTransformationNode;
//    if (window.XMLHttpRequest && !wso2.wsf.Util.isIE()) {
//        tmpTransformationNode =
//        document.implementation.createDocument("", "ejbDetailsTemplate", null);
//    } else if (window.ActiveXObject) {
//        tmpTransformationNode = new ActiveXObject("Microsoft.XmlDom");
//        var sXml = "<ejbDetailsTemplate></ejbDetailsTemplate>";
//        tmpTransformationNode.loadXML(sXml);
//    }
//    var objDiv = document.getElementById("divEJBProvider_step2");
//    wso2.wsf.Util.processXML(tmpTransformationNode, "ejb_provider_wizard_step2.xsl", objDiv);
//    wso2.wsf.Util.showOnlyOneMain(objDiv);
//}
//
//function showEJBServiceWizardStep3() {
//    var tmpTransformationNode;
//    if (window.XMLHttpRequest && !wso2.wsf.Util.isIE()) {
//        tmpTransformationNode =
//        document.implementation.createDocument("", "serviceGroupTemplate", null);
//    } else if (window.ActiveXObject) {
//        tmpTransformationNode = new ActiveXObject("Microsoft.XmlDom");
//        var sXml = "<serviceGroupTemplate></serviceGroupTemplate>";
//        tmpTransformationNode.loadXML(sXml);
//    }
//    var objDiv = document.getElementById("divEJBProvider_step3");
//    wso2.wsf.Util.processXML(tmpTransformationNode, "ejb_provider_wizard_step3.xsl", objDiv);
//    wso2.wsf.Util.showOnlyOneMain(objDiv);
//}










//function getEJBRemoteInterfaceClassListFromArchive(archiveId) {
//    var genFileKey = archiveId;
//
////    var dynamicServiceObj = document.getElementById('divDynamicService');
//    //    wso2.wsf.Util.showOnlyOneMain(dynamicServiceObj);
//
//    var body_xml = '<req:getClassNamesRequest xmlns:req="http://org.apache.axis2/xsd">\n' +
//                   ' <req:archiveId>' + genFileKey + '</req:archiveId>\n' +
//                   ' </req:getClassNamesRequest>\n';
//
//    var callURL = serverURL + "/" + "EJBProviderAdmin" ;
//    new wso2.wsf.WSRequest(callURL, "getClassNames", body_xml, function() {
//        var xslAbsPath = "ejb_provider_wizard_step2.xsl";
//        wso2.wsf.Util.callbackhelper(this.req.responseXML, xslAbsPath, document.getElementById("divEJBProvider_step2"));
//
//    });
//}

function processServiceOperationParameterConfigUpdateEJB(arrayOfNames, arrayOfValues) {
    processMultipleServiceOperationParameterConfigEntryEJB(lastUsedServiceId, arrayOfNames, arrayOfValues);
}

function processMultipleServiceOperationParameterConfigEntryEJB(serviceId,
                                                                parameterNamesObj,
                                                                parameterValueObj) {
    var paramElements = '';
    for (var i = 0; i < parameterNamesObj.length; i++) {
        paramElements += '<parameter locked="false" name="' + parameterNamesObj[i] + '"><![CDATA[' +
                         parameterValueObj[i] + ']]></parameter>\n';
    }
    var body_xml = '<ns1:setServiceParameters xmlns:ns1="http://org.apache.axis2/xsd">' +
                   '<ns1:serviceId>' + serviceId + '</ns1:serviceId>' +
                   '<ns1:serviceVersion>' + serviceId + '</ns1:serviceVersion>' +
                   paramElements +
                   '</ns1:setServiceParameters>';

    var callURL = serverURL + "/" + "ServiceAdmin";

    new wso2.wsf.WSRequest(callURL, "setServiceParameters", body_xml, processMultipleServiceOperationParameterConfigEntryEJBCallback);


}

function getServiceSpecficParametersForConfig(serviceName, serviceType) {
    /*stopping the refreshing first */
    //    stoppingRefreshingMethodsHook();

    //    if (serviceName == null) {
    //        serviceName = lastUsedServiceId;
    //    } else {
    //        lastUsedServiceId = serviceName;
    //        serviceSpecificName = serviceName;
    //    }
    //    var body_xml = '<req:getDeclaredServiceParametersRequest xmlns:req="http://org.apache.axis2/xsd">\n' +
    //                   ' <req:serviceId>' + serviceName + '</req:serviceId>\n' +
    //                   ' <req:serviceVersion>' + serviceName + '</req:serviceVersion>\n' +
    //                   ' </req:getDeclaredServiceParametersRequest>\n';
    //
    //    var callURL = serverURL + "/" + "ServiceAdmin" ;
    //
    //    if (serviceType != 'ejb_service' || serviceType == null) {
    //        new wso2.wsf.WSRequest(callURL, "getDeclaredServiceParameters", body_xml, getServiceSpecficParametersForConfigCallback);
    //    } else {
    //        new wso2.wsf.WSRequest(callURL, "getDeclaredServiceParameters", body_xml, getServiceSpecficParametersForEJBConfigCallback);
    //    }
}

//Used javascript functions

