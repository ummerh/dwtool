<%@ taglib prefix="s" uri="/struts-tags"%>
<script type="text/javascript" src="./js/jquery/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="./js/jquery-easyui/jquery.easyui.min.js"></script>
<script type='text/javascript' src='./js/filedrop/jquery.filedrop.js'></script>
<link rel="stylesheet" type="text/css" href="./js/jquery-easyui/themes/gray/easyui.css">
<link rel="stylesheet" type="text/css" href="./js/jquery-easyui/themes/icon.css">

<style>
td,tr,th {
	border: 1px;
	border-style: solid;
	border-color: gray;
}

textarea {
	padding: 0px;
	margin: 0px;
}
</style>
<script>
	$(function() {
		var dropbox = $('#dropbox'), message = $('.message', dropbox);
		dropbox.filedrop({
			// The name of the $_FILES entry:
			paramname : 'excelFile',
			maxfiles : 10,
			maxfilesize : 10, // in mb
			url : './excelDBAction!upload.action',
			uploadFinished : function(i, file, response) {
				$('#message').html($('#message').html() + '<br/>' + response.status);
			},
			error : function(err, file) {
				switch (err) {
				case 'BrowserNotSupported':
					showMessage('Your browser does not support HTML5 file uploads!');
					break;
				case 'TooManyFiles':
					alert('Too many files! Please select 5 at most!');
					break;
				case 'FileTooLarge':
					alert(file.name + ' is too large! Please upload files up to 2mb.');
					break;
				default:
					break;
				}
			},

			// Called before each upload is started
			beforeEach : function(file) {
				if (file.type.indexOf('excel') == -1) {
					alert('Only excel files are allowed.');
					return false;
				}
			},

			uploadStarted : function(i, file, len) {

			},

			progressUpdated : function(i, file, progress) {

			}

		});

		function showMessage(msg) {
			message.html(msg);
		}

	});

	function submitQuery() {
		$.ajax({
			url : './excelDBAction!query.action?sqlText=' + escape($("#sqltext").val()),
			success : function(data) {
				var $user_table = $("#resultTable");
				$user_table.html("");
				resObj = jQuery.parseJSON(data);
				var $row = $("<tr></tr>");
				for (j = 0; j < resObj.headers.length; j++) {
					var $cell = $("<th data-options=\"field:'"+resObj.headers[j]+"'\"></th>");
					$cell.text(resObj.headers[j]);
					$row.append($cell);
				}
				$user_table.append($row);
				$("#countstat").html('Total records - ' + resObj.data.length + '&nbsp;<a href="' + './excelDBAction!exportResult.action?sqlText=' + escape($("#sqltext").val()) + '"><img title="Download" src="./images/download.jpg" border="0" height="20" width="20"></a>');
				for (i = 0; i < resObj.data.length; i++) {
					var $row = $("<tr></tr>");
					for (j = 0; j < resObj.data[i].length; j++) {
						var $cell = $("<td></td>");
						$cell.text(resObj.data[i][j]);
						$row.append($cell);
					}
					$user_table.append($row);
				}

			}
		});
	}
</script>
<s:form action="excelDBAction" method="POST" enctype="multipart/form-data" theme="simple">
	<div class="span-18 easyui-tabs">
		<div title="Files" style="padding: 10px">
			<div id="dropbox" style="border: 1px; border-style: dotted; background-color: #D8D8D8;">
				<div style="margin: 10em;">Drag and drop [.xls] files here to upload...</div>
			</div>
			<div id="message" class="quiet" style="margin: 1em"></div>
			<div id="available">
				<h3>Available Files</h3>
				<s:iterator value="availableFiles">
					<p>
						<a href="./excelDBAction!download.action?fileName=<s:property value="top" />"><img src='images/excel-icon.jpg' height='20' width='20' /></a>&nbsp;
						<s:property value="top" />
						&nbsp; <a href="./excelDBAction!delete.action?fileName=<s:property value="top" />"><img src='images/delete.jpg'></a>
					</p>
				</s:iterator>
			</div>
		</div>
		<div title="Excel DB" style="padding: 10px">
			<ul id="tt" class="easyui-tree"
				data-options="url:'./excelDBAction!fileView.action',
				onClick: function(node){
				$(this).tree('toggle', node.target);
				if(node.text=='DB'){
					$(this).tree('reload');
				}
				}
			">
			</ul>
		</div>
		<div title="Query" style="padding: 10px">
			<div id="querypane" class="easyui-panel" title="SQL" style="padding: 1px; background: #fafafa;" data-options="collapsible:true">
				<div>
					<textarea id="sqltext"> </textarea>
				</div>
				<div>
					<input type="button" value="Run" onclick="submitQuery();">
				</div>
			</div>
			<div id="querypane" class="easyui-panel" title="Results" style="padding: 0px; background: #fafafa; height: 500px;" data-options="collapsible:true">
				<div id="countstat"></div>
				<table id="resultTable" style="border: 1px; border-style: solid;"></table>
			</div>
		</div>
	</div>
</s:form>
