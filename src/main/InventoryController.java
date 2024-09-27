/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package main;

import Clases.DBconection;
import Clases.Producto;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * FXML Controller class for managing the inventory view. Handles user
 * interactions and updates the inventory table.
 *
 * @author Fabricio CUM
 */
public class InventoryController implements Initializable {

    @FXML
    private TableView<Producto> tableInventory;

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

    private ObservableList<Producto> productosList;

    @FXML
    private Button nuevoProdcto;
    @FXML
    private Button facturas;

    @FXML
    private TextField searchField;
    @FXML
    private Button updateProduct;
    @FXML
    private Button delete;
    @FXML
    private Button newClient;
    @FXML
    private Button newSeller;
    @FXML
    private Button buttonReport;

    /**
     * Initializes the controller class. Sets up the table columns and loads
     * data from the database. Adds a listener to the search field for real-time
     * filtering.
     *
     * @param url The location used to resolve relative paths for the root
     * object.
     * @param rb The resources used to localize the root object.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        id.setCellValueFactory(new PropertyValueFactory<>("productoId"));
        Descripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        estado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        precio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        stock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        descuento.setCellValueFactory(new PropertyValueFactory<>("descuentoId")); // Mantener ID de descuento

        // Cargar los datos desde la base de datos
        productosList = FXCollections.observableArrayList();
        cargarDatosDesdeDB();
        tableInventory.setItems(productosList);

        // Configurar la columna de precio con formato
        precio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        precio.setCellFactory(column -> new TableCell<Producto, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        // Configurar la columna de descuento para mostrar el porcentaje
        descuento.setCellFactory(column -> new TableCell<Producto, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Obtener el porcentaje de descuento de la lista
                    Producto producto = getTableView().getItems().get(getIndex());
                    if (producto.getDescuentoId() > 0) {
                        // Lógica para obtener el porcentaje según el ID (esto puede requerir otro método o lógica)
                        setText(item + "%");  // Mostrar el porcentaje de descuento
                    } else {
                        setText("Sin descuento");
                    }
                }
            }
        });

        // Añadir el listener al TextField para la búsqueda en tiempo real
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarProductos(newValue);
        });
    }

    /**
     * Loads data from the database and populates the observable list.
     *
     * @throws SQLException If a database access error occurs or this method is
     * called on a closed connection.
     */
    private void cargarDatosDesdeDB() {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            // Establecer conexión a la base de datos
            conn = new DBconection().establishConnection();
            String query = "SELECT p.*, d.porcentaje AS descuentoPorcentaje FROM Productos p "
                    + "LEFT JOIN Descuentos d ON p.descuentoId = d.descuentoId";

            // Crear una declaración y ejecutar la consulta
            st = conn.createStatement();
            rs = st.executeQuery(query);

            // Limpiar la lista observable antes de agregar nuevos datos
            productosList.clear();

            // Procesar los resultados
            while (rs.next()) {
                int productoId = rs.getInt("productoId");
                String descripcion = rs.getString("descripcion");
                String estado = rs.getString("estado");
                double precio = rs.getDouble("precio");
                int stock = rs.getInt("stock");
                int descuentoId = rs.getInt("descuentoId");
                int descuentoPorcentaje = rs.getInt("descuentoPorcentaje"); // Obtener el porcentaje de descuento

                // Validar los datos recuperados
                if (descripcion != null && !descripcion.trim().isEmpty()
                        && estado != null && !estado.trim().isEmpty()
                        && precio >= 0 && stock >= 0) {

                    // Agregar a la lista observable, pero usaremos solo descuentoId
                    productosList.add(new Producto(productoId, descripcion, estado, precio, stock, descuentoId));
                } else {
                    System.err.println("Error: Datos inválidos para el producto ID: " + productoId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al recuperar datos desde la base de datos: " + e.getMessage());
            e.printStackTrace();
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
                System.err.println("Error al cerrar los recursos de la base de datos: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Filters the products based on the search text and updates the table.
     *
     * @param searchText The text used to filter the products.
     */
    private void filtrarProductos(String searchText) {
        if (searchText == null) {
            System.err.println("Error: El texto de búsqueda no puede ser nulo.");
            return;
        }

        if (searchText.isEmpty()) {
            System.err.println("Advertencia: El texto de búsqueda está vacío.");
            // Si el texto de búsqueda está vacío, podríamos mostrar todos los productos
            tableInventory.setItems(productosList);
            return;
        }

        try {
            // Filtrar la lista de productos
            ObservableList<Producto> filteredList = productosList.filtered(producto
                    -> String.valueOf(producto.getProductoId()).contains(searchText)
                    || // Filtrar por productoId
                    producto.getEstado().toLowerCase().contains(searchText.toLowerCase())
                    || // Filtrar por estado
                    producto.getDescripcion().toLowerCase().contains(searchText.toLowerCase()) // Filtrar por descripcion
            );
            // Establecer la lista filtrada en la tabla
            tableInventory.setItems(filteredList);

            // Mostrar un alert si no se encuentran productos
            if (filteredList.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Sin Resultados", "No se encontraron productos que coincidan con la búsqueda.");
                searchField.clear();
            }
        } catch (Exception e) {
            System.err.println("Error al filtrar productos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens the window to add a new product.
     *
     * @param event The event that triggered this method.
     */
    @FXML
    private void newProduct(ActionEvent event) {
        loadNextWindow1();
    }

    /**
     * Opens the window for billing methods.
     *
     * @param event The event that triggered this method.
     */
    @FXML
    private void billMethod(ActionEvent event) {
        loadNextWindow2();
    }

    /**
     * Loads the view for adding a new product.
     */
    private void loadNextWindow1() {
        try {
            // Cargar la nueva vista
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Newproduct.fxml"));
            Parent root = fxmlLoader.load();

            // Obtener la ventana actual
            Stage stage = (Stage) tableInventory.getScene().getWindow();
            stage.close();  // Cerrar la ventana de login

            // Crear una nueva ventana para la próxima vista
            Stage mainStage = new Stage();
            mainStage.setTitle("Nuevo producto");
            mainStage.setScene(new Scene(root));
            mainStage.show();

        } catch (IOException e) {
        }
    }

    /**
     * Loads the view for billing.
     */
    private void loadNextWindow2() {
        try {
            // Cargar la nueva vista
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Bill.fxml"));
            Parent root = fxmlLoader.load();

            // Obtener la ventana actual
            Stage stage = (Stage) tableInventory.getScene().getWindow();
            stage.close();  // Cerrar la ventana de inventario

            // Crear una nueva ventana para la próxima vista
            Stage mainStage = new Stage();
            mainStage.setTitle("Facturas");
            mainStage.setScene(new Scene(root));
            mainStage.show();

        } catch (IOException e) {
        }
    }

    /**
     * Loads the view for updating a product.
     */
    private void loadNextWindow3() {
        try {
            // Cargar la nueva vista
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("UpdateProduct.fxml"));
            Parent root = fxmlLoader.load();

            // Obtener la ventana actual
            Stage stage = (Stage) tableInventory.getScene().getWindow();
            stage.close();  // Cerrar la ventana de login

            // Crear una nueva ventana para la próxima vista
            Stage mainStage = new Stage();
            mainStage.setTitle("Facturas");
            mainStage.setScene(new Scene(root));
            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the view for add a new client.
     */
    private void loadNextWindow4() {
        try {
            // Cargar la nueva vista
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NewClient.fxml"));
            Parent root = fxmlLoader.load();

            // Obtener la ventana actual
            Stage stage = (Stage) tableInventory.getScene().getWindow();
            stage.close();  // Cerrar la ventana de login

            // Crear una nueva ventana para la próxima vista
            Stage mainStage = new Stage();
            mainStage.setTitle("Facturas");
            mainStage.setScene(new Scene(root));
            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the view for add a new seller.
     */
    private void loadNextWindow5() {
        try {
            // Cargar la nueva vista
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NewSeller.fxml"));
            Parent root = fxmlLoader.load();

            // Obtener la ventana actual
            Stage stage = (Stage) tableInventory.getScene().getWindow();
            stage.close();  // Cerrar la ventana de login

            // Crear una nueva ventana para la próxima vista
            Stage mainStage = new Stage();
            mainStage.setTitle("Facturas");
            mainStage.setScene(new Scene(root));
            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the window to update an existing product.
     *
     * @param event The event that triggered this method.
     */
    @FXML
    private void updateProduct(ActionEvent event) {
        loadNextWindow3();
    }

    /**
     * Deletes the selected product from the inventory.
     *
     * @param event The event that triggered this method.
     */
    @FXML
    private void deleteProduct(ActionEvent event) {

        // Obtener el producto seleccionado
        Producto productoSeleccionado = tableInventory.getSelectionModel().getSelectedItem();

        if (productoSeleccionado != null) {
            // Confirmar eliminación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmación de eliminación");
            alert.setHeaderText("¿Está seguro de que desea eliminar este producto?");
            alert.setContentText("Producto: " + productoSeleccionado.getDescripcion());

            if (alert.showAndWait().get() == ButtonType.OK) {
                Connection conn = null;
                try {
                    // Establecer conexión a la base de datos
                    conn = new DBconection().establishConnection();
                    String query = "DELETE FROM Productos WHERE productoId = ?";

                    // Eliminar de la base de datos
                    PreparedStatement pst = conn.prepareStatement(query);
                    pst.setInt(1, productoSeleccionado.getProductoId());
                    int rowsAffected = pst.executeUpdate();

                    if (rowsAffected > 0) {
                        // Eliminar de la lista observable (que actualiza la tabla)
                        productosList.remove(productoSeleccionado);

                        // Mensaje de éxito
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Eliminación exitosa");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("Producto eliminado exitosamente.");
                        successAlert.showAndWait();
                    } else {
                        // Mensaje si no se eliminó ningún registro
                        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                        infoAlert.setTitle("Sin cambios");
                        infoAlert.setHeaderText(null);
                        infoAlert.setContentText("No se encontró el producto para eliminar.");
                        infoAlert.showAndWait();
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    // Mensaje de error
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("No se pudo eliminar el producto.");
                    errorAlert.showAndWait();
                } finally {
                    // Cerrar la conexión en el bloque finally para asegurar que se cierra siempre
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Mensaje de error al cerrar la conexión
                            Alert closeErrorAlert = new Alert(Alert.AlertType.ERROR);
                            closeErrorAlert.setTitle("Error de Cierre de Conexión");
                            closeErrorAlert.setHeaderText(null);
                            closeErrorAlert.setContentText("Ocurrió un error al cerrar la conexión a la base de datos.");
                            closeErrorAlert.showAndWait();
                        }
                    }
                }
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
     * Opens the window to add a new client.
     *
     * @param event The event that triggered this method.
     */
    @FXML
    private void newClient(ActionEvent event) {
        loadNextWindow4();
    }

    /**
     * Opens the window to add a new seller.
     *
     * @param event The event that triggered this method.
     */
    @FXML
    private void newSeller(ActionEvent event) {
        loadNextWindow5();
    }

    /**
     * Generates a PDF report for the current inventory. This method retrieves
     * inventory data from the database, formats it into a PDF file and attempts
     * to open the file in the system's default PDF viewer.
     *
     * @param event the event triggered by the button click to generate the
     * report.
     */
    @FXML
    private void generateReport(ActionEvent event) {
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

            // Add title and other information
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(margin, pageHeight - logoHeight - 100);
            contentStream.showText("Reporte de Inventario");
            contentStream.endText();

            // Define column widths and positions
            int productIdWidth = 80;
            int descriptionWidth = 200;
            int priceWidth = 60;
            int stockWidth = 60;
            int statusWidth = 80;
            int lineHeight = 12;

            int xProductId = 25;
            int xDescription = xProductId + productIdWidth;
            int xPrice = xDescription + descriptionWidth;
            int xStock = xPrice + priceWidth;
            int xStatus = xStock + stockWidth;

            int headerHeight = 20; // Height for header row
            int yPosition = (int) (pageHeight - logoHeight - 140 - headerHeight); // Initial y-position for data

            // Add table headers and draw lines
            contentStream.setLineWidth(1f);
            contentStream.setStrokingColor(Color.BLACK);

            // Draw headers
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            contentStream.newLineAtOffset(xProductId, yPosition + headerHeight);
            contentStream.showText("Product ID");
            contentStream.newLineAtOffset(xDescription - xProductId, 0);
            contentStream.showText("Description");
            contentStream.newLineAtOffset(xPrice - xDescription, 0);
            contentStream.showText("Price");
            contentStream.newLineAtOffset(xStock - xPrice, 0);
            contentStream.showText("Stock");
            contentStream.newLineAtOffset(xStatus - xStock, 0);
            contentStream.showText("Status");
            contentStream.endText();

            // Draw horizontal line under the headers
            contentStream.moveTo(xProductId, yPosition + headerHeight - 2);
            contentStream.lineTo(xStatus + statusWidth, yPosition + headerHeight - 2);
            contentStream.stroke();

            // Draw vertical lines
            contentStream.moveTo(xProductId, yPosition + headerHeight);
            contentStream.lineTo(xProductId, yPosition - (lineHeight * 40));
            contentStream.stroke();

            contentStream.moveTo(xDescription, yPosition + headerHeight);
            contentStream.lineTo(xDescription, yPosition - (lineHeight * 40));
            contentStream.stroke();

            contentStream.moveTo(xPrice, yPosition + headerHeight);
            contentStream.lineTo(xPrice, yPosition - (lineHeight * 40));
            contentStream.stroke();

            contentStream.moveTo(xStock, yPosition + headerHeight);
            contentStream.lineTo(xStock, yPosition - (lineHeight * 40));
            contentStream.stroke();

            contentStream.moveTo(xStatus, yPosition + headerHeight);
            contentStream.lineTo(xStatus, yPosition - (lineHeight * 40));
            contentStream.stroke();

            // Draw bottom border of the table
            contentStream.moveTo(xProductId, yPosition - (lineHeight * 40) - 2);
            contentStream.lineTo(xStatus + statusWidth, yPosition - (lineHeight * 40) - 2);
            contentStream.stroke();

            // Draw right border of the table
            contentStream.moveTo(xStatus + statusWidth, yPosition + headerHeight);
            contentStream.lineTo(xStatus + statusWidth, yPosition - (lineHeight * 40));
            contentStream.stroke();

            // Retrieve data from the database
            String query = "SELECT productoId, descripcion, precio, stock, estado FROM Productos";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Write table rows
            while (rs.next()) {
                // Create the line of text
                String line = String.format("%-12d | %-20s | %-6.2f | %-6d | %s",
                        rs.getInt("productoId"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        rs.getInt("stock"),
                        rs.getString("estado"));

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
                    contentStream.newLineAtOffset(margin, newPage.getMediaBox().getHeight() - margin - 20);
                    contentStream.showText("Reporte de Inventario");
                    contentStream.endText();

                    // Add table headers and draw lines
                    contentStream.setLineWidth(1f);
                    contentStream.setStrokingColor(Color.BLACK);

                    // Draw headers
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    contentStream.newLineAtOffset(xProductId, newPage.getMediaBox().getHeight() - margin - headerHeight);
                    contentStream.showText("Product ID");
                    contentStream.newLineAtOffset(xDescription - xProductId, 0);
                    contentStream.showText("Description");
                    contentStream.newLineAtOffset(xPrice - xDescription, 0);
                    contentStream.showText("Price");
                    contentStream.newLineAtOffset(xStock - xPrice, 0);
                    contentStream.showText("Stock");
                    contentStream.newLineAtOffset(xStatus - xStock, 0);
                    contentStream.showText("Status");
                    contentStream.endText();

                    // Draw horizontal line under the headers
                    contentStream.moveTo(xProductId, newPage.getMediaBox().getHeight() - margin - headerHeight - 2);
                    contentStream.lineTo(xStatus + statusWidth, newPage.getMediaBox().getHeight() - margin - headerHeight - 2);
                    contentStream.stroke();

                    // Draw vertical lines
                    contentStream.moveTo(xProductId, newPage.getMediaBox().getHeight() - margin - headerHeight);
                    contentStream.lineTo(xProductId, newPage.getMediaBox().getHeight() - margin - (lineHeight * 40));
                    contentStream.stroke();

                    contentStream.moveTo(xDescription, newPage.getMediaBox().getHeight() - margin - headerHeight);
                    contentStream.lineTo(xDescription, newPage.getMediaBox().getHeight() - margin - (lineHeight * 40));
                    contentStream.stroke();

                    contentStream.moveTo(xPrice, newPage.getMediaBox().getHeight() - margin - headerHeight);
                    contentStream.lineTo(xPrice, newPage.getMediaBox().getHeight() - margin - (lineHeight * 40));
                    contentStream.stroke();

                    contentStream.moveTo(xStock, newPage.getMediaBox().getHeight() - margin - headerHeight);
                    contentStream.lineTo(xStock, newPage.getMediaBox().getHeight() - margin - (lineHeight * 40));
                    contentStream.stroke();

                    contentStream.moveTo(xStatus, newPage.getMediaBox().getHeight() - margin - headerHeight);
                    contentStream.lineTo(xStatus, newPage.getMediaBox().getHeight() - margin - (lineHeight * 40));
                    contentStream.stroke();

                    // Draw bottom border of the table
                    contentStream.moveTo(xProductId, newPage.getMediaBox().getHeight() - margin - (lineHeight * 40) - 2);
                    contentStream.lineTo(xStatus + statusWidth, newPage.getMediaBox().getHeight() - margin - (lineHeight * 40) - 2);
                    contentStream.stroke();

                    // Draw right border of the table
                    contentStream.moveTo(xStatus + statusWidth, newPage.getMediaBox().getHeight() - margin - headerHeight);
                    contentStream.lineTo(xStatus + statusWidth, newPage.getMediaBox().getHeight() - margin - (lineHeight * 40));
                    contentStream.stroke();

                    yPosition = (int) (newPage.getMediaBox().getHeight() - margin - headerHeight - lineHeight);
                }

                // Draw the row content
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(xProductId, yPosition);
                contentStream.showText(String.format("%-12d", rs.getInt("productoId")));
                contentStream.newLineAtOffset(xDescription - xProductId, 0);
                contentStream.showText(String.format("%-20s", rs.getString("descripcion")));
                contentStream.newLineAtOffset(xPrice - xDescription, 0);
                contentStream.showText(String.format("%.2f", rs.getDouble("precio")));
                contentStream.newLineAtOffset(xStock - xPrice, 0);
                contentStream.showText(String.format("%d", rs.getInt("stock")));
                contentStream.newLineAtOffset(xStatus - xStock, 0);
                contentStream.showText(rs.getString("estado"));
                contentStream.endText();

                yPosition -= lineHeight;
            }

            // Close the content stream
            contentStream.close();

            // Save the document
            String outputPath = "reporte_inventario.pdf";
            document.save(outputPath);

            // Open the PDF file with the default viewer
            Desktop.getDesktop().open(new File(outputPath));

            System.out.println("Reporte generado exitosamente!");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Se ha producido un error al generar el reporte.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } finally {
            // Clean up resources
            try {
                if (contentStream != null) {
                    contentStream.close();
                }
                if (document != null) {
                    document.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Displays an alert dialog with the specified title and message.
     *
     * @param title The title of the alert dialog.
     * @param message The message to be displayed in the alert dialog.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays an alert dialog with the specified title and message.
     *
     * @param title The title of the alert dialog.
     * @param message The message to be displayed in the alert dialog.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
