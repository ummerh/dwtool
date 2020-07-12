<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="er"%>
<s:form theme="simple">
	<h3>Revision Report - [${project}]</h3>
	<div>
		Browse projects <a href="revDataAction!reportByProject.action?project=rice">rice</a>&nbsp;&nbsp;<a href="revDataAction!reportByProject.action?project=kfs">kfs</a>&nbsp;&nbsp;<a
			href="revDataAction!reportByProject.action?project=kmm">kmm</a>
	</div>
	<br>
	<s:if test="project != null">
		<er:aerDetails aerDetails="aerDetails" aerResult="aerResult" />
		<er:dttDetails dttDetails="dttDetails" dttResult="dttResult" />
		<er:asrDetails asrDetails="asrDetails" asrResult="asrResult" />
	</s:if>
</s:form>
