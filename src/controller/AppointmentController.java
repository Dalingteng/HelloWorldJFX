package controller;

import database.AppointmentDao;
import database.JDBC;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Appointment;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.ResourceBundle;

public class AppointmentController implements Initializable {
    public RadioButton weekRadioButton;
    public RadioButton monthRadioButton;
    public RadioButton allRadioButton;
    public TableView<Appointment> appointmentTable;
    public TableColumn<Appointment, Integer> appointmentIdColumn;
    public TableColumn<Appointment, String> titleColumn;
    public TableColumn<Appointment, String> descriptionColumn;
    public TableColumn<Appointment, String> locationColumn;
    public TableColumn<Appointment, String> typeColumn;
    public TableColumn<Appointment, LocalDate> startDateColumn;
    public TableColumn<Appointment, LocalDate> endDateColumn;
    public TableColumn<Appointment, LocalTime> startTimeColumn;
    public TableColumn<Appointment, LocalTime> endTimeColumn;
    public TableColumn<Appointment, Integer> customerIdColumn;
    public TableColumn<Appointment, Integer> userIdColumn;
    public TableColumn<Appointment, Integer> contactIdColumn;
    public Button customerButton;
    public Button reportButton;
    public Button logOutButton;
    public Button addAppointmentButton;
    public Button modifyAppointmentButton;
    public Button deleteAppointmentButton;
    @FXML
    ToggleGroup view = new ToggleGroup();

    public void onWeekRadioButton(ActionEvent actionEvent) throws SQLException {
        weekRadioButton.setToggleGroup(view);
        LocalDate now = LocalDate.now(ZoneId.systemDefault());

        FilteredList<Appointment> filteredList = new FilteredList<>(AppointmentDao.getAllAppointments());
        filteredList.setPredicate(a -> {
            LocalDate appointmentDate = a.getStartDate();
            return ((appointmentDate.isEqual(now) || appointmentDate.isAfter(now)) && appointmentDate.isBefore(now.plusWeeks(1)));
        });
        appointmentTable.setItems(filteredList);
    }

    public void onMonthRadioButton(ActionEvent actionEvent) throws SQLException {
        monthRadioButton.setToggleGroup(view);
        LocalDate now = LocalDate.now(ZoneId.systemDefault());

        FilteredList<Appointment> filteredList = new FilteredList<>(AppointmentDao.getAllAppointments());
        filteredList.setPredicate(a -> {
            LocalDate appointmentDate = a.getStartDate();
            return ((appointmentDate.isEqual(now) || appointmentDate.isAfter(now)) && appointmentDate.isBefore(now.plusMonths(1)));
        });
        appointmentTable.setItems(filteredList);
    }

    public void onAllRadioButton(ActionEvent actionEvent) throws SQLException {
        allRadioButton.setToggleGroup(view);
        appointmentTable.setItems(AppointmentDao.getAllAppointments());
    }

    public void onCustomerButton(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage)((Button)actionEvent.getSource()).getScene().getWindow();
        Parent parent = FXMLLoader.load(getClass().getResource("../view/Customer.fxml"));
        stage.setScene(new Scene(parent));
        stage.show();
    }

    public void onReportButton(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage)((Button)actionEvent.getSource()).getScene().getWindow();
        Parent parent = FXMLLoader.load(getClass().getResource("../view/Report.fxml"));
        stage.setScene(new Scene(parent));
        stage.show();
    }

    public void onLogOutButton(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Exit The Application");
        alert.setContentText("Are you sure you want to exit?");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    public void onAddAppointmentButton(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage)((Button)actionEvent.getSource()).getScene().getWindow();
        Parent parent = FXMLLoader.load(getClass().getResource("../view/AddAppointment.fxml"));
        stage.setScene(new Scene(parent));
        stage.show();
    }

    public void onModifyAppointmentButton(ActionEvent actionEvent) throws IOException, SQLException {
        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
        if(selectedAppointment != null) {
            Stage stage = (Stage)((Button)actionEvent.getSource()).getScene().getWindow();
            FXMLLoader loader =  new FXMLLoader(getClass().getResource("../view/ModifyAppointment.fxml"));
            Parent parent = loader.load();
            stage.setScene(new Scene(parent));
            stage.show();

            ModifyAppointmentController modifyAppointmentController = loader.getController();
            modifyAppointmentController.passAppointment(selectedAppointment);
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("No Appointment Selected");
            alert.setContentText("Please select an appointment to modify.");
            alert.showAndWait();
        }
    }

    public void onDeleteAppointmentButton(ActionEvent actionEvent) throws SQLException {
        JDBC.makeConnection();
        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();

        if(selectedAppointment != null) {
            int appointmentId = selectedAppointment.getAppointmentId();
            String type = selectedAppointment.getType();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Delete Confirmation");
            alert.setContentText("Are you sure you want to delete?");
            Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK) {
                AppointmentDao.deleteAppointment(appointmentId, type);
                appointmentTable.setItems(AppointmentDao.getAllAppointments());
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("No Appointment Selected");
            alert.setContentText("Please select an appointment to delete.");
            alert.showAndWait();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        contactIdColumn.setCellValueFactory(new PropertyValueFactory<>("contactId"));

        try {
            JDBC.makeConnection();
            appointmentTable.setItems(AppointmentDao.getAllAppointments());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
