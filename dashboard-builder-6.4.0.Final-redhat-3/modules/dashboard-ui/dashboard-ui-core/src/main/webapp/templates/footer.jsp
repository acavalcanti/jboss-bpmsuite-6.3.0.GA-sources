<%--

    Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ page import="org.jboss.dashboard.LocaleManager" %>
<%@ page import="org.jboss.dashboard.ui.components.js.JSIncluder" %>
<%@ page import="org.jboss.dashboard.ui.UIServices" %>
<%@ taglib uri="http://dashboard.jboss.org/taglibs/i18n-1.0" prefix="i18n" %>
<%@ taglib uri="resources.tld" prefix="resource" %>
<%@ taglib uri="mvc_taglib.tld" prefix="mvc" %>
<%@ taglib uri="bui_taglib.tld" prefix="panel" %>
<%@ taglib uri="factory.tld" prefix="factory" %>
<%@ taglib prefix="static" uri="static-resources.tld" %>
<i18n:bundle baseName="org.jboss.dashboard.ui.messages" locale="<%=LocaleManager.currentLocale()%>"/>
<div id="ajaxLoadingDiv" style="position:absolute;position: absolute; left: 50%; top: 50%; z-index: 6000; opacity: 0.6; display: none;">
    <img  src="<static:image relativePath="general/loading.gif"/>" title="<i18n:message key="ui.admin.configuration.tree.loading"/>">
</div>
<%
    JSIncluder jsIncluder = UIServices.lookup().getJsIncluder();
    for (String jsFile : jsIncluder.getJsBottomFiles()) {
%>
    <script src='<mvc:context uri="<%= jsFile %>" />'></script>
<%
    }
%>
<script  language="Javascript" type="text/javascript">
<%
    for (String jspFile : jsIncluder.getJspBottomFiles()) {
%>
    <jsp:include page="<%= jspFile %>" flush="true"/>
<%
    }
%>
</script>