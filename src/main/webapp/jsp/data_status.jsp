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
	<h3>Data Integrity Failures for connection <s:property
		value="connectionName" />&nbsp;<s:property value="tableName" /></h3>
	<div class="span-8">
	<table border="1" width="60%" cellspacing="0" cellpadding="1">
		<tr>
			<th>Table</th>
			<th>Status</th>
		</tr>
		<s:iterator value="dataStatus">
			<s:set name="statusVal" value="#dataStatus.value" />
			<tr>
				<s:if test="value == 'FAIL'">
					<td class="error"><s:property value="key" /></td>
					<td align="center" class="error"><a title="View Bad Records"
						href="./dbErAction!printDataIntegrityErrors.action?connectionName=<s:property value="connectionName" />&tableName=<s:property value="key" />">Details</a></td>
				</s:if>
			</tr>
		</s:iterator>
	</table>
	</div>
</s:form>

