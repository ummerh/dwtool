<%@ taglib prefix="s" uri="/struts-tags"%>
<script>
	function confirmDelete(val) {
		if (confirm('Are you sure you want to delete '+val+'?')) {
			return true;
		} else {
			return false;
		}
	}
</script>
<s:form action="ojbAction" method="POST" enctype="multipart/form-data"
	theme="simple">
	<h3>OJB Files !</h3>
	<p>
		<font color="red"> <s:property value="message" />
		</font>
	</p>
	<fieldset>
		<legend>Upload new OJB files</legend>
		<div>Accepts file name patterns OJB-*.xml, *.jar, *.zip</div>
		<div>
			<s:file name="xmlFile" value="Browse" size="50">
			</s:file>
			<s:submit name="method:uploadXMLFile" align="left" value="Upload" />
		</div>
	</fieldset>
	<table>
		<tr>
			<th>File Name</th>
			<th>View</th>
			<th>Download</th>
			<th>Delete</th>
		</tr>
		<s:iterator value="fileLists">
			<tr>
				<td><s:property value="name" /></td>
				<td align="center"><a target="<s:property value="name" />"
					href="./ojbAction!viewXMLFile.action?ojbFileName=<s:property value="name"/>"><img
						src="./images/xml.jpg" border="0" height="20" width="20"></a></td>
				<td align="center"><s:a title="Download"
						href="./ojbAction!downloadXMLFile.action?ojbFileName=%{name}">
						<img src="./images/download.jpg" border="0" height="20" width="20">
					</s:a></td>
				<td align="center"><s:a title="Delete"
						onclick="return confirmDelete('%{name}')"
						href="./ojbAction!deleteXMLFile.action?ojbFileName=%{name}">
						<img src="./images/delete.jpg" border="0" height="18">
					</s:a></td>
			</tr>
		</s:iterator>
	</table>
	<fieldset>
		<legend>Download/Refresh</legend>
		<s:a title="Download All" href="./ojbAction!downloadAllXMLFiles.action">
			<img src="./images/download.jpg" border="0" height="20" width="20">
		</s:a>&nbsp;&nbsp;
		<s:a title="Refresh All" href="./ojbAction!refreshMap.action">
			<img src="./images/refresh.jpg" border="0" height="20" width="20">
		</s:a>&nbsp;
		<s:a title="Delete All" onclick="return confirmDelete('All')" href="./ojbAction!deleteAll.action">
			<img src="./images/delete.jpg" border="0" height="20" width="20">
		</s:a>
	</fieldset>
</s:form>
