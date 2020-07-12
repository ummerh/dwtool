<%@ taglib prefix="s" uri="/struts-tags"%>
<s:form action="batchDataAction" method="POST"
	enctype="multipart/form-data" theme="simple">
	<div><a href='./connectionAction!displayConnections.action'>
	<img src="./images/back.jpg" border="0" height="24" width="24"> </a></div>
	<h3>Batch Data Files For Connection:<s:property
		value="connectionName" /></h3>
	<h5>Batch data directory is <s:property
		value="batchLoadDirectory.absolutePath" /></h5>
	<p><font color="red"> <s:property value="message" /> </font></p>
	<s:hidden name="connectionName"></s:hidden>
	<table border="1" cellspacing="0" cellpadding="1" width="70%">
		<tr>
			<th>File</th>
			<th>Status</th>
			<th>Good</th>
			<th>Bad</th>
			<th><input type="checkbox" name="excludeAll"
				onclick="groupSelect(this, 'excludeList');">&nbsp;Exclude</th>
		</tr>
		<s:iterator value="statList">
			<tr>
				<td><s:property value="file.name" /></td>
				<td align="center"><s:property value="fileError" />&nbsp;</td>
				<td align="center"><s:if test="goodCount > 0">
					<s:a title="Good Records"
						href="./batchDataAction!downloadResult.action?resultFile=%{file.name}&suffix=good&connectionName=%{connectionName}">
						[<s:property value="goodCount" />]
				</s:a>
				</s:if>&nbsp;</td>
				<td align="center"><s:if test="badCount > 0">
					<s:a title="Bad Records"
						href="./batchDataAction!downloadResult.action?resultFile=%{file.name}&suffix=bad&connectionName=%{connectionName}">
						[<s:property value="badCount" />]
				</s:a>

				</s:if>&nbsp;</td>
				<td><input type="checkbox" name="excludeList"
					<s:if test="excluded">checked="checked"</s:if>
					value="<s:property value="file.name" />" /></td>
			</tr>
		</s:iterator>
	</table>
	<br>
	<table>
		<s:submit name="method:processBatchFiles" align="left"
			value="Process All" />
		<s:if test="processedCount > 0">
			<s:submit name="method:updateDbWithGood" align="left"
				value="Update DB With Good Records" />
		</s:if>
	</table>
</s:form>
