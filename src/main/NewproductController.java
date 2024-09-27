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
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/**
 * FXML Controller class for managing the product creation view.
 *
 * This controller handles user interactions for adding new products to the
 * inventory, including validating input, inserting product data into the
 * database, and handling errors. It also provides functionality to cancel the
 * current action and return to the previous view.
 *
 * @author Fabricio CUM
 */
public class NewproductController implements Initializable {

    @FXML
    private TextField desciption; // Field for product description
    @FXML
    private ComboBox<String> state; // ComboBox for selecting product state
    @FXML
    private TextField price; // Field for product price
    @FXML
    private TextField stock; // Field for product stock quantity
    @FXML
    private ComboBox<String> category; // ComboBox for selecting product category
    @FXML
    private ComboBox<String> discount; // ComboBox for selecting product discount
    @FXML
    private Button newProduct; // Button for submitting a new product
    @FXML
    private Button cancel; // Button for canceling the current action

    /**
     * Initializes the controller class. This method is called to initialize the
     * controller after its root element has been completely processed.
     *
     * @param url The URL location of the FXML file that was loaded.
     * @param rb The ResourceBundle used to localize the FXML file.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarCategoriasYDescuentos();
        cargarEstados();
    }

    /**
     * Loads categories and discounts into their respective ComboBoxes.
     */
    private void cargarCategoriasYDescuentos() {
        Connection conn = null;

        // Load categories
        String categoriaQuery = "SELECT DISTINCT categoriaId, nombre FROM Categorias"; // Use DISTINCT to avoid duplicates
        try {
            conn = new DBconection().establishConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(categoriaQuery);

            while (rs.next()) {
                String nombreCategoria = rs.getString("nombre");
                if (!category.getItems().contains(nombreCategoria)) {
                    category.getItems().add(nombreCategoria);  // Add category names to ComboBox if not already present
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Base de Datos", "Ocurrió un error al cargar las categorías: " + e.getMessage());
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error de Base de Datos", "Ocurrió un error al cerrar la conexión: " + e.getMessage());
            }
        }

        // Load discounts
        String descuentoQuery = "SELECT descuentoId, descripcion FROM Descuentos";
        try {
            conn = new DBconection().establishConnection(); // Re-establish connection for the next query
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(descuentoQuery);

            while (rs.next()) {
                String descripcionDescuento = rs.getString("descripcion");
                if (!discount.getItems().contains(descripcionDescuento)) {
                    discount.getItems().add(descripcionDescuento);  // Add discount descriptions to ComboBox if not already present
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Base de Datos", "Ocurrió un error al cargar los descuentos: " + e.getMessage());
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error de Base de Datos", "Ocurrió un error al cerrar la conexión: " + e.getMessage());
            }
        }
    }

    /**
     * Loads possible product states into the ComboBox.
     */
    private void cargarEstados() {
        // Define possible states in the Productos table
        state.getItems().clear();
        state.getItems().addAll("Disponible", "No disponible", "Descontinuado", "Dañado");
    }

    /**
     * Retrieves the ID based on the provided name from the specified table and
     * column.
     *
     * @param tabla The table name.
     * @param columna The column name.
     * @param valor The value to match.
     * @return The ID corresponding to the value, or -1 if not found or an error
     * occurs.
     */
    private int obtenerIdPorNombre(String tabla, String columna, String valor) {
        Connection conn = new DBconection().establishConnection();
        String query = "SELECT categoriaId FROM " + tabla + " WHERE " + columna + " = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, valor);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);  // Return the category ID
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Database Error", "An error occurred while retrieving ID by name: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Database Error", "An error occurred while closing the connection: " + e.getMessage());
            }
        }
        return -1;  // In case of error
    }

