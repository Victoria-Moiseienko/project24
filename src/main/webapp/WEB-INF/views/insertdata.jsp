<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Insert Data</title>
</head>
<body>
<table>
    <tr>
        <th><a href="${pageContext.request.contextPath}/users/registration">Registration</a></th>
        <th><a href="${pageContext.request.contextPath}/users">User list</a></th>
        <th><a href="${pageContext.request.contextPath}/products/add">Add product</a></th>
        <th><a href="${pageContext.request.contextPath}/products">Product list</a></th>
        <th><a href="${pageContext.request.contextPath}/products/manage">Product list Admin</a></th>
        <th><a href="${pageContext.request.contextPath}/shopping-cart/info">Shopping cart</a></th>
        <th><a href="${pageContext.request.contextPath}/user/orders">Orders</a></th>
        <th><a href="${pageContext.request.contextPath}/orders">Orders Admin</a></th>
        <th><a href="${pageContext.request.contextPath}/inject">Inject</a></th>
    </tr>
</table>

<form method="post" action="${pageContext.request.contextPath}/inject">
    <button type="submit">Inject</button>
</form>
</body>
</html>