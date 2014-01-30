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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.repomanager.axis2.ui.i18n.Resources">
<carbon:breadcrumb label="axis2.repo.management"
		resourceBundle="org.wso2.carbon.repomanager.axis2.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />

<%@ page import="org.wso2.carbon.repomanager.axis2.ui.Axis2RepoManagerClient" %>
<%@ page import="org.wso2.carbon.repomanager.axis2.ui.Axis2RepoManagerUIConstants" %>
<%@ page import="java.io.File"%>

<!-- Default CSS containing the style details for a generic treeview -->
<link type="text/css" rel="stylesheet" href="../yui/build/treeview/assets/treeview-2.8.1.css">
<!-- Required CSS to display the folder structure styles-->
<link type="text/css" rel="stylesheet" href="../yui/build/treeview/assets/folder-styles.css">

<style type="text/css">

.file td.ygtvln { background: url(../yui/build/treeview/assets/images/ln.gif) 0 0 no-repeat; cursor:default;}
.file td.ygtvtn { background: url(../yui/build/treeview/assets/images/tn.gif) 0 0 no-repeat; cursor:default}
.folder td.ygtvln { background: url(../yui/build/treeview/assets/images/lm.gif) 0 0 no-repeat; width:34px; }
.folder td.ygtvtn { background: url(../yui/build/treeview/assets/images/tm.gif) 0 0 no-repeat; width:34px; }
.ygtvfocus{ background-color:#ffffff; border:none;}
.addArtifact { background-color:#EEEEEE; background-image:url("images/add.gif"); background-position:5px 5px;
    background-repeat:no-repeat; border:1px solid #CCCCCC;color:#2F7ABD; cursor:pointer; display:block; float:right;
    margin-bottom:2px; padding:3px 5px 3px 20px; }
.ygtvlabel:hover { margin-left:2px; text-decoration:none; cursor:default; }
    
</style>

<!-- TreeView source file -->
<script src="../yui/build/treeview/treeview-2.8.1.js" type="text/javascript"></script>

<%
    String cookie = (String)session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);
    Axis2RepoManagerClient axis2repoManagerClient = new Axis2RepoManagerClient(cookie, config, session);

    String repo = axis2repoManagerClient.getDirs().getDirectoryStructureJSON();
    boolean requireRestart = axis2repoManagerClient.getDirs().getReloadRequired();

    final String repositoryRoot = "repository";

    boolean requireReloadOnUpload = false;
    if (session.getAttribute(Axis2RepoManagerUIConstants.UPLOAD_STATUS) != null) {
        requireReloadOnUpload = ((String)session.getAttribute(Axis2RepoManagerUIConstants.UPLOAD_STATUS)).equals("successful");
        session.removeAttribute(Axis2RepoManagerUIConstants.UPLOAD_STATUS);
    }
    
    // Handle escape character for Windows OS. 
    String fileSeparator = File.separator.equals("\\")  ? "\\\\" : File.separator;

%>

<script type="text/javascript">
    // Variable that decides whether the div containing the restart prompt should be displayed or not.
    // The JS variable is used to ensure that the div is correctly displayed while displaying the
    // message after  uploading a dependency.  
    var requireRestart_JS = <%= requireRestart%>;
    var reloadOnUpload_JS = <%= requireReloadOnUpload%>;
    
    // Prompt warning when Axis2 config requires restarting due to a change in dependencies (.jar)
    if(reloadOnUpload_JS) {
        requireRestart_JS = false;
        CARBON.showConfirmationDialog('<fmt:message key="upload.success.msg"/>', function() {
            restartServer();
            reloadOnUpload_JS = false;
        }, function() {
            document.getElementById("msgDiv").style.display = "block";
        })
    }

    // This is the JSON object returned from the BE. NOTE: Don't put this within quotes!
    var treeData = <%= repo%>;

</script>

