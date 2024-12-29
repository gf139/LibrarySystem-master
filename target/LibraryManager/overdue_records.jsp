<%@ page import="java.time.temporal.ChronoUnit" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page language="java" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <title>违规记录</title>
    <link rel="stylesheet" type="text/css" href="assets/css/common.css"/>
    <link rel="stylesheet" type="text/css" href="assets/css/main.css"/>
</head>
<body>
<div class="topbar-wrap white">
    <div class="topbar-inner clearfix">
        <div class="topbar-logo-wrap clearfix">
            <h1 class="topbar-logo none"><a class="navbar-brand">违规记录</a></h1>
            <ul class="navbar-list clearfix">
                <li><a href="/book.do?type=pageList">首页</a></li>
            </ul>
        </div>
        <div class="top-info-wrap">
            <ul class="top-info-list clearfix">
                <li><a>${username}</a></li>
                <li><a href="/login.do?type=logout">退出</a></li>
            </ul>
        </div>
    </div>
</div>
<div class="container clearfix">
    <div class="main-wrap">
        <div class="crumb-wrap">
            <div class="crumb-list"><i class="icon-font"></i><a href="/book.do?type=pageList">首页</a><span
                    class="crumb-step">&gt;</span><span class="crumb-name">违规记录</span></div>
        </div>
        <div class="result-wrap">
            <div class="result-content">
                <table class="result-tab" width="100%">
                    <tr id="col-title">
                        <th>ID</th>
                        <th>书名</th>
                        <th>借阅日期</th>
                        <th>超时天数</th>
                    </tr>
                    <c:forEach var="book" items="${overdueBooks}">
                        <tr>
                            <td>${book.id}</td>
                            <td>${book.bookName}</td>
                            <td>${book.borrowDate}</td>
<%--                            <td>${(empty book.returnDate) ? (currentDate.time - book.borrowDate.time) / (1000 * 60 * 60 * 24) : 0}</td>--%>
                            <td>${book.daysOverdue}</td>
                        </tr>
                    </c:forEach>
                </table>
                <p>违规次数: ${overdueCount}</p>
                <c:if test="${overdueCount >= 3}">
                    <p style="color: red;">您的账号已被停用，请联系管理员。</p>
                </c:if>
            </div>
        </div>
    </div>
</div>
</body>
</html>
