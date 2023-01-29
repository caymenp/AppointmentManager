package com.example.controller;

import com.example.model.Appointment;
import com.example.model.Customer;
import com.example.model.User;
import com.example.utilities.AlertMessages;
import com.example.utilities.DateTimeConversion;
import com.example.utilities.UserUtilities;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ResourceBundle;

import static javafx.application.Platform.exit;

/** Controller for Main GUI - Handles all button clicks to perform tasks and serves as the application home page */
public class AppointmentsOverviewController implements Initializable {

    public Text currentUser;
    public Button logout;
    public Text localTimeZone;
    public Button reports;
    public Button addAppointment;
    public Button modifyAppointment;
    public Button deleteAppointment;
    public Button addCustomer;
    public Button modifyCustomer;
    public Button deleteCustomer;
    public RadioButton viewWeekRadio;
    public ToggleGroup mainCalendar;
    public RadioButton viewMonthRadio;
    public RadioButton viewAllRadio;
    public Button changeTableView;
    public GridPane radioGroup;


    //Customer Table

    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<?, ?> cxColID;
    @FXML
    private TableColumn<?, ?> cxColName;
    @FXML
    private TableColumn<?, ?> cxColAddress;
    @FXML
    private TableColumn<?, ?> cxColZip;
    @FXML
    private TableColumn<?, ?> cxColPhone;
    @FXML
    private TableColumn<?, ?> cxColCreateDate;
    @FXML
    private TableColumn<?, ?> cxColCreateBy;
    @FXML
    private TableColumn<?, ?> cxColUpdateTime;
    @FXML
    private TableColumn<?, ?> cxColUpdateBy;
    @FXML
    private TableColumn<?, ?> cxColDivision;


//Appointment Table


    @FXML
    private TableView<Appointment> appointmentTable;
    @FXML
    private TableColumn<?, ?> colAppID;
    @FXML
    private TableColumn<?, ?> colAptTitle;
    @FXML
    private TableColumn<?, ?> colAptDesc;
    @FXML
    private TableColumn<?, ?> colAptLocation;
    @FXML
    private TableColumn<?, ?> colAptContact;
    @FXML
    private TableColumn<?, ?> colAptType;
    @FXML
    private TableColumn<?, ?> colAptStart;
    @FXML
    private TableColumn<?, ?> colAptEnd;
    @FXML
    private TableColumn<?, ?> colAptCXid;
    @FXML
    private TableColumn<?, ?> colAptUserID;


    /**
     * Initializes User data by calling an helper function
     */
    UserUtilities userUtilities = new UserUtilities();

    //Current Logged In User
    private User loggedInUser;

    /** Gets active user object, passed from Login
     * Assigned the logged in user to a local variable to be referenced by other methods
     * @param activeUser
     */
    public void getActiveUser(User activeUser) {
        loggedInUser = activeUser;
    }

    /** Delete existing customer
     * Ensures that there is a customer selected on the table. Then sends verification pops to user to verify they
     * want to delete the customer. If confirmed, sends to the customer form handler to delete with DB. Verifies
     * successful deletion with another pop on conpletion.
     * @param actionEvent
     */
    public void deleteCustomer(ActionEvent actionEvent) {
        if (customerTable.getSelectionModel().isEmpty()) {
            AlertMessages alertMessages = new AlertMessages();
            alertMessages.errorMessage("Cannot Delete", "No customer is selected." +
                    "Please select an customer from the customer table.");
            return;
        }

        AlertMessages alertMessages = new AlertMessages();

        ObservableList<Appointment> customersAppointments = FXCollections.observableArrayList();

        String customerName = customerTable.getSelectionModel().getSelectedItem().getName();
        int selectedCustomerID = customerTable.getSelectionModel().getSelectedItem().getCustomerID();

        customersAppointments.addAll(Appointment.customerAppointments(selectedCustomerID));

        if (customersAppointments.isEmpty()) {
            alertMessages.confirmationMessage("Delete Customer", "This customer has 0 scheduled appointments"
                    + "\n"+ "Are you sure you want to delete this customer?");
        } else {
            int numberOfAppointments = customersAppointments.size();

            alertMessages.confirmationMessage("Delete Customer", "Customer has " +numberOfAppointments+ " scheduled appointments." +"\n"+
                    "If you delete this customer, all scheduled appointments will ALSO be deleted.");

            for (Appointment a : customersAppointments) {
                int appointmentID = a.getAppointmentID();
                Appointment.deleteAppointment(appointmentID);
            }
        }


        Customer.deleteCX(selectedCustomerID);
        customerTableView();

        alertMessages.informationMessage("Customer Deleted", customerName + ", was successfully deleted.");
    }

