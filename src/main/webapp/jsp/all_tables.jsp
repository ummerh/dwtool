<%@ taglib prefix="s" uri="/struts-tags"%>
<script>
		var status = ' ';
		function getStatus(){
			status = ' ';			
			DwrServices.currentStatus("<s:property value="connectionName" />",printStatus);
		}
		function printStatus(str){
			status = str;
			document.getElementById("stat").innerHTML='Processing...'+str;
			if(str != 'DONE' && str != 'ERROR'){
				setTimeout("getStatus();",2000)
			}			
		}
		function getProgress(){
			DwrServices.currentProgressInd("<s:property value="connectionName" />",printProgress);
		}		
		function printProgress(prg){
			pgwid = 2*prg;
			if(status != 'ERROR'){
				if(prg < 100){
					document.getElementById("divpg").style.width = (5*prg)+'px';
					document.getElementById("divpg").innerHTML=prg+'%';
					setTimeout("getProgress();",2000)
				}else{
					document.getElementById("divpg").style.width = '500px';
					document.getElementById("divpg").innerHTML='100%';
				}		
			}
		}
</script>
<s:form theme="simple">
	<div><a
		href='./connectionAction!displayConnections.action'><img
		src="./images/back.jpg" border="0" height="24" width="24"></a></div>
	<h3>All Tables within connection <s:property
		value="connectionName" /></h3>
	<p><font color="red"> <s:property value="warningMessage" />
	</font></p>
	<div style="width: 500px;" >
	<div id="divpg" style="background-color: blue; color: white;">&nbsp;</div>
	</div>
	<br />
	<div align="left" id="stat"></div>
	<script>
		getStatus();
		getProgress();				
	</script>
	<iframe frameborder="0"
		src="./dbErAction!displayAllTables2.action?connectionName=<s:property value="connectionName" />"
		height="700" width="780"> </iframe>
</s:form>

