<%@ taglib prefix="s" uri="/struts-tags"%>
<s:form theme="simple">
	<div><s:if test="tableName != null">
		<a
			href='./dbErAction!displayAllTables.action?connectionName=<s:property value="connectionName" />'>
		<img src="./images/back.jpg" border="0" height="24" width="24">
		</a>
	</s:if> <s:else>
		<a href='./connectionAction!displayConnections.action'> <img
			src="./images/back.jpg" border="0" height="24" width="24"> </a>
	</s:else></div>
	<h3>Auto Updated Tables for connection <s:property
		value="connectionName" />&nbsp;<s:property value="tableName" /></h3>
	<div>
	<table border="1" width="60%" cellspacing="0" cellpadding="1">
		<tr>
			<th>Table</th>

		</tr>
		<s:iterator value="autoUpdateableReferences">
			<tr>
				<td><s:property value="top" /> &nbsp;</td>
			</tr>
		</s:iterator>
	</table>
	</div>
</s:form>

