package com.example.group_project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LogDAO {
    private Connection conn;

    public LogDAO() {
        conn = DBConnection.getInstance().getConnection();
    }

    public void logAction(String action) {
        int userId = UserSession.getCurrentUserId();
        String query = "INSERT INTO systemlogs (UserID, ActionPerformed) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            if (userId != -1) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER); // if not legged in
            }
            stmt.setString(2, action);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to write log: " + action);
            e.printStackTrace();
        }
    }

    // combine all the log to a long string, using for display in textarea
    public String getAllLogsAsString() {
        StringBuilder sb = new StringBuilder();
        // connect db for sql query, turn the userid to username for more readable
        String query = "SELECT s.LogTimestamp, u.Username, s.ActionPerformed " +
                "FROM systemlogs s LEFT JOIN users u ON s.UserID = u.UserID " +
                "ORDER BY s.LogTimestamp DESC";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String time = rs.getTimestamp("LogTimestamp").toString();
                String user = rs.getString("Username") != null ? rs.getString("Username") : "System";
                String action = rs.getString("ActionPerformed");

                sb.append("[").append(time).append("] ")
                        .append(user).append(" : ")
                        .append(action).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}