<%@ taglib prefix="s" uri="/struts-tags"%>
<s:form theme="simple">
	<div>
		<s:if test="tableName != null">
			<a href='./dbErAction!displayAllTables.action?connectionName=<s:property value="connectionName" />'> <img src="./images/back.jpg" border="0" height="24" width="24">
			</a>
		</s:if>
		<s:else>
			<a href='./connectionAction!displayConnections.action'> <img src="./images/back.jpg" border="0" height="24" width="24">
			</a>
		</s:else>
	</div>
	<h3>
		Ordered Tables for connection
		<s:property value="connectionName" />
		&nbsp;
		<s:property value="tableName" />
	</h3>
	<div class="span-8">
		<table>
			<tr>
				<th>Table &nbsp;<s:a href="./dbErAction!downloadOrderSql.action?connectionName=%{connectionName}">
						<img src="./images/download.jpg" border="0" height="20" width="24">
					</s:a></th>
			</tr>
			<s:iterator value="orderedTables">
				<tr>
					<td><s:property value="top" /></td>
				</tr>
			</s:iterator>
		</table>
	</div>
</s:form>

