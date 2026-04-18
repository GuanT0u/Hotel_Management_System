package com.example.group_project;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.io.IOException;
import java.util.Optional;

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

    @FXML
    public void handleAddReceptionist() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddUserDialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Receptionist");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // 弹窗关闭后，检查是否添加成功
            AddUserController controller = loader.getController();
            if (controller.isOperationSuccessful()) {
                // 刷新表格
                setupUserTable();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRevokeAccess() {
        // get what u selecting
        Map<String, Object> selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to revoke access.");
            return;
        }

        // check is the user that u selected are admin or not
        String role = (String) selectedUser.get("Role");
        if ("Admin".equalsIgnoreCase(role)) {
            showAlert(Alert.AlertType.ERROR, "Action Denied", "You cannot revoke access from an Admin account.");
            return;
        }

        // 2nd confirm dialog if u wanna revoke access for the user that u selected
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Revoke Access for " + selectedUser.get("Username") + "?");
        confirmDialog.setContentText("This user will no longer be able to log into the system.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // call backend to del the user that u selected
            int userId = (int) selectedUser.get("UserID");
            boolean success = userDAO.deleteUser(userId);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User access revoked successfully.");
                setupUserTable(); // refresh tb
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete the user.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}