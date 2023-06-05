package controller;

import database.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.*;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class ModifyAppointmentController implements Initializable {
    public TextField appointmentIdTextField;
    public TextField titleTextField;
    public TextField descriptionTextField;
    public TextField locationTextField;
    public TextField typeTextField;
    public ComboBox<Contact> contactIdComboBox;
    public ComboBox<Customer> customerIdComboBox;
    public ComboBox<User> userIdComboBox;
    public DatePicker startDatePicker;
    public DatePicker endDatePicker;
    public ComboBox<LocalTime> startTimeComboBox;
    public ComboBox<LocalTime> endTimeComboBox;
    public Button saveButton;
    public Button cancelButton;
    private Appointment passSelectedAppointment;

    public void passAppointment(Appointment selectedAppointment) throws SQLException {
        JDBC.makeConnection();
        passSelectedAppointment = selectedAppointment;
        appointmentIdTextField.setText(String.valueOf(selectedAppointment.getAppointmentId()));
        titleTextField.setText(String.valueOf(selectedAppointment.getTitle()));
        descriptionTextField.setText(selectedAppointment.getDescription());
        locationTextField.setText(selectedAppointment.getLocation());
        typeTextField.setText(selectedAppointment.getType());

        contactIdComboBox.getItems().forEach(contact -> {
            if(contact.getContactId() == selectedAppointment.getContactId()) {
                contactIdComboBox.setValue(contact);
            }
        });
        customerIdComboBox.getItems().forEach(customer -> {
            if(customer.getCustomerId() == selectedAppointment.getCustomerId()) {
                customerIdComboBox.setValue(customer);
            }
        });
        userIdComboBox.getItems().forEach(user -> {
            if(user.getUserId() == selectedAppointment.getUserId()) {
                userIdComboBox.setValue(user);
            }
        });
//        for(Contact contact: contactIdComboBox.getItems()) {
//            if(contact.getContactId() == selectedAppointment.getContactId()) {
//                contactIdComboBox.setValue(contact);
//                break;
//            }
//        }
//        for(Customer c: customerIdComboBox.getItems()) {
//            if(c.getCustomerId() == selectedAppointment.getCustomerId()) {
//                customerIdComboBox.setValue(c);
//                break;
//            }
//        }
//        for(User u: userIdComboBox.getItems()) {
//            if(u.getUserId() == selectedAppointment.getUserId()) {
//                userIdComboBox.setValue(u);
//                break;
//            }
//        }

        startDatePicker.setValue(selectedAppointment.getStartDate());
        endDatePicker.setValue(selectedAppointment.getEndDate());
        startTimeComboBox.getSelectionModel().select(selectedAppointment.getStartTime());
        endTimeComboBox.getSelectionModel().select(selectedAppointment.getEndTime());
    }

    public void onSaveButton(ActionEvent actionEvent) throws SQLException, IOException {
        int appointmentId = passSelectedAppointment.getAppointmentId();
        String title = titleTextField.getText();
        String description = descriptionTextField.getText();
        String location = locationTextField.getText();
        String type = typeTextField.getText();
        int contactId = contactIdComboBox.getValue().getContactId();
        int customerId = customerIdComboBox.getValue().getCustomerId();
        int userId = userIdComboBox.getValue().getUserId();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        LocalTime startTime = startTimeComboBox.getSelectionModel().getSelectedItem();
        LocalTime endTime = endTimeComboBox.getSelectionModel().getSelectedItem();
        LocalDateTime startDateTime = LocalDateTime.of(startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth(), startTime.getHour(), startTime.getMinute());
        LocalDateTime endDateTime = LocalDateTime.of(endDate.getYear(), endDate.getMonth(), endDate.getDayOfMonth(), endTime.getHour(), endTime.getMinute());

        if(title.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Field Cannot Be Empty");
            alert.setContentText("Please fill out 'Title' field.");
            alert.showAndWait();
            return;
        }
        if(description.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Field Cannot Be Empty");
            alert.setContentText("Please fill out 'Description' field.");
            alert.showAndWait();
            return;
        }
        if(location.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Field Cannot Be Empty");
            alert.setContentText("Please fill out 'Location' field.");
            alert.showAndWait();
            return;
        }
        if(type.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Field Cannot Be Empty");
            alert.setContentText("Please fill out 'Type' field.");
            alert.showAndWait();
            return;
        }

        //Convert to EST
        ZonedDateTime estStartDateTime = startDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("US/Eastern"));
        ZonedDateTime estEndDateTime = endDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("US/Eastern"));

        //Check business day
        int startDay = estStartDateTime.getDayOfWeek().getValue();
        int endDay = estEndDateTime.getDayOfWeek().getValue();
        int monday = DayOfWeek.MONDAY.getValue();
        int friday = DayOfWeek.FRIDAY.getValue();

        if(startDay < monday || startDay > friday || endDay < monday || endDay > friday) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Business Not In Operation");
            alert.setContentText("The appointment day is outside of business operations (Monday-Friday between 08:00 and 22:00 EST).");
            alert.showAndWait();
            return;
        }

        //Check business hours
        LocalTime appointmentStartTime = estStartDateTime.toLocalTime();
        LocalTime appointmentEndTime = estEndDateTime.toLocalTime();
        LocalTime businessStartTime = LocalTime.of(8, 0);
        LocalTime businessEndTime = LocalTime.of(22, 0);

        if(appointmentStartTime.isBefore(businessStartTime) || appointmentStartTime.isAfter(businessEndTime) || appointmentEndTime.isBefore(businessStartTime) || appointmentEndTime.isAfter(businessEndTime)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Business Not In Operation");
            alert.setContentText("The appointment time [" + appointmentStartTime + "-" + appointmentEndTime + " EST] is outside of business operations (Monday-Friday between 8:00 and 22:00 EST).");
            alert.showAndWait();
            return;
        }
        if(startDateTime.isAfter(endDateTime)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Invalid Date/Time");
            alert.setContentText("The appointment \"Start Date/Time\" must be before \"End Date/Time\".");
            alert.showAndWait();
            return;
        }
        if(startDateTime.isEqual(endDateTime)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Invalid Date/Time");
            alert.setContentText("The appointment \"Start Date/Time\" must be before \"End Date/Time\".");
            alert.showAndWait();
            return;
        }

        for(Appointment a: AppointmentDao.getAppointmentsByCustomer(customerId)) {
            LocalDateTime existedStartDateTime = LocalDateTime.of(a.getStartDate(), a.getStartTime());
            LocalDateTime existedEndDateTime = LocalDateTime.of(a.getEndDate(), a.getEndTime());
            if (a.getAppointmentId() == appointmentId) {
                continue;
            }
            else {
                if ((startDateTime.isBefore(existedStartDateTime) || startDateTime.isEqual(existedStartDateTime)) &&
                        (endDateTime.isAfter(existedEndDateTime) || endDateTime.isEqual(existedEndDateTime))) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Overlapping Appointments");
                    alert.setContentText("The appointment overlaps with existing appointment.");
                    alert.showAndWait();
                    return;
                }
                if ((startDateTime.isAfter(existedStartDateTime) || startDateTime.isEqual(existedStartDateTime)) &&
                        startDateTime.isBefore(existedEndDateTime)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Overlapping Appointments");
                    alert.setContentText("The appointment \"Start Time\" overlaps with existing appointment.");
                    alert.showAndWait();
                    return;
                }
                if (endDateTime.isAfter(existedStartDateTime) &&
                        (endDateTime.isBefore(existedEndDateTime) || endDateTime.isEqual(existedEndDateTime))) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Overlapping Appointments");
                    alert.setContentText("The appointment \"End Time\" overlaps with existing appointments");
                    alert.showAndWait();
                    return;
                }
            }
        }

        AppointmentDao appointmentDao = new AppointmentDao();
        appointmentDao.updateAppointment(appointmentId, title, description, location, type, startDateTime, endDateTime, customerId, userId, contactId);

        Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        Parent parent = FXMLLoader.load(getClass().getResource("../view/Appointment.fxml"));
        stage.setScene(new Scene(parent));
        stage.show();
    }

    public void onCancelButton(ActionEvent actionEvent) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Cancel Confirmation");
        alert.setContentText("Are you sure you want to cancel?");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
            Parent parent = FXMLLoader.load(getClass().getResource("../view/Appointment.fxml"));
            stage.setScene(new Scene(parent));
            stage.show();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            JDBC.makeConnection();
            contactIdComboBox.setItems(ContactDao.getAllContacts());
            customerIdComboBox.setItems(CustomerDao.getAllCustomers());
            userIdComboBox.setItems(UserDao.getAllUsers());

            ObservableList<LocalTime> startTimeList = FXCollections.observableArrayList();
            ObservableList<LocalTime> endTimeList = FXCollections.observableArrayList();
            for(LocalTime time = LocalTime.of(0, 0); time.isBefore(LocalTime.of(23, 30)); time = time.plusMinutes(30)) {
                startTimeList.add(time);
                endTimeList.add(time.plusMinutes(30));
            }
            startTimeList.add(LocalTime.of(23, 30));
            endTimeList.add(LocalTime.of(0, 0));
            startTimeComboBox.setItems(startTimeList);
            endTimeComboBox.setItems(endTimeList);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