<div id="middle">
    <h2><fmt:message key="axis2.repo.mgt.title"/></h2>

    <div id="workArea" style="min-height: 20em;">
        <div id="msgDiv" style="background-color:#CFDBEF; height:20px; margin-bottom:10px; padding:12px 0 10px 0; display: none;">
            <label>&nbsp;<fmt:message key="reload.msg.one"/> <a style="text-decoration:underline; cursor: pointer;" onclick="restartServer();"><fmt:message key="here"/></a> <fmt:message key="reload.msg.two"/></label>
        </div>

        <label>
            &nbsp;<b><fmt:message key="upload.shared.dependency.msg"/></b>
        </label>
        <br/><br/>

        <form name="dirTreeForm" action="tree-manipulator.jsp" method="post">

            <div class="yui-skin-sam">
                <div id="treeDiv"></div>
            </div>

            <input type="hidden" name="<%= Axis2RepoManagerUIConstants.SELECTED_DIR_PATH%>" />
            <input type="hidden" name="<%= Axis2RepoManagerUIConstants.DIR_NAME%>" />
            <input type="hidden" name="<%= Axis2RepoManagerUIConstants.SERVICE_TYPE%>" />

        </form>

    </div>
</div>

<script type="text/javascript">


    if(requireRestart_JS) {
        document.getElementById("msgDiv").style.display = "block";
    }

    var tree;

    function treeInit() {
        //instantiate the TreeView control:
        tree = new YAHOO.widget.TreeView("treeDiv");

        //get a reference to the root node;
        var rootNode = tree.getRoot();
                
        if(treeData == null) {
            treeData = <%=repo%>;
        }
        rootNodeName = treeData.dirname;
        drawTreeStructure(treeData, rootNode);

        tree.subscribe("clickEvent", function (oArgs) {
            var node = oArgs.node, event = oArgs.event,
            target = YAHOO.util.Event.getTarget(event);
            if (node.className == "file" && target.nodeName == "A") {
                if (target.title=="Delete artifact") {
                    if(node.depth == 2) {
                        deleteLib(node, "lib" + '<%=fileSeparator%>' + target.id);
                    } else if (node.depth == 3) {
                        deleteLib(node, node.parent.parent.label + '<%=fileSeparator%>' + "lib" + '<%=fileSeparator%>' +target.id);
                    }
                } else if (target.title=="Download artifact") {
                    if (node.depth == 2) {
                        downloadArtifact("lib" + '<%=fileSeparator%>' + target.id, target.id);
                    } else if (node.depth == 3) {
                        downloadArtifact(node.parent.parent.label + '<%=fileSeparator%>' + "lib" + '<%=fileSeparator%>' + target.id, target.id);
                    }
                }
            } else if (node.className == "folder" && target.className == "addArtifact") {
                if (node.depth == 1) {
                    document.forms["dirTreeForm"].elements["<%= Axis2RepoManagerUIConstants.DIR_NAME%>"].value = node.parent.label;
                    document.forms["dirTreeForm"].elements["<%= Axis2RepoManagerUIConstants.SELECTED_DIR_PATH%>"].value = '<%=fileSeparator%>' + node.parent.label + '<%=fileSeparator%>' + "lib";
                    document.forms["dirTreeForm"].elements["<%= Axis2RepoManagerUIConstants.SERVICE_TYPE%>"].value = target.id;
                    document.forms["dirTreeForm"].submit();
                }if (node.depth == 2) {
                    document.forms["dirTreeForm"].elements["<%= Axis2RepoManagerUIConstants.DIR_NAME%>"].value = node.parent.label;
                    document.forms["dirTreeForm"].elements["<%= Axis2RepoManagerUIConstants.SELECTED_DIR_PATH%>"].value = '<%=fileSeparator%>' + node.parent.parent.label + '<%=fileSeparator%>' + node.parent.label + '<%=fileSeparator%>' + "lib";
                    document.forms["dirTreeForm"].elements["<%= Axis2RepoManagerUIConstants.SERVICE_TYPE%>"].value = target.id;
                    document.forms["dirTreeForm"].submit();
                }
            }
            
            // Prevent default clickEvent functionality
            return false;
        });

        //draw the tree structure that has been created dynamically
        tree.draw();


    }

    function restartServer() {
        CARBON.showConfirmationDialog('<fmt:message key="restart.confirmation"/>', function() {
            $.post("restart-axis2-server-ajaxprocessor.jsp",
                   function(restartResponse) {
                       if (restartResponse) {
                           document.getElementById("msgDiv").style.display = "none";
                           CARBON.showInfoDialog('<fmt:message key="restart.success"/>');
                       } else {
                           CARBON.showErrorDialog('<fmt:message key="failed.restart"/>');
                       }
                   });
        })
    }

    function deleteLib(node, path) {
        CARBON.showConfirmationDialog('<fmt:message key="deletion.confirmation"/><b>' + " " + path + "</b> <fmt:message key='permanently'/>", function() {
            $.post("delete-lib-ajaxprocessor.jsp", {deleteLibPath: path},
                   function(deleteResponse) {
                       deleteResponse = deleteResponse.replace(/^\s+|\s+$/g, '');
                       if(deleteResponse == "true") {
                           <%--document.getElementById("msgDiv").style.display = "block";--%>
                           <%--CARBON.showInfoDialog('<fmt:message key="delete.success"/>');--%>
                           tree.removeNode(node, true);
                           // If deletion succeed prompt to restart Axis2 Config
                           CARBON.showConfirmationDialog('<fmt:message key="delete.success"/>', function() {
                               restartServer();
                           }, function() {
                               document.getElementById("msgDiv").style.display = "block"                                                             
                           })

                       } else {
                            CARBON.showErrorDialog('<fmt:message key="failed.to.delete.artifact"/>');
                       }
                   });
        })
    }

    function drawTreeStructure(treeData, rootNode) {
        if(treeData.dirname == "lib") {
            if(rootNode.label == "axis2services" ) {
                var currentDir = new YAHOO.widget.HTMLNode("<span class=\"ygtvlabel\" style='cursor:default;'>" + treeData.dirname + "</span>&nbsp;&nbsp;<a title='<fmt:message key="add.dependencies"/>' class='addArtifact' id='Axis2 services'><fmt:message key="add.dependencies"/></a>", rootNode, true, true);
                currentDir.className = "folder";
            } else if (rootNode.label == "axis2modules") {
                var currentDir = new YAHOO.widget.HTMLNode("<span class=\"ygtvlabel\" style='cursor:default;'>" + treeData.dirname + "</span>&nbsp;&nbsp;<a title='<fmt:message key="add.dependencies"/>' class='addArtifact' id='Axis2 modules'><fmt:message key="add.dependencies"/></a>", rootNode, true, true);
                currentDir.className = "folder";
            } else if (rootNode.label == "servicejars") {
                var currentDir = new YAHOO.widget.HTMLNode("<span class=\"ygtvlabel\" style='cursor:default;'>" + treeData.dirname + "</span>&nbsp;&nbsp;<a title='<fmt:message key="add.dependencies"/>' class='addArtifact' id='Jar services'><fmt:message key="add.dependencies"/></a>", rootNode, true, true);
                currentDir.className = "folder";
            } else if (rootNode.label == "server") {
                var currentDir = new YAHOO.widget.HTMLNode("<span class=\"ygtvlabel\" style='cursor:default;'>" + treeData.dirname + "</span>&nbsp;&nbsp;<a title='<fmt:message key="add.dependencies"/>' class='addArtifact' id='Jar services'><fmt:message key="add.dependencies"/></a>", rootNode, true, true);
                currentDir.className = "folder";
            }
        } else {
            if(rootNode == tree.root) {
                var currentDir = new YAHOO.widget.TextNode("server", rootNode, true);
                currentDir.className = "folder";
            } else {
                var currentDir = new YAHOO.widget.TextNode(treeData.dirname, rootNode, true);
                currentDir.className = "folder";
            }
        }


        for(var i=0; i<treeData.dirs.length; i++) {
            drawTreeStructure(treeData.dirs[i], currentDir);
        }

        for(var j=0; j<treeData.filelist.length; j++) {
            var tempFile = new YAHOO.widget.HTMLNode("<span class=\"ygtvlabel\" style='cursor:default;'>" + treeData.filelist[j].filename + "</span>&nbsp;&nbsp;<a title='Delete artifact' style='background-image:url(images/delete.gif); cursor: pointer; background-repeat: no-repeat; padding-left: 20px; padding-bottom: 3px;' id=" + treeData.filelist[j].filename + "></a>&nbsp;<a title='Download artifact' target='_self' style='background-image:url(images/download.gif); cursor: pointer; background-repeat: no-repeat; padding-left: 20px; padding-bottom: 3px;' id=" + treeData.filelist[j].filename + "></a>", currentDir, false, true);
            tempFile.className = "file";
        }
    }

    function downloadArtifact(filepath, filename) {
        location.href = "download-ajaxprocessor.jsp?filepath=" + filepath + "&filename=" + filename; 
    }

    YAHOO.util.Event.onDOMReady(treeInit);

</script>

</fmt:bundle>