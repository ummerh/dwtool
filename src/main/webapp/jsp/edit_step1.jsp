<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="/WEB-INF/tlds/dwtool.tld" prefix="er"%>
<div>
	<a href='./dbErAction!displayAllTables.action?connectionName=<s:property value="connectionName" />'>
	<img src="./images/back.jpg" border="0" height="24" width="24">
	</a>
</div>
<s:form action="maintenanceAction!editStep1" theme="simple">
	<p style="color: red"><s:property value="message" /></p>
	<table>
		<caption><s:property value="connectionName" />:<s:property
			value="tableName" /></caption>
		<s:iterator value="tableDescriptor.columns" status="loop">
			<tr>
				<td><s:property value="name" /></td>

				<td><s:set name="data" value="getValue(name)"></s:set> <er:displayDBField
					id="${name}" columnName="${name}" property="fieldMap['${name}']"
					tableDescriptor="${tableDescriptor}" data="${data}" /></td>
			</tr>
		</s:iterator>
	</table>
	<div style=""><s:submit
		name="action:maintenanceAction!findRecords" value="Find" /> <s:submit
		name="action:maintenanceAction!addStep1" value="Add" /> <input
		type="reset" value="Reset" /></div>
	<table>
		<tr>
			<th>Action</th>
			<s:iterator value="tableDescriptor.columns" status="loop">
				<th><s:property value="name" /></th>
			</s:iterator>
		</tr>
		<s:iterator value="records" status="loop">
			<s:property escape="false"
				value="toHtmlRow(formId,fkName,columnName)" />
		</s:iterator>
	</table>
	<s:hidden name="tableName"></s:hidden>
	<s:hidden name="connectionName"></s:hidden>
	<s:hidden name="fkName"></s:hidden>
	<s:hidden name="columnName"></s:hidden>
	<s:hidden name="fromLookup"></s:hidden>
	<s:hidden name="formId"></s:hidden>
	<s:hidden name="method"></s:hidden>
</s:form>