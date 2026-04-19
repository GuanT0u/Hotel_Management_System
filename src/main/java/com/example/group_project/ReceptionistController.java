package com.example.group_project;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReceptionistController {

    @FXML private TextField txtGuestId;
    @FXML private TextField txtRoomNumber;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TextField txtTotalPrice;

    @FXML private ComboBox<String> comboRoomType;
    @FXML private ComboBox<String> comboStatus;
    @FXML private TableView<Map<String, Object>> searchRoomTable;
    @FXML private TableColumn<Map<String, Object>, Integer> colSearchRoomNum;
    @FXML private TableColumn<Map<String, Object>, String> colSearchType;
    @FXML private TableColumn<Map<String, Object>, Integer> colSearchCapacity;
    @FXML private TableColumn<Map<String, Object>, String> colSearchStatus;

    @FXML private TextField txtGuestSearch;
    @FXML private TableView<Map<String, Object>> guestTable;
    @FXML private TableColumn<Map<String, Object>, Integer> colGuestId;
    @FXML private TableColumn<Map<String, Object>, String> colGuestName;
    @FXML private TableColumn<Map<String, Object>, String> colGuestPhone;
    @FXML private TableColumn<Map<String, Object>, String> colGuestEmail;

    private RoomDAO roomDAO = new RoomDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();
    private GuestDAO guestDAO = new GuestDAO();
    private LogDAO logDAO = new LogDAO();


    @FXML
    public void initialize() {
        comboRoomType.setItems(FXCollections.observableArrayList("All", "Single", "Double", "Suite"));
        comboRoomType.setValue("All");

        comboStatus.setItems(FXCollections.observableArrayList("All", "Available", "Occupied", "Under Maintenance"));
        comboStatus.setValue("All");

        colSearchRoomNum.setCellValueFactory(data ->
                new SimpleObjectProperty<>((Integer) data.getValue().get("RoomNumber")));
        colSearchType.setCellValueFactory(data ->
                new SimpleStringProperty((String) data.getValue().get("RoomType")));
        colSearchCapacity.setCellValueFactory(data ->
                new SimpleObjectProperty<>((Integer) data.getValue().get("Capacity")));
        colSearchStatus.setCellValueFactory(data ->
                new SimpleStringProperty((String) data.getValue().get("Status")));

        colGuestId.setCellValueFactory(data ->
                new SimpleObjectProperty<>((Integer) data.getValue().get("GuestID")));
        colGuestName.setCellValueFactory(data ->
                new SimpleStringProperty((String) data.getValue().get("FullName")));
        colGuestPhone.setCellValueFactory(data ->
                new SimpleStringProperty((String) data.getValue().get("ContactNumber")));
        colGuestEmail.setCellValueFactory(data ->
                new SimpleStringProperty((String) data.getValue().get("Email")));
    }

    @FXML
    public void handleSearchRooms() {
        String type = comboRoomType.getValue();
        String status = comboStatus.getValue();

        List<Map<String, Object>> results = roomDAO.searchRooms(type, status);

        ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(results);
        searchRoomTable.setItems(data);

        if (results.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Search Result", "No rooms found matching your criteria.");
        }

        new LogDAO().logAction("Search for Room type " + type + " and status " + status);
    }

    @FXML
    public void handleCreateReservation() {
        // 1. Strict Input Validation (UX Requirement)
        if (txtGuestId.getText().isEmpty() || txtRoomNumber.getText().isEmpty() ||
                dpStartDate.getValue() == null || dpEndDate.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Details", "Please fill out all fields.");
            return;
        }

        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();

        if (start.isBefore(LocalDate.now()) || end.isBefore(start)) {
            showAlert(Alert.AlertType.ERROR, "Date Error", "Please ensure dates are valid and logical.");
            return;
        }

        int guestId = 0;
        int roomNum = 0;
        double price = 0;
        try {
            guestId = Integer.parseInt(txtGuestId.getText());
            roomNum = Integer.parseInt(txtRoomNumber.getText());
            price = Double.parseDouble(txtTotalPrice.getText());

            // 2. Integration with Transactional DAO
            boolean success = reservationDAO.createReservation(
                    guestId, roomNum, Date.valueOf(start), Date.valueOf(end), price
            );

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Reservation created and Room status updated!");
                clearReservationForm(); // Clear form logic here

                new LogDAO().logAction("Created a new reservation for Room " + roomNum + " for Guest ID: " + guestId);
            } else {
                showAlert(Alert.AlertType.ERROR, "Booking Failed", "The room may be unavailable. Check logs.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format Error", "Please ensure IDs and Price are valid numbers.");
        }
    }

    @FXML
    public void handleOpenAddGuestDialog() {
        openGuestDialog(null); // pass null mean add mode
    }

    @FXML
    public void handleSearchGuests() {
        String keyword = (txtGuestSearch != null) ? txtGuestSearch.getText().trim() : "";

        List<Map<String, Object>> results = guestDAO.searchGuests(keyword);

        guestTable.setItems(FXCollections.observableArrayList(results));

        new LogDAO().logAction("Search for Guests keyword " + keyword);
    }

    @FXML
    public void handleEditGuest() {
        // 1. 获取表格中当前选中的那一行数据
        Map<String, Object> selectedGuest = guestTable.getSelectionModel().getSelectedItem();

        if (selectedGuest == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a guest from the table to edit.");
            return;
        }

        // 2. 调用通用的弹窗方法，并传入数据
        openGuestDialog(selectedGuest);
    }

    private void openGuestDialog(Map<String, Object> guestData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddGuestDialog.fxml"));
            Parent root = loader.load();

            GuestDialogController controller = loader.getController();

            //if the guestData not empty, it mean its edit mode and data is injected
            if (guestData != null) {
                controller.setGuestData(guestData);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(guestData == null ? "Register New Guest" : "Edit Guest");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            int resultId = controller.getGuestId();
            if (resultId != -1) {
                handleSearchGuests();

                if (guestData == null) {
                    txtGuestId.setText(String.valueOf(resultId));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
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

    public RoomDAO getRoomDAO() {
        return roomDAO;
    }

    public void setRoomDAO(RoomDAO roomDAO) {
        this.roomDAO = roomDAO;
    }

    private void clearReservationForm() {
        txtGuestId.clear();
        txtRoomNumber.clear();
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);
        txtTotalPrice.clear();
    }

    public void setGuestDAO(GuestDAO guestDAO) {
        this.guestDAO = guestDAO;
    }
}