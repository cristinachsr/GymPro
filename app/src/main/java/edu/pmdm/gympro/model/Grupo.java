package edu.pmdm.gympro.model;

public class Grupo {

    private String idgrupo;
    private String nombre;
    private String descripcion;
    private String photo;
    private String id_empleado;
    private String idAdministrador;

    public Grupo() {}

    public Grupo(String idgrupo, String nombre, String descripcion, String photo, String id_empleado, String idAdministrador) {
        this.idgrupo = idgrupo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.photo = photo;
        this.id_empleado = id_empleado;
        this.idAdministrador = idAdministrador;
    }

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
}
