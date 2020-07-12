<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="/WEB-INF/tlds/dwtool.tld" prefix="er"%>
<script>
	function validateSubmit(){
		$('#error').html('');
		var requiredFields = new Array();
		<c:set var="index" value="0" />
		<s:iterator value="tableDescriptor.columns" status="loop">
			<s:if test="!nullable">
 				requiredFields[${index}] = "${name}";
				<c:set var="index" value="${index+1}" />
			</s:if>
		</s:iterator>
		var proceed = true;
		for (var i=0; i< requiredFields.length; i++ ){
			var val = $('#'+requiredFields[i]).val();
			if(val == '' || val == null || val.length == 0){				
				$('#error').html($('#error').html() + '<p>'+requiredFields[i]+' is a required field.</p>');
				$('#'+requiredFields[i]).css('border-color','red');
				proceed = false;
			}
		}
		return proceed;
	}
</script>
<div>
	<a href='./dbErAction!displayAllTables.action?connectionName=<s:property value="connectionName" />'>
	<img src="./images/back.jpg" border="0" height="24" width="24">
	</a>
</div>
<s:form theme="simple" >
	<div id="error" style="color: red"><s:property value="message" /></div>
	<table>
		<caption><s:property value="connectionName" />:<s:property value="tableName" /></caption>
		<s:iterator value="tableDescriptor.columns" status="loop">
			<tr>
				<td><s:property value="name" /><s:if test="!nullable">&nbsp;<span style="color: red;">*</span></s:if></td>				
				<td>
				<s:if test="tableDescriptor.isMemberOfPK(name)"> 
					<s:set name="readOnly" value="true"></s:set>
				</s:if>
				<s:if test="!tableDescriptor.isMemberOfPK(name)"> 
					<s:set name="readOnly" value="false"></s:set>
				</s:if>
				<s:set name="data" value="getValue(name)"></s:set>
				<er:displayDBField id="${name}" columnName="${name}" property="fieldMap['${name}']" tableDescriptor="${tableDescriptor}" data="${data}" readOnly="${readOnly}" />
				</td>
			</tr>
		</s:iterator>
	</table>	
	<div style="clear">
	<s:submit name="action:maintenanceAction!saveRecord" value="Submit"  onclick="return validateSubmit();" /> <input type="reset" value="Reset" />
	</div>
	<s:hidden name="tableName"></s:hidden>
	<s:hidden name="connectionName"></s:hidden>
	<s:hidden name="fkName"></s:hidden>
	<s:hidden name="columnName"></s:hidden>
	<s:hidden name="fromLookup"></s:hidden>
	<s:hidden name="formId"></s:hidden>
	<s:hidden name="method"></s:hidden>	
</s:form>