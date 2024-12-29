package com.zy.servlet;

import com.zy.bean.User;
import com.zy.dao.UserDao;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet(name = "UserManagementServlet", urlPatterns = {"/userManagement.do"})
public class UserManagementServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role");
        request.setCharacterEncoding("UTF-8");

        if ("1".equals(role)) {
            UserDao userDao = new UserDao();
            String type = request.getParameter("type");
            String userId = request.getParameter("userId");
            if (type.equals("query")&& StringUtils.isEmpty(userId)) {
                System.out.println("查询时type:"+type+"  userId:"+userId);
                try {
                    showUsers(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (type.equals("query")&&StringUtils.isNotEmpty(userId)) {
                System.out.println("查询时type:" + type + "  userId:" + userId);
                try {
                    getOneUser(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (type.equals("add")||type.equals("update")) {
                try {
                    if(type.equals("add")){
                        System.out.println("进入添加功能");
                    }
                    if(type.equals("update")){
                        System.out.println("进入更新功能");
                    }
                    addOrUpdate(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(type.equals("delete")&&StringUtils.isNotEmpty(userId)){
                try {
                    deleteUser(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(type.equals("delete")&&StringUtils.isEmpty(userId)){
                try {
                    getOneUser(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else{
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "您没有权限访问");
        }
    }

    //后台主页显示所有用户
    public void showUsers(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 查询操作通常不需要重定向，可以保持原样或根据需求调整
        UserDao userDao = new UserDao();
        List users = userDao.getAllUsers();
        request.setAttribute("users", users);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/userManagement.jsp");
        dispatcher.forward(request, response);
    }

    //删除用户
    public void deleteUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 查询操作通常不需要重定向，可以保持原样或根据需求调整
        UserDao userDao = new UserDao();
        Map<String, Object> map = new HashMap<>();
        map.put("status", "0");
        map.put("data", "操作失败");

        if(userDao.deleteUser(Integer.parseInt(request.getParameter("userId")))){
            map.put("status", "1");
            map.put("data", "删除成功");
        }
        //放回json到前端
        JSONObject msg = JSONObject.fromObject(map);
        System.out.println(msg);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(msg);

    }

    //返回单个数据给前端
    public void getOneUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 查询操作通常不需要重定向，可以保持原样或根据需求调整
        UserDao userDao = new UserDao();
        int userId = Integer.parseInt(request.getParameter("userId"));

        Map<String, Object> map = new HashMap<>();
        map.put("status", "0");
        map.put("data", "操作失败");

        try {
            User oneUser = userDao.getOneUser(userId);
            oneUser.setPassword("");
            if (oneUser != null) {
                map.put("status", "1");
                map.put("data", oneUser);
            }
        } catch (Exception e) {
            map.put("status", "0");
            map.put("data", "查询用户失败: " + e.getMessage());
        }

        //放回json到前端
        JSONObject msg = JSONObject.fromObject(map);
        System.out.println(msg);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(msg);
    }

    //把添加和更新写在一个方法中
    public void addOrUpdate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding("UTF-8");

        String type = request.getParameter("type");   //判断请求业务是add还是update=
        System.out.println("进入"+type+"用户");
        String role = request.getParameter("role");
        System.out.println("用户角色：" + role);

        int id = 0;
        if (type.equals("update")) id = Integer.parseInt(request.getParameter("userId")); //update业务需要指定id

        int realRole = 0;
        if (role.equals("admin")) {
            realRole = 1;
        }
        UserDao userDao = new UserDao();
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Map map = new HashMap();
        map.put("status", "0");
        map.put("data", "操作失败");

        if(StringUtils.isNotEmpty(username)&&StringUtils.isNotEmpty(password)) {
            if (type.equals("add")) {
                if (userDao.addUser(username, password, realRole)) {
                    map.put("status", "1");
                    map.put("data", "添加成功！");
                }
            } else {
                if (userDao.updateUser(id, username, password, realRole)) {
                    map.put("status", "1");
                    map.put("data", "修改成功！");
                }
            }
        }

        //放回json到前端
        JSONObject msg = JSONObject.fromObject(map);
        System.out.println(msg);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(msg);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}