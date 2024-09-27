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
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class for managing the login functionality.
 *
 * @author Fabricio CUM
 */
public class LoginController implements Initializable {

    @FXML
    private PasswordField password;
    @FXML
    private TextField userName;
    @FXML
    private Label label;
    @FXML
    private Button singIn;

    private DBconection dbConnection = new DBconection();

    private String nombreVendedor;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization code if needed
    }

    /**
     * Handles the sign-in button click event. Validates the input fields and
     * attempts to log in.
     *
     * @param event The ActionEvent triggered by clicking the sign-in button.
     */
    @FXML
    private void singInVerify(ActionEvent event) {
        String username = userName.getText().trim();
        String passwordd = password.getText().trim();

        // Validate if the fields are empty
        if (username.isEmpty() || passwordd.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Fields", "Por favor, rellene todos los campos.");
            return;
        }

        // Validate the credentials
        if (validateLogin(username, passwordd)) {
            loadNextWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Nombre de usuario o contraseña incorrectos.");
        }
    }

    /**
     * Validates the login credentials by querying the database.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     * @return true if the credentials are valid; false otherwise.
     */
    private boolean validateLogin(String username, String password) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dbConnection.establishConnection();
            String query = "SELECT * FROM Vendedor WHERE cedula = ? AND contrasena = ?"; // Consulta original
            pst = conn.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);
            rs = pst.executeQuery();

            if (rs.next()) { // If a result is returned, the credentials are valid
                nombreVendedor = rs.getString("nombre"); // Get the seller's name
                return true;
            } else {
                return false; // Credentials are invalid
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Connection Error", "No se puede conectar a la base de datos.");
            return false;
        } finally {
            // Close resources in the finally block to ensure they are always closed
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pst != null) {
                    pst.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadNextWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Inventory.fxml"));
            Parent root = fxmlLoader.load();

            // Cargar la ventana de inventario
            InventoryController inventoryController = fxmlLoader.getController();
            
            Stage stage = (Stage) userName.getScene().getWindow();
            stage.close();  // Cierra la ventana de inicio de sesión

            Stage mainStage = new Stage();
            mainStage.setTitle("Inventory");
            mainStage.setScene(new Scene(root));
            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Loading Error", "No se puede cargar la ventana de inventario.");
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

}
