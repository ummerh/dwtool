<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ attribute name="dttDetails" required="false" description="Details"%>
<%@ attribute name="dttResult" required="false" description="Status"%>
<!-- DTT -->
<s:if test="dttResult != null">
	<div class="highlight"><s:property value="dttResult" /></div>
	<br>
</s:if>
<s:elseif test="dttDetails.size > 0">
	<table>
		<caption>DTT Results</caption>
		<tr>
			<th>SN</th>
			<th>DTT</th>
			<th>Name</th>
			<th>AER</th>
			<th>Team</th>
			<th>Reporter</th>
			<th>Assigned To</th>
			<th>Cntrb</th>
			<th>LNDB Stg Status</th>
		</tr>
		<s:iterator value="dttDetails" status="stat">
			<tr>
				<td><a href="revDataAction!search.action?term=DTT ${itemid}"
					target="${itemid}" title="Search Details">${stat.count}</a></td>
				<td><a
					href='https://sp.lndb.com/W/Testing/Lists/Defect Tracking Tool/DispFormCustom.aspx?ID=${itemid}'
					target='_blank'>${itemid}</a></td>
				<td>${defectname}</td>
				<td>${aernumber}</td>
				<td>${team}</td>
				<td>${reporter}</td>
				<td>${assignment}</td>
				<td>${cntrb}</td>
				<td>${cntrbStatus}</td>
			</tr>
		</s:iterator>
	</table>
</s:elseif>