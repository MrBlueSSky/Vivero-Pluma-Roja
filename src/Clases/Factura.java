/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clases;

import java.sql.Date;

/**
 * The Factura class represents an invoice in the system. It contains details
 * such as the invoice ID, associated sales ID, invoice date, invoice status,
 * and currency information. This class provides getter and setter methods for
 * accessing and modifying these fields.
 *
 * @author Fabricio CUM
 */
public class Factura {

    private int facturaId;
    private int ventasId;
    private Date fechaFactura;
    private String estado;
    private int monedaId;
    private String moneda;
    private String nombreCliente;
    private int detalleVentasId;

    /**
     * Constructs a new Factura with the specified parameters.
     *
     * @param facturaId The ID of the invoice.
     * @param ventasId The ID of the associated sales record.
     * @param fechaFactura The date of the invoice.
     * @param estado The status of the invoice (e.g., "Paid", "Pending").
     * @param monedaId The ID of the currency used in the invoice.
     * @param moneda The name of the currency used in the invoice.
     * @param nombreCliente The name of the client.
     * @param detalleVentasId Id of the detalleVenta.
     */
    public Factura(int facturaId, int ventasId, Date fechaFactura, String estado, int monedaId, String moneda, String nombreCliente, int detalleVentasId) {
        this.facturaId = facturaId;
        this.ventasId = ventasId;
        this.fechaFactura = fechaFactura;
        this.estado = estado;
        this.monedaId = monedaId;
        this.moneda = moneda;
        this.nombreCliente = nombreCliente;
        this.detalleVentasId = detalleVentasId;
    }

    public int getDetalleVentasId() {
        return detalleVentasId;
    }

    public void setDetalleVentasId(int detalleVentasId) {
        this.detalleVentasId = detalleVentasId;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public int getFacturaId() {
        return facturaId;
    }

    public void setFacturaId(int facturaId) {
        this.facturaId = facturaId;
    }

    public int getVentasId() {
        return ventasId;
    }

    public void setVentasId(int ventasId) {
        this.ventasId = ventasId;
    }

    public Date getFechaFactura() {
        return fechaFactura;
    }

    public void setFechaFactura(Date fechaFactura) {
        this.fechaFactura = fechaFactura;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getMonedaId() {
        return monedaId;
    }

    public void setMonedaId(int monedaId) {
        this.monedaId = monedaId;
    }

    /**
     * Returns a string representation of the invoice object.
     *
     * @return A string that represents the invoice.
     */
    @Override
    public String toString() {
        return "Factura{"
                + "facturaId=" + facturaId
                + ", ventasId=" + ventasId
                + ", fechaFactura=" + fechaFactura
                + ", estado='" + estado + '\''
                + ", monedaId=" + monedaId
                + '}';
    }

}
