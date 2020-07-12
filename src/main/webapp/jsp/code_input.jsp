<%@ taglib prefix="s" uri="/struts-tags"%>
<s:form action="codeGeneratorAction">
	<table border="0" width="100%" cellspacing="0" cellpadding="1">
		<tr>
			<td align="right"><a href='./dbErAction!displayAllTables.action?connectionName=<s:property value="connectionName" />'> <img src="./images/back.jpg" border="0" height="24" width="24">
			</a></td>
		</tr>
	</table>
	<h3>Code Generator</h3>
	<table border="0" cellspacing="0" cellpadding="1">
		<s:textfield name="connectionName" label="Connection" readonly="true" />
		<s:textfield name="tableName" label="Table" readonly="true" />
		<s:textfield name="classDescriptor.packageName" label="Package" required="false" maxlength="300" />
		<s:textfield name="classDescriptor.superClassName" label="Super Class Name" required="false" maxlength="300" />
		<s:textfield name="classDescriptor.className" label="Class Name" required="false" maxlength="300" />
	</table>
	<table border="1" cellpadding="0" cellspacing="0">
		<s:iterator value="fieldMap">
			<tr>
				<td><s:property value="value.column" /></td>
				<td><input type="text" name="fieldMap['<s:property value="value.column" />'].name" value="<s:property value="value.name" />" /></td>
			</tr>
		</s:iterator>
	</table>
	<table border="0">
		<tr>
			<td>
			<table border="0">
				<s:submit name="method:generatePojoBo" align="left" value="Submit" />
			</table>
			</td>
			<td>
			<table border="0">
				<s:submit name="action:connectionAction!displayConnections" align="left" value="Cancel" />
			</table>
			</td>
		</tr>
	</table>
</s:form>
