/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package main;

import Clases.DBconection;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class for managing the addition of new sellers.
 * 
 * This class handles the user input for adding a new seller and performs necessary validation
 * before inserting the seller details into the database. After successful addition, it returns
 * to the previous window.
 * 
 * @author Fabricio CUM
 */
public class NewSellerController implements Initializable {

    @FXML
    private TextField cedula;
    @FXML
    private TextField name;
    @FXML
    private PasswordField password;
    @FXML
    private Button newSeller;

    private DBconection dbConnection = new DBconection();
    @FXML
    private Button cancel;

    /**
     * Initializes the controller class.
     * This method is called after the FXML file has been loaded.
     * 
     * @param url The location used to resolve relative paths for the root object, or null if the URL is not known.
     * @param rb The resources used to localize the root object, or null if the root object does not need localization.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization code if needed
    }    

        /**
     * Handles the new seller button click event.
     * Validates the input fields and attempts to add a new seller to the database.
     * 
     * @param event The ActionEvent triggered by clicking the new seller button.
     */
    @FXML
    private void newSeller(ActionEvent event) {
        String cedulaText = cedula.getText().trim();
        String nameText = name.getText().trim();
        String passwordText = password.getText().trim();

        // Validate if fields are empty
        if (cedulaText.isEmpty() || nameText.isEmpty() || passwordText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos Vacíos", "Todos los campos deben ser llenados.");
            return;
        }

        // Validate cedula (must be numeric and contain no spaces or dashes)
        if (!cedulaText.matches("\\d+")) {
            showAlert(Alert.AlertType.WARNING, "Cédula Inválida", "La cédula debe contener solo números sin espacios ni guiones.");
            return;
        }

        // Validate password (must not contain spaces)
        if (passwordText.contains(" ")) {
            showAlert(Alert.AlertType.WARNING, "Contraseña Inválida", "La contraseña no debe contener espacios.");
            return;
        }

        // Insert the seller into the database
        try (Connection conn = dbConnection.establishConnection()) {
            String query = "INSERT INTO Vendedor (cedula, nombre, contrasena) VALUES (?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, cedulaText);
            pst.setString(2, nameText);
            pst.setString(3, passwordText);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Vendedor agregado con éxito.");
                // Close current window and go back to previous window
                Stage stage = (Stage) newSeller.getScene().getWindow();
                stage.close();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Inventory.fxml.fxml"));
                Parent root = loader.load();
                Stage mainStage = new Stage();
                mainStage.setTitle("Ventana Anterior");
                mainStage.setScene(new Scene(root));
                mainStage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error de Inserción", "Error al agregar el vendedor.");
            }

            // Clear the text fields after adding
            cedula.clear();
            name.clear();
            password.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error de Base de Datos", "Error al conectar con la base de datos.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Desconocido", "Error desconocido al agregar el vendedor.");
        }
    }
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
