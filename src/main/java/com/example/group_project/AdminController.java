package com.example.group_project;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.scene.Node;
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
    private LogDAO logDAO = new LogDAO();

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

        loadOccupancyData();
    }

    private void loadOccupancyData() {

        // call DAO to get newest statistics
        Map<String, Integer> statusCounts = roomDAO.getRoomStatusCounts();

        // got an empty dataset
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // loop the db results and create chart slices
        // for clear checking, i put the specific num like: "Occupied (5)"
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            String labelName = entry.getKey() + " (" + entry.getValue() + ")";
            pieChartData.add(new PieChart.Data(labelName, entry.getValue()));
        }

        // put those data to the piechart
        occupancyChart.setData(pieChartData);
        occupancyChart.setTitle("Real-Time Room Occupancy");
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
        // call DAO to got sys log
        String logs = logDAO.getAllLogsAsString();
        txtSystemLogs.setText(logs);
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

            // check is work or not after dialog close
            AddUserController controller = loader.getController();
            if (controller.isOperationSuccessful()) {
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

                logDAO.logAction("Has Revoked Access for " + selectedUser.get("Username"));
                loadSystemLogs();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete the user.");
            }
        }
    }

    @FXML
    public void handleAddRoom() {
        openRoomDialog(null);
    }

    @FXML
    public void handleEditRoom() {
        Map<String, Object> selectedRoom = roomTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a room from the table to edit.");
            return;
        }
        openRoomDialog(selectedRoom);
    }

    private void openRoomDialog(Map<String, Object> roomData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RoomDialog.fxml"));
            Parent root = loader.load();

            RoomDialogController controller = loader.getController();
            if (roomData != null) {
                controller.setRoomData(roomData); // inject data to enter edit mode
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(roomData == null ? "Add Room" : "Edit Room");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // if the operation working, refresh tb
            if (controller.isOperationSuccessful()) {
                refreshTable();

                logDAO.logAction(roomData == null ? "Added a new room." : "Updated room details.");
                loadSystemLogs();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDeleteRoom() {
        Map<String, Object> selectedRoom = roomTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a room to delete.");
            return;
        }

        int roomNum = (int) selectedRoom.get("RoomNumber");

        // 2nd confirm b4 del dangerous operation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Room");
        confirm.setHeaderText("Delete Room " + roomNum + "?");
        confirm.setContentText("Warning: If this room is linked to existing reservations, the database will block the deletion to protect data integrity.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = roomDAO.deleteRoom(roomNum);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Room deleted successfully.");
                refreshTable();

                new LogDAO().logAction("Has Deleted a Room for " + roomNum);
                loadSystemLogs();
            } else {
                // capture and alert fk constraint got error
                showAlert(Alert.AlertType.ERROR, "Delete Failed", "Cannot delete this room. It is likely tied to existing guest reservations in the database.");
            }
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        // notice logout info
        if (logDAO == null) {
            logDAO = new LogDAO();
        }
        logDAO.logAction("Logged out of the system.");

        // clear global login status (Session)
        UserSession.logout();

        // redirect to login page
        try {
            // get which stage user at (admi / stuff dashboard)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // load Login.fxml
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));

            // change back the size of the login interface
            Scene scene = new Scene(root, 500, 400);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("HRMS - Login");
            stage.setResizable(false); // dont allow resize
            stage.centerOnScreen();    // put it on mid of moniter

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "System Error", "Failed to load the login screen.");
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