    /**Modify Exisiting Customer Action Event
     * Listens for button click on "Modify Customer" and then triggers the customerForm controller to handle the processing.
     * Passes the selected customer to the customerForm controller to pre-populate customer data in the fields.
     * Sets the new scene and pops open the window and waits on the form submission or cancellation.
     * @param actionEvent
     * @throws IOException
     */
    public void modifyCustomer(ActionEvent actionEvent) throws IOException {
        if (customerTable.getSelectionModel().isEmpty()) {
            AlertMessages alertMessages = new AlertMessages();
            alertMessages.errorMessage("Cannot Modify", "No customer is selected." +
                    "Please select an customer from the customer table.");
            return;
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/example/appointmentmanager/CustomerForm-view.fxml"));

        loader.load();


        CustomerFormController cfc = loader.getController();
        cfc.getData(customerTable.getSelectionModel().getSelectedItem(), loggedInUser);
        cfc.customerFormLabel.setText("Modify Customer");


        //New Window(Stage)
        Stage customerFormWindow = new Stage();
        Parent formScene = loader.getRoot();
        customerFormWindow.setTitle("Modify Customer");
        customerFormWindow.setScene(new Scene(formScene));

        //Modality For New Window
        customerFormWindow.initModality(Modality.WINDOW_MODAL);

        //Owner stage
        Stage mainStage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        customerFormWindow.initOwner(mainStage);

        //Set Position Of New Window
        customerFormWindow.setX(mainStage.getX()+20);
        customerFormWindow.setY(mainStage.getY()+20);

        customerFormWindow.showAndWait();
        customerTableView();
    }
    /**Add New Customer Action Event
     * Listens for button click on "Add Customer" and then triggers the customerForm controller to handle the processing.
     * Sets the new scene and pops open the window and waits on the form submission or cancellation.
     * @param actionEvent
     * @throws IOException
     */
    public void addCustomer(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/example/appointmentmanager/CustomerForm-view.fxml"));

        loader.load();


        CustomerFormController cfc = loader.getController();
        cfc.getUser(loggedInUser);
        cfc.customerFormLabel.setText("Add Customer");


        //New Window(Stage)
        Stage customerFormWindow = new Stage();
        Parent formScene = loader.getRoot();
        customerFormWindow.setTitle("Add Customer");
        customerFormWindow.setScene(new Scene(formScene));

        //Modality For New Window
        customerFormWindow.initModality(Modality.WINDOW_MODAL);

        //Owner stage
        Stage mainStage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        customerFormWindow.initOwner(mainStage);

        //Set Position Of New Window
        customerFormWindow.setX(mainStage.getX()+20);
        customerFormWindow.setY(mainStage.getY()+20);

        customerFormWindow.showAndWait();
        customerTableView();
    }
    /** Delete existing appointment
     * Ensures that there is an appointment selected on the table. Then sends verification pops to user to verify they
     * want to delete the appointment. If confirmed, sends to the Appointment handler to delete with DB. Verifies
     * successful deletion with another pop on conpletion.
     * @param actionEvent
     */
    public void deleteAppointment(ActionEvent actionEvent) {

        if (appointmentTable.getSelectionModel().isEmpty()) {
            AlertMessages alertMessages = new AlertMessages();
            alertMessages.errorMessage("Cannot Delete", "No appointment is selected." +
                    "Please select an appointment from the appointments table.");
            return;
        }

        String appointmentID = String.valueOf(appointmentTable.getSelectionModel().getSelectedItem().getAppointmentID());
        String appointmentType = appointmentTable.getSelectionModel().getSelectedItem().getType();

        AlertMessages alertMessages = new AlertMessages();
        alertMessages.confirmationMessage("Delete Appointment", "Are you sure you want to delete this appointment?");

        Appointment.deleteAppointment(appointmentTable.getSelectionModel().getSelectedItem().getAppointmentID());

        appointmentTableView("viewAll");

        alertMessages.informationMessage("Appointment ID: "+appointmentID+" - Deleted", "\n"
                + "Appointment ID: " + appointmentID + "\n" + "Appointment Type: " +appointmentType+ "\n" + "Successfully Deleted!");

    }
    /**Modify Exisiting Appointment Action Event
     * Listens for button click on "Modify Appointment" and then triggers the appointmentForm controller to handle the processing.
     * Passes the selected appointment to the appointmentForm controller to pre-populate appointment data in the fields.
     * Sets the new scene and pops open the window and waits on the form submission or cancellation.
     * @param actionEvent
     * @throws IOException
     */
    public void modifyAppointment(ActionEvent actionEvent) throws IOException {

        if (appointmentTable.getSelectionModel().isEmpty()) {

            new AlertMessages().errorMessage("No Appointment Selected",
                   "No appointment selected. Please select an appointment to modify");
           return;
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/example/appointmentmanager/AppointmentForm-view.fxml"));

        loader.load();


        AppointmentFormController afc = loader.getController();
        afc.getData(appointmentTable.getSelectionModel().getSelectedItem(), loggedInUser);
        afc.appointmentFormLabel.setText("Modify Appointment");


        //New Window(Stage)
        Stage appointmentFormWindow = new Stage();
        Parent formScene = loader.getRoot();
        appointmentFormWindow.setTitle("Modify Appointment");
        appointmentFormWindow.setScene(new Scene(formScene));

        //Modality For New Window
        appointmentFormWindow.initModality(Modality.WINDOW_MODAL);

        //Owner stage
        Stage mainStage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        appointmentFormWindow.initOwner(mainStage);

        //Set Position Of New Window
        appointmentFormWindow.setX(mainStage.getX()+20);
        appointmentFormWindow.setY(mainStage.getY()+20);

        appointmentFormWindow.showAndWait();

        mainCalendar.selectToggle(viewAllRadio);
        appointmentTableView("viewAll");

    }
    /**Add New Appointment Action Event
     * Listens for button click on "Add Appointment" and then triggers the appointmentForm controller to handle the processing.
     * Sets the new scene and pops open the window and waits on the form submission or cancellation.
     * @param actionEvent
     * @throws IOException
     */
    public void addAppointment(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/example/appointmentmanager/AppointmentForm-view.fxml"));

        loader.load();


        AppointmentFormController afc = loader.getController();
        afc.getData(loggedInUser);
        afc.appointmentFormLabel.setText("Add Appointment");


        //New Window(Stage)
        Stage appointmentFormWindow = new Stage();
        Parent formScene = loader.getRoot();
        appointmentFormWindow.setTitle("Add Appointment");
        appointmentFormWindow.setScene(new Scene(formScene));

        //Modality For New Window
        appointmentFormWindow.initModality(Modality.WINDOW_MODAL);

        //Owner stage
        Stage mainStage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        appointmentFormWindow.initOwner(mainStage);

        //Set Position Of New Window
        appointmentFormWindow.setX(mainStage.getX()+20);
        appointmentFormWindow.setY(mainStage.getY()+20);

        appointmentFormWindow.showAndWait();

        mainCalendar.selectToggle(viewAllRadio);
        appointmentTableView("viewAll");


    }

    /** Report Button Action handler
     * Listens for click on the report button. When clicked, the stage and scene change to the report page. The reporting
     * page stays open waiting on the close button to be clicked and then is returned to the main stage and scene.
     * @param actionEvent
     * @throws IOException
     */
    public void reports(ActionEvent actionEvent) throws IOException {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/example/appointmentmanager/Reports-view.fxml"));

        loader.load();


        ReportController rc = loader.getController();


        //New Window(Stage)
        Stage reportWindow = new Stage();
        Parent formScene = loader.getRoot();
        reportWindow.setTitle("Reports");
        reportWindow.setScene(new Scene(formScene));

        //Modality For New Window
        reportWindow.initModality(Modality.WINDOW_MODAL);

        //Owner stage
        Stage mainStage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        reportWindow.initOwner(mainStage);

        //Set Position Of New Window
        reportWindow.setX(mainStage.getX()+20);
        reportWindow.setY(mainStage.getY()+20);

        reportWindow.showAndWait();

        mainCalendar.selectToggle(viewAllRadio);
        appointmentTableView("viewAll");
    }

    /** Logout Button Handler
     * Listens for actionEvent on the logout button. When clicked, the current user is logged out, and the application closes.
     * @param actionEvent
     */
    public void logout(ActionEvent actionEvent) {
        exit();
    }

    /** Populates The Appointment Table with data from DB.
     * This method holds the data and table assignment for the appointment table. It takes a String param of viewType to
     * determine what "filter" to apply to the appointment observable list. To properly display the appointments, whichever
     * toggle button is clicked.
     * @param viewType
     */
    public void appointmentTableView(String viewType) {
        ObservableList<Appointment> appointmentsView = FXCollections.observableArrayList();

        colAppID.setCellValueFactory(new PropertyValueFactory<>("appointmentID"));
        colAptTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAptDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAptLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colAptContact.setCellValueFactory(new PropertyValueFactory<>("contactID"));
        colAptType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colAptStart.setCellValueFactory(new PropertyValueFactory<>("startDateTime"));
        colAptEnd.setCellValueFactory(new PropertyValueFactory<>("endDateTime"));
        colAptCXid.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        colAptUserID.setCellValueFactory(new PropertyValueFactory<>("userID"));

        if (viewType == "viewMonth") {
            LocalDate startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            LocalDate endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
            appointmentsView = Appointment.getFilteredAppointments(startDate, endDate);
            appointmentTable.setItems(appointmentsView);
        } else if (viewType == "viewWeek") {
            Pair<LocalDate, LocalDate> getDates = DateTimeConversion.getWeek();
            appointmentsView = Appointment.getFilteredAppointments(getDates.getKey(), getDates.getValue());
            appointmentTable.setItems(appointmentsView);
        } else {
            appointmentsView = Appointment.getAllAppointments();
            appointmentTable.setItems(appointmentsView);
        }
    }

    public void aptCalFilter(ActionEvent actionEvent) throws SQLException {

        if (actionEvent.getTarget().equals(viewAllRadio)) {
            appointmentTableView("viewAll");
        } else if (actionEvent.getTarget().equals(viewWeekRadio)) {
            appointmentTableView("viewWeek");
        } else if (actionEvent.getTarget().equals(viewMonthRadio)) {
            appointmentTableView("viewMonth");
        }

    }

    public void changeTableView(ActionEvent actionEvent) throws SQLException {
        String changeView = changeTableView.getText();

        if (changeView.equals("View Customers")) {
            customerTableView();
            radioGroup.setVisible(false);
            appointmentTable.setVisible(false);
            customerTable.setVisible(true);
            changeTableView.setText("View Appointments");
            appointmentTable.getSelectionModel().clearSelection();
            return;
        }

        radioGroup.setVisible(true);
        customerTable.setVisible(false);
        appointmentTable.setVisible(true);
        customerTable.getSelectionModel().clearSelection();
        appointmentTableView("viewAll");
        changeTableView.setText("View Customers");

    }


    public void customerTableView() {

        cxColID.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        cxColName.setCellValueFactory(new PropertyValueFactory<>("name"));
        cxColAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        cxColZip.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
        cxColPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        cxColCreateDate.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        cxColCreateBy.setCellValueFactory(new PropertyValueFactory<>("createdBy"));
        cxColUpdateTime.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));
        cxColUpdateBy.setCellValueFactory(new PropertyValueFactory<>("lastUpdatedBy"));
        cxColDivision.setCellValueFactory(new PropertyValueFactory<>("divisionName"));

        customerTable.setItems(Customer.getAllCustomers());
    }

