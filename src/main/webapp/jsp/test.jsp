<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<meta content="0" http-equiv="expires">
<title><tiles:getAsString name="title" /></title>
<script type='text/javascript' src='../dwr/interface/DwrServices.js'></script>
<script type='text/javascript' src='../dwr/engine.js'></script>
<script type='text/javascript' src='../js/common.js'></script>
<script type="text/javascript" src="http://code.jquery.com/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="../js/jquery-easyui/jquery.easyui.min.js"></script>
<script type='text/javascript' src='../js/filedrop/jquery.filedrop.js'></script>
<link rel="stylesheet" type="text/css" href="../js/jquery-easyui/themes/gray/easyui.css">
<link rel="stylesheet" type="text/css" href="../js/jquery-easyui/themes/icon.css">
<link rel="stylesheet" href="../styles/blueprint/screen.css" type="text/css" media="screen, projection">
<link rel="stylesheet" href="../styles/blueprint/print.css" type="text/css" media="print">
<link rel="stylesheet" href="../styles/blueprint/ie.css" type="text/css" media="screen, projection" />
<link rel="Stylesheet" href="../styles/common.css" type="text/css" media="screen">

<script>
	$(document).ready(function() {
		$("tr").mouseover(function() {
			$(this).css("background-color", "#DDD");
		});
		$("tr").mouseout(function() {
			$(this).css("background-color", "white");
		});
	});
</script>
</head>
<body>
	<div class="container centerContainer">
		<div id="header" class="span-34 top_header append-bottom banner last">Tool Box</div>
		<div class="main_content span-34">
			<div id="sidebar" class="span-4"></div>
		</div>
		<div id="body" class="span-28 last">
			<div class="right_content span-28 last " style="overflow: auto;">
				<div id="resDiv">
					<table class="sdsd" style="width: 400px; height: 250px" data-options="fitColumns:true,singleSelect:true">
						<thead>
							<tr>
								<th data-options="field:'code',width:100">Code</th>
								<th data-options="field:'name',width:100">Name</th>
								<th data-options="field:'price',width:100,align:'right'">Price</th>
							</tr>
						</thead>
						<tr>
							<td>sdsd</td>
							<td>sdsd</td>
							<td>sdsdd</td>
						</tr>
					</table>
				</div>
			</div>
		</div>
		<div id="footer" class="span-34 prepend-4 prepend-top last"></div>
	</div>
</body>
</html>
