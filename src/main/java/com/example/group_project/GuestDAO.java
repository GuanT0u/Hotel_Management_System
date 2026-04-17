package com.example.group_project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuestDAO {
    private Connection conn;

    public GuestDAO() {
        conn = DBConnection.getInstance().getConnection();
    }

    /**
     * using for add new guest. and return back new GuestID auto。
     * @return succeed create new GuestID, fail -1。
     */
    public int addNewGuest(String fullName, String contactNumber, String email) {
        String query = "INSERT INTO guests (FullName, ContactNumber, Email) VALUES (?, ?, ?)";
        int generatedId = -1;

        try {
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, fullName);
            stmt.setString(2, contactNumber);
            stmt.setString(3, email);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1); // 获取第一列（也就是新生成的 GuestID）
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return generatedId;
    }

    public List<Map<String, Object>> searchGuests(String keyword) {
        List<Map<String, Object>> guestList = new ArrayList<>();
        // 支持模糊查询：姓名或电话包含关键字
        String query = "SELECT * FROM guests WHERE FullName LIKE ? OR ContactNumber LIKE ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> guest = new HashMap<>();
                    guest.put("GuestID", rs.getInt("GuestID"));
                    guest.put("FullName", rs.getString("FullName"));
                    guest.put("ContactNumber", rs.getString("ContactNumber"));
                    guest.put("Email", rs.getString("Email"));
                    guestList.add(guest);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guestList;
    }

    public boolean updateGuest(int guestId, String fullName, String contactNumber, String email) {
        String query = "UPDATE guests SET FullName = ?, ContactNumber = ?, Email = ? WHERE GuestID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fullName);
            stmt.setString(2, contactNumber);
            stmt.setString(3, email);
            stmt.setInt(4, guestId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}