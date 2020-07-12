/**
  Highlights with preferences
**/
	function highlight(obj,stat,bgHighlight,bgOriginal){		
		var elem = obj;		
		var cells = elem.cells;
		if(bgHighlight==null){
			bgHighlight = '#CCC';
		}
		if(bgOriginal == null){
			bgOriginal = 'white';
		}
		
		for(var i=0; i< cells.length; i++){
			if(stat == 'on'){
				if(elem.cells[i].style != null){
					elem.cells[i].style.backgroundColor= bgHighlight;
				}
			}else{				
				if(elem.cells[i].style != null){
					elem.cells[i].style.backgroundColor=bgOriginal;
				}
			}
		}		
	}
function groupSelect(obj,grpnm){
	var elms = document.getElementsByName(grpnm);
	for(var i=0; i< elms.length; i++){
		elms[i].checked = obj.checked;
	}
}

function allowNumbers(e, currVal, isDecimal){
		var keynum;
		var keychar;
		if(window.event) // IE
		{keynum = e.keyCode;}
		else if(e.which) // Netscape/Firefox/Opera
		{keynum = e.which;}
		keychar = String.fromCharCode(keynum);
		if(isDecimal){
			if(keychar == '.' && currVal.indexOf('.') == -1){
				return true;
			}			
		}
		return (keynum > 47 && keynum < 58 ) || (keynum==8);
}		
function confirmDelete(msg){
	if(confirm(msg)){
		return true;
	}else{
		return false;
	}
}

function lookup(fkName, columnName){
	document.forms[0].fkName.value=fkName;
	document.forms[0].columnName.value=columnName;
	document.forms[0].action='maintenanceAction!lookupReference.action';
	document.forms[0].submit();
	return true;
}