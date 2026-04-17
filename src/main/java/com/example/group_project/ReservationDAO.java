package com.example.group_project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;

public class ReservationDAO {
    private Connection conn;

    public ReservationDAO() {
        conn = DBConnection.getInstance().getConnection();
    }

    /**
     * Creates a new reservation and updates the room status to 'Occupied' inside a Transaction.
     * * @Frontend Usage:
     * - Parameters: int guestId, int roomNumber, java.sql.Date startDate, java.sql.Date endDate, double totalPrice.
     * - Returns: boolean true if the reservation was created AND the room status was updated.
     * - Error Handling: If false, show "Booking failed. Room might be unavailable." 
     * The transaction ensures either BOTH the reservation and room update succeed, or NEITHER do.
     */
    public boolean createReservation(int guestId, int roomNumber, Date startDate, Date endDate, double totalPrice) {
        String insertReservation = "INSERT INTO reservations (GuestID, RoomNumber, StartDate, EndDate, TotalPrice, Status) VALUES (?, ?, ?, ?, ?, 'Confirmed')";
        String updateRoom = "UPDATE rooms SET Status = 'Occupied' WHERE RoomNumber = ?";
        
        try {
            // Disable auto-commit to start a transaction
            conn.setAutoCommit(false);
            
            // 1. Insert Reservation
            try (PreparedStatement stmt1 = conn.prepareStatement(insertReservation)) {
                stmt1.setInt(1, guestId);
                stmt1.setInt(2, roomNumber);
                stmt1.setDate(3, startDate);
                stmt1.setDate(4, endDate);
                stmt1.setDouble(5, totalPrice);
                stmt1.executeUpdate();
            }

            // 2. Update Room Status
            try (PreparedStatement stmt2 = conn.prepareStatement(updateRoom)) {
                stmt2.setInt(1, roomNumber);
                stmt2.executeUpdate();
            }

            // Commit the transaction
            conn.commit();
            return true;

        } 
        catch (SQLException e) {
            try {
                conn.rollback(); // Undo changes if anything fails
            }

            catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }

        finally {
            try {
                conn.setAutoCommit(true); // Reset default behavior
            }

            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes a Check-out, updating both reservation and room statuses.
     * * @Frontend Usage:
     * - Parameters: int reservationId (from selected row), int roomNumber (associated with the reservation).
     * - Returns: boolean true if check-out is successful.
     * - Error Handling: If false, show "Check-out process failed."
     */
    public boolean processCheckOut(int reservationId, int roomNumber) {
        String updateReservation = "UPDATE reservations SET Status = 'Checked-out' WHERE ReservationID = ?";
        String updateRoom = "UPDATE rooms SET Status = 'Available' WHERE RoomNumber = ?";
        
        try {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt1 = conn.prepareStatement(updateReservation)) {
                stmt1.setInt(1, reservationId);
                stmt1.executeUpdate();
            }
            
            try (PreparedStatement stmt2 = conn.prepareStatement(updateRoom)) {
                stmt2.setInt(1, roomNumber);
                stmt2.executeUpdate();
            }
            
            conn.commit();
            return true;
        }

        catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }

        finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}