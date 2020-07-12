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
	<h3>Dependency Map for connection <s:property
		value="connectionName" />&nbsp;<s:property value="tableName" /></h3>
	<s:set name="levelVal" value="0" />
	<div><s:iterator
		value="dependencyMap.allLevelDetails">
		<br>
		<table border="1" width="600" cellspacing="0" cellpadding="1">
			<tr>
				<th colspan="3" width="600">Level - <s:property
					value="top[0].pos" />&nbsp;</th>
			</tr>
			<s:iterator value="top">
				<tr>
					<td width="300"><s:property value="dependencyByTable" /></td>
					<td width="300"><s:property value="dependencyColsList" /></td>
					<td width="300"><s:property value="dependencyToTable" /></td>
				</tr>
			</s:iterator>
		</table>
	</s:iterator></div>
</s:form>
