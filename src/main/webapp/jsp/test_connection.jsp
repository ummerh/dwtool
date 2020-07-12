<%@ taglib prefix="s" uri="/struts-tags"%>
<s:form name="TestConnection"  theme="simple">
	<div><a href='./connectionAction!displayConnections.action'>
	<img src="./images/back.jpg" border="0" height="24" width="24"> </a></div>
	<h3>Test Connection !</h3>
	<table border="1" width="60%" cellspacing="0" cellpadding="1">
		<tr>
			<th>Connection</th>
			<th>Valid</th>
			<th>Message</th>
		</tr>
		<tr>
			<td><s:property value="connectionName" /></td>
			<td><s:property value="status" /></td>
			<td><s:property value="statusMessage" /></td>
		</tr>
	</table>
</s:form>

