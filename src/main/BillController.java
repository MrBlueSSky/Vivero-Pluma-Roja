package main;

import Clases.DBconection;
import Clases.Factura;
import Clases.Producto;
import Clases.Vendedor;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * Controller for the bills window. Manages the user interface logic for bill
 * management.
 */
public class BillController implements Initializable {

    @FXML
    private Button inventory;
    @FXML
    private TableColumn<Factura, Integer> idBill;
    @FXML
    private TableColumn<Factura, Integer> idSales;
    @FXML
    private TableColumn<Factura, Date> dateSales;
    @FXML
    private TableColumn<Factura, String> state;
    @FXML
    private TableColumn<Factura, String> currency;
    @FXML
    private TableView<Factura> tableBills;
    @FXML
    private Button delete;
    private Button createBill;
    @FXML
    private TextField search;

    private ObservableList<Factura> facturaList = FXCollections.observableArrayList();
    @FXML
    private Button createBill1;
    @FXML
    private TableColumn<Factura, String> customerName;
    @FXML
    private Button generateSaleReportButton;
    @FXML
    private TableColumn<Factura, Integer> detalleVentasIdColumn;
    @FXML
    private Button updateState;

    /**
     * Initializes the controller. Loads bills and sets up table columns.
     *
     * @param url The location used to resolve relative paths for the root
     * object, or null if the location is not known.
     * @param rb The resources used to localize the root object, or null if the
     * resources are not known.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadBills();
        setTableColumns();

        // Add listener to TextField for real-time search
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            searchBill();
        });
    }

    /**
     * Sets up the table columns for the bills.
     */
    private void setTableColumns() {
        idBill.setCellValueFactory(new PropertyValueFactory<>("facturaId"));
        idSales.setCellValueFactory(new PropertyValueFactory<>("ventasId"));
        dateSales.setCellValueFactory(new PropertyValueFactory<>("fechaFactura"));
        state.setCellValueFactory(new PropertyValueFactory<>("estado"));
        currency.setCellValueFactory(new PropertyValueFactory<>("moneda"));
        customerName.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        detalleVentasIdColumn.setCellValueFactory(new PropertyValueFactory<>("detalleVentasId"));
    }

    /**
     * Loads the bills from the database and adds them to the observable list.
     */
    private void loadBills() {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = new DBconection().establishConnection();
            String query = "SELECT f.*, m.nombre AS monedaNombre, c.nombre AS nombreCliente, dv.detalleVentasId "
                    + "FROM Facturas f "
                    + "JOIN Moneda m ON f.monedaId = m.monedaId "
                    + "JOIN Ventas v ON f.ventasId = v.ventasId "
                    + "JOIN Clientes c ON v.clienteId = c.clienteId "
                    + "JOIN DetalleVentas dv ON v.ventasId = dv.ventasId"; // Agregar JOIN

            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            while (rs.next()) {
                Factura factura = new Factura(
                        rs.getInt("facturaId"),
                        rs.getInt("ventasId"),
                        rs.getDate("fechaFactura"),
                        rs.getString("estado"),
                        rs.getInt("monedaId"),
                        rs.getString("monedaNombre"),
                        rs.getString("nombreCliente"),
                        rs.getInt("detalleVentasId") // Asignar detalleVentasId
                );
                facturaList.add(factura);
            }

            tableBills.setItems(facturaList);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Filters the bills in the table based on the text entered in the search
     * field.
     */
    private void searchBill() {
        String query = search.getText().toLowerCase();
        ObservableList<Factura> filteredList = FXCollections.observableArrayList();

        for (Factura factura : facturaList) {
            boolean matches = false;

            // Filter by facturaId
            if (String.valueOf(factura.getFacturaId()).toLowerCase().contains(query)) {
                matches = true;
            } // Filter by ventasId
            else if (String.valueOf(factura.getVentasId()).toLowerCase().contains(query)) {
                matches = true;
            } // Filter by fechaFactura
            else if (factura.getFechaFactura().toString().toLowerCase().contains(query)) {
                matches = true;
            } // Filter by estado
            else if (factura.getEstado().toLowerCase().contains(query)) {
                matches = true;
            } // Filter by moneda
            else if (factura.getMoneda().toLowerCase().contains(query)) {
                matches = true;
            } // Filter by nombreCliente (customer name)
            else if (factura.getNombreCliente().toLowerCase().contains(query)) {
                matches = true;
            }

            if (matches) {
                filteredList.add(factura);
            }
        }

        if (filteredList.isEmpty()) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Búsqueda sin resultados");
            alert.setHeaderText(null);
            alert.setContentText("No se encontró ninguna factura con los criterios de búsqueda.");
            alert.showAndWait();
        }

        tableBills.setItems(filteredList);
    }

    /**
     * Deletes the selected bill after confirming the action.
     *
     * @param event The action event that triggers the deletion.
     */
    @FXML
    private void deleteBill(ActionEvent event) {
        Factura selectedFactura = tableBills.getSelectionModel().getSelectedItem();

        if (selectedFactura == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Eliminar Factura");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, seleccione una factura para eliminar.");
            alert.showAndWait();
            return;
        }

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmación");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("¿Estás seguro de que deseas eliminar la factura con ID: " + selectedFactura.getFacturaId() + "?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            Connection conn = null;
            PreparedStatement pst = null;

