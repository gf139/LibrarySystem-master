package com.zy.dao;

import com.zy.bean.User;
import com.zy.utils.JdbcUtil;
import com.zy.utils.Md5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private Connection con;
    public boolean isRegistered(String username,String password) throws SQLException {
        boolean IsRegistered=false;
        password= Md5.md5Password(password);
        con= JdbcUtil.getConnecttion();
        String sql="SELECT * FROM user WHERE username=? and password=?";
        try{
            PreparedStatement psmt=con.prepareStatement(sql);
            psmt.setString(1,username);
            psmt.setString(2,password);
            ResultSet rs=psmt.executeQuery();
            System.out.println(rs);
            while(rs.next()){
                System.out.println(rs.getString(2)+"\t"+rs.getString(3));
                IsRegistered=true;
            }

        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            con.close();
        }
        return IsRegistered;
    }

    public int getUserIdByUsername(String username) throws SQLException {
        int userId = -1; // 默认值表示未找到用户
        con= JdbcUtil.getConnecttion();
        String sql="SELECT id FROM user WHERE username=?";
        try{
            PreparedStatement psmt=con.prepareStatement(sql);
            psmt.setString(1,username);
            ResultSet rs=psmt.executeQuery();
            System.out.println(rs);
            while(rs.next()){
                userId = rs.getInt("id");
            }

        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            con.close();
        }
        return userId;
    }

    public boolean borrowBook(int userId,String username,int bookId) throws SQLException {
        boolean IsBorrowed=false;
        //如果借到数量大于0，则借阅成功
        con= JdbcUtil.getConnecttion();
        String sql="SELECT id FROM user WHERE username=?";
        try{
            PreparedStatement psmt=con.prepareStatement(sql);
            psmt.setString(1,username);
            ResultSet rs=psmt.executeQuery();
            System.out.println(rs);
            while(rs.next()){
                userId = rs.getInt("id");
            }

        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            con.close();
        }
        return IsBorrowed;
    }


    public boolean addUser(String username, String password, int role) throws SQLException {
        boolean isAdded = false;
        password = Md5.md5Password(password);
        con = JdbcUtil.getConnecttion();
        String sql = "INSERT INTO user (id,username, password, role) VALUES (null,?, ?, ?)";
        try {
            PreparedStatement psmt = con.prepareStatement(sql);
            psmt.setString(1, username);
            psmt.setString(2, password);
            psmt.setInt(3, role);
            psmt.executeUpdate();
            isAdded = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            con.close();
        }
        return isAdded;
    }

    public boolean updateUser(int userId, String username, String password, int role) throws SQLException {
        boolean isUpdated = false;
        password = Md5.md5Password(password);
        con = JdbcUtil.getConnecttion();
        String sql = "UPDATE user SET username = ?, password = ?, role = ? WHERE id = ?";
        try {
            PreparedStatement psmt = con.prepareStatement(sql);
            psmt.setString(1, username);
            psmt.setString(2, password);
            psmt.setInt(3, role);
            psmt.setInt(4, userId);
            psmt.executeUpdate();
            isUpdated = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            con.close();
        }
        return isUpdated;
    }

    public boolean deleteUser(int userId) throws SQLException {
        boolean isDeleted = false;
        con = JdbcUtil.getConnecttion();
        String sql = "DELETE FROM user WHERE id = ?";
        try {
            PreparedStatement psmt = con.prepareStatement(sql);
            psmt.setInt(1, userId);
            int rowsAffected = psmt.executeUpdate();
            isDeleted = rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            con.close();
        }
        return isDeleted;
    }

    public List<User> getAllUsers() throws SQLException {
        PreparedStatement psmt = null;
        List<User> users = new ArrayList<>();
        con = JdbcUtil.getConnecttion();
        String sql = "SELECT * FROM user";
        try {
            psmt = con.prepareStatement(sql);
            ResultSet rs = psmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            con.close();
            JdbcUtil.release(psmt, con);
        }
        return users;
    }

    //获取单个用户
    public User getOneUser(int userId) throws SQLException {
        User user = new User();
        con = JdbcUtil.getConnecttion();
        String sql = "SELECT * FROM user where id = ?";
        try {
            PreparedStatement psmt = con.prepareStatement(sql);
            psmt.setInt(1, userId);
            ResultSet rs = psmt.executeQuery();
            while (rs.next()) {
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            con.close();
        }
        return user;
    }

    // 新增方法获取用户角色
    public String getUserRoleByUsername(String username) throws SQLException {
        con = JdbcUtil.getConnecttion();
        String sql = "SELECT role FROM user WHERE username = ?";
        try {
            PreparedStatement psmt = con.prepareStatement(sql);
            psmt.setString(1, username);
            ResultSet rs = psmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            con.close();
        }
        return null;
    }
}
