package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Main application class for launching the Vivero application.
 * <p>
 * This class is the entry point of the JavaFX application and is responsible
 * for loading the initial FXML file and setting up the primary stage.
 * </p>
 *
 * @author Fabricio CUM
 */
public class Vivero extends Application {

    /**
     * Initializes and shows the primary stage of the application.
     * <p>
     * This method loads the FXML file for the login screen, sets up the scene,
     * and displays the primary stage.
     * </p>
     *
     * @param stage The primary stage for this application, onto which the scene
     * will be set.
     * @throws Exception If there is an issue loading the FXML file or setting
     * up the scene.
     */
    @Override
    public void start(Stage stage) {
        try {
            // Load the FXML file for the login screen
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));

            // Create the scene and set it on the stage
            Scene scene = new Scene(root);
            stage.setTitle("Vivero Pluma Roja");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            // Display an alert dialog if an error occurs
            showAlert(AlertType.ERROR, "Error al Cargar la Interfaz", "No se pudo cargar la interfaz de usuario. Por favor, verifica los archivos y vuelve a intentar.");
            e.printStackTrace();
        }
    }

    /**
     * Displays an alert dialog with the specified parameters.
     *
     * @param alertType The type of alert to be displayed (e.g., ERROR,
     * WARNING).
     * @param title The title of the alert dialog.
     * @param content The content text of the alert dialog.
     */
    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * The main method that launches the application.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        launch(args);
    }
}
