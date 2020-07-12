<%@ taglib prefix="s" uri="/struts-tags"%>
<s:form action="newConnectionSave" theme="simple">
	<s:hidden name="edit"></s:hidden>
	<div><a href='./connectionAction!displayConnections.action'><img src="./images/back.jpg" border="0" height="24" width="24"></a></div>
	<h3>Connection</h3>
	<div class="span-10">
	<table>
		<tr>
			<td>Name</td>
			<td><s:textfield name="connectionName" label="Name" required="true" maxlength="30" readonly="%{edit}" /></td>
		</tr>
		<tr>
			<td>URL</td>
			<td><s:textfield name="url" label="URL" required="true" maxlength="200" /></td>
		</tr>
		<tr>
			<td>Driver</td>
			<td><s:select label="Driver" name="driverClass" emptyOption="true" list="#{'oracle.jdbc.OracleDriver':'Oracle', 'com.mysql.jdbc.Driver':'MySQL','net.sourceforge.jtds.jdbc.Driver':'SQLServer','org.hsqldb.jdbc.JDBCDriver':'Hsql DB'}"
				value="driverClass" required="true">
			</s:select></td>
		</tr>
		<tr>
			<td>Schema</td>
			<td><s:textfield name="schema" label="Schema" required="true" maxlength="50" /></td>
		</tr>
		<tr>
			<td>User</td>
			<td><s:textfield name="user" label="User" required="true" maxlength="30" /></td>
		</tr>
		<tr>
			<td>Password</td>
			<td><s:password name="password" label="Password" required="true" maxlength="30" /></td>
		</tr>
	</table>
	<s:submit name="method:configureConnection" align="left" value="Submit" /><s:submit name="action:connectionAction!displayConnections" align="left" value="Cancel" /></div>
</s:form>
