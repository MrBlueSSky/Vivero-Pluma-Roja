package main;

import Clases.DBconection;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Fabricio CUM
 */
public class UpdateStateController implements Initializable {

    @FXML
    private Button update;
    @FXML
    private TextField billId;
    @FXML
    private ComboBox<String> newState;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        newState.getItems().addAll("Pagada", "Pendiente", "Cancelada");

    }

    /**
     * Handles the action of updating the invoice status. This method
     * establishes a database connection, verifies if the provided invoice ID
     * exists, and updates the status of the invoice in the database. It
     * displays alerts to inform the user of the success or failure of the
     * operation.
     *
     * @param event the action event triggered by the user (e.g., button click)
     * @throws SQLException if a database access error occurs
     */
    @FXML
    private void updateBill(ActionEvent event) throws SQLException {
        Connection conn = null;
        PreparedStatement pst = null;

        try {
            // Establecer conexión
            conn = new DBconection().establishConnection();

            // Obtener el ID de la factura y el nuevo estado
            int facturaIdValue = Integer.parseInt(billId.getText());
            String newEstadoValue = newState.getValue();

            // Verificar si el ID de la factura existe
            String checkQuery = "SELECT COUNT(*) FROM Facturas WHERE facturaId = ?";
            pst = conn.prepareStatement(checkQuery);
            pst.setInt(1, facturaIdValue);
            ResultSet rs = pst.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // El ID de la factura es válido, proceder a actualizar el estado
                String updateQuery = "UPDATE Facturas SET estado = ? WHERE facturaId = ?";
                pst = conn.prepareStatement(updateQuery);
                pst.setString(1, newEstadoValue);
                pst.setInt(2, facturaIdValue);
                int rowsAffected = pst.executeUpdate();

                // Verificar si la actualización fue exitosa
                if (rowsAffected > 0) {
                    // Mostrar mensaje de éxito
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Éxito");
                    alert.setHeaderText(null);
                    alert.setContentText("La factura se actualizó correctamente.");
                    alert.showAndWait();

                    // Cerrar la ventana actual
                    Stage currentStage = (Stage) update.getScene().getWindow();
                    currentStage.close();
                    
                    returnToPreviousWindow();

                } else {
                    // Mostrar mensaje de error si no se actualizó nada
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("No se pudo actualizar la factura. Por favor, inténtelo de nuevo.");
                    alert.showAndWait();
                }
            } else {
                // Mostrar un mensaje de error si el ID de la factura no es válido
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("El ID de la factura ingresado no es válido.");
                alert.showAndWait();
            }
        } catch (NumberFormatException e) {
            // Manejo de errores si el campo billId no es un número válido
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, ingrese un ID de factura válido.");
            alert.showAndWait();
        } finally {
            // Cerrar recursos
            if (pst != null) {
                pst.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void returnToPreviousWindow() {
        try {
            // Cargar la ventana anterior (MainWindow.fxml) o la ventana principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Bill.fxml")); // Reemplaza con el nombre de tu ventana anterior
            Parent root = loader.load();

            // Crear una nueva etapa para la ventana anterior
            Stage stage = new Stage();
            stage.setTitle("Ventana Principal"); // Título de la ventana
            stage.setScene(new Scene(root));
            stage.show();

            // Cerrar la ventana actual
            Stage currentStage = (Stage) update.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
