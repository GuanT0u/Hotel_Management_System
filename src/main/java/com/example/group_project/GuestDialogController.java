package com.example.group_project;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.Map;

public class GuestDialogController {

    @FXML private Label lblTitle;
    @FXML private TextField txtFullName;
    @FXML private TextField txtContact;
    @FXML private TextField txtEmail;
    @FXML private Button btnSave;

    // 用于保存数据库生成的新 ID，初始值为 -1 表示未生成
    private GuestDAO guestDAO = new GuestDAO();
    private int existingGuestId = -1;
    private boolean isOperationSuccessful = false;

    public void setGuestData(Map<String, Object> guest) {
        this.existingGuestId = (int) guest.get("GuestID");

        // 自动填充旧数据
        txtFullName.setText((String) guest.get("FullName"));
        txtContact.setText((String) guest.get("ContactNumber"));
        txtEmail.setText((String) guest.get("Email"));

        // 动态修改 UI 显示
        lblTitle.setText("Update Guest Info");
        btnSave.setText("Update");
    }

    @FXML
    public void handleSave() {
        String name = txtFullName.getText().trim();
        String contact = txtContact.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty() || contact.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Info", "Please fill in all fields.");
            return;
        }

        if (existingGuestId == -1) {
            // 执行添加逻辑
            int newId = guestDAO.addNewGuest(name, contact, email);
            if (newId != -1) {
                this.existingGuestId = newId; // 传回新生成的ID
                isOperationSuccessful = true;
            }
        } else {
            // 执行更新逻辑
            isOperationSuccessful = guestDAO.updateGuest(existingGuestId, name, contact, email);
        }

        if (isOperationSuccessful) {
            closeWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Operation failed. Email may already exist.");
        }
    }

    @FXML
    public void handleCancel() {
        isOperationSuccessful = false;
        closeWindow();
    }

    public int getGuestId() {
        return isOperationSuccessful ? existingGuestId : -1;
    }

    private void closeWindow() {
        Stage stage = (Stage) txtFullName.getScene().getWindow();
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