package com.example.group_project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class RoomDAO {
    private Connection conn;

    public RoomDAO() {
        conn = DBConnection.getInstance().getConnection();
    }

    /**
     * Searches for rooms based on type and availability status.
     * * @Frontend Usage:
     * - Parameters: String type (from JComboBox: "Single", "Double", "Suite", or "All"), 
     * String status (from JComboBox: "Available", "Occupied", "Under Maintenance", or "All").
     * - Returns: List<Map<String, Object>> containing room details. Map keys: RoomNumber, RoomType, PricePerNight, Capacity, Status.
     * - Error Handling: If the list is empty, display a message in the GUI "No rooms found matching the criteria". 
     * Bind the returned list directly to a DefaultTableModel for a JTable.
     */
    public List<Map<String, Object>> searchRooms(String type, String status) {
        List<Map<String, Object>> roomList = new ArrayList<>();
        
        StringBuilder query = new StringBuilder("SELECT * FROM rooms WHERE 1=1");
        if (!type.equals("All")) query.append(" AND RoomType = ?");
        if (!status.equals("All")) query.append(" AND Status = ?");
        
        try (PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            int paramIndex = 1;
            if (!type.equals("All")) stmt.setString(paramIndex++, type);
            if (!status.equals("All")) stmt.setString(paramIndex, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> room = new HashMap<>();
                    room.put("RoomNumber", rs.getInt("RoomNumber"));
                    room.put("RoomType", rs.getString("RoomType"));
                    room.put("PricePerNight", rs.getDouble("PricePerNight"));
                    room.put("Capacity", rs.getInt("Capacity"));
                    room.put("Status", rs.getString("Status"));
                    roomList.add(room);
                }
            }
        }
        
        catch (SQLException e) {
            e.printStackTrace();
        }
        return roomList;
    }

    public Map<String, Integer> getRoomStatusCounts() {
        Map<String, Integer> counts = new HashMap<>();
        String query = "SELECT Status, COUNT(*) as Count FROM rooms GROUP BY Status";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                counts.put(rs.getString("Status"), rs.getInt("Count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counts;
    }

    public boolean addRoom(int roomNumber, String roomType, double pricePerNight, int capacity, String status) {
        String query = "INSERT INTO rooms (RoomNumber, RoomType, PricePerNight, Capacity, Status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, roomNumber);
            stmt.setString(2, roomType);
            stmt.setDouble(3, pricePerNight);
            stmt.setInt(4, capacity);
            stmt.setString(5, status);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // if roomNum already have, itll throw exception and rt F
        }
    }

    public boolean updateRoom(int roomNumber, String roomType, double pricePerNight, int capacity, String status) {
        String query = "UPDATE rooms SET RoomType = ?, PricePerNight = ?, Capacity = ?, Status = ? WHERE RoomNumber = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, roomType);
            stmt.setDouble(2, pricePerNight);
            stmt.setInt(3, capacity);
            stmt.setString(4, status);
            stmt.setInt(5, roomNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRoom(int roomNumber) {
        String query = "DELETE FROM rooms WHERE RoomNumber = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, roomNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // if the room already got reservation (connect fk), del will fail
        }
    }

    //filtrate all available roomNum based on roomtype and reservation date
    public List<Integer> getAvailableRoomsForBooking(String roomType, java.sql.Date startDate, java.sql.Date endDate) {
        List<Integer> availableRooms = new ArrayList<>();

        // 核心 SQL：筛选条件排除掉那些在指定时间内有 Confirmed/Checked-in 记录的房间
        String query = "SELECT RoomNumber FROM rooms WHERE RoomType = ? AND Status != 'Under Maintenance' " +
                "AND RoomNumber NOT IN (" +
                "    SELECT RoomNumber FROM reservations " +
                "    WHERE Status IN ('Confirmed', 'Checked-in') " +
                "    AND (StartDate < ? AND EndDate > ?)" +
                ")";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, roomType);
            stmt.setDate(2, endDate);   // 注意顺序：旧开始时间 < 新结束时间
            stmt.setDate(3, startDate); // 注意顺序：旧结束时间 > 新开始时间

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    availableRooms.add(rs.getInt("RoomNumber"));
                }
            }
        }

        catch (SQLException e) {
            e.printStackTrace();
        }
        return availableRooms;
    }
}