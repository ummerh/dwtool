<%@ taglib prefix="s" uri="/struts-tags"%>
<s:form action="excelImportAction" theme="simple" method="post" enctype="multipart/form-data">
	<h3>Excel Import</h3>
	<table class="span-10">
		<tr>
			<td>Target DB</td>
			<td><s:select label="Staging DB" name="connectionName" emptyOption="false" list="connections" value="name" required="true" listKey="name" listValue="name">
				</s:select></td>
		</tr>
		<tr>
			<td>Select Excel file for conversion</td>
			<td><s:file label="File" name="upload" /></td>
		</tr>
    	<tr><td><s:submit name="method:upload" value="Convert To DML" align="center" /></td></tr>
	</table>
</s:form>