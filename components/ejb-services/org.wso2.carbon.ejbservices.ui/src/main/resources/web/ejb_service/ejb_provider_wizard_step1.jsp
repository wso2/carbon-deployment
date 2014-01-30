<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="js/ejbservices.js"></script>

<%
    String providerUrl = request.getParameter("providerUrl");
    session.setAttribute("providerUrl", providerUrl);
%>

<fmt:bundle basename="org.wso2.carbon.ejbservices.ui.i18n.Resources">
<carbon:breadcrumb label="upload.ejb.interfaces"
		resourceBundle="org.wso2.carbon.ejbservices.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

    <script type="text/javascript">

        function cancel() {
            location.href = 'index.jsp';
        }

        function validateFileUpload() {
            var jarinput = document.serviceUpload.filename.value;
            if (jarinput == '') {
                CARBON.showErrorDialog('<fmt:message key="illegal.file.type"/>');
            } else if (jarinput.lastIndexOf(".jar") == -1 && jarinput.lastIndexOf(".zip") == -1) {
                CARBON.showErrorDialog('<fmt:message key="illegal.file.type"/>');
            } else {
                document.serviceUpload.submit();
            }
        }
    </script>

    <div id="middle">
        <h2><fmt:message key="create.new.ejb.service.step1"/></h2>

        <div id="workArea">
            <form method="post" name="serviceUpload" action="../../fileupload/ejbinterface"
                      enctype="multipart/form-data" target="_self">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="upload.ejb.interfaces"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <table class="normal">
                                <tr>
                                    <td>
                                        <label><fmt:message key="path.to.archive"/> :</label>
                                    </td>
                                    <td>
                                        <input type="file" id="filename"
                                                                   name="filename" size="50"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input name="back_1" type="button" value=" &lt; <fmt:message key="back"/>"
                                   onclick="javascript:history.back();return false;"/>
                            <input name="upload" type="button" value=" <fmt:message key="next"/> &gt; "
                                   onclick="validateFileUpload();"/>
                            <input type="button" onClick="cancel()" value="<fmt:message key="cancel"/>"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>

</fmt:bundle>