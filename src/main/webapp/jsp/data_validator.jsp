<%@ taglib prefix="s" uri="/struts-tags"%>
<s:form action="dataValidatorAction" theme="simple">
	<h3>Data Validator</h3>
	<table class="span-10">
		<tr>
			<td>Staging DB <span class="small"><br>(Data is validated against the relationships defined in Target DB)</span></td>
			<td><s:select label="Staging DB" name="connectionName" emptyOption="true" list="connections" value="name" required="true" listKey="name" listValue="name">
				</s:select></td>
		</tr>
		<tr>
			<td>Target DB</td>
			<td><s:select label="Target DB" name="targetDb" emptyOption="true" list="connections" value="name" required="true" listKey="name" listValue="name">
				</s:select></td>
		</tr>
	</table>
	<div>
		<s:submit name="method:validateDb" align="left" value="Submit" />
	</div>
</s:form>
