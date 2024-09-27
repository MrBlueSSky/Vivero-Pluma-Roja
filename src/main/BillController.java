package main;

import Clases.DBconection;
import Clases.Factura;
import Clases.Vendedor;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

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
    private TableColumn<Factura, String> currency; // Displays the name of the currency
    @FXML
    private TableView<Factura> tableBills;
    @FXML
    private Button delete;
    @FXML
    private Button createBill;
    @FXML
    private TextField search;

    private ObservableList<Factura> facturaList = FXCollections.observableArrayList();
    @FXML
    private Button createBill1;
    @FXML
    private TableColumn<Factura, String> customerName;

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
            // Updated query to include the customer's name
            String query = "SELECT f.*, m.nombre AS monedaNombre, c.nombre AS nombreCliente "
                    + "FROM Facturas f "
                    + "JOIN Moneda m ON f.monedaId = m.monedaId "
                    + "JOIN Ventas v ON f.ventasId = v.ventasId "
                    + "JOIN Clientes c ON v.clienteId = c.clienteId";
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
                        rs.getString("nombreCliente")
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GenerateBill.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Generar Factura");
            stage.show();

            // Close the current window
            Stage currentStage = (Stage) createBill.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
