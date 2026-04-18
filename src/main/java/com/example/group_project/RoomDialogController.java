package com.example.group_project;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.Map;

public class RoomDialogController {

    @FXML private Label lblTitle;
    @FXML private TextField txtRoomNumber;
    @FXML private ComboBox<String> comboType;
    @FXML private TextField txtPrice;
    @FXML private TextField txtCapacity;
    @FXML private ComboBox<String> comboStatus;
    @FXML private Button btnSave;

    private RoomDAO roomDAO = new RoomDAO();
    private boolean isEditMode = false;
    private boolean isSuccess = false;
    private int existingRoomNum = -1;

    @FXML
    public void initialize() {
        comboType.setItems(FXCollections.observableArrayList("Single", "Double", "Suite"));
        comboStatus.setItems(FXCollections.observableArrayList("Available", "Occupied", "Under Maintenance"));

        comboType.getSelectionModel().selectFirst();
        comboStatus.getSelectionModel().selectFirst();
    }

    // get data from frontend and turn to edit mode
    public void setRoomData(Map<String, Object> room) {
        isEditMode = true;
        existingRoomNum = (int) room.get("RoomNumber");

        txtRoomNumber.setText(String.valueOf(existingRoomNum));
        txtRoomNumber.setDisable(true); // <!> dont allow edit roomNum to avoid fk crashe <!>

        comboType.setValue((String) room.get("RoomType"));
        txtPrice.setText(String.valueOf(room.get("PricePerNight")));
        txtCapacity.setText(String.valueOf(room.get("Capacity")));
        comboStatus.setValue((String) room.get("Status"));

        lblTitle.setText("Edit Room Details");
        btnSave.setText("Update Room");
    }

    @FXML
    public void handleSave() {
        try {
            int roomNum = Integer.parseInt(txtRoomNumber.getText().trim());
            String type = comboType.getValue();
            double price = Double.parseDouble(txtPrice.getText().trim());
            int capacity = Integer.parseInt(txtCapacity.getText().trim());
            String status = comboStatus.getValue();

            if (isEditMode) {
                isSuccess = roomDAO.updateRoom(existingRoomNum, type, price, capacity, status);
            } else {
                isSuccess = roomDAO.addRoom(roomNum, type, price, capacity, status);
            }

            if (isSuccess) {
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save room. Room number might already exist.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please ensure Room Number, Price, and Capacity are valid numbers.");
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    public boolean isOperationSuccessful() {
        return isSuccess;
    }

    private void closeWindow() {
        Stage stage = (Stage) txtRoomNumber.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}