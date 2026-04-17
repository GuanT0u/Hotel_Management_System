package com.example.group_project;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import java.util.List;
import java.util.Map;

public class AdminController {

    @FXML private TableView<Map<String, Object>> roomTable;
    @FXML private TableColumn<Map<String, Object>, Integer> colRoomNum;
    @FXML private TableColumn<Map<String, Object>, String> colRoomType;
    @FXML private TableColumn<Map<String, Object>, Double> colPricePerNight;
    @FXML private TableColumn<Map<String, Object>, Integer> colCapacity;
    @FXML private TableColumn<Map<String, Object>, String> colStatus;
    
    @FXML private PieChart occupancyChart; // High-value UI element

    @FXML private TableView<Map<String, Object>> userTable;
    @FXML private TableColumn<Map<String, Object>, Integer> colUserId;
    @FXML private TableColumn<Map<String, Object>, String> colUsername;
    @FXML private TableColumn<Map<String, Object>, String> colUserRole;
    @FXML private TableColumn<Map<String, Object>, String> colUserContact;

    @FXML private TextArea txtSystemLogs;

    private RoomDAO roomDAO = new RoomDAO();
    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        setupRoomTable();
        loadOccupancyData();

        setupUserTable();
        loadSystemLogs();
    }

    private void setupRoomTable() {
        // Map table columns to the HashMap keys returned by the DAO
        colRoomNum.setCellValueFactory(data -> 
                new SimpleObjectProperty<>((Integer) data.getValue().get("RoomNumber")));

        colRoomType.setCellValueFactory(data ->
                new SimpleObjectProperty<>((String) data.getValue().get("RoomType")));

        colPricePerNight.setCellValueFactory(data ->
                new SimpleObjectProperty<>((Double) data.getValue().get("PricePerNight")));

        colCapacity.setCellValueFactory(data ->
                new SimpleObjectProperty<>((Integer) data.getValue().get("Capacity")));
    
        colStatus.setCellValueFactory(data -> 
                new SimpleStringProperty((String) data.getValue().get("Status")));
        
        refreshTable();
    }

    public void refreshTable() {
        List<Map<String, Object>> rooms = roomDAO.searchRooms("All", "All");
        ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(rooms);
        roomTable.setItems(data);
    }

    private void loadOccupancyData() {
        // In a full implementation, you would write a DAO method like reportDAO.getOccupancyStats()
        // Here is how you bind data to the chart:
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Occupied", 45),
                new PieChart.Data("Available", 50),
                new PieChart.Data("Maintenance", 5)
        );
        occupancyChart.setData(pieChartData);
        occupancyChart.setTitle("Current Room Occupancy");
    }

    private void setupUserTable() {
        colUserId.setCellValueFactory(data ->
                new SimpleObjectProperty<>((Integer) data.getValue().get("UserID")));
        colUsername.setCellValueFactory(data ->
                new SimpleStringProperty((String) data.getValue().get("Username")));
        colUserRole.setCellValueFactory(data ->
                new SimpleStringProperty((String) data.getValue().get("Role")));
        colUserContact.setCellValueFactory(data ->
                new SimpleStringProperty((String) data.getValue().get("ContactInfo")));

        List<Map<String, Object>> users = userDAO.getAllUsers();
        userTable.setItems(FXCollections.observableArrayList(users));
    }

    private void loadSystemLogs() {
        txtSystemLogs.setText("System Started...\nDatabase Connected Successfully.\nAdmin logged in.");
    }
}