package main;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import Clases.DBconection;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * FXML Controller class for managing the addition of new clients.
 *
 * This class handles the user input for adding a new client and performs
 * necessary validation before inserting the client details into the database.
 *
 * @author Fabricio CUM
 */
public class NewClientController implements Initializable {

    @FXML
    private TextField clientName;
    @FXML
    private Button addClient;

    private DBconection dbConnection = new DBconection();
    @FXML
    private Button cancel;

    /**
     * Initializes the controller class. This method is called after the FXML
     * file has been loaded.
     *
     * @param url The location used to resolve relative paths for the root
     * object, or null if the URL is not known.
     * @param rb The resources used to localize the root object, or null if the
     * root object does not need localization.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization code if needed
    }

    /**
     * Handles the add client button click event. Validates the input fields and
     * attempts to add a new client to the database.
     *
     * @param event The ActionEvent triggered by clicking the add client button.
     */
       @FXML
    private void addClient(ActionEvent event) {
        String name = clientName.getText().trim();

        // Validate if the field is empty
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campo vacío", "El nombre del cliente no puede estar vacío.");
            return;
        }

        // Insert the client into the database
        try (Connection conn = dbConnection.establishConnection()) {
            String query = "INSERT INTO Cliente (nombre) VALUES (?)";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, name);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Cliente agregado con éxito.");
                
                // Close current window and return to the previous view
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.close(); // Close the current window

                // Load the previous view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Inventory.fxml"));
                Parent root = loader.load();
                Stage mainStage = new Stage();
                mainStage.setTitle("Menú Principal");
                mainStage.setScene(new Scene(root));
                mainStage.show();
                
            } else {
                showAlert(Alert.AlertType.ERROR, "Error de Inserción", "Error al agregar el cliente.");
            }

            // Clear the text field after adding
            clientName.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error de Base de Datos", "Error al conectar con la base de datos.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error de Carga", "Error al cargar la vista anterior.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Desconocido", "Error desconocido al agregar el cliente.");
        }
    }

    /**
     * Displays an alert dialog with the specified parameters.
     *
     * @param alertType The type of alert to be displayed (e.g., WARNING,
     * ERROR).
     * @param title The title of the alert dialog.
     * @param content The content text of the alert dialog.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Handles the cancellation event by closing the current window and loading
     * the previous view.
     *
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void cancel(ActionEvent event) {
        try {
            // Load the previous view (change the name of FXML according to your view)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Inventory.fxml"));
            Parent root = fxmlLoader.load();

            // Get the current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();  // Close the current window

            // Show the previous view
            Stage mainStage = new Stage();
            mainStage.setTitle("Main Menu");
            mainStage.setScene(new Scene(root));
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Use the showAlert method to display an error message
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Failed to load the previous view.");
        }
    }
}
