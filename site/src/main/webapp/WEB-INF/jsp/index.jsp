<!doctype html>
<%@page import="org.example.repository.JCRNodeManager"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@ page import="javax.jcr.Item" %>
<%@ include file="/WEB-INF/jspf/htmlTags.jspf" %>

<html lang="en">
<head>
  <meta charset="utf-8"/>
  <title>Assessment Solutions</title>
</head>
<body>
<h3>Content Node and SubNodes:</h3>
<%
List<Item> nodesList = (List)request.getAttribute("nodes");
List<Item> keywordNodesList = (List)request.getAttribute("keywordNodes");
for(Item item : nodesList){
%>
	<%=JCRNodeManager.generateLeftSpace(item)%>
	<%=item.getName() %>
	<%=item.isNode() ? "(Node)" : "(Property)" %> 
	- Path=<%=item.getPath() %><br/>
<%}
if(keywordNodesList!=null){
	String keyword = (String)request.getAttribute("keyword");
%>
<h3><%=keywordNodesList.size()%> Items Found for keyword=<%=keyword%>:</h3>
<%for(Item item : keywordNodesList){%>
	<%=item.getName() %>
	<%=item.isNode() ? "(Node)" : "(Property)" %> 
	- Path=<%=item.getPath() %><br/>
<%}
}%>
</body>
</html>