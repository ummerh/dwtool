<%@ taglib prefix="s" uri="/struts-tags"%>
<link rel="Stylesheet" href="./styles/common.css" type="text/css">
<script type='text/javascript' src='js/common.js'></script>
<script type='text/javascript' src='js/jquery/jquery.js'></script>
<script language="javascript">
       window.onload = function(){
              if (document.all){
                     var gridBody = document.getElementById("GridBody");
                     gridBody.className = '';
              } else {
                     var gridDiv = document.getElementById("GridContainer");
                     gridDiv.className = '';       
              }
       }
</script>
<script>
$(document).ready(function() {
	$("tr").mouseover(function() {
		$(this).css("background-color", "#CCC");
	});
	$("tr").mouseout(function() {
		$(this).css("background-color", "white");
	});
});
</script>
<s:form theme="simple">
	<table id="Grid" border="1" width="100%" cellspacing="0"
		cellpadding="1">
		<tr id="GridHeader" class="table-header">
			<th>Table</th>
			<th>Maintain</th>
			<th>Load Order</th>
			<th>Data Dependency</th>
			<th>Auto Update List</th>
			<th>Data Integrity</th>
			<th>File Import</th>
			<th>Java Pojo</th>
			<th>Junit Fixture</th>
		</tr>
		<tbody id="GridBody" class="table-body">
			<s:iterator value="allTables">
				<tr>
					<td><s:property value="tableName" /></td>
					<td align="center" title="Maintain"><a target="_top"
						href='./maintenanceAction!editStep1.action?connectionName=<s:property value="connectionName" />&tableName=<s:property value="tableName" />'>
						<img src="./styles/blueprint/plugins/buttons/icons/tick.png"/> </a></td>
					<td align="center" title="Load Order"><a target="_top"
						href='./dbErAction!defineTableLoadOrder.action?connectionName=<s:property value="connectionName" />&tableName=<s:property value="tableName" />'>
					<img src="./styles/blueprint/plugins/buttons/icons/tick.png"/> </a></td>
					<td align="center"><a target="_top" title="Data Dependency"
						href='./dbErAction!defineTableDependency.action?connectionName=<s:property value="connectionName" />&tableName=<s:property value="tableName" />'>
					<img src="./styles/blueprint/plugins/buttons/icons/tick.png"/> </a></td>
					<td align="center"><a target="_top" title="Auto Update List"
						href='./dbErAction!displayAutoUpdateableReferences.action?connectionName=<s:property value="connectionName" />&tableName=<s:property value="tableName" />'>
					<img src="./styles/blueprint/plugins/buttons/icons/tick.png"/> </a></td>
					<td align="center"><a target="_top" title="Data Integrity"
						href='./dbErAction!checkDataIntegrityByTable.action?connectionName=<s:property value="connectionName" />&tableName=<s:property value="tableName" />'>
					<img src="./styles/blueprint/plugins/buttons/icons/tick.png"/> </a></td>
					<td align="center"><a target="_top" title="Import Data"
						href='./dataAction!displayInput.action?connectionName=<s:property value="connectionName" />&tableName=<s:property value="tableName" />'>
					<img src="./styles/blueprint/plugins/buttons/icons/tick.png"/> </a></td>
					<td align="center"><a target="_top" title="Junit Fixture"
						href='./dbErAction!downloadPojo.action?connectionName=<s:property value="connectionName" />&tableName=<s:property value="tableName" />'>
					<img src="./images/download.jpg" border="0" height="18"> </a></td>
					<td align="center"><a target="_top" title="Junit Fixture"
						href='./dbErAction!downloadJunitFixture.action?connectionName=<s:property value="connectionName" />&tableName=<s:property value="tableName" />'>
					<img src="./images/download.jpg" border="0" height="18"> </a></td>
				</tr>
			</s:iterator>
		</tbody>
	</table>
</s:form>

