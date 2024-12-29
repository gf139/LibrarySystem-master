package com.zy.dao;

import com.zy.bean.BorrowedBook;
import com.zy.utils.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BorrowedBooksDAO {
    Connection con = null;

    public boolean BorrowedBookAdd(BorrowedBook borrowedBook) {
        boolean isSuccess = false;
        PreparedStatement psmt = null;
        String sql = "insert into borrowedbooks values(null,?,?,?,?,?,?)";
        try {
            con = JdbcUtil.getConnecttion();
            psmt = con.prepareStatement(sql);
            psmt.setInt(1, borrowedBook.getBookID());
            psmt.setString(2, borrowedBook.getBookName());
            psmt.setInt(3, borrowedBook.getBorrowerID());
            psmt.setString(4, borrowedBook.getBorrowerName());
            psmt.setDate(5, borrowedBook.getBorrowDate());
            psmt.setDate(6, borrowedBook.getReturnDate());
            psmt.executeUpdate();
            isSuccess = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.release(psmt, con);
        }
        return isSuccess;
    }

    public int BorrowedBookCount(BorrowedBook borrowedBook) {
        String sql = "SELECT COUNT(*) FROM borrowedbooks WHERE BookID = ? AND BorrowerID = ?";
        try {
            con = JdbcUtil.getConnecttion();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, borrowedBook.getBookID());
            pstmt.setInt(2, borrowedBook.getBorrowerID());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean deletByIdAndUserIdAndReturnDate(BorrowedBook borrowedBook) {
        String selectSql = "SELECT id FROM borrowedbooks WHERE BookID = ? AND BorrowerID = ? ORDER BY BorrowDate ASC LIMIT 1";
        String deleteSql = "DELETE FROM borrowedbooks WHERE id = ?";
        try {
            con = JdbcUtil.getConnecttion();
            PreparedStatement pstmt = con.prepareStatement(selectSql);
            PreparedStatement deletePstmt = con.prepareStatement(deleteSql);

            pstmt.setInt(1, borrowedBook.getBookID());
            pstmt.setInt(2, borrowedBook.getBorrowerID());

            //执行查询
            ResultSet rs = pstmt.executeQuery();

            //找到了才删除
            if (rs.next()) {
                int recordId = rs.getInt("id");

                // 设置删除语句的参数
                deletePstmt.setInt(1, recordId);

                // 执行删除操作
                int affectedRows = deletePstmt.executeUpdate();

                // 返回删除是否成功
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public List<BorrowedBook> getOverdueRecords(int borrowerId) {
        List<BorrowedBook> overdueBooks = new ArrayList<>();
        String sql = "SELECT * FROM borrowedbooks WHERE BorrowerID = ? AND ReturnDate IS NULL AND DATEDIFF(CURDATE(), BorrowDate) > 7";
        try {
            con = JdbcUtil.getConnecttion();
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, borrowerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                BorrowedBook book = new BorrowedBook();
                book.setId(rs.getInt("id"));
                book.setBookID(rs.getInt("BookID"));
                book.setBookName(rs.getString("BookName"));
                book.setBorrowerID(rs.getInt("BorrowerID"));
                book.setBorrowerName(rs.getString("BorrowerName"));
                book.setBorrowDate(rs.getDate("BorrowDate"));
                book.setReturnDate(rs.getDate("ReturnDate"));
                overdueBooks.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return overdueBooks;
    }
}
