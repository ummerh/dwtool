<%@ taglib prefix="s" uri="/struts-tags"%>
<s:form action="dBComparatorAction" theme="simple">
	<div class="span-10">
		<h3>Database Comparator</h3>
		<table>
			<tr>
				<td>Base</td>
				<td><s:select label="Source DB" name="sourceDb" emptyOption="true" list="connections" value="name" required="true" listKey="name" listValue="name">
					</s:select></td>
			</tr>
			<tr>
				<td>Reference</td>
				<td><s:select label="Target DB" name="targetDb" emptyOption="true" list="connections" value="name" required="true" listKey="name" listValue="name">
					</s:select></td>
			</tr>
		</table>
		<div>
			<s:submit name="method:dbDiffReport" align="left" value="Diff Report" />
			<s:submit name="method:compareDb" align="left" value="Diff SQL" />
			<s:submit name="method:dataDiffReport" align="left" value="Compare Data" />
		</div>
	</div>
</s:form>