    public void upcomingAppointmentAlert() {
        AlertMessages alertMessages = new AlertMessages();
        LocalDateTime currentUserTime = Timestamp.valueOf(LocalDateTime.now()).toLocalDateTime();

        ObservableList<Appointment> allAppointments = FXCollections.observableArrayList();
        allAppointments.addAll(Appointment.getAllAppointments());

        for (Appointment a : allAppointments) {
            LocalDateTime appointmentStart = a.getStartDateTime().toLocalDateTime();
            if (appointmentStart.toLocalDate().isEqual(currentUserTime.toLocalDate())) {
                LocalTime timerBuffer = appointmentStart.toLocalTime().minusMinutes(15);
                if (currentUserTime.toLocalTime().isAfter(timerBuffer) && currentUserTime.toLocalTime().isBefore(appointmentStart.toLocalTime())) {
                    alertMessages.informationMessage("Upcoming Appointment", "Appointment: "
                            +String.valueOf(a.getAppointmentID())+ "\n" + "Scheduled for: "+a.getStartDateTime()+"\n"+
                            "Is starting within 15 minutes from now.");
                    return;
                }
            }
        }
        alertMessages.informationMessage("No Appointments Upcoming", "You do not have any " +
                "scheduled appointments within 15 minutes from " +DateTimeConversion.getUserDisplayTime(Timestamp.valueOf(currentUserTime)));
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        localTimeZone.setText(userUtilities.getTimeZone());
        upcomingAppointmentAlert();
        customerTableView();
        appointmentTableView("viewAll");



    }

}
