package Clases;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the connection to the MySQL database.
 * <p>
 * This class is responsible for establishing a connection to the database using
 * the JDBC driver. It handles the connection details and provides a method to
 * establish the connection.
 * </p>
 *
 * @author Fabricio CUM
 */
public class DBconection  implements AutoCloseable{

    private Connection conection;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "MRarcangel12";
    private static final String DATABASE = "Viveros";
    private static final String HOST = "127.0.0.1";
    private static final String PORT = "3306";
    private static final String JDBC_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;

    /**
     * Establishes a connection to the MySQL database.
     * <p>
     * This method loads the MySQL JDBC driver and establishes a connection to
     * the database using the provided connection details.
     * </p>
     *
     * @return A {@link Connection} object representing the connection to the
     * database. Returns null if the connection could not be established.
     */
    public Connection establishConnection() {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish the connection
            conection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException ex) {
            // Handle error when JDBC driver class is not found
            Logger.getLogger(DBconection.class.getName()).log(Level.SEVERE, "JDBC Driver not found", ex);
            showAlert(AlertType.ERROR, "Error de Conexión", "No se pudo encontrar el controlador JDBC. " + ex.getMessage());
        } catch (SQLException ex) {
            // Handle SQL exceptions (e.g., incorrect URL, invalid credentials)
            Logger.getLogger(DBconection.class.getName()).log(Level.SEVERE, "SQL Exception", ex);
            showAlert(AlertType.ERROR, "Error de Conexión", "Problemas en la conexión a la base de datos. " + ex.getMessage());
        } catch (Exception ex) {
            // Handle any other exceptions
            Logger.getLogger(DBconection.class.getName()).log(Level.SEVERE, "Unexpected Error", ex);
            showAlert(AlertType.ERROR, "Error de Conexión", "Error inesperado: " + ex.getMessage());
        }
        return conection;
    }
    
        @Override
    public void close() {
        try {
            if (conection != null && !conection.isClosed()) {
                conection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays an alert dialog with the specified parameters.
     *
     * @param alertType The type of alert to be displayed (e.g., ERROR).
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
}
