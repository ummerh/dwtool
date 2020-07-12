<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ attribute name="asrDetails" required="false" description="Details"%>
<%@ attribute name="asrResult" required="false" description="Status"%>
<!-- ASR -->
<s:if test="asrResult != null">
	<div class="highlight"><s:property value="asrResult" /></div>
	<br>
</s:if>
<s:elseif test="asrDetails.size > 0">
	<table>
		<caption>ASR Results</caption>
		<tr>
			<th>SN</th>
			<th>ASR</th>
			<th>Item Number</th>
			<th>Title</th>
			<th>Group</th>
		</tr>
		<s:iterator value="asrDetails" status="stat">
			<tr>
				<td><a href="revDataAction!search.action?term=${itemid}"
					target="${itemid}" title="Search Details">${stat.count}</a></td>
				<td><a
					href='https://sp.lndb.com/G/AppSupportRequest/Lists/Issues/DispForm.aspx?ID=${itemid}'
					target='_blank'>${itemid}</a></td>
				<td>${itemnumber}</td>
				<td>${title}</td>
				<td>${groupnm}</td>
			</tr>
		</s:iterator>
	</table>
</s:elseif>