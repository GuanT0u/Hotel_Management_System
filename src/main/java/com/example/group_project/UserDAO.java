package com.example.group_project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDAO {
    private Connection conn;

    public UserDAO() {
        conn = DBConnection.getInstance().getConnection();
    }

    /**
     * Authenticates a user trying to log into the system.
     * * @Frontend Usage:
     * - Parameters: String username (from JTextField), String password (from JPasswordField).
     * - Returns: String representing the Role ("Admin" or "Receptionist"), or null if login fails.
     * - Error Handling: If null is returned, show a JOptionPane error dialog "Invalid Username or Password". 
     * If an exception occurs, show "Database connection error".
     */
    public String authenticateUser(String username, String password) {
        String role = null;
        // In a real production environment, you would hash the incoming password and compare. 
        // Assuming plain text matching for the scope of standard OOP GUI testing unless hashed check is implemented.
        String query = "SELECT Role FROM users WHERE Username = ? AND Password = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); 
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    role = rs.getString("Role");
                }
            }
        }
        
        catch (SQLException e) {
            e.printStackTrace();
        }
        return role;
    }

    /**
     * Adds a new receptionist to the system (Admin only).
     * * @Frontend Usage:
     * - Parameters: String username, String password, String contactInfo.
     * - Returns: boolean true if insertion was successful, false otherwise.
     * - Error Handling: If false, it likely means the username already exists (UNIQUE constraint). 
     * Show a JOptionPane warning: "Username already taken or database error."
     */
    public boolean addReceptionist(String username, String password, String contactInfo) {
        String query = "INSERT INTO users (Username, Password, Role, ContactInfo) VALUES (?, ?, 'Receptionist', ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // Note: Hash this before passing in production
            stmt.setString(3, contactInfo);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
        
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<Map<String, Object>> getAllUsers() {
        List<Map<String, Object>> userList = new ArrayList<>();
        String query = "SELECT UserID, Username, Role, ContactInfo FROM users";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("UserID", rs.getInt("UserID"));
                user.put("Username", rs.getString("Username"));
                user.put("Role", rs.getString("Role"));
                user.put("ContactInfo", rs.getString("ContactInfo"));
                userList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    public boolean deleteUser(int userId) {
        String query = "DELETE FROM users WHERE UserID = ? AND Role != 'Admin'";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}