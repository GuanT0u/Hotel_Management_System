package com.example.group_project;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddUserController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtContactInfo;

    private UserDAO userDAO = new UserDAO();
    private boolean isSuccess = false;

    @FXML
    public void handleSave() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String contact = txtContactInfo.getText().trim();

        if (username.isEmpty() || password.isEmpty() || contact.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Info", "Please fill in all fields.");
            return;
        }

        // call userDAO to get Receptionist
        isSuccess = userDAO.addReceptionist(username, password, contact);

        if (isSuccess) {
            closeWindow();

            new LogDAO().logAction("Added a New Receptionist for " + username);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add user. Username might already exist.");
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
        Stage stage = (Stage) txtUsername.getScene().getWindow();
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