    /**
     * Retrieves the ID based on the provided description from the specified
     * table and column.
     *
     * @param tabla The table name.
     * @param columna The column name.
     * @param valor The description to match.
     * @return The ID corresponding to the description, or -1 if not found or an
     * error occurs.
     */
    private int obtenerIdPorDescripcion(String tabla, String columna, String valor) {
        Connection conn = new DBconection().establishConnection();
        String query = "SELECT descuentoId FROM " + tabla + " WHERE " + columna + " = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, valor);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);  // Return the discount ID
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Database Error", "An error occurred while retrieving ID by description: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Database Error", "An error occurred while closing the connection: " + e.getMessage());
            }
        }
        return -1;  // In case of error
    }

    /**
     * Handles the event of adding a new product.
     *
     * @param event The ActionEvent triggered by the user action.
     */
    @FXML
    private void newProduct(ActionEvent event) {
        // Obtain values from the form
        String descripcion = desciption.getText();
        String estado = state.getSelectionModel().getSelectedItem();
        String precioStr = price.getText();
        String stockStr = stock.getText();
        String categoriaSeleccionada = category.getSelectionModel().getSelectedItem();
        String descuentoSeleccionado = discount.getSelectionModel().getSelectedItem();

        // Basic validations
        if (descripcion == null || descripcion.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Field", "La descripción no puede estar vacía.");
            return;
        }

        if (estado == null || estado.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Field", "Debes seleccionar un estado.");
            return;
        }

        if (precioStr == null || precioStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Field", "El precio no puede estar vacío.");
            return;
        }

        if (stockStr == null || stockStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Field", "El stock no puede estar vacío.");
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);
            int stockk = Integer.parseInt(stockStr);

            // Additional validations
            if (precio <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Value", "El precio debe ser un valor positivo.");
                return;
            }

            if (stockk < 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Value", "El stock no puede ser negativo.");
                return;
            }

            if (categoriaSeleccionada == null || categoriaSeleccionada.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Field", "Debes seleccionar una categoría.");
                return;
            }

            // Obtain IDs from the database
            int categoriaId = obtenerIdPorNombre("Categorias", "nombre", categoriaSeleccionada);
            int descuentoId = obtenerIdPorDescripcion("Descuentos", "descripcion", descuentoSeleccionado);

            // Insert the product into the database
            Connection conn = new DBconection().establishConnection();
            String query = "INSERT INTO Productos (descripcion, estado, precio, stock, categoriaId, descuentoId) VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, descripcion);
            pst.setString(2, estado);
            pst.setDouble(3, precio);
            pst.setInt(4, stockk);
            pst.setInt(5, categoriaId);
            pst.setInt(6, descuentoId);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Producto agregado con éxito.");

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/Inventory.fxml"));
                    Parent root = loader.load();

                    Stage previousStage = new Stage();
                    previousStage.setTitle("Inventory View");
                    previousStage.setScene(new Scene(root));
                    previousStage.show();

                    // Cerrar la ventana actual (Agregar Producto)
                    Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    currentStage.close();  // Cierra la ventana actual después de cargar la nueva

                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Loading Error", "No se pudo cargar la vista de inventario.");
                }

            } else {
                showAlert(Alert.AlertType.ERROR, "Insertion Error", "Error al agregar el producto.");
                // Close the current window and return to the previous one
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.close();  // Close the current window

                // Load the previous view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Invetory.fxml"));
                Parent root = loader.load();

                Stage previousStage = new Stage();
                previousStage.setTitle("Inventory View");
                previousStage.setScene(new Scene(root));
                previousStage.show();
            }

            // Clear fields after adding
            desciption.clear();
            state.getSelectionModel().clearSelection();
            price.clear();
            stock.clear();
            category.getSelectionModel().clearSelection();
            discount.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format Error", "El precio y el stock deben ser valores numéricos.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error al agregar el producto.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Unknown Error", "Error desconocido al agregar el producto.");
        }
    }

    /**
     * Handles the event of canceling the current action and returning to the
     * previous view.
     *
     * @param event The ActionEvent triggered by the user action.
     */
    @FXML
    private void cancel(ActionEvent event) {

        try {
            // Load the previous view (change the name of FXML as needed)
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
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Error al cargar la vista anterior.");
        }
    }

    /**
     * Shows an alert dialog with the specified type, title, and message.
     *
     * @param type The type of the alert.
     * @param title The title of the alert.
     * @param message The message to be displayed.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
