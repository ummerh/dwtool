<%@ page contentType="text/html; charset=ISO-8859-1" isErrorPage="true"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<h2>An unexpected error has occurred</h2>
<p>Please report this error to technical support personnel. Thank
	you for your cooperation.</p>
<hr />
<h3>Error Message</h3>
<s:actionerror />
<p>
	<s:property value="%{exception.message}" />
</p>
<hr />
<h3>Technical Details</h3>
<p>
	<s:property value="%{exceptionStack}" />
</p>