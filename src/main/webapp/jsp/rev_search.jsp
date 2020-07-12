<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="er"%>
<script>
	$(function() {
		$("#term").autocomplete({
			source : "revDataAction!suggestInput.action",
			minLength : 3,
			select : function(event, ui) {
				$("#term").val(ui.item.value);
				$("#search").click();
			}
		});
	});
</script>
<s:form theme="simple">
	<h3>Revision Searcher</h3>
	<div>
		Browse projects <a href="revDataAction!reportByProject.action?project=rice">rice</a>&nbsp;&nbsp;<a href="revDataAction!reportByProject.action?project=kfs">kfs</a>&nbsp;&nbsp;<a
			href="revDataAction!reportByProject.action?project=kmm">kmm</a>
	</div>
		Search Keywords...<s:textfield id="term" name="term" size="50" label="Keywords" required="true" maxlength="100" />
	<s:submit name="method:search" id="search" align="left" value="go" />
	<s:if test="term != null">
		<er:aerDetails aerDetails="aerDetails" aerResult="aerResult" />
		<er:dttDetails dttDetails="dttDetails" dttResult="dttResult" />
		<er:asrDetails asrDetails="asrDetails" asrResult="asrResult" />
	</s:if>
</s:form>
<hr class="space">
<s:if test="term != null">
	<iframe src="revDataAction!page.action?pageId=<s:property value="fisheyePages.pageId" />&pageNumber=1" width="780px" height="2200px" />
</s:if>
<br>
<br>