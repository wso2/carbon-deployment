<%--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
--%>

<%@ page import="org.wso2.carbon.repomanager.axis2.ui.Axis2RepoManagerUIConstants" %>

<%
    if (request.getParameter(Axis2RepoManagerUIConstants.SELECTED_DIR_PATH) == null ||
            request.getParameter(Axis2RepoManagerUIConstants.DIR_NAME) == null ||
            request.getParameter(Axis2RepoManagerUIConstants.SERVICE_TYPE) == null) {
%>
    <script type="text/javascript">
        location.href = "index.jsp?region=region1&item=axis2_repo_mgt_menu";
    </script>
<%
    } else {
        session.setAttribute(Axis2RepoManagerUIConstants.SELECTED_DIR_PATH,
                             request.getParameter(Axis2RepoManagerUIConstants.SELECTED_DIR_PATH));
        session.setAttribute(Axis2RepoManagerUIConstants.DIR_NAME,
                             request.getParameter(Axis2RepoManagerUIConstants.DIR_NAME));
        session.setAttribute(Axis2RepoManagerUIConstants.SERVICE_TYPE,
                             request.getParameter(Axis2RepoManagerUIConstants.SERVICE_TYPE));

%>
        <script type="text/javascript">
            location.href = "upload.jsp?";
        </script>
<%
    }
%>