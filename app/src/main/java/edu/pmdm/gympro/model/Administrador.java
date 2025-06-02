package edu.pmdm.gympro.model;

public class Administrador {
    private String idadministrador;
    private String nombreAdministrador;
    private String apellidoAdministrador;
    private String fechaNacimiento;
    private String correo;
    private String dni;
    private String telefono;
    private String foto;
    private String rol;

    // Constructor vacío requerido por Firestore
    public Administrador() {}

    // Constructor actualizado con todos los campos
    public Administrador(String idadministrador, String nombre, String apellidos, String fecha, String correo, String dni, String telefono, String foto){
        this.idadministrador = idadministrador;
        this.nombreAdministrador = nombre;
        this.apellidoAdministrador = apellidos;
        this.fechaNacimiento = fecha;
        this.correo = correo;
        this.dni = dni;
        this.telefono = telefono;
        this.foto = foto;
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

    public String getCorreo() {
        return correo;
    }

    public String getDni() {
        return dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getFoto() {
        return foto;
    }

    public String getRol() {
        return rol;
    }
}
