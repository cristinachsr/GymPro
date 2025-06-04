package edu.pmdm.gympro.model;

import java.util.List;

public class Grupo {

    private String idgrupo;
    private String nombre;
    private String descripcion;
    private String foto;
    private String idMonitor;
    private String idAdministrador;
    private List<Horario> horarios;

    public Grupo() {
    }

    public Grupo(String idgrupo, String nombre, String descripcion, String foto,
                 String idMonitor, String idAdministrador, List<Horario> horarios) {
        this.idgrupo = idgrupo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.foto = foto;
        this.idMonitor = idMonitor;
        this.idAdministrador = idAdministrador;
        this.horarios = horarios;
    }

    public String getIdgrupo() { return idgrupo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getFoto() { return foto; }
    public String getIdMonitor() { return idMonitor; }
    public String getIdAdministrador() { return idAdministrador; }
    public List<Horario> getHorarios() { return horarios; }

    public void setIdgrupo(String idgrupo) { this.idgrupo = idgrupo; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFoto(String foto) { this.foto = foto; }
    public void setIdMonitor(String idMonitor) { this.idMonitor = idMonitor; }
    public void setIdAdministrador(String idAdministrador) { this.idAdministrador = idAdministrador; }
    public void setHorarios(List<Horario> horarios) { this.horarios = horarios; }
}
