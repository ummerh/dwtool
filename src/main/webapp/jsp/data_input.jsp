<%@ taglib prefix="s" uri="/struts-tags"%>
<s:form action="dataAction" method="POST" enctype="multipart/form-data"
	theme="simple">
	<div><s:if test="tableName != null">
		<a
			href='./dbErAction!displayAllTables.action?connectionName=<s:property value="connectionName" />'>
		<img src="./images/back.jpg" border="0" height="24" width="24">
		</a>
	</s:if> <s:else>
		<a href='./connectionAction!displayConnections.action'> <img
			src="./images/back.jpg" border="0" height="24" width="24"> </a>
	</s:else></div>
	<h3>Import Data</h3>
	<p><font color="red"> <s:property value="message" /> </font></p>
	<s:hidden name="tableName"></s:hidden>
	<s:hidden name="connectionName"></s:hidden>
	<s:hidden name="inputFileName"></s:hidden>
	<s:hidden name="badRecords"></s:hidden>
	<s:hidden name="goodRecords"></s:hidden>
	<div class="span-10">
	<table>
		<tr>
			<td>Connection</td>
			<td><s:property value="connectionName" /></td>
		</tr>
		<tr>
			<td>Table Name</td>
			<td><s:property value="tableName" /></td>
		</tr>
		<tr>
			<td>Header Included?</td>
			<td><s:checkbox label="Header Row" name="headerIncluded"
				value="%{headerIncluded}" fieldValue="true" /></td>
		</tr>

		<tr>
			<td colspan="2" class="note">Accepts *.xls, *.csv, *.txt</td>
		</tr>
		<tr>
			<td colspan="2"><s:file name="dataFile" value="Browse" size="50">
			</s:file><s:submit name="method:uploadDataFile" align="left" value="Upload" /></td>
		</tr>
		<tr>
			<td colspan="2"><s:submit name="method:downloadBlankSheet"
				align="left" value="Create Blank Sheet" /><s:submit
				name="method:downloadBlankCsv" align="left" value="Create Blank CSV" /></td>
		</tr>
	</table>
	<table>
		<s:if test="badRecords">
			<tr>
				<td>Bad Records (<s:property value="statistics.badCount" />)<a
					href='./dataAction!downloadBad.action?inputFileName=<s:property value="inputFileName"/>'><img
					src="./images/download.jpg" border="0" height="24" width="24"></a>
				</td>
			</tr>
		</s:if>
		<s:if test="goodRecords">
			<tr>
				<td>Good Records (<s:property value="statistics.goodCount" />)
				<a
					href='./dataAction!downloadGood.action?inputFileName=<s:property value="inputFileName"/>'><img
					src="./images/download.jpg" border="0" height="24" width="24"></a>
				<s:submit name="method:updateDBWithGood" align="left"
					value="Update Table" /></td>
			</tr>
		</s:if>
		<s:if test="dbUpdated">
			<tr>
				<td>No of records updated: <s:property
					value="statistics.updateRecordCount" /></td>
			</tr>
			<tr>
				<td>No of records inserted: <s:property
					value="statistics.insertRecordCount" /></td>
			</tr>
			<tr>
				<td>No of records (Total) : <s:property
					value="statistics.totalRecordCount" /></td>
			</tr>
		</s:if>
	</table>
	</div>
</s:form>
