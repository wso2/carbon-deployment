function generateTryit(serviceName, backendServerURL) {
    var resourcePath = getAppContext();

    var proxyAddress = getProxyAddress();

    var bodyXml = '<req:generateAJAXAppForExistingService xmlns:req="http://org.wso2.wsf/tools">' +
                  '<serviceName>' + serviceName + '</serviceName>' +
                  '<resourcePath>' + resourcePath + '</resourcePath>' +
                  '<httpURL>' + GURL + '</httpURL>\n' +
                  '<httpsURL>' + URL + '</httpsURL>\n' +
                  '</req:generateAJAXAppForExistingService>';

    var callURL = wso2.wsf.Util.getBackendServerURL(frontendURL, backendServerURL) + "GenericAJAXClient/generateAJAXAppForExistingService" ;
    wso2.wsf.Util.cursorWait();
    new wso2.wsf.WSRequest(callURL, "urn:generateAJAXAppForExistingService", bodyXml,
            generateTryitCallback, [], undefined, proxyAddress);
}

function generateTryitCallback() {
    var data = this.req.responseXML;
    var responseTextValue = getResponseValue(data);
    window.open(responseTextValue);
}

function getAppContext() {
    var urlSegments = document.location.href.split("/");
    return urlSegments[3];
}

function getResponseValue(responseXML) {
    var returnElementList = responseXML.getElementsByTagName("ns:return");
    // Older browsers might not recognize namespaces (e.g. FF2)
    if (returnElementList.length == 0)
        returnElementList = responseXML.getElementsByTagName("return");
    var returnElement = returnElementList[0];

    return returnElement.firstChild.nodeValue;
}

function htmlEncode(str){
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace('\n','').replace('\r','');
}
