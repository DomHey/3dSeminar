<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>${url}</title>
</head>
<body>
	<h2>Available ressources on the WFS</h2>
	<c:set var="information" value="${parsedInformation}" />
    <c:set var="splitArray" value="${fn:split(information,';')}" />
	<c:forEach var="i" items="${splitArray}">
		<li>${i}</li>
	</c:forEach>
	
	</br>
	
	<form action="Configuration" method="post">
		<h3>Choose ressource</h3>
		<input name="ressource" type="text" size="30" maxlength="100">
		<h3>Layername</h3>
		<input name="layername" type="text" size="30" maxlength="100">
		</br>
		</br>
		<label for="check1">
        <input type="checkbox" name="cache" value="true" id="check1">
        Cache Layer?
    	</label>
    	</br>
		</br>
		
		<input type="submit" value="Store Selection" >
		<input type="hidden" name="serverUrl" value="${serverUrl}">		
		
	</form>
    

</body>
</html>