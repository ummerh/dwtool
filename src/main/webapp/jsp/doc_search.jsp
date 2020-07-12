<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="er"%>
<script>
	var fields = [ "Doc Type", "Chart Code", "Org Code" ];
	var oprs = [ "equal", "not equal", "greater than", "less than", "in", "not in" ];
	$(function() {
		$("#field").autocomplete({
			source : fields,
			minLength : 1
		});
		$("#opr").autocomplete({
			source : oprs,
			minLength : 1
		});
		$("#addq").click(function() {
			if (jQuery.inArray($("#field").val(), fields) == -1) {
				redBorder($("#field"));
				return false;
			}
			resetBorder($("#field"));
			if (jQuery.inArray($("#opr").val(), oprs) == -1) {
				redBorder($("#opr"));
				return false;
			}
			resetBorder($("#opr"));
			if ($("#val").val().trim() == "") {
				redBorder($("#val"));
				return false;
			}
			resetBorder($("#val"));
			var first = false;
			if ($("#query").val() == "") {
				first = true;
			}
			if (!first) {
				$("#query").val($("#query").val() + " " + $("#addend").val() + " ");
			}
			$("#query").val($("#query").val() + "" + $("#field").val() + " " + $("#opr").val() + " (\"" + $("#val").val() + "\") ");
			$("#field").val("");
			$("#opr").val("");
			$("#val").val("");
			return false;
		});
	});

	function redBorder(fld) {
		fld.css("border", "solid");
		fld.css("border-color", "red");
		fld.css("border-width", "1px");
	}

	function resetBorder(fld) {
		fld.css("border", "solid");
		fld.css("border-color", "#BBB");
		fld.css("border-width", "1px");
	}
</script>
<s:form theme="simple">
	<h3>Document Searcher</h3>	
		Search Query
	<table style="width: 75%; text-align: center;">
		<tr>
			<td>Field</td>
			<td>Operator</td>
			<td>Value</td>
			<td>Add</td>
			<td></td>
		</tr>
		<tr>
			<td><s:textfield id="field" name="field" size="20" label="Field" required="true" maxlength="50" /></td>
			<td><s:textfield id="opr" name="opr" size="10" label="Operator" required="true" maxlength="20" /></td>
			<td><s:textfield id="val" name="val" size="30" label="Operator" required="true" maxlength="1000" /></td>
			<td><s:select id="addend" name="addend" emptyOption="false" list="#{'AND':'AND', 'OR':'OR'}" required="true">
				</s:select></td>
			<td><img id="addq" src="./images/add.jpg" border="0" height="18"></td>
		</tr>
	</table>


	<s:textarea id="query" name="term" cols="50" label="Search Query" />
	<br>
	<s:submit name="method:search" id="search" align="left" value="go" />
</s:form>