<%--
  Created by IntelliJ IDEA.
  User: victo
  Date: 09.09.2020
  Time: 22:32
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Add Product</title>
</head>
<body>
<h5 style="color: grey">${message}</h5>
<h1>Please, provide new product details</h1>
<form method="post" action="${pageContext.request.contextPath}/products/addproduct">
    Product Name:<input type="text" name="productname">
    Price:<input type="number" name="price">
    <button type="submit">Add</button>
</form>
</body>
</html>