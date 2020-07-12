<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="er"%>
<html>
<head>
<meta content="0" http-equiv="expires">
<link rel="stylesheet" href="./styles/blueprint/screen.css" type="text/css" media="screen, projection">
<link rel="stylesheet" href="./styles/blueprint/print.css" type="text/css" media="print">
<link rel="stylesheet" href="./styles/blueprint/ie.css" type="text/css" media="screen, projection" />
<link rel="Stylesheet" href="./styles/common.css" type="text/css" media="screen">
<script type='text/javascript' src='./dwr/interface/DwrServices.js'></script>
<script type='text/javascript' src='./dwr/engine.js'></script>
<script type='text/javascript' src='./js/common.js'></script>

<script type='text/javascript' src='js/jquery/jquery.js'></script>
<script type='text/javascript' src='js/jquery/jquery.hotkeys.js'></script>
<script type='text/javascript' src='js/jquery/jquery.cookie.js'></script>
<link rel="stylesheet" href="./js/jquery-ui/css/smoothness/jquery-ui-1.8.6.custom.css" type="text/css" media="screen">
<script type="text/javascript" src="./js/jquery-ui/js/jquery-ui-1.8.6.custom.min.js"></script>
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
	<div class="leftalign">
		<er:fisheyeDetails fisheyeDetails="fisheyeDetails" fisheyeResult="fisheyeResult" fisheyePages="fisheyePages" />
	</div>
</body>