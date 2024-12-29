package com.zy.servlet;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date; // 导入 java.util.Date
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.google.gson.Gson;

import com.zy.bean.Book;
import com.zy.bean.BorrowedBook;
import com.zy.dao.BookDao;
import com.zy.dao.BorrowedBooksDAO;
import net.sf.json.JSONObject;

@WebServlet(name = "BookServlet", urlPatterns = {"/book.do"})
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024
)
public class BookServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String type = request.getParameter("type");
        if (type.equals("add") || type.equals("update")) {
            try {
                addOrUpdate(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String type = request.getParameter("type");
        //不分页显示所有书籍
        if (type.equals("show")) {
            try {
                showBooks(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //分页显示所有书籍
        if (type.equals("pageList")) {
            try {
                pageList(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (type.equals("delete")) {
            try {
                deleteBook(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (type.equals("batchDel")) {
            try {
                deleteManyBooks(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (type.equals("update")) {
            try {
                updatePage(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (type.equals("search")) {
            try {
                searchBook(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (type.equals("detail")) {
            try {
                bookDetail(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (type.equals("borrow")) {
            try {
                borrowBook(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (type.equals("return")) {
            try {
                returnBook(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (type.equals("viewOverdue")) {
            try {
                viewOverdueRecords(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void bookDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int id = Integer.parseInt(request.getParameter("id"));
        BookDao bookDao = new BookDao();
        Book book = bookDao.queryBookById(id);
        request.setAttribute("book", book);
        request.getRequestDispatcher("detail.jsp").forward(request, response);
    }

    //根据条件查询书籍并分页
    public void searchBook(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取查询条件的参数
        String name = request.getParameter("bookname");
        int cateId = Integer.parseInt(request.getParameter("cateId"));
        double minprice;
        double maxprice;
        try {
            //防止查询表单minprice输入为空转化为double出现异常
            minprice = Integer.valueOf(request.getParameter("minprice"));
        } catch (NumberFormatException e) {
            minprice = 0;
        }
        try {
            //防止查询表单minprice输入为空转化为double出现异常
            maxprice = Integer.valueOf(request.getParameter("maxprice"));
        } catch (NumberFormatException e) {
            maxprice = 0;
        }

        String minpdate = request.getParameter("minPdate");
        String maxpdate = request.getParameter("maxPdate");

        String queryParams = "&bookname=" + name + "&cateId=" + cateId + "&minprice=" + minprice + "&maxprice=" + maxprice + "&minPdate=" + minpdate + "&maxPdate=" + maxpdate;
        request.setAttribute("queryParams", queryParams);
        //因为本查询方式是form的get，会刷新当前页面，导致表单数据丢失，这里为了交互良好起见，把提交前表单的值写入Session中
        HttpSession session = request.getSession();
        session.setAttribute("formerKeywords", name);
        session.setAttribute("formerCateId", cateId);
        session.setAttribute("formerMinprice", minprice);
        session.setAttribute("formerMaxprice", maxprice);
        session.setAttribute("formerMinpdate", minpdate);
        session.setAttribute("formerMaxpdate", minpdate);
        //sql拼接，以便号获取List<Book>和满足条件的count,这里是定义条件
        String baseSql = "from book where price>=" + minprice;  //如果前端表单传入minprice为空，则minprice=0为异常处理的结果
        String sql = baseSql;
        if (maxprice != 0) sql = baseSql + " and price<=" + maxprice;        //如果前端表单maxprice输入不为空
        if (!minpdate.equals("")) sql += " and pdate>= '" + minpdate + "'";
        if (!maxpdate.equals("")) sql += " and pdate<= '" + maxpdate + "'";
        if (cateId != 0) sql += " and cateId=" + cateId;
        if (!name.equals("")) {
            String keywords = "";
            for (int i = 0; i < name.length(); i++) //将关键字分解成一个一个字进行模糊查询
                keywords += "%" + name.charAt(i);
            sql += " and name like " + "'" + keywords + "%'";
        }
        String countSql = "select count(*) " + sql;   //查询满足条件的记录数
        sql = "select * " + sql + " limit ?,?";
        System.out.println(sql);
        System.out.println(countSql);


        //以下作为分页处理
        BookDao bookDao = new BookDao();
        String p = request.getParameter("page");
        int page;
        try {
            //当前页数
            page = Integer.valueOf(p);
        } catch (NumberFormatException e) {
            page = 1;
        }
        //书籍总数
        int totalBooks = bookDao.counts(countSql);
        //每页书籍数
        int booksPerPage = 5;
        //总页数
        int totalPages = totalBooks % booksPerPage == 0 ? totalBooks / booksPerPage : totalBooks / booksPerPage + 1;
        //本页起始书籍序号
        int beginIndex = (page - 1) * booksPerPage;
        List<Book> books = bookDao.listAllOf(beginIndex, booksPerPage, sql);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("page", page);
        request.setAttribute("books", books);
        request.setAttribute("type", "search");  //这里区分请求是查询还是不查询 因为分页按钮的href=/book.do?type=${type}&page=页号
        request.getRequestDispatcher("home.jsp").forward(request, response);
    }

    //删除单本书
    public void deleteBook(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int id = Integer.parseInt(request.getParameter("id"));
        BookDao bookDao = new BookDao();
        bookDao.deleteById(id);

    }

    public void updatePage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int id = Integer.parseInt(request.getParameter("id"));
        BookDao bookDao = new BookDao();
        Book book = bookDao.queryBookById(id);
        request.setAttribute("book", book);
        RequestDispatcher rd = request.getRequestDispatcher("update.jsp");
        rd.forward(request, response);

    }

    public void deleteManyBooks(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String idStr = request.getParameter("idStr");
        idStr = idStr.substring(0, idStr.length() - 1);
        String[] ids = idStr.split(",");
        int[] bookIds = new int[20];
        for (int i = 0; i < ids.length; i++) {
            bookIds[i] = Integer.parseInt(ids[i]);
        }
        BookDao bookDao = new BookDao();
        bookDao.deleteMany(bookIds);
    }

    //后台主页显示所有书籍
    public void showBooks(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BookDao bookDao = new BookDao();
        List list = bookDao.queryAllBooks();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        request.setAttribute("books", list);
        RequestDispatcher rd = request.getRequestDispatcher("home.jsp");
        rd.forward(request, response);
    }


    //把添加和更新写在一个方法中
    public void addOrUpdate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取表单数据
        request.setCharacterEncoding("UTF-8");
        String type = request.getParameter("type");   //判断请求业务是add还是update
        System.out.println(type);
        int id = 0;   //add业务不需要指定id
        if (type.equals("update")) id = Integer.parseInt(request.getParameter("id")); //update业务需要指定id
        String bookname = request.getParameter("bookname");
        int cateId = Integer.parseInt(request.getParameter("cateId"));
        double price = Double.parseDouble(request.getParameter("price"));
        String author = request.getParameter("author");
        String pdate = request.getParameter("pdate");
        String address = request.getParameter("address");
        String description = request.getParameter("description");
        String detail = request.getParameter("content");

        //上传图片业务处理
        Part part = request.getPart("img");//前台的文件标签的name,若ajax直接提交表单，这里无法获取
        String file = part.getHeader("Content-Disposition");
        //获取文件名
        String fileName = file.substring(file.lastIndexOf("=") + 2, file.length() - 1);
        String img = "";
        if (!fileName.equals("")) {         //这里判断是否上传了图片
            fileName = new Date().getTime() + "_" + new Random().nextInt(1000) + fileName;  //防止图片重名
            String relativePath = "/assets/bookImg/";      //图片上传到Tomcat相对本项目的路径
            String basePath = request.getSession().getServletContext().getRealPath("");  //项目根目录
            String absolutePath = basePath + relativePath + fileName; //保存在服务器绝对路径
            //上传文件到部署路劲
            part.write(absolutePath);
            img = relativePath + fileName;
        } else {
            img = request.getParameter("formerImg");
        }
        //添加进数据库
        Book book = new Book(id, bookname, price, author, cateId, pdate, img, description, detail, address,1);
        BookDao bookDao = new BookDao();
        Map map = new HashMap();
        map.put("status", "0");
        map.put("data", "操作失败");
        if (type.equals("add")) {
            if (bookDao.bookAdd(book)) {
                map.put("status", "1");
                map.put("data", "添加成功！");
            }
        } else {
            if (bookDao.bookUpdate(book)) {
                map.put("status", "1");
                map.put("data", "修改成功！");
            }
        }

        //放回json到前端
        JSONObject msg = JSONObject.fromObject(map);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(msg);
    }

    //后台主页查询出所有记录并分页显示
    public void pageList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BookDao bookDao = new BookDao();
        String p = request.getParameter("page");
        int page;
        try {
            //当前页数
            page = Integer.valueOf(p);
        } catch (NumberFormatException e) {
            page = 1;
        }
        //书籍总数
        int totalBooks = bookDao.counts("select count(*) from book");
        //每页书籍数
        int booksPerPage = 5;
        //总页数
        int totalPages = totalBooks % booksPerPage == 0 ? totalBooks / booksPerPage : totalBooks / booksPerPage + 1;
        //本页起始书籍序号
        int beginIndex = (page - 1) * booksPerPage;
        List<Book> books = bookDao.getAllBooks(beginIndex, booksPerPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("page", page);
        request.setAttribute("books", books);
        request.setAttribute("type", "pageList");
        request.getRequestDispatcher("home.jsp").forward(request, response);
    }

    public void borrowBook(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 实例化图书数据库
        BorrowedBooksDAO borrowedBooksDAO = new BorrowedBooksDAO();
        BookDao bookDao = new BookDao();

        // 获取图书ID
        String bookIdStr = request.getParameter("id");
        int bookId = Integer.parseInt(bookIdStr);

        // 获取当前用户的ID或其他信息，这里假设是从session中获取
        HttpSession session = request.getSession();
        int userId = (Integer) session.getAttribute("userId");
        String userName = (String) session.getAttribute("username");

        String bookName = bookDao.getBookNameById(bookId);

        // 构造响应信息
        Map<String, Object> resultMap = new HashMap<>();


        Book book = bookDao.queryBookById(bookId);
        int count = book.getCount();
        // 有书才能借
        if (count <= 0) {
            resultMap.put("success", false);
            resultMap.put("message", "借阅失败，图书数量不足或已借出");
            System.out.println("借阅失败，图书数量不足或已借出,数量是:"+count);
        } else {
            // 调用服务层方法进行借书操作
            // 条件插入
            BorrowedBook borrowedBook = new BorrowedBook();
            borrowedBook.setBookID(bookId);
            borrowedBook.setBookName(bookName);
            borrowedBook.setBorrowerName(userName);
            borrowedBook.setBorrowerID(userId);
            // 获取当前日期
            java.util.Date utilDate = new java.util.Date();
            // 将 java.util.Date 转换为 java.sql.Date
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            borrowedBook.setBorrowDate(sqlDate);

            // 书的数量减一更新
            book.setCount(count - 1);
            bookDao.bookUpdate(book);
            // 插入借阅表
            boolean isBorrowed = borrowedBooksDAO.BorrowedBookAdd(borrowedBook);

            if (isBorrowed) {
                resultMap.put("success", true);
                resultMap.put("message", "借阅成功");
            } else {
                resultMap.put("success", false);
                resultMap.put("message", "借阅失败，图书数量不足或已借出");
            }
        }

        // 设置响应内容类型
        response.setContentType("application/json");
        // 将响应信息转换为JSON格式并返回给前端
        JSONObject msg = JSONObject.fromObject(resultMap);
        System.out.println(msg);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(msg);
    }


    public void returnBook(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 实例化图书数据库
        BorrowedBooksDAO borrowedBooksDAO = new BorrowedBooksDAO();
        BookDao bookDao = new BookDao();

        // 获取图书ID
        String bookIdStr = request.getParameter("id");
        int bookId = Integer.parseInt(bookIdStr);
        // 获取当前用户的ID或其他信息，这里假设是从session中获取
        HttpSession session = request.getSession();
        int userId = (Integer) session.getAttribute("userId");
        String userName = (String) session.getAttribute("username");

        BorrowedBook borrowedBook = new BorrowedBook();
        borrowedBook.setBookID(bookId);
        borrowedBook.setBorrowerID(userId);
        int i = borrowedBooksDAO.BorrowedBookCount(borrowedBook);

        // 构造响应信息
        Map<String, Object> resultMap = new HashMap<>();

        if(i<=0){
            System.out.println("没有借过");
            resultMap.put("success", false);
            resultMap.put("message", "还书失败，未查到借阅情况");
        }else{
            //删除最早图书的消息
            boolean b = borrowedBooksDAO.deletByIdAndUserIdAndReturnDate(borrowedBook);

            //在数据库里加1
            Book book = bookDao.queryBookById(bookId);
            book.setCount(book.getCount()+1);
            bookDao.bookUpdate(book);

            //封装正确信息返回
            resultMap.put("success", true);
            resultMap.put("message", "归还成功");
        }

        // 设置响应内容类型
        response.setContentType("application/json");
        // 将响应信息转换为JSON格式并返回给前端
        JSONObject msg = JSONObject.fromObject(resultMap);
        System.out.println(msg);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(msg);
    }


    public void viewOverdueRecords(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        int userId = (Integer) session.getAttribute("userId");

        BorrowedBooksDAO borrowedBooksDAO = new BorrowedBooksDAO();
        List<BorrowedBook> overdueBooks = borrowedBooksDAO.getOverdueRecords(userId);

        int overdueCount = overdueBooks.size();
        request.setAttribute("overdueBooks", overdueBooks);
        request.setAttribute("overdueCount", overdueCount);

        if (overdueCount >= 3) {
            session.setAttribute("accountDisabled", true);
        }

        // 计算每个借阅记录的逾期天数
        LocalDate currentDate = LocalDate.now();
        for (BorrowedBook book : overdueBooks) {
            LocalDate borrowDate = book.getBorrowDate().toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(borrowDate, currentDate);
            book.setDaysOverdue(daysBetween); // 假设BorrowedBook类有一个setDaysOverdue方法
        }

        request.getRequestDispatcher("overdue_records.jsp").forward(request, response);
    }
}