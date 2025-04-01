<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title>Item List</title>
</head>
<body>
    <h1>Items</h1>
    <ul>
        <s:iterator value="items">
            <li><s:property /></li>
        </s:iterator>
    </ul>
</body>
</html>
