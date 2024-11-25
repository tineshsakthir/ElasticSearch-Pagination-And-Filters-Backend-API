<%--
  Created by IntelliJ IDEA.
  User: tines
  Date: 16-Nov-2024
  Time: 09:38 pm
  To change this template use File | Settings | File Templates.
--%>

<%@ page isELIgnored="false" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Checkers for module</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">


</head>
<body>

<button type="button" onclick="myFunctionToCheckModulesInJs()">click me to check the module working</button>

<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/moduleChecker2.js"></script>
<script src="${pageContext.request.contextPath}/js/moduleChecker1.js"></script>
</body>
</html>