            try {
                conn = new DBconection().establishConnection();
                String deleteQuery = "DELETE FROM Facturas WHERE facturaId = ?";
                pst = conn.prepareStatement(deleteQuery);
                pst.setInt(1, selectedFactura.getFacturaId());
                pst.executeUpdate();

                // Remove from the list
                facturaList.remove(selectedFactura);
                tableBills.refresh();

                Alert successAlert = new Alert(AlertType.INFORMATION);
                successAlert.setTitle("Factura eliminada");
                successAlert.setHeaderText(null);
                successAlert.setContentText("La factura ha sido eliminada exitosamente.");
                successAlert.showAndWait();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (pst != null) {
                        pst.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns to the inventory window, closing the current window.
     *
     * @param event The action event that triggers the return.
     */
    @FXML
    private void returnInventory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Inventory.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Inventario");
            stage.show();

            // Close the current window
            Stage currentStage = (Stage) inventory.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a new bill by loading the corresponding window.
     *
     * @param event The action event that triggers the bill generation.
     */
    @FXML
    private void generateBill(ActionEvent event) {
        try {
            // Cargar la nueva vista
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("GenerateBill.fxml"));
            Parent root = fxmlLoader.load();

            // Obtener la ventana actual
            Stage stage = (Stage) createBill1.getScene().getWindow();
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
     * Handles the event when the "Generate Report" button is clicked. This
     * method connects to the database, retrieves the selected invoice, and
     * generates a PDF report containing details of the sale, including the
     * seller's name, client information, and a table of products sold.
     *
     * @param event the ActionEvent triggered by the button click
     * @throws IOException if an I/O error occurs during PDF generation or file
     * saving
     */
    @FXML
private void onGenerateReportButtonClick(ActionEvent event) throws IOException {
    Connection conn = null;
    PDDocument document = null;
    PDPageContentStream contentStream = null;

    try {
        // Establecer conexión a la base de datos
        conn = new DBconection().establishConnection();

        // Obtener la factura seleccionada en la tabla
        Factura selectedFactura = tableBills.getSelectionModel().getSelectedItem();
        if (selectedFactura == null) {
            // Si no hay una factura seleccionada, mostrar un mensaje
            Alert alert = new Alert(Alert.AlertType.WARNING, "Por favor, selecciona una factura.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // Consultar el nombre del vendedor usando el ventasId de la factura seleccionada
        String vendedorNombre = "";
        String vendedorQuery = "SELECT v.nombre FROM Ventas vt JOIN Vendedor v ON vt.vendedorId = v.vendedorId WHERE vt.ventasId = ?";
        try (PreparedStatement pstmtVendedor = conn.prepareStatement(vendedorQuery)) {
            pstmtVendedor.setInt(1, selectedFactura.getVentasId());
            ResultSet rsVendedor = pstmtVendedor.executeQuery();
            if (rsVendedor.next()) {
                vendedorNombre = rsVendedor.getString("nombre");
            }
        }

        // Crear el documento PDF
        document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        // Crear el flujo de contenido para escribir en el PDF
        contentStream = new PDPageContentStream(document, page);

        // Añadir el logo
        File logoFile = new File("src\\Image\\logo.jpeg");
        if (!logoFile.exists()) {
            throw new FileNotFoundException("Logo file not found at: " + logoFile.getPath());
        }
        PDImageXObject logo = PDImageXObject.createFromFile(logoFile.getPath(), document);
        float logoWidth = 200;
        float logoHeight = 150;
        contentStream.drawImage(logo, 25, page.getMediaBox().getHeight() - logoHeight - 25, logoWidth, logoHeight);

        // Añadir el título "Detalle de venta"
        String title = "Detalle de venta";
        float titleFontSize = 16f;
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, titleFontSize);

        // Calcular el ancho del texto para centrarlo
        float titleWidth = title.length() * (titleFontSize * 0.6f); // Aproximación de ancho
        float titleX = (page.getMediaBox().getWidth() - titleWidth) / 2; // Centrar en X

        float titleY = page.getMediaBox().getHeight() - logoHeight - 25 - 30; // Espacio debajo del logo
        contentStream.beginText();
        contentStream.newLineAtOffset(titleX, titleY);
        contentStream.showText(title);
        contentStream.endText();

        // Restante código para agregar información y detalles...
        // Añadir fecha y hora en la esquina superior derecha
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        float margin = 25;
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        contentStream.newLineAtOffset(pageWidth - margin - 200, pageHeight - margin);
        contentStream.showText("Fecha de Generación: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        contentStream.endText();

        // Añadir nombre del vivero debajo de la fecha
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(pageWidth - margin - 200, pageHeight - margin - 20);
        contentStream.showText("Vivero Pluma Roja");
        contentStream.endText();

        // Añadir teléfono y dirección
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

        // Espacio vertical entre dirección y datos de la factura
        float textSpacing = 15;
        float currentY = page.getMediaBox().getHeight() - 350; // Ajustar para evitar que se superponga con el logo y la dirección

        // Añadir información de la factura
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(25, currentY); // Empezar después de la dirección
        contentStream.showText("Vendedor: " + vendedorNombre); // Cambiar Factura ID por Vendedor
        contentStream.endText();

        // Espacio para el nombre del cliente
        currentY -= textSpacing;
        contentStream.beginText();
        contentStream.newLineAtOffset(25, currentY);
        contentStream.showText("Cliente: " + selectedFactura.getNombreCliente());
        contentStream.endText();

        // Espacio para la fecha
        currentY -= textSpacing;
        contentStream.beginText();
        contentStream.newLineAtOffset(25, currentY);
        contentStream.showText("Fecha: " + selectedFactura.getFechaFactura());
        contentStream.endText();

        // Consultar los detalles de la factura
        String query = "SELECT dv.detalleVentasId, p.descripcion, dv.cantidad, dv.precioUnitario "
                + "FROM DetalleVentas dv "
                + "JOIN Productos p ON dv.productoId = p.productoId "
                + "WHERE dv.ventasId = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, selectedFactura.getVentasId());
        ResultSet rs = pstmt.executeQuery();

        // Añadir un encabezado de tabla
        currentY -= (textSpacing * 2); // Espacio antes de la tabla
        float tableStartX = 25;
        float col1Width = 200;
        float col2Width = 100;
        float col3Width = 100;
        float col4Width = 100;

        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(tableStartX, currentY);
        contentStream.showText("Descripción Producto");
        contentStream.newLineAtOffset(col1Width, 0);
        contentStream.showText("Cantidad");
        contentStream.newLineAtOffset(col2Width, 0);
        contentStream.showText("Precio Unitario");
        contentStream.newLineAtOffset(col3Width, 0);
        contentStream.showText("Total");
        contentStream.endText();

        // Línea debajo del encabezado
        currentY -= textSpacing;
        contentStream.moveTo(tableStartX, currentY);
        contentStream.lineTo(pageWidth - tableStartX, currentY);
        contentStream.stroke();

        // Ajuste para las filas (desplazamiento debajo del encabezado)
        currentY -= textSpacing;

        // Añadir filas de los detalles de la venta
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        while (rs.next()) {
            String descripcionProducto = rs.getString("descripcion");
            int cantidad = rs.getInt("cantidad");
            double precioUnitario = rs.getDouble("precioUnitario");
            double total = cantidad * precioUnitario;

            // Descripción del producto
            contentStream.beginText();
            contentStream.newLineAtOffset(tableStartX, currentY);
            contentStream.showText(descripcionProducto);
            contentStream.endText();

            // Cantidad
            contentStream.beginText();
            contentStream.newLineAtOffset(tableStartX + col1Width, currentY);
            contentStream.showText(String.valueOf(cantidad));
            contentStream.endText();

            // Precio unitario
            contentStream.beginText();
            contentStream.newLineAtOffset(tableStartX + col1Width + col2Width, currentY);
            contentStream.showText(String.format("%.2f", precioUnitario));
            contentStream.endText();

            // Total
            contentStream.beginText();
            contentStream.newLineAtOffset(tableStartX + col1Width + col2Width + col3Width, currentY);
            contentStream.showText(String.format("%.2f", total));
            contentStream.endText();

            // Avanzar a la siguiente fila
            currentY -= textSpacing;

            // Si llegamos al final de la página, agregar una nueva página
            if (currentY < margin) {
                contentStream.close();
                page = new PDPage();
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                currentY = page.getMediaBox().getHeight() - margin; // Reiniciar el cursor vertical
            }
        }

        // Cerrar el flujo de contenido y guardar el documento
        contentStream.close();
        String filePath = "factura_" + selectedFactura.getFacturaId() + ".pdf";
        document.save(filePath);

        // Mostrar mensaje de éxito y abrir el PDF
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "El PDF ha sido generado correctamente.", ButtonType.OK);
        successAlert.showAndWait();
        
        File pdfFile = new File(filePath);
        if (pdfFile.exists()) {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "El sistema no soporta la apertura automática de PDFs.", ButtonType.OK);
                errorAlert.showAndWait();
            }
        }
    } catch (SQLException | IOException e) {
        e.printStackTrace();
        Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Error al generar el PDF: " + e.getMessage(), ButtonType.OK);
        errorAlert.showAndWait();
    } finally {
        // Cerrar recursos
        if (contentStream != null) {
            try {
                contentStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (document != null) {
            try {
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}


    /**
     * Handles the action of updating the state of the invoice. This method
     * loads the UpdateState.fxml file to display the update interface for the
     * invoice state. It also closes the current window after opening the new
     * window.
     *
     * @param event the action event triggered by the user (e.g., button click)
     */
    @FXML
    private void updateState(ActionEvent event) {
        try {
            // Load the new window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UpdateState.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Actualizar estado de factura");
            stage.show();

            // Close the current window
            Stage currentStage = (Stage) updateState.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}