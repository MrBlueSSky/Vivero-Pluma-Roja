package main;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
import Clases.DBconection;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Fabricio CUM
 */
public class UpdateProductController implements Initializable {

    @FXML
    private TextField idProduct;
    private TextField stateProduct;
    @FXML
    private TextField stoke;
    @FXML
    private Button update;
    @FXML
    private ComboBox<String> discount;
    @FXML
    private Button cancel;
    @FXML
    private TextField price;
    @FXML
    private ComboBox<String> state;

    /**
     * Initializes the controller class. This method is called to initialize the
     * controller class. It is executed after all @FXML fields have been
     * injected. It is used to perform any necessary setup or initialization
     * tasks for the controller. In this case, it loads discounts and states
     * into their respective UI components.
     *
     * @param url The URL location of the FXML file that was loaded.
     * @param rb The ResourceBundle that was used to localize the root object.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarDescuentos();
        cargarEstados();
    }

    /**
     * Loads discount options into the ComboBox from the database. This method
     * establishes a connection to the database, retrieves discount data, and
     * populates the ComboBox with discount descriptions. It includes error
     * handling and validation to ensure data integrity and proper resource
     * management.
     *
     * @throws SQLException If there is an error establishing the connection or
     * executing the query.
     */
    private void cargarDescuentos() {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            // Establecer conexión a la base de datos
            conn = new DBconection().establishConnection();
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión con la base de datos.");
            }

            String query = "SELECT descuentoId, descripcion FROM Descuentos";
            st = conn.createStatement();
            rs = st.executeQuery(query);

            // Limpiar el ComboBox antes de llenarlo
            discount.getItems().clear();

            // Verificar si el ResultSet contiene datos
            if (!rs.isBeforeFirst()) {
                throw new SQLException("No se encontraron descuentos en la base de datos.");
            }

            while (rs.next()) {
                int descuentoId = rs.getInt("descuentoId");
                String descripcion = rs.getString("descripcion");

                // Validar los datos obtenidos
                if (descripcion != null && !descripcion.trim().isEmpty()) {
                    discount.getItems().add(descripcion); // Agrega la descripción al ComboBox
                } else {
                    System.err.println("Error: Descripción del descuento vacía o nula para el ID: " + descuentoId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Carga");
            alert.setHeaderText(null);
            alert.setContentText("No se pudieron cargar los descuentos: " + e.getMessage());
            alert.showAndWait();
        } finally {
            // Cerrar recursos en el bloque finally
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de Cierre de Recursos");
                alert.setHeaderText(null);
                alert.setContentText("No se pudieron cerrar los recursos de la base de datos: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * Loads state options into the ComboBox. This method populates the ComboBox
     * with predefined state options that are used to represent the status of
     * products in the table. It ensures that the ComboBox is cleared before
     * adding the new items.
     *
     * This method does not interact with the database or external resources, so
     * no specific error handling is required for database connections. However,
     * it includes basic validation to ensure that the ComboBox is cleared
     * before adding items.
     */
    private void cargarEstados() {
        try {
            // Clear the ComboBox before adding new items
            state.getItems().clear();

            // Add predefined state options to the ComboBox
            state.getItems().addAll("Disponible", "No disponible", "Descontinuado", "Dañado");

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Carga de Estados");
            alert.setHeaderText(null);
            alert.setContentText("No se pudieron cargar los estados: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Updates a product in the database based on the information provided in
     * the form. This method performs the following operations: 1. Validates
     * that all required fields are filled. 2. Parses and validates the input
     * data. 3. Retrieves the discount ID from the database based on the
     * selected discount description. 4. Updates the product record in the
     * database. 5. Provides feedback to the user based on the outcome of the
     * update operation.
     *
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void updateProduct(ActionEvent event) {
        if (idProduct.getText().isEmpty() || state.getValue() == null || stoke.getText().isEmpty() || price.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campos Vacíos");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, complete todos los campos.");
            alert.showAndWait();
            return;
        }

        String idText = idProduct.getText();
        String estado = state.getValue();
        String stockText = stoke.getText();
        String precioText = price.getText();
        String descripcionDescuento = discount.getValue().toString();

        int productoId;
        int stock;
        double precio;
        int descuentoId = -1;

        try {
            productoId = Integer.parseInt(idText);
            stock = Integer.parseInt(stockText);
            precio = Double.parseDouble(precioText);
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Formato");
            alert.setHeaderText(null);
            alert.setContentText("El ID, el stock o el precio tienen un formato incorrecto.");
            alert.showAndWait();
            return;
        }

        String queryDescuentoId = "SELECT descuentoId FROM Descuentos WHERE descripcion = ?";
        String updateQuery = "UPDATE Productos SET estado = ?, precio = ?, stock = ?, descuentoId = ? WHERE productoId = ?";

        try (Connection conn = new DBconection().establishConnection(); PreparedStatement pstDescuento = conn.prepareStatement(queryDescuentoId); PreparedStatement pstUpdate = conn.prepareStatement(updateQuery)) {

            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión con la base de datos.");
            }

            pstDescuento.setString(1, descripcionDescuento);
            try (ResultSet rs = pstDescuento.executeQuery()) {
                if (rs.next()) {
                    descuentoId = rs.getInt("descuentoId");
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Descuento No Encontrado");
                    alert.setHeaderText(null);
                    alert.setContentText("El descuento seleccionado no se encontró.");
                    alert.showAndWait();
                    return;
                }
            }

            pstUpdate.setString(1, estado);
            pstUpdate.setDouble(2, precio);
            pstUpdate.setInt(3, stock);
            pstUpdate.setInt(4, descuentoId);
            pstUpdate.setInt(5, productoId);

            int rowsAffected = pstUpdate.executeUpdate();
            if (rowsAffected > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Actualización Exitosa");
                alert.setHeaderText(null);
                alert.setContentText("El producto se actualizó correctamente.");
                alert.showAndWait();

                // Close current window and go back to previous window
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.close();

                // Load previous view (change "Inventory.fxml" to your previous view's FXML name)
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Inventory.fxml"));
                Parent root = fxmlLoader.load();

                Stage mainStage = new Stage();
                mainStage.setTitle("Menú Principal");
                mainStage.setScene(new Scene(root));
                mainStage.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de Actualización");
                alert.setHeaderText(null);
                alert.setContentText("No se pudo actualizar el producto. Verifique el ID.");
                alert.showAndWait();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Actualización");
            alert.setHeaderText(null);
            alert.setContentText("Ocurrió un error al actualizar el producto.");
            alert.showAndWait();
        }

        // Clear the fields after updating
        idProduct.clear();
        state.getSelectionModel().clearSelection();
        price.clear();
        stoke.clear();
        discount.getSelectionModel().clearSelection();
    }

    /**
     * Displays an alert with the given type, title, and message.
     *
     * @param type The type of the alert (e.g., ERROR, INFORMATION, WARNING).
     * @param title The title of the alert window.
     * @param message The message to be displayed in the alert.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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
