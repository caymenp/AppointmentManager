package com.example.DAO;

import com.example.model.Contact;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;
/** Contact DOE used for creation/updating/deleting Contact objects from the DB*/

public class ContactDoeImpl {
    /**
     * Gets contact from DB that matches passed id.
     * @param id
     * @return
     * @throws SQLException
     */
    public static Contact getContact(String id) throws SQLException {
        String sqlStatement = "SELECT * FROM client_schedule.contacts WHERE contact_id = '"+id+"'";

        Query.makeQuery(sqlStatement);

        Contact contact;
        ResultSet result = Query.getResult();
        while (result.next()) {
            int contactID = result.getInt("contact_id");
            String contactName = result.getString("contact_name");
            String contactEmail = result.getString("email");

            contact = new Contact(contactID, contactName, contactEmail);
            return contact;
        }
        return null;
    }

    /**
     * Gets contact from DB that matches the passed name.
     * @param name
     * @return
     * @throws SQLException
     */
    public static Contact getContactByName(String name) throws SQLException {
        String sqlStatement = "SELECT * FROM client_schedule.contacts WHERE contact_name = '"+name+"'";

        Query.makeQuery(sqlStatement);

        Contact contact;
        ResultSet result = Query.getResult();
        while (result.next()) {
            int contactID = result.getInt("contact_id");
            String contactName = result.getString("contact_name");
            String contactEmail = result.getString("email");

            contact = new Contact(contactID, contactName, contactEmail);
            return contact;
        }
        return null;
    }

    /**
     * Gets all appointment records from the DB
     * @return
     * @throws SQLException
     */
    public static ObservableList<Contact> getAllContacts() throws SQLException {
        ObservableList<Contact> allContacts = FXCollections.observableArrayList();
        String sqlStatement = "SELECT * FROM client_schedule.contacts";

        Query.makeQuery(sqlStatement);

        Contact contact;
        ResultSet result = Query.getResult();
        while (result.next()) {
            int contactID = result.getInt("contact_id");
            String contactName = result.getString("contact_name");
            String contactEmail = result.getString("email");

            contact = new Contact(contactID, contactName, contactEmail);

            allContacts.add(contact);
        }
        return allContacts;
    }
}
