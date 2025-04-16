package edu.pmdm.gympro.model;

public class Administrador {
    private String idadministrador;
    private String nombreAdministrador;
    private String apellidoAdministrador;
    private String fechaNacimiento;
    private String email;
    private String dni;
    private String telefono;
    private String photo;
    private String rol;

    // Constructor vacío requerido por Firestore
    public Administrador() {}

    // Constructor actualizado con todos los campos
    public Administrador(String idadministrador, String nombre, String apellidos, String fecha, String email, String dni, String telefono, String photo) {
        this.idadministrador = idadministrador;
        this.nombreAdministrador = nombre;
        this.apellidoAdministrador = apellidos;
        this.fechaNacimiento = fecha;
        this.email = email;
        this.dni = dni;
        this.telefono = telefono;
        this.photo = photo;
        this.rol = "administrador";
    }

    public String getIdadministrador() {
        return idadministrador;
    }

    public String getNombreAdministrador() {
        return nombreAdministrador;
    }

    public String getApellidoAdministrador() {
        return apellidoAdministrador;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getEmail() {
        return email;
    }

    public String getDni() {
        return dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getPhoto() {
        return photo;
    }

    public String getRol() {
        return rol;
    }
}
