package edu.pmdm.gympro.model;

import java.util.List;

public class Grupo {

    private String idgrupo;
    private String nombre;
    private String descripcion; // Texto libre escrito por el usuario
    private String photo;
    private String id_empleado;
    private String idAdministrador;
    private List<Horario> horarios; // Lista de horarios

    public Grupo() {
        // Constructor vacío necesario para Firestore
    }

    // Constructor completo
    public Grupo(String idgrupo, String nombre, String descripcion, String photo, String id_empleado, String idAdministrador, List<Horario> horarios) {
        this.idgrupo = idgrupo;
        this.nombre = nombre;
        this.descripcion = descripcion; // Texto libre, NO generado automáticamente
        this.photo = photo;
        this.id_empleado = id_empleado;
        this.idAdministrador = idAdministrador;
        this.horarios = horarios;
    }

    // Getters
    public String getIdgrupo() {
        return idgrupo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getPhoto() {
        return photo;
    }

    public String getId_empleado() {
        return id_empleado;
    }

    public String getIdAdministrador() {
        return idAdministrador;
    }

    public List<Horario> getHorarios() {
        return horarios;
    }

    // Setters (opcionales, por si necesitas editar los campos)
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setHorarios(List<Horario> horarios) {
        this.horarios = horarios;
    }
}
