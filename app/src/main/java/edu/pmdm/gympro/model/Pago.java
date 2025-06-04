package edu.pmdm.gympro.model;

public class Pago {

    private String idPago;
    private String idCliente;
    private int mes;
    private int año;
    private boolean pagado;
    private String idAdministrador;

    public Pago() {
    }

    public Pago(String idPago, String idCliente, int mes, int año, boolean pagado, String idAdministrador) {
        this.idPago = idPago;
        this.idCliente = idCliente;
        this.mes = mes;
        this.año = año;
        this.pagado = pagado;
        this.idAdministrador = idAdministrador;
    }

    public String getIdPago() {
        return idPago;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public int getMes() {
        return mes;
    }

    public int getAño() {
        return año;
    }

    public boolean isPagado() {
        return pagado;
    }

    public String getIdAdministrador() {
        return idAdministrador;
    }
}
