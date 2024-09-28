/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package main;

import Clases.DBconection;
import Clases.Factura;
import Clases.Producto;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * FXML Controller class responsible for generating bills. This class handles
 * the interaction between the user interface and the database to retrieve
 * client information and update the displayed list of clients.
 *
 * @author Fabricio CUM
 */
public class GenerateBillController implements Initializable {

    private String nombreVendedor;
    @FXML
    private TextField clientName;
    @FXML
    private ListView<String> clientList;
    @FXML
    private TextField codigoVendedorLabel;

    private DBconection dbConnection;
    @FXML
    private TableView<Producto> tableIBill;
    @FXML
    private TableColumn<Producto, Integer> id;
    @FXML
    private TableColumn<Producto, String> Descripcion;

    @FXML
    private TableColumn<Producto, String> estado;

    @FXML
    private TableColumn<Producto, Double> precio;

    @FXML
    private TableColumn<Producto, Integer> stock;

    @FXML
    private TableColumn<Producto, Integer> descuento;
    @FXML
    private ListView<String> productList;
    @FXML
    private TextField searchFieldProduct;
    @FXML
    private Button addProduct;
    @FXML
    private TextField cantidadProduct;
    @FXML
    private Button cancel;
    @FXML
    private Button billButton;
    @FXML
    private Button billWithoutButton;
    @FXML
    private ComboBox<String> state;
    @FXML
    private TextField total;
    @FXML
    private TextField subTotal;
    @FXML
    private TextField discount;
    @FXML
    private ComboBox<String> currency;
    @FXML
    private Button delete;

    /**
     * Initializes the controller class. This method is called to initialize the
     * controller after its root element has been processed. It sets up
     * listeners for the client name input and the client list selection.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configuración de columnas de la tabla con sus propiedades
        id.setCellValueFactory(new PropertyValueFactory<>("productoId"));
        Descripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        estado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        stock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        descuento.setCellValueFactory(new PropertyValueFactory<>("descuentoId"));
        precio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        // Listener para la búsqueda de clientes.
        clientList.setVisible(false); // Oculta el ListView al inicio
        clientName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                updateClientList(newValue);
                clientList.setVisible(true); // Muestra el ListView
            } else {
                clientList.getItems().clear();
                clientList.setVisible(false); // Oculta el ListView
            }
        });

        // Configuración del listener para la selección de cliente.
        clientList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                clientName.setText(newValue);
                clientList.getItems().clear(); // Limpiar la lista después de seleccionar
                clientList.setVisible(false); // Oculta el ListView después de seleccionar
            }
        });

        // Configuración para el listener de búsqueda de productos.
        productList.setVisible(false); // Oculta el ListView al inicio
        searchFieldProduct.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                updateProductList(newValue); // Actualiza la lista de productos
                productList.setVisible(true); // Muestra el ListView
            } else {
                productList.getItems().clear();
                productList.setVisible(false); // Oculta el ListView
            }
        });

        // Configuración del listener para la selección de producto.
        productList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                searchFieldProduct.setText(newValue); // Establece la descripción seleccionada en el TextField
                productList.getItems().clear(); // Limpia la lista después de seleccionar
                productList.setVisible(false); // Oculta el ListView después de seleccionar
            }
        });

        // Configuración adicional de columnas de la tabla.
        // Configurar la columna de ID como Integer.
        id.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getProductoId()).asObject());

        // Descripción como String.
        Descripcion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));

        // Estado como String.
        estado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));

        precio.setCellFactory(tc -> new javafx.scene.control.TableCell<Producto, Double>() {

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item)); // Formato para mostrar con dos decimales y símbolo de moneda.
                }
            }
        });

        // Configurar la columna de stock para mostrar las cantidades de productos.
        stock.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getStock()).asObject());

        // Formateo para mostrar el descuento (Integer) con símbolo de porcentaje.
        descuento.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDescuentoId()).asObject());
        descuento.setCellFactory(tc -> new javafx.scene.control.TableCell<Producto, Integer>() {
            @Override
            protected void updateItem(Integer discount, boolean empty) {
                super.updateItem(discount, empty);
                if (empty || discount == null) {
                    setText(null);
                } else {
                    setText(discount + "%"); // Muestra el descuento como porcentaje entero.
                }
            }
        });

        // Cargar datos adicionales desde la base de datos (monedas y estados).
        cargarEstadosDesdeBaseDeDatos();
        cargarMonedasDesdeBaseDeDatos();
        updateInvoiceTotals();
    }

    /**
     * Updates the client list based on the provided search text. This method
     * fetches client names that match the search criteria and updates the
     * ListView.
     *
     * @param searchText The text entered by the user to search for clients.
     */
    private void updateClientList(String searchText) {
        List<String> clients = searchClients(searchText);
        clientList.getItems().clear();
        clientList.getItems().addAll(clients);
    }

    /**
     * Searches for clients in the database whose names match the provided
     * search text.
     *
     * @param searchText The text to search for in client names.
     * @return A list of client names that match the search criteria.
     */
    private List<String> searchClients(String searchText) {
        List<String> clientNames = new ArrayList<>();
        String query = "SELECT nombre FROM Clientes WHERE nombre LIKE ?";

        try (Connection connection = new DBconection().establishConnection(); // Establece la conexión aquí
                 PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + searchText + "%");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                clientNames.add(resultSet.getString("nombre"));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Manejar la excepción de manera adecuada en producción
        }
        return clientNames;
    }

    /**
     * Updates the list of products based on the input text.
     *
     * This method calls the searchProducts method to retrieve a list of
     * products that match the given search text and updates the ListView with
     * the results.
     *
     * @param searchText the text used to filter the product list
     */
    private void updateProductList(String searchText) {
        List<String> products = searchProducts(searchText); // Busca productos en la base de datos por descripción
        productList.getItems().clear();
        productList.getItems().addAll(products); // Agrega las descripciones encontradas a la lista
    }

    /**
     * Searches for products in the database that match the given search text.
     *
     * This method executes a SQL query to find product names that contain the
     * specified search text. It returns a list of product names found.
     *
     * @param searchText the text to search for in product names
     * @return a list of product names that match the search criteria
     */
    private List<String> searchProducts(String searchText) {
        List<String> productDescriptions = new ArrayList<>();
        String query = "SELECT descripcion FROM Productos WHERE descripcion LIKE ?"; // Filtra por descripción

        try (Connection connection = new DBconection().establishConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + searchText + "%"); // Usa LIKE para buscar coincidencias
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                productDescriptions.add(resultSet.getString("descripcion")); // Agrega la descripción a la lista
            }
        } catch (Exception e) {
            e.printStackTrace(); // Manejar la excepción de manera adecuada en producción
        }
        return productDescriptions;
    }

    /**
     * Adds a product to the invoice table if the quantity is valid and there is
     * sufficient stock. The product is identified based on its description from
     * the search field.
     *
     * @param event The action event triggered when the "Add Product" button is
     * clicked.
     */
    @FXML
    private void addProductTable(ActionEvent event) {
        String productDescription = searchFieldProduct.getText();
        String quantityText = cantidadProduct.getText();

        if (productDescription.isEmpty() || quantityText.isEmpty()) {
            showAlert("Error", "Debe ingresar un producto y la cantidad.", Alert.AlertType.ERROR);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showAlert("Error", "La cantidad debe ser un número positivo.", Alert.AlertType.ERROR);
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "La cantidad debe ser un número entero.", Alert.AlertType.ERROR);
            return;
        }

        Producto producto = getProductByDescription(productDescription);
        if (producto == null) {
            showAlert("Error", "El producto no existe o la descripción es incorrecta.", Alert.AlertType.ERROR);
            return;
        }

        // Validar el estado del producto
        if (!producto.getEstado().equalsIgnoreCase("Disponible")) {
            showAlert("Error", "No se puede agregar un producto que no está disponible.", Alert.AlertType.ERROR);
            return;
        }

        if (producto.getPrecio() <= 0) {
            showAlert("Error", "El precio del producto no es válido.", Alert.AlertType.ERROR);
            return;
        }

        if (producto.getStock() < quantity) {
            showAlert("Error", "Stock insuficiente. Stock disponible: " + producto.getStock(), Alert.AlertType.ERROR);
            return;
        }

        for (Producto p : tableIBill.getItems()) {
            if (p.getProductoId() == producto.getProductoId()) {
                int newQuantity = p.getStock() + quantity;
                if (producto.getStock() < newQuantity) {
                    showAlert("Error", "Stock insuficiente para la nueva cantidad. Stock disponible: " + producto.getStock(), Alert.AlertType.ERROR);
                    return;
                }
                p.setStock(newQuantity);
                tableIBill.refresh();
                showAlert("Éxito", "Cantidad actualizada en la factura.", Alert.AlertType.INFORMATION);
                updateInvoiceTotals();
                return;
            }
        }

        Producto productoParaAgregar = new Producto(producto.getProductoId(), producto.getDescripcion(), producto.getEstado(),
                producto.getPrecio(), quantity, producto.getDescuentoId());
        tableIBill.getItems().add(productoParaAgregar);

        searchFieldProduct.clear();
        cantidadProduct.clear();
        showAlert("Éxito", "Producto agregado correctamente a la factura.", Alert.AlertType.INFORMATION);

        updateInvoiceTotals();
    }

    /**
     * Retrieves the discount percentage for the specified discount ID from the
     * database.
     *
     * @param descuentoId The ID of the discount.
     * @return The discount percentage, or 0 if not found.
     */
    private double getDiscountById(Integer descuentoId) {
        double discountPercentage = 0;

        // Cambia "id" por "descuentoId"
        String query = "SELECT porcentaje FROM Descuentos WHERE descuentoId = ?";
        DBconection dbConnection = new DBconection(); // Create an instance of your DB connection class
        Connection connection = dbConnection.establishConnection(); // Establish the connection
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, descuentoId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                discountPercentage = resultSet.getDouble("porcentaje");
            }
        } catch (SQLException e) {
            showAlert("Error", "Could not retrieve the discount: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try {
                connection.close(); // Close the connection in the finally block
            } catch (SQLException e) {
                showAlert("Error", "Could not close the connection: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }

        return discountPercentage;
    }

    /**
     * Retrieves a product from the database based on its description.
     *
     * @param description The product description.
     * @return A Producto object with all details, including the discount
     * percentage, or null if not found.
     */
    private Producto getProductByDescription(String description) {
        Producto producto = null;

        // Consulta ajustada con JOIN para recuperar el porcentaje de descuento
        String query = "SELECT p.productoId, p.descripcion, p.estado, p.precio, p.stock, d.porcentaje "
                + "FROM Productos p LEFT JOIN Descuentos d ON p.descuentoId = d.descuentoId "
                + "WHERE p.descripcion = ?";

        DBconection dbConnection = new DBconection();
        Connection connection = dbConnection.establishConnection();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, description);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Recuperar los datos de la consulta
                int productoId = resultSet.getInt("productoId");
                String prodDescripcion = resultSet.getString("descripcion");
                String estado = resultSet.getString("estado");
                double precio = resultSet.getDouble("precio");
                int stock = resultSet.getInt("stock");

                // Obtener el porcentaje de descuento y convertir a entero
                int porcentajeDescuento = (int) resultSet.getDouble("porcentaje");

                // Imprimir en consola para verificar los valores
                System.out.println("Producto encontrado: ID=" + productoId + ", Descripción=" + prodDescripcion
                        + ", Precio=" + precio + ", Stock=" + stock + ", Estado=" + estado + ", Descuento=" + porcentajeDescuento + "%");

                // Crear el objeto Producto con el porcentaje de descuento como entero
                producto = new Producto(productoId, prodDescripcion, estado, precio, stock, porcentajeDescuento);
            }
        } catch (SQLException e) {
            showAlert("Error", "No se pudo recuperar el producto: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                showAlert("Error", "No se pudo cerrar la conexión: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }

        return producto;
    }

    /**
     * Displays an alert dialog with the specified title, message, and type.
     *
     * @param title The title of the alert dialog.
     * @param message The content message to be displayed.
     * @param alertType The type of alert (e.g., ERROR, INFORMATION, WARNING).
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Bill.fxml"));
            Parent root = fxmlLoader.load();

            // Get the current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();  // Close the current window

            // Show the previous view
            Stage mainStage = new Stage();
            mainStage.setTitle("Bill Menu");
            mainStage.setScene(new Scene(root));
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Use the showAlert method to display an error message
            showAlert(Alert.AlertType.ERROR, "Loading Error", "Failed to load the previous view.");
        }
    }

    /**
     * Loads the ENUM values for the "estado" field from the database and
     * populates the ComboBox.
     * <p>
     * This method connects to the MySQL database using the `DBconection` class
     * and retrieves the ENUM values defined in the "estado" column of the
     * "Facturas" table. The values are parsed, extracted, and added to the
     * ComboBox named `state`.
     * </p>
     */
    private void cargarEstadosDesdeBaseDeDatos() {
        // ObservableList to hold the ENUM values for the ComboBox
        ObservableList<String> estados = FXCollections.observableArrayList();

        // Create an instance of the DBconection class to establish a database connection
        DBconection dbConnection = new DBconection();
        Connection connection = dbConnection.establishConnection();

        // Check if the connection is not null before proceeding
        if (connection != null) {
            try {
                // SQL query to fetch the ENUM values for the "estado" column in the "Facturas" table
                String query = "SELECT COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_NAME = 'Facturas' AND COLUMN_NAME = 'estado'";
                PreparedStatement statement = connection.prepareStatement(query);

                // Execute the query and store the result
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // Retrieve the ENUM values as a string from the COLUMN_TYPE field
                    String enumValues = resultSet.getString("COLUMN_TYPE");

                    // Extract only the values inside the parentheses, e.g., "enum('Pagada','Pendiente','Cancelada')"
                    enumValues = enumValues.substring(enumValues.indexOf("(") + 1, enumValues.indexOf(")"));

                    // Split the values into an array, remove quotes, and add each to the ObservableList
                    String[] valuesArray = enumValues.replace("'", "").split(",");

                    // Loop through the array and add each value to the `estados` list
                    for (String value : valuesArray) {
                        estados.add(value);
                    }
                }

                // Assign the ObservableList to the ComboBox
                state.setItems(estados);

                // Close the ResultSet and Statement to release database resources
                resultSet.close();
                statement.close();
            } catch (Exception e) {
                // Print the stack trace in case of an exception during the database operation
                e.printStackTrace();
            } finally {
                try {
                    // Close the connection to avoid memory leaks
                    connection.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Loads the currency names from the "Moneda" table and populates the
     * ComboBox.
     * <p>
     * This method establishes a connection to the MySQL database using the
     * {@link DBconection} class, retrieves the currency names defined in the
     * "Moneda" table, and adds these values to the ComboBox named `currency`.
     * </p>
     *
     * <p>
     * The process involves executing a simple SQL SELECT query to obtain the
     * `nombre` values, storing them in an {@link ObservableList}, and then
     * setting this list as the items for the ComboBox. In case of a database
     * error, it catches and prints the exception stack trace.
     * </p>
     *
     * <pre>
     * Example usage:
     * // Assuming this is called inside a Controller class with the ComboBox `currency`
     * cargarMonedasDesdeBaseDeDatos();
     * </pre>
     *
     * <p>
     * Note: This method should be called during the initialization phase (e.g.,
     * inside the `initialize` method) to ensure the ComboBox is populated when
     * the interface is displayed.
     * </p>
     *
     * @see DBconection#establishConnection() for database connection handling.
     */
    private void cargarMonedasDesdeBaseDeDatos() {
        // ObservableList to hold the currency names for the ComboBox
        ObservableList<String> currencies = FXCollections.observableArrayList();

        // Create an instance of the DBconection class to establish a database connection
        DBconection dbConnection = new DBconection();
        Connection connection = dbConnection.establishConnection();

        // Check if the connection is not null before proceeding
        if (connection != null) {
            try {
                // SQL query to fetch the currency names from the "Moneda" table
                String query = "SELECT nombre FROM Moneda";
                PreparedStatement statement = connection.prepareStatement(query);

                // Execute the query and store the result
                ResultSet resultSet = statement.executeQuery();

                // Loop through the ResultSet and add each currency name to the ObservableList
                while (resultSet.next()) {
                    // Retrieve the value of the "nombre" column and add it to the list
                    currencies.add(resultSet.getString("nombre"));
                }

                // Assign the ObservableList to the ComboBox
                currency.setItems(currencies);

                // Close the ResultSet and Statement to release database resources
                resultSet.close();
                statement.close();
            } catch (Exception e) {
                // Print the stack trace in case of an exception during the database operation
                e.printStackTrace();
            } finally {
                try {
                    // Close the connection to avoid memory leaks
                    connection.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Handles the billing process by validating the required fields and
     * inserting the corresponding records in the database tables. It also
     * deducts stock quantities for each product included in the invoice.
     *
     * @param event The action event triggered by the "bill" button.
     */
    @FXML
    private void bill(ActionEvent event) {

    }

    /**
     * Handles the billing process without affecting the product stock. This
     * method validates the required fields and inserts the corresponding
     * records in the database tables but skips the stock update step.
     *
     * @param event The action event triggered by the "billWithout" button.
     */
    @FXML
    private void billWithout(ActionEvent event) {

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
     * Formats the given amount to display correctly in the TextField. If the
     * amount has no significant decimal places, shows it without trailing
     * zeros.
     */
    private String formatAmount(double amount) {
        if (amount == (long) amount) {
            // Si el valor no tiene parte decimal significativa, muestra como número entero
            return String.format("%d", (long) amount);
        } else {
            // Si tiene parte decimal significativa, muestra con un solo decimal o con dos si es necesario
            return String.format("%.2f", amount).replaceAll("0*$", ""); // Elimina ceros al final
        }
    }

    /**
     * Updates the subtotal amount displayed in the subTotal TextField.
     */
    @FXML
    private void updateSubTotalAmount() {
        double subTotalAmount = calculateSubTotal(); // Calcula el subtotal
        subTotal.setText(formatAmount(subTotalAmount)); // Actualiza con formato corregido
    }

    /**
     * Updates the discount amount displayed in the discount TextField.
     */
    @FXML
    private void updateDiscountAmount() {
        double discountAmount = calculateDiscount(); // Calcula el descuento total
        discount.setText(formatAmount(discountAmount)); // Actualiza con formato corregido
    }

    /**
     * Updates the total amount displayed in the total TextField.
     */
    @FXML
    private void updateTotalAmount() {
        double finalTotal = calculateFinalTotal(); // Calcula el total final
        total.setText(formatAmount(finalTotal)); // Actualiza con formato corregido
    }

    /**
     * Calculates the subtotal of the invoice based on the products in the
     * table.
     */
    private double calculateSubTotal() {
        double subTotalAmount = 0.0;
        for (Producto product : tableIBill.getItems()) {
            subTotalAmount += product.getPrecio() * product.getStock(); // Subtotal por cada producto: precio * cantidad
        }
        return subTotalAmount; // Devuelve el subtotal
    }

    /**
     * Calculates the total discount based on the discount field in the
     * products.
     */
    private double calculateDiscount() {
        double totalDiscount = 0.0;
        for (Producto product : tableIBill.getItems()) {
            double discountPercentage = product.getDescuentoId(); // Obtiene el valor de descuento del producto (puede ser 0)
            if (discountPercentage > 0) { // Solo calcula el descuento si el porcentaje es mayor que 0
                double discountDecimal = discountPercentage / 100.0; // Convierte a decimal (40 -> 0.40)
                double discountAmount = product.getPrecio() * discountDecimal * product.getStock(); // Precio * porcentaje * cantidad
                totalDiscount += discountAmount; // Acumula el descuento total
            }
        }
        return totalDiscount; // Devuelve el total de descuento
    }

    /**
     * Calculates the final total after applying the discount.
     */
    private double calculateFinalTotal() {
        double subtotal = calculateSubTotal(); // Obtiene el subtotal
        double discountAmount = calculateDiscount(); // Obtiene el total de descuento
        return subtotal - discountAmount; // Devuelve el total final
    }

    /**
     * Updates the totals displayed in the invoice.
     */
    private void updateInvoiceTotals() {
        updateSubTotalAmount(); // Actualiza el subtotal
        updateDiscountAmount(); // Actualiza el descuento
        updateTotalAmount(); // Actualiza el total
    }

    /**
     * Deletes the selected product from the inventory.
     *
     * @param event The event that triggered this method.
     */
    @FXML
    private void delete(ActionEvent event) {
        // Obtener el producto seleccionado
        Producto productoSeleccionado = tableIBill.getSelectionModel().getSelectedItem();

        if (productoSeleccionado != null) {
            // Confirmar eliminación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmación de eliminación");
            alert.setHeaderText("¿Está seguro de que desea eliminar este producto?");
            alert.setContentText("Producto: " + productoSeleccionado.getDescripcion());

            if (alert.showAndWait().get() == ButtonType.OK) {
                // Eliminar solo de la tabla
                tableIBill.getItems().remove(productoSeleccionado); // Eliminar de la tabla

                // Mensaje de éxito
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Eliminación exitosa");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Producto eliminado exitosamente de la tabla.");
                successAlert.showAndWait();
            }
        } else {
            // Mostrar mensaje si no hay producto seleccionado
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Selección inválida");
            alert.setHeaderText("No hay producto seleccionado");
            alert.setContentText("Seleccione un producto para eliminar.");
            alert.showAndWait();
        }
    }

    /**
     * Generates a PDF report for the sale details. This method retrieves sale
     * data from the database, formats it into a PDF file, and attempts to open
     * the file in the system's default PDF viewer.
     *
     * @param event the event triggered by the button click to generate the
     * report.
     */
    @FXML
    private void generateSaleReport(ActionEvent event) throws SQLException, IOException {
        Connection conn = null;
        PDDocument document = null;
        PDPageContentStream contentStream = null;

        try {
            // Establish connection using your custom DB connection class
            conn = new DBconection().establishConnection();

            // Create a new document
            document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            // Create content stream to write to the PDF
            contentStream = new PDPageContentStream(document, page);

            // Add logo
            File logoFile = new File("C:\\Users\\fabri\\Documents\\NetBeansProjects\\Vivero\\src\\Image\\logo.jpeg");
            if (!logoFile.exists()) {
                throw new FileNotFoundException("Logo file not found at: " + logoFile.getPath());
            }
            PDImageXObject logo = PDImageXObject.createFromFile(logoFile.getPath(), document);
            float logoWidth = 200;
            float logoHeight = 150;
            contentStream.drawImage(logo, 25, page.getMediaBox().getHeight() - logoHeight - 25, logoWidth, logoHeight);

            // Add date and time in the top-right corner
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            float margin = 25;
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            contentStream.newLineAtOffset(pageWidth - margin - 200, pageHeight - margin);
            contentStream.showText("Fecha de Generación: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            contentStream.endText();

            // Add Vivero name below the date
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(pageWidth - margin - 200, pageHeight - margin - 20);
            contentStream.showText("Vivero Pluma Roja");
            contentStream.endText();

            // Add phone and address
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(logoWidth + 70, page.getMediaBox().getHeight() - 100);
            contentStream.showText("Teléfono: +506 85651597");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Dirección: 300 metros sur de escuela Darizara,");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Barrio: Plaza Canoas, Canoas, Cantón Corredores,");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Provincia Puntarenas, 61003, Costa Rica.");
            contentStream.endText();

            // Add title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(margin, pageHeight - logoHeight - 100);
            contentStream.showText("Reporte de Detalle de Ventas");
            contentStream.endText();

            // Add customer and seller names
            String customerName = "Nombre del Cliente"; // Replace with actual data from your database
            String sellerName = "Nombre del Vendedor"; // Replace with actual data from your database

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(margin, pageHeight - logoHeight - 120); // Position below title
            contentStream.showText("Cliente: " + customerName);
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Vendedor: " + sellerName);
            contentStream.endText();

            // Define column widths and positions
            int saleIdWidth = 80;
            int productIdWidth = 80;
            int descriptionWidth = 200;
            int quantityWidth = 60;
            int priceWidth = 60;
            int totalWidth = 60;
            int lineHeight = 12;

            int xSaleId = 25;
            int xProductId = xSaleId + saleIdWidth;
            int xDescription = xProductId + productIdWidth;
            int xQuantity = xDescription + descriptionWidth;
            int xPrice = xQuantity + quantityWidth;
            int xTotal = xPrice + priceWidth;

            int headerHeight = 20; // Height for header row
            int yPosition = (int) (pageHeight - logoHeight - 140 - headerHeight); // Initial y-position for data

            // Add table headers
            contentStream.setLineWidth(1f);
            contentStream.setStrokingColor(Color.BLACK);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            contentStream.newLineAtOffset(xSaleId, yPosition + headerHeight);
            contentStream.showText("Sale ID");
            contentStream.newLineAtOffset(xProductId - xSaleId, 0);
            contentStream.showText("Product ID");
            contentStream.newLineAtOffset(xDescription - xProductId, 0);
            contentStream.showText("Description");
            contentStream.newLineAtOffset(xQuantity - xDescription, 0);
            contentStream.showText("Quantity");
            contentStream.newLineAtOffset(xPrice - xQuantity, 0);
            contentStream.showText("Price");
            contentStream.newLineAtOffset(xTotal - xPrice, 0);
            contentStream.showText("Total");
            contentStream.endText();

            // Draw horizontal line under the headers
            contentStream.moveTo(xSaleId, yPosition + headerHeight - 2);
            contentStream.lineTo(xTotal + totalWidth, yPosition + headerHeight - 2);
            contentStream.stroke();

            // Draw vertical lines
            contentStream.moveTo(xSaleId, yPosition + headerHeight);
            contentStream.lineTo(xSaleId, yPosition - (lineHeight * 40));
            contentStream.stroke();

            contentStream.moveTo(xProductId, yPosition + headerHeight);
            contentStream.lineTo(xProductId, yPosition - (lineHeight * 40));
            contentStream.stroke();

            contentStream.moveTo(xDescription, yPosition + headerHeight);
            contentStream.lineTo(xDescription, yPosition - (lineHeight * 40));
            contentStream.stroke();

            contentStream.moveTo(xQuantity, yPosition + headerHeight);
            contentStream.lineTo(xQuantity, yPosition - (lineHeight * 40));
            contentStream.stroke();

            contentStream.moveTo(xPrice, yPosition + headerHeight);
            contentStream.lineTo(xPrice, yPosition - (lineHeight * 40));
            contentStream.stroke();

            contentStream.moveTo(xTotal, yPosition + headerHeight);
            contentStream.lineTo(xTotal, yPosition - (lineHeight * 40));
            contentStream.stroke();

            // Draw bottom border of the table
            contentStream.moveTo(xSaleId, yPosition - (lineHeight * 40) - 2);
            contentStream.lineTo(xTotal + totalWidth, yPosition - (lineHeight * 40) - 2);
            contentStream.stroke();

            // Draw right border of the table
            contentStream.moveTo(xTotal + totalWidth, yPosition + headerHeight);
            contentStream.lineTo(xTotal + totalWidth, yPosition - (lineHeight * 40));
            contentStream.stroke();

            // Retrieve sale data from the database
            String query = "SELECT saleId, productId, description, quantity, price, total, customerName, sellerName FROM SalesDetails"; // Adjust the query as necessary
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Write table rows
            while (rs.next()) {
                // Check if the line fits within the page, otherwise create a new page
                if (yPosition - lineHeight < 50) {
                    contentStream.endText();
                    contentStream.close();

                    // Add a new page if needed
                    PDPage newPage = new PDPage();
                    document.addPage(newPage);
                    contentStream = new PDPageContentStream(document, newPage);

                    // Add header to the new page
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    contentStream.newLineAtOffset(margin, newPage.getMediaBox().getHeight() - logoHeight - 100);
                    contentStream.showText("Reporte de Detalle de Ventas");
                    contentStream.endText();

                    // Reset yPosition for the new page
                    yPosition = (int) (newPage.getMediaBox().getHeight() - logoHeight - 140 - headerHeight);
                }

                // Write each row of sale details
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(xSaleId, yPosition);
                contentStream.showText(rs.getString("saleId"));
                contentStream.newLineAtOffset(xProductId - xSaleId, 0);
                contentStream.showText(rs.getString("productId"));
                contentStream.newLineAtOffset(xDescription - xProductId, 0);
                contentStream.showText(rs.getString("description"));
                contentStream.newLineAtOffset(xQuantity - xDescription, 0);
                contentStream.showText(rs.getString("quantity"));
                contentStream.newLineAtOffset(xPrice - xQuantity, 0);
                contentStream.showText(rs.getString("price"));
                contentStream.newLineAtOffset(xTotal - xPrice, 0);
                contentStream.showText(rs.getString("total"));
                contentStream.endText();

                // Move to the next row
                yPosition -= lineHeight;
            }

            // Finalize the PDF
            contentStream.close();
            document.save("C:\\Users\\fabri\\Downloads\\detalle_venta.pdf");
            document.close();

            // Open the PDF
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File("C:\\Users\\fabri\\Downloads\\detalle_venta.pdf"));
            }

            // Inform the user
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Report Generated");
            alert.setHeaderText(null);
            alert.setContentText("The sale report has been generated successfully.");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred while generating the report.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (document != null) {
                document.close();
            }
        }
    }

}
