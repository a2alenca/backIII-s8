package cl.banco.xyz.batch.domain;

public class Transaccion {
    private Long id;
    private String fecha;
    private Double monto;
    private String tipo;
    private String estadoAlerta;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEstadoAlerta() { return estadoAlerta; }
    public void setEstadoAlerta(String estadoAlerta) { this.estadoAlerta = estadoAlerta; }

    @Override
    public String toString() {
        return "Transaccion{id=" + id + ", fecha='" + fecha + "', monto=" + monto + ", tipo='" + tipo + "', estadoAlerta='" + estadoAlerta + "'}";
    }
}