<%@ taglib prefix="s" uri="/struts-tags"%>
<s:form theme="simple">
	<h3>Available Definitions !</h3>
	<table>
		<tr>
			<th>Name</th>
			<th>User</th>
			<th>DB Definition</th>
			<th>OJB Definition</th>
			<th>OJB Errors</th>
			<th>DDL</th>
			<th>Hive DDL</th>
			<th>OJB DDL</th>
			<th>Drop Schema DDL</th>
		</tr>
		<s:iterator value="connections">
			<tr>
				<td class="<s:property value="highlight" />"><s:property
						value="name" /></td>
				<td class="<s:property value="highlight" />"><s:property
						value="userId" /></td>
				<td align="center"><a
					href='./erAction!downloadDBRelationships.action?connectionName=<s:property value="name" />'><img
						src="./images/download.jpg" border="0" height="20" width="24"></a></td>
				<td align="center"><s:a
						href="./erAction!downloadOJBRelationships.action?connectionName=%{name}">
						<img src="./images/download.jpg" border="0" height="20" width="24">
					</s:a></td>
				<td align="center"><s:a
						href="./erAction!ojbMappingErrors.action?connectionName=%{name}">
						<img src="./images/download.jpg" border="0" height="20" width="24">
					</s:a></td>
				<td align="center"><s:a
						href="./erAction!downloadDDL.action?connectionName=%{name}">
						<img src="./images/download.jpg" border="0" height="20" width="24">
					</s:a></td>
				<td align="center"><s:a
						href="./erAction!downloadHiveETLTableDDL.action?connectionName=%{name}">
						<img src="./images/download.jpg" border="0" height="20" width="24">
					</s:a></td>
				<td align="center"><s:a
						href="./erAction!downloadOJBDDL.action?connectionName=%{name}">
						<img src="./images/download.jpg" border="0" height="20" width="24">
					</s:a></td>
				<td align="center"><s:a
						href="./erAction!dropSchemaDDL.action?connectionName=%{name}">
						<img src="./images/download.jpg" border="0" height="20" width="24">
					</s:a></td>
			</tr>
		</s:iterator>
	</table>
</s:form>
