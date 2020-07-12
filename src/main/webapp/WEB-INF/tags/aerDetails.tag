<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ attribute name="aerDetails" required="false" description="Details"%>
<%@ attribute name="aerResult" required="false" description="Status"%>
<s:if test="aerResult != null">
	<div class="highlight"><s:property value="aerResult" /></div>
	<br>
</s:if>
<!-- AER -->
<s:if test="aerDetails.size > 0">
	<table>
		<caption>AER</caption>
		<tr>
			<th>SN</th>
			<th>AER</th>
			<th>Title</th>
			<th>Team</th>
			<th>Assignment</th>
			<th>Cntrb</th>
		</tr>
		<s:iterator value="aerDetails" status="stat">
			<tr>
				<td><a href="revDataAction!search.action?term=${itemnumber}"
					target="${itemnumber}" title="Search Details">${stat.count}</a></td>
				<td><a target='_blank'
					href='https://sp.lndb.com/T/ProjectManagement/Lists/Non-HCM Development AER/DispForm.aspx?ID=${aerid}'
					title="Sharepoint AER">${itemnumber}</a></td>
				<td>${title}</td>
				<td>${team}</td>
				<td>${assignment}</td>
				<td>${cntrb}</td>
			</tr>
		</s:iterator>
	</table>
</s:if>