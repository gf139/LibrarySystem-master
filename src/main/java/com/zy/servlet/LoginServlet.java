package com.zy.servlet;

import com.zy.bean.BorrowedBook;
import com.zy.dao.BorrowedBooksDAO;
import com.zy.dao.UserDao;
import com.zy.utils.VerifyCodeUtils;
import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name="LoginServlet", urlPatterns={"/login.do"})
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String  username= request.getParameter("username");
        String  password= request.getParameter("password");
        String checkcode=request.getParameter("checkcode");
        HttpSession session=request.getSession();
        String code= (String) session.getAttribute("verCode");
        Map map=new HashMap();
        map.put("status","0");
        map.put("data","登录失败");


        //TODO 验证码临时屏蔽


        if(checkcode.length()==4) {   //设定验证码为4位
            checkcode = VerifyCodeUtils.codeToLowerCase(checkcode);  //前台输入的验证码转为小写字母
            if (code.equals(checkcode)) {
                UserDao userDao = new UserDao();
                try {
                    if (userDao.isRegistered(username, password)) {
                        int userId = userDao.getUserIdByUsername(username);
                        String role = userDao.getUserRoleByUsername(username); // 新增获取用户角色的方法
                        BorrowedBooksDAO borrowedBooksDAO = new BorrowedBooksDAO();
                        List<BorrowedBook> overdueBooks = borrowedBooksDAO.getOverdueRecords(userId);
                        int overdueCount = overdueBooks.size();

                        if (overdueCount >= 3) {
                            map.put("status", "0");
                            map.put("data", "账号已被停用，请联系管理员。");
                            session.setAttribute("accountDisabled", true);
                        } else {
                            session.setAttribute("userId", userId);
                            session.setAttribute("username", username);
                            session.setAttribute("overdueCount", overdueCount);
                            session.setAttribute("role", role); // 存储用户角色
                            map.put("status", "1");
                            map.put("data", "登录成功！");
                            System.out.println("userId:" + userId + " username:" + username + " role:" + role);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        // 设置响应内容类型
        response.setContentType("application/json");
        // 将响应信息转换为JSON格式并返回给前端
        JSONObject msg=JSONObject.fromObject(map);
        System.out.println(msg);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out=response.getWriter();
        out.println(msg);
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String type=request.getParameter("type");
        if(type.equals("logout")){
            HttpSession session=request.getSession();
            session.removeAttribute("username");
            session.removeAttribute("userId");
            response.sendRedirect("/login.jsp");
        }
    }
}
