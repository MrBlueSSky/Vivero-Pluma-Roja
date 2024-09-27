package Clases;

/**
 * Represents a product in the inventory system.
 * <p>
 * This class contains the attributes and methods related to a product,
 * including its ID, description, state, price, stock, and discount.
 * </p>
 *
 * @author Fabricio CUM
 */
public class Producto {

    private int productoId;
    private String descripcion;
    private String estado;
    private double precio;
    private int stock;
    private int descuentoId;

    /**
     * Constructor to initialize a Product object.
     *
     * @param productoId The unique identifier of the product.
     * @param descripcion A brief description of the product.
     * @param estado The current state of the product (e.g., Available, Out of
     * Stock).
     * @param precio The price of the product.
     * @param stock The amount of product in stock.
     * @param descuentoId The discount ID applied to the product.
     */
    public Producto(int productoId, String descripcion, String estado, double precio, int stock, int descuentoId) {
        this.productoId = productoId;
        this.descripcion = descripcion;
        this.estado = estado;
        this.precio = precio;
        this.stock = stock;
        this.descuentoId = descuentoId;
    }

    /**
     * Gets the discount ID applied to the product.
     *
     * @return The discount ID.
     */
    public int getDescuentoId() {
        return descuentoId;
    }

    /**
     * Sets the discount ID applied to the product.
     *
     * @param descuentoId The discount ID to set.
     * @throws IllegalArgumentException If the discount ID is negative.
     */
    public void setDescuentoId(int descuentoId) {
        if (descuentoId < 0) {
            throw new IllegalArgumentException("Discount ID cannot be negative.");
        }
        this.descuentoId = descuentoId;
    }

    /**
     * Gets the unique identifier of the product.
     *
     * @return The product ID.
     */
    public int getProductoId() {
        return productoId;
    }

    /**
     * Sets the unique identifier of the product.
     *
     * @param productoId The product ID to set.
     * @throws IllegalArgumentException If the product ID is less than 0.
     */
    public void setProductoId(int productoId) {
        if (productoId < 0) {
            throw new IllegalArgumentException("Product ID cannot be negative.");
        }
        this.productoId = productoId;
    }

    /**
     * Gets the description of the product.
     *
     * @return The description of the product.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Sets the description of the product.
     *
     * @param descripcion The description to set.
     * @throws IllegalArgumentException If the description is null or empty.
     */
    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty.");
        }
        this.descripcion = descripcion;
    }

    /**
     * Gets the state of the product.
     *
     * @return The state of the product.
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Sets the state of the product.
     *
     * @param estado The state to set.
     * @throws IllegalArgumentException If the state is null or empty.
     */
    public void setEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("State cannot be null or empty.");
        }
        this.estado = estado;
    }

    /**
     * Gets the price of the product.
     *
     * @return The price of the product.
     */
    public double getPrecio() {
        return precio;
    }

    /**
     * Sets the price of the product.
     *
     * @param precio The price to set.
     * @throws IllegalArgumentException If the price is negative.
     */
    public void setPrecio(double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.precio = precio;
    }

    /**
     * Gets the amount of product in stock.
     *
     * @return The amount of product in stock.
     */
    public int getStock() {
        return stock;
    }

    /**
     * Sets the amount of product in stock.
     *
     * @param stock The stock amount to set.
     * @throws IllegalArgumentException If the stock is negative.
     */
    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        this.stock = stock;
    }
}
