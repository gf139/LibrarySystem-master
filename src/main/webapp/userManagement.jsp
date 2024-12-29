<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <title>用户管理</title>
    <link rel="stylesheet" type="text/css" href="assets/css/common.css" />
    <link rel="stylesheet" type="text/css" href="assets/css/main.css" />
    <script src="assets/js/jquery-3.2.1.js"></script>
    <script src="assets/layer/layer.js"></script>
</head>
<body>
<div class="topbar-wrap white">
    <div class="topbar-inner clearfix">
        <div class="topbar-logo-wrap clearfix">
            <h1 class="topbar-logo none"><a class="navbar-brand">后台管理</a></h1>
            <ul class="navbar-list clearfix">
                <li><a href="/book.do?type=pageList">首页</a></li>
                <li><a href="/book.do?type=pageList" target="_blank">网站首页</a></li>
            </ul>
        </div>
        <div class="top-info-wrap">
            <ul class="top-info-list clearfix">
                <li><a>${username}</a></li>
                <li><a>修改密码</a></li>
                <li><a href="/login.do?type=logout">退出</a></li>
            </ul>
        </div>
    </div>
</div>
<div class="container clearfix">
    <div class="sidebar-wrap">
        <div class="sidebar-title">
            <h1>菜单</h1>
        </div>
        <div class="sidebar-content">
            <ul class="sidebar-list">
                <li>
                    <ul class="sub-menu">
                        <li><a href="/book.do?type=pageList">图书管理</a></li>
                        <li><a href="/book.do?type=viewOverdue">查看逾期记录</a></li>
                        <li><a href="/userManagement.do?type=query">用户管理</a></li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
    <div class="main-wrap">
        <div class="crumb-wrap">
            <div class="crumb-list"><i class="icon-font"></i><a href="/book.do?type=pageList">首页</a><span class="crumb-step">&gt;</span><span class="crumb-name">用户管理</span></div>
        </div>
        <div class="result-wrap">
            <form id="userForm">
                <div class="result-title">
                    <div class="result-list">
                        <a id="addUserBtn" href="javascript:void(0)" onclick="showAddUserModal()">添加用户</a>
                    </div>
                </div>
                <div class="result-content">
                    <table class="result-tab" width="100%">
                        <tr id="col-title">
                            <th>ID</th>
                            <th>用户名</th>
                            <th>角色</th>
                            <th>操作</th>
                        </tr>
                        <tbody id="userTableBody">
                        <c:forEach var="user" items="${users}">
                            <tr>
                                <td>${user.id}</td>
                                <td>${user.username}</td>
                                <td>${user.role}</td>
                                <td>
                                    <button type="button" onclick="showEditUserModal(${user.id})">编辑</button>
                                    <button type="button" onclick="deleteUser(${user.id})">删除</button>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- 添加用户模态框 -->
<div id="addUserModal" style="display:none;">
    <form id="addUserForm">
        <label for="newUsername">用户名:</label>
        <input type="text" id="newUsername" name="username" required>
        <label for="newPassword">密码:</label>
        <input type="password" id="newPassword" name="password" required>
        <label for="newRole">角色:</label>
        <select id="newRole" name="role" required>
            <option value="admin">管理员</option>
            <option value="user">普通用户</option>
        </select>
        <button type="button" onclick="addUser()">确定</button>
        <button type="button" onclick="hideAddUserModal()">取消</button>
    </form>
</div>

<!-- 编辑用户模态框 -->
<div id="editUserModal" style="display:none;">
    <form id="editUserForm">
        <input type="hidden" id="editUserId" name="userId">
        <label for="editUsername">用户名:</label>
        <input type="text" id="editUsername" name="username" required>
        <label for="editPassword">密码:</label>
        <input type="password" id="editPassword" name="password">
        <label for="editRole">角色:</label>
        <select id="editRole" name="role" required>
            <option value="admin">管理员</option>
            <option value="user">普通用户</option>
        </select>
        <button type="button" onclick="updateUser()">确定</button>
        <button type="button" onclick="hideEditUserModal()">取消</button>
    </form>
</div>

<script>
    $(document).ready(function () {
        loadUsers(); // Uncomment this if you have a loadUsers function
    });

    function showAddUserModal() {
        $("#addUserModal").show();
    }

    function hideAddUserModal() {
        $("#addUserModal").hide();
    }

    function addUser() {
        var formData = $("#addUserForm").serialize();
        $.ajax({
            type: "POST",
            url: "/userManagement.do?type=add",
            data: formData,
            dataType: "json",
            success: function (response) {
                if (response.status == 1) {
                    layer.msg(response.data);
                    hideAddUserModal();
                    loadUsers(); // Uncomment this if you have a loadUsers function
                } else {
                    layer.msg(response.data);
                }
            }
        });
    }

    function showEditUserModal(userId) {
        $.ajax({
            url: "/userManagement.do?type=query&userId=" + userId,
            method: "POST",
            dataType: "json",
            success: function (response) {
                if (response.status == 1) {
                    var user = response.data;
                    $("#editUserId").val(user.id);
                    $("#editUsername").val(user.username);
                    $("#editPassword").val(user.password);
                    $("#editRole").val(user.role);
                    $("#editUserModal").show();
                } else {
                    layer.msg(response.data);
                }
            }
        });
    }

    function hideEditUserModal() {
        $("#editUserModal").hide();
    }

    function updateUser() {
        var formData = $("#editUserForm").serialize();
        $.ajax({
            type: "POST",
            url: "/userManagement.do?type=update",
            data: formData,
            dataType: "json",
            success: function (response) {
                if (response.status == 1) {
                    layer.msg(response.data);
                    hideEditUserModal();
                    loadUsers(); // Uncomment this if you have a loadUsers function
                } else {
                    layer.msg(response.data);
                }
            }
        });
    }

    function deleteUser(userId) {
        layer.confirm('确认要删除该用户吗？', function (index) {
            $.ajax({
                type: "POST",
                url: "/userManagement.do?type=delete",
                data: { userId: userId },
                dataType: "json",
                success: function (response) {
                    if (response.status == 1) {
                        layer.msg(response.data);
                        loadUsers(); // Uncomment this if you have a loadUsers function
                    } else {
                        layer.msg(response.data);
                    }
                }
            });
            layer.close(index);
        });
    }
</script>
</body>
</html>
