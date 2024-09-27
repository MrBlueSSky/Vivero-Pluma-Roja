/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package main;

import Clases.DBconection;
import Clases.Producto;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

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

    /**
     * Initializes the controller class. This method is called to initialize the
     * controller after its root element has been processed. It sets up
     * listeners for the client name input and the client list selection.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clientList.setVisible(false); // Oculta el ListView al inicio

        // Configuración para el listener de búsqueda de clientes.
        clientName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                updateClientList(newValue);
                clientList.setVisible(true); // Muestra el ListView
            } else {
                clientList.getItems().clear();
                clientList.setVisible(false); // Oculta el ListView
            }
        });

        // Configuración para el listener de selección de cliente.
        clientList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                clientName.setText(newValue);
                clientList.getItems().clear(); // Limpiar la lista después de seleccionar
                clientList.setVisible(false); // Oculta el ListView después de seleccionar
            }
        });

        productList.setVisible(false); // Oculta el ListView al inicio

        // Configuración para el listener de búsqueda de productos.
        searchFieldProduct.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                updateProductList(newValue); // Actualiza la lista de productos
                productList.setVisible(true); // Muestra el ListView
            } else {
                productList.getItems().clear();
                productList.setVisible(false); // Oculta el ListView
            }
        });

        // Configuración para el listener de selección de producto.
        productList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                searchFieldProduct.setText(newValue); // Establece la descripción seleccionada en el TextField
                productList.getItems().clear(); // Limpia la lista después de seleccionar
                productList.setVisible(false); // Oculta el ListView después de seleccionar
            }
        });

        // Configuración de columnas de la tabla.
        id.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getProductoId()).asObject());
        Descripcion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));
        estado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));

        // Formateo para mostrar siempre dos decimales en el precio.
        precio.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrecio()).asObject());
        precio.setCellFactory(tc -> new javafx.scene.control.TableCell<Producto, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", price)); // Formatea con 2 decimales
                }
            }
        });

        stock.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getStock()).asObject());

        // Formateo para mostrar el descuento
        descuento.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDescuentoId()).asObject());
        descuento.setCellFactory(tc -> new javafx.scene.control.TableCell<Producto, Integer>() {
            @Override
            protected void updateItem(Integer discount, boolean empty) {
                super.updateItem(discount, empty);
                if (empty || discount == null) {
                    setText(null);
                } else {
                    setText(discount + "%"); // Agrega el símbolo de porcentaje al valor del descuento
                }
            }
        });
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
        // Verificar que el campo de producto y cantidad no estén vacíos
        String productDescription = searchFieldProduct.getText();
        String quantityText = cantidadProduct.getText();

        if (productDescription.isEmpty() || quantityText.isEmpty()) {
            showAlert("Error", "Debe ingresar un producto y la cantidad.", Alert.AlertType.ERROR);
            return;
        }

        // Verificar que la cantidad sea un número entero válido
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

        // Buscar el producto en la base de datos basado en la descripción
        Producto producto = getProductByDescription(productDescription);
        if (producto == null) {
            showAlert("Error", "El producto no existe o la descripción es incorrecta.", Alert.AlertType.ERROR);
            return;
        }

        // Verificar que haya suficiente stock
        if (producto.getStock() < quantity) {
            showAlert("Error", "Stock insuficiente. Stock disponible: " + producto.getStock(), Alert.AlertType.ERROR);
            return;
        }

        // Agregar el producto a la tabla
        Producto productoParaAgregar = new Producto(producto.getProductoId(), producto.getDescripcion(), producto.getEstado(),
                producto.getPrecio(), quantity, producto.getDescuentoId());
        tableIBill.getItems().add(productoParaAgregar);

        // Limpiar los campos de texto
        searchFieldProduct.clear();
        cantidadProduct.clear();
        showAlert("Éxito", "Producto agregado correctamente a la factura.", Alert.AlertType.INFORMATION);
    }

    /**
     * Searches for a product in the database by its description and retrieves
     * its details.
     *
     * @param description The description of the product to be searched.
     * @return A `Producto` object with the product's details if found, or
     * `null` if not found.
     */
    private Producto getProductByDescription(String description) {
        String query = "SELECT * FROM Productos WHERE descripcion = ?";
        try (Connection connection = new DBconection().establishConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, description);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new Producto(
                        resultSet.getInt("productoId"),
                        resultSet.getString("descripcion"),
                        resultSet.getString("estado"),
                        resultSet.getDouble("precio"),
                        resultSet.getInt("stock"),
                        resultSet.getInt("descuentoId")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
}
