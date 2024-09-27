
package Clases;

/**
 * Represents a seller (Vendedor) in the system.
 *
 * @author Fabricio CUM
 */
public class Vendedor {

    private int vendedorId;
    private String nombre;
    private String cedula;
    
 /**
     * Constructs a Vendedor object with the specified details.
     *
     * @param vendedorId The ID of the seller.
     * @param nombre The name of the seller.
     * @param cedula The ID number of the seller.
     */
    public Vendedor(int vendedorId, String nombre, String cedula) {
        this.vendedorId = vendedorId;
        this.nombre = nombre;
        this.cedula = cedula;
    }

    public int getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(int vendedorId) {
        this.vendedorId = vendedorId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

}
