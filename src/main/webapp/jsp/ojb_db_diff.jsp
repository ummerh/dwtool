<%@ taglib prefix="s" uri="/struts-tags"%>
<s:form theme="simple">
	<table border="0" width="100%" cellspacing="0" cellpadding="1">
		<tr>
			<td><a href='./erAction!displayMain.action'> <img
				src="./images/back.jpg" border="0" height="24" width="24"> </a></td>
		</tr>
	</table>
	<h3>Difference Map for connection <s:property
		value="connectionName" /></h3>
	<table>
		<tr>
			<td><s:iterator value="differenceMap.allTables">
				<s:property value="top" />
			</s:iterator></td>
		</tr>
	</table>
</s:form>
