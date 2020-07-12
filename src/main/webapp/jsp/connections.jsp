<%@ taglib prefix="s" uri="/struts-tags"%>
<script>
	function confirmDelete(){
		if(confirm('Do you want to delete this connection?')){
			return true;
		}else{
			return false;
		}
	}
</script>
<h3>Databases</h3>
<s:form theme="simple">
	<div>Configure New<a
		href="./connectionAction!newConnectionInput.action"><img
		src="./images/add.jpg" border="0" width="24" height="24"></a></div>
	<table>
		<tr>
			<th>Name</th>
			<th>User</th>
			<th>View Tables</th>
			<th>View Load order</th>
			<th>Check Data Integrity</th>
			<th>Batch Data Load</th>
			<th>Test</th>
			<th>Edit</th>
			<th>Delete</th>
			<th>Reset Map</th>
		</tr>
		<tbody>
			<s:iterator value="connections">
				<tr>
					<td class="<s:property value="highlight" />"><s:property
						value="name" /></td>
					<td class="<s:property value="highlight" />"><s:property
						value="userId" /></td>
					<td class="<s:property value="highlight" />"><a
						href='./dbErAction!displayAllTables.action?connectionName=<s:property value="name" />'>
					<img src="./styles/blueprint/plugins/buttons/icons/tick.png" /></a></td>
					<td class="<s:property value="highlight" />"><s:a
						href="./dbErAction!defineDBLoadOrder.action?connectionName=%{name}">
						<img src="./styles/blueprint/plugins/buttons/icons/tick.png" />
					</s:a></td>
					<td class="<s:property value="highlight" />"><s:a
						href="./dbErAction!checkDataIntegrity.action?connectionName=%{name}">
						<img src="./styles/blueprint/plugins/buttons/icons/tick.png"/>
					</s:a></td>
					<td class="<s:property value="highlight" />"><s:a
						href="./batchDataAction!displayBatchFiles.action?connectionName=%{name}">
						<img src="./styles/blueprint/plugins/buttons/icons/tick.png" />
					</s:a></td>
					<td class="<s:property value="highlight" />"><a
						href='./TestConnection.action?connectionName=<s:property value="name" />'><img
						src="./styles/blueprint/plugins/buttons/icons/tick.png" /></a></td>
					<td class="<s:property value="highlight" />"><s:a
						href="./newConnectionSave!editConnectionInput.action?connectionName=%{name}">
						<img src="./styles/blueprint/plugins/buttons/icons/tick.png"/>
					</s:a></td>
					<td class="<s:property value="highlight" />"><s:a
						onclick="return confirmDelete()"
						href="./connectionAction!deleteConnection.action?connectionName=%{name}">
						<img src="./images/delete.jpg" border="0" height="18">
					</s:a></td>
					<td class="<s:property value="highlight" />"><s:a
						href="./connectionAction!refreshConnection.action?connectionName=%{name}">
						<img src="./styles/blueprint/plugins/buttons/icons/tick.png"/>
					</s:a></td>
				</tr>
			</s:iterator>
		</tbody>
	</table>
</s:form>
