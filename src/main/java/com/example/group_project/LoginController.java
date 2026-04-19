package com.example.group_project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Input Validation
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Username and Password cannot be empty.");
            return;
        }

        // DAO Integration
        String role = userDAO.authenticateUser(username, password);

        // Routing & Exception Management
        if (role != null) {
            if (role.equals("Admin")) {
                loadDashboard("AdminDashboard.fxml", "Admin Dashboard");
                new LogDAO().logAction("Login as " + role);
            } else if (role.equals("Receptionist")) {
                loadDashboard("ReceptionistDashboard.fxml", "Receptionist Dashboard");
                new LogDAO().logAction("Login as " + role);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password. Please try again.");
        }
    }

    private void loadDashboard(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("HRMS - " + title);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "System Error", "Failed to load the dashboard.");
            e.printStackTrace();
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