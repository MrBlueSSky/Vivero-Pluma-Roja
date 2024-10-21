/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package main;

import Clases.DBconection;
import Clases.Producto;
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
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

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
     * This method connects to the MySQL database using the DBconection class
     * and retrieves the ENUM values defined in the "estado" column of the
     * "Facturas" table. The values are parsed, extracted, and added to the
     * ComboBox named state.
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

                    // Loop through the array and add each value to the estados list
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
     * "Moneda" table, and adds these values to the ComboBox named currency.
     * </p>
     *
     * <p>
     * The process involves executing a simple SQL SELECT query to obtain the
     * nombre values, storing them in an {@link ObservableList}, and then
     * setting this list as the items for the ComboBox. In case of a database
     * error, it catches and prints the exception stack trace.
     * </p>
     *
     * <pre>
     * Example usage:
     * // Assuming this is called inside a Controller class with the ComboBox currency
     * cargarMonedasDesdeBaseDeDatos();
     * </pre>
     *
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
        // Validate fields
        if (codigoVendedorLabel.getText().isEmpty()) {
            showAlert("Error", "El código del vendedor no puede estar vacío.");
            return;
        }

        String codigoVendedor = codigoVendedorLabel.getText();
        if (!vendedorExiste(codigoVendedor)) {
            showAlert("Error", "El vendedor no existe en la base de datos.");
            return;
        }

        if (state.getSelectionModel().isEmpty()) {
            showAlert("Error", "Seleccione un estado.");
            return;
        }

        if (currency.getSelectionModel().isEmpty()) {
            showAlert("Error", "Seleccione una moneda.");
            return;
        }

        if (tableIBill.getItems().isEmpty()) {
            showAlert("Error", "La tabla debe tener al menos un producto para poder facturar.");
            return;
        }

        // All validations passed, proceed to create the invoice
        DBconection dbConnection = new DBconection();
        try (Connection connection = dbConnection.establishConnection()) {
            if (connection != null) {
                connection.setAutoCommit(false); // Start transaction

                // Get the current date
                java.util.Date today = new java.util.Date();
                java.sql.Date sqlDate = new java.sql.Date(today.getTime());

                // Insert into Ventas
                String insertVentasQuery = "INSERT INTO Ventas (fecha, clienteId, vendedorId) VALUES (?, ?, ?)";
                try (PreparedStatement ventasStatement = connection.prepareStatement(insertVentasQuery, Statement.RETURN_GENERATED_KEYS)) {
                    ventasStatement.setDate(1, sqlDate);
                    ventasStatement.setInt(2, getClienteId(clientName.getText())); // Implement this method
                    ventasStatement.setInt(3, getVendedorId(codigoVendedor)); // Implement this method too

                    int rowsAffected = ventasStatement.executeUpdate();
                    System.out.println("Rows affected in Ventas: " + rowsAffected); // Debugging message

                    if (rowsAffected > 0) {
                        ResultSet generatedKeys = ventasStatement.getGeneratedKeys();
                        int ventasId = 0;
                        if (generatedKeys.next()) {
                            ventasId = generatedKeys.getInt(1);
                            System.out.println("Generated ventasId: " + ventasId); // Debugging message

                            // Insert into Facturas
                            String insertFacturasQuery = "INSERT INTO Facturas (ventasId, fechaFactura, estado, monedaId) VALUES (?, ?, ?, ?)";
                            try (PreparedStatement facturasStatement = connection.prepareStatement(insertFacturasQuery)) {
                                facturasStatement.setInt(1, ventasId);
                                facturasStatement.setDate(2, sqlDate);
                                facturasStatement.setString(3, state.getValue());
                                facturasStatement.setInt(4, getMonedaId(currency.getValue())); // Implement this method

                                if (facturasStatement.executeUpdate() > 0) {
                                    System.out.println("Factura generated successfully."); // Debugging message

                                    // Insert into DetalleVentas
                                    String insertDetalleVentasQuery = "INSERT INTO DetalleVentas (ventasId, productoId, cantidad, precioUnitario) VALUES (?, ?, ?, ?)";
                                    try (PreparedStatement detalleStatement = connection.prepareStatement(insertDetalleVentasQuery)) {
                                        for (Producto producto : tableIBill.getItems()) {
                                            detalleStatement.setInt(1, ventasId);
                                            detalleStatement.setInt(2, producto.getProductoId());
                                            detalleStatement.setInt(3, producto.getStock()); // Obtiene la cantidad de stock del producto
                                            detalleStatement.setDouble(4, producto.getPrecio()); // Asumiendo que getPrecio() devuelve el precio
                                            detalleStatement.executeUpdate();

                                            // Disminuir el stock para el producto
                                            updateProductStock(producto.getProductoId(), producto.getStock(), connection);
                                        }
                                    }
                                    connection.commit(); // Commit transaction
                                    showAlert("Éxito", "Factura generada correctamente.");
                                    returnBill();
                                } else {
                                    showAlert("Error", "No se pudo generar la factura. Por favor, intente de nuevo.");
                                }
                            }
                        } else {
                            showAlert("Error", "No se pudo obtener el ID de la venta. Por favor, intente de nuevo.");
                        }
                    } else {
                        showAlert("Error", "No se pudo registrar la venta. Por favor, intente de nuevo.");
                    }
                } catch (SQLException e) {
                    connection.rollback(); // Rollback transaction if something goes wrong
                    e.printStackTrace();
                    showAlert("Error", "Error al generar la factura. Intente de nuevo.");
                }
            } else {
                showAlert("Error", "No se pudo establecer conexión a la base de datos.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Error de conexión a la base de datos.");
        }
    }

    /**
     * Helper method to check if the vendor exists in the database.
     *
     * @param cedulaVendedor The vendor's identification number (cedula) to
     * check for existence.
     * @return true if the vendor exists, false otherwise.
     */
    private boolean vendedorExiste(String cedulaVendedor) {
        DBconection dbConnection = new DBconection();
        Connection connection = dbConnection.establishConnection();

        if (connection != null) {
            String query = "SELECT COUNT(*) FROM Vendedor WHERE cedula = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, cedulaVendedor);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // Returns true if exists
                }
            } catch (SQLException e) {
                showAlert("Error al verificar el vendedor", "No se pudo verificar la existencia del vendedor. Por favor, inténtelo de nuevo.", AlertType.ERROR);
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * Method to get the client ID based on the client's name.
     *
     * @param clientName The name of the client whose ID is to be retrieved.
     * @return The ID of the client, or -1 if the client is not found.
     */
    private int getClienteId(String clientName) {
        DBconection dbConnection = new DBconection();
        Connection connection = dbConnection.establishConnection();
        int clientId = -1; // Default value for not found

        if (connection != null) {
            String query = "SELECT clienteId FROM Clientes WHERE nombre = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, clientName);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    clientId = resultSet.getInt("clienteId"); // Retrieve client ID
                }
            } catch (SQLException e) {
                showAlert("Error al obtener el ID del cliente", "No se pudo obtener el ID del cliente. Por favor, inténtelo de nuevo.", AlertType.ERROR);
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return clientId; // Returns -1 if client not found
    }

    /**
     * Method to get the vendor ID based on the vendor's identification number
     * (cedula).
     *
     * @param cedulaVendedor The vendor's identification number (cedula) to
     * retrieve the vendor ID.
     * @return The ID of the vendor, or -1 if the vendor is not found.
     */
    private int getVendedorId(String cedulaVendedor) {
        DBconection dbConnection = new DBconection();
        Connection connection = dbConnection.establishConnection();
        int vendedorId = -1; // Default value for not found

        if (connection != null) {
            String query = "SELECT vendedorId FROM Vendedor WHERE cedula = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, cedulaVendedor);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    vendedorId = resultSet.getInt("vendedorId");
                }
            } catch (SQLException e) {
                showAlert("Error al obtener el ID del vendedor",
                        "No se pudo obtener el ID del vendedor. Por favor,"
                        + " inténtelo de nuevo.", AlertType.ERROR);
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return vendedorId; // Returns -1 if vendor not found
    }

    /**
     * Method to get the currency ID based on the currency name.
     *
     * @param currencyName The name of the currency whose ID is to be retrieved.
     * @return The ID of the currency, or -1 if the currency is not found.
     */
    private int getMonedaId(String currencyName) {
        DBconection dbConnection = new DBconection();
        Connection connection = dbConnection.establishConnection();
        int monedaId = -1; // Default value for not found

        if (connection != null) {
            String query = "SELECT monedaId FROM Moneda WHERE nombre = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, currencyName);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    monedaId = resultSet.getInt("monedaId"); // Retrieve currency ID
                }
            } catch (SQLException e) {
                showAlert("Error al obtener el ID de la moneda", "No se pudo obtener el ID de la moneda. Por favor, inténtelo de nuevo.", AlertType.ERROR);
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return monedaId; // Returns -1 if currency not found
    }

    /**
     * Method to update the stock of a product in the database.
     *
     * @param productoId The ID of the product whose stock is to be updated.
     * @param cantidad The amount to subtract from the product's stock.
     * @param connection The database connection to be used for the update.
     */
    private void updateProductStock(int productoId, int cantidad, Connection connection) {
        String updateQuery = "UPDATE Productos SET stock = stock - ? WHERE productoId = ?";

        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            updateStatement.setInt(1, cantidad);   // Cantidad a restar del stock
            updateStatement.setInt(2, productoId); // ID del producto a actualizar

            int rowsAffected = updateStatement.executeUpdate();
            if (rowsAffected == 0) {
                // No se encontró el producto
                showAlert("Error", "No se encontró el producto con ID: " + productoId, AlertType.ERROR);
            }
        } catch (SQLException e) {
            // Manejo de excepciones
            showAlert("Error al actualizar el stock", "No se pudo actualizar el stock del producto. Por favor, inténtelo de nuevo.", AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Method to show an alert with a specified title and message.
     *
     * @param title The title of the alert dialog.
     * @param message The message to be displayed in the alert dialog.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        // Validate fields
        if (codigoVendedorLabel.getText().isEmpty()) {
            showAlert("Error", "El código del vendedor no puede estar vacío.");
            return;
        }

        String codigoVendedor = codigoVendedorLabel.getText();
        if (!vendedorExiste(codigoVendedor)) {
            showAlert("Error", "El vendedor no existe en la base de datos.");
            return;
        }

        if (state.getSelectionModel().isEmpty()) {
            showAlert("Error", "Seleccione un estado.");
            return;
        }

        if (currency.getSelectionModel().isEmpty()) {
            showAlert("Error", "Seleccione una moneda.");
            return;
        }

        if (tableIBill.getItems().isEmpty()) {
            showAlert("Error", "La tabla debe tener al menos un producto para poder facturar.");
            return;
        }

        // All validations passed, proceed to create the invoice
        DBconection dbConnection = new DBconection();
        try (Connection connection = dbConnection.establishConnection()) {
            if (connection != null) {
                connection.setAutoCommit(false); // Start transaction

                // Get the current date
                java.util.Date today = new java.util.Date();
                java.sql.Date sqlDate = new java.sql.Date(today.getTime());

                // Insert into Ventas
                String insertVentasQuery = "INSERT INTO Ventas (fecha, clienteId, vendedorId) VALUES (?, ?, ?)";
                try (PreparedStatement ventasStatement = connection.prepareStatement(insertVentasQuery, Statement.RETURN_GENERATED_KEYS)) {
                    ventasStatement.setDate(1, sqlDate);
                    ventasStatement.setInt(2, getClienteId(clientName.getText())); // Get client ID
                    ventasStatement.setInt(3, getVendedorId(codigoVendedor)); // Get vendor ID

                    int rowsAffected = ventasStatement.executeUpdate();
                    System.out.println("Rows affected in Ventas: " + rowsAffected); // Debugging message

                    if (rowsAffected > 0) {
                        ResultSet generatedKeys = ventasStatement.getGeneratedKeys();
                        int ventasId = 0;
                        if (generatedKeys.next()) {
                            ventasId = generatedKeys.getInt(1);
                            System.out.println("Generated ventasId: " + ventasId); // Debugging message

                            // Insert into Facturas
                            String insertFacturasQuery = "INSERT INTO Facturas (ventasId, fechaFactura, estado, monedaId) VALUES (?, ?, ?, ?)";
                            try (PreparedStatement facturasStatement = connection.prepareStatement(insertFacturasQuery)) {
                                facturasStatement.setInt(1, ventasId);
                                facturasStatement.setDate(2, sqlDate);
                                facturasStatement.setString(3, state.getValue());
                                facturasStatement.setInt(4, getMonedaId(currency.getValue())); // Get currency ID

                                if (facturasStatement.executeUpdate() > 0) {
                                    System.out.println("Factura generated successfully."); // Debugging message

                                    // Insert into DetalleVentas without updating stock
                                    String insertDetalleVentasQuery = "INSERT INTO DetalleVentas (ventasId, productoId, cantidad, precioUnitario) VALUES (?, ?, ?, ?)";
                                    try (PreparedStatement detalleStatement = connection.prepareStatement(insertDetalleVentasQuery)) {
                                        for (Producto producto : tableIBill.getItems()) {
                                            detalleStatement.setInt(1, ventasId);
                                            detalleStatement.setInt(2, producto.getProductoId());
                                            detalleStatement.setInt(3, producto.getStock()); // Get the quantity from the product
                                            detalleStatement.setDouble(4, producto.getPrecio()); // Assuming getPrecio() returns the price
                                            detalleStatement.executeUpdate();
                                        }
                                        connection.commit(); 
                                        showAlert("Éxito", "Factura generada correctamente.");
                                        returnBill();
                                    }
                                } else {
                                    showAlert("Error", "No se pudo generar la factura. Por favor, intente de nuevo.");
                                }
                            }
                        } else {
                            showAlert("Error", "No se pudo obtener el ID de la venta. Por favor, intente de nuevo.");
                        }
                    } else {
                        showAlert("Error", "No se pudo registrar la venta. Por favor, intente de nuevo.");
                    }
                } catch (SQLException e) {
                    connection.rollback();
                    e.printStackTrace();
                    showAlert("Error", "Error al generar la factura. Intente de nuevo.");
                }
            } else {
                showAlert("Error", "No se pudo establecer conexión a la base de datos.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Error de conexión a la base de datos.");
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
                updateInvoiceTotals();
                successAlert.setHeaderText(null);
                successAlert.setContentText("Producto eliminado exitosamente de la tabla.");
                successAlert.showAndWait();
                updateInvoiceTotals();
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
     * Returns to the inventory window, closing the current window.
     *
     * @param event The action event that triggers the return.
     */
    
    private void returnBill() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Bill.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Facturas");
            stage.show();

            // Close the current window
            Stage currentStage = (Stage) billButton.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}