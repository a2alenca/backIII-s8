package cl.duoc.bff.web.dto;

public class CuentaBancariaDto {
    private String nroCuenta;
    private String rutCliente;
    private double saldo;
    private String tipoCuenta;

    // Getters y Setters
    public String getNroCuenta() { return nroCuenta; }
    public void setNroCuenta(String nroCuenta) { this.nroCuenta = nroCuenta; }
    public String getRutCliente() { return rutCliente; }
    public void setRutCliente(String rutCliente) { this.rutCliente = rutCliente; }
    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }
    public String getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta; }
}