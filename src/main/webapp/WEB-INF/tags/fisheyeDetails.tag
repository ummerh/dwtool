<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ attribute name="fisheyeDetails" required="false" description="Details"%>
<%@ attribute name="fisheyeResult" required="false" description="Status"%>
<%@ attribute name="fisheyePages" required="false" description="Pages"%>
<!-- Fisheye -->
<s:form theme="simple">
	<s:if test="fisheyeResult != null">
		<div class="highlight">
			<s:property value="fisheyeResult" />
		</div>
		<br>
	</s:if>
	<s:elseif test="fisheyeDetails.size > 0">
		<table>
			<caption>
				Fisheye Results &nbsp;<a href="revDataAction!exportRevisions.action?pageId=<s:property value="fisheyePages.pageId" />"><img title="Download" src="./images/download.jpg" border="0" height="20"
					width="20"></a>
			</caption>
			<caption>
				Page
				<s:textfield name="pageNumber" label="Page" required="true" size="2" /><s:submit name="method:page" id="go" align="left" value="go"  />
				<s:hidden name="pageId" />
				<s:hidden name="sortBy" />
				of
				<s:property value="fisheyePages.totalCount" />
				&nbsp; [
				<s:if test="fisheyePages.prevPage">
					<a href="revDataAction!page.action?pageId=<s:property value="fisheyePages.pageId" />&pageNumber=<s:property value="fisheyePages.prev" />">&lt;&lt;</a>
				</s:if>
				<s:else>&lt;&lt;
			</s:else>
				&nbsp;&nbsp; &nbsp;&nbsp;
				<s:if test="fisheyePages.nextPage">
					<a href="revDataAction!page.action?pageId=<s:property value="fisheyePages.pageId" />&pageNumber=<s:property value="fisheyePages.next" />">&gt;&gt;</a>
				</s:if>
				<s:else>&gt;&gt; 
			</s:else>
				]
			</caption>
			<tr>
				<th>SN</th>
				<th>Project</th>
				<th><a href="revDataAction!sortRevisions.action?pageId=<s:property value="fisheyePages.pageId" />&pageNumber=<s:property value="pageNumber" /> &sortBy=csid">Revision</a></th>
				<th>Comments</th>
				<th><a href="revDataAction!sortRevisions.action?pageId=<s:property value="fisheyePages.pageId" />&pageNumber=<s:property value="pageNumber" /> &sortBy=path">Path</a></th>
			</tr>
			<s:iterator value="fisheyeDetails" status="stat">
				<tr>
					<td>${((pageNumber-1)*50)+stat.count}</td>
					<td>${project}</td>
					<td><a href='http://fisheye.lndb.com/changelog/${project}?cs=${csid}' target='_blank'>${csid}</a></td>
					<td title="${comments}">${displayComments}</td>
					<td width="30em"><a title="${path}" href='http://fisheye.lndb.com/browse/${project}/${path}' target='_blank'>${displayPath}</a></td>
				</tr>
			</s:iterator>
			<tr>
				<td colspan="5">Page <s:property value="pageNumber" /> of <s:property value="fisheyePages.totalCount" /> &nbsp; [ <s:if test="fisheyePages.prevPage">
						<a href="revDataAction!page.action?pageId=<s:property value="fisheyePages.pageId" />&pageNumber=<s:property value="fisheyePages.prev" />">&lt;&lt;</a>
					</s:if> <s:else>&lt;&lt;
			</s:else> &nbsp;&nbsp;&nbsp;&nbsp; <s:if test="fisheyePages.nextPage">
						<a href="revDataAction!page.action?pageId=<s:property value="fisheyePages.pageId" />&pageNumber=<s:property value="fisheyePages.next" />">&gt;&gt;</a>
					</s:if> <s:else>&gt;&gt; 
			</s:else> ]
				</td>
			</tr>
		</table>
	</s:elseif>
</s:form>