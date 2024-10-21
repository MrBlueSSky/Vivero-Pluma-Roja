
package Clases;


/**
 * The DetalleVenta class represents a single item in a sales transaction.
 * It contains details such as the ID of the sales detail, product description,
 * quantity, and unit price.
 *
 * @author Fabricio
 */
public class DetalleVenta {

    
    private int detalleVentasId;         // ID del detalle de la venta
    private String descripcionProducto;  // Descripción del producto vendido
    private int cantidad;                // Cantidad de productos vendidos
    private double precioUnitario;       // Precio unitario del producto

    /**
     * Constructor for creating a DetalleVenta instance with specified parameters.
     *
     * @param detalleVentasId the ID of the sales detail.
     * @param descripcionProducto the description of the product.
     * @param cantidad the quantity of the product sold.
     * @param precioUnitario the unit price of the product.
     */
    public DetalleVenta(int detalleVentasId, String descripcionProducto, int cantidad, double precioUnitario) {
        this.detalleVentasId = detalleVentasId;
        this.descripcionProducto = descripcionProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Getters and Setters

    /**
     * Gets the ID of the sales detail.
     *
     * @return the ID of the sales detail.
     */
    public int getDetalleVentasId() {
        return detalleVentasId;
    }

    /**
     * Sets the ID of the sales detail.
     *
     * @param detalleVentasId the ID to set.
     */
    public void setDetalleVentasId(int detalleVentasId) {
        this.detalleVentasId = detalleVentasId;
    }

    /**
     * Gets the description of the product.
     *
     * @return the description of the product.
     */
    public String getDescripcionProducto() {
        return descripcionProducto;
    }

    /**
     * Sets the description of the product.
     *
     * @param descripcionProducto the product description to set.
     */
    public void setDescripcionProducto(String descripcionProducto) {
        this.descripcionProducto = descripcionProducto;
    }

    /**
     * Gets the quantity of the product sold.
     *
     * @return the quantity of the product sold.
     */
    public int getCantidad() {
        return cantidad;
    }

    /**
     * Sets the quantity of the product sold.
     *
     * @param cantidad the quantity to set.
     */
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    /**
     * Gets the unit price of the product.
     *
     * @return the unit price of the product.
     */
    public double getPrecioUnitario() {
        return precioUnitario;
    }

    /**
     * Sets the unit price of the product.
     *
     * @param precioUnitario the unit price to set.
     */
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    /**
     * Returns a string representation of the sales detail.
     *
     * @return a formatted string with sales detail information.
     */
    @Override
    public String toString() {
        return "DetalleVenta{" +
                "detalleVentasId=" + detalleVentasId +
                ", descripcionProducto='" + descripcionProducto + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                '}';
    }
}

