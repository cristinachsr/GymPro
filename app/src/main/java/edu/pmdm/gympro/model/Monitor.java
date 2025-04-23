package edu.pmdm.gympro.model;

public class Monitor {

    private String idMonitor;
    private String nombre;
    private String apellidos;
    private String dni;
    private String fechaNacimiento;
    private String telefono;
    private String correo;
    private String foto;
    private String idAdministrador;

    public Monitor() {
        // Constructor vacío requerido por Firestore
    }

    public Monitor(String idMonitor, String nombre, String apellidos, String dni, String fechaNacimiento,
                   String telefono, String correo, String foto, String idAdministrador) {
        this.idMonitor = idMonitor;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.correo = correo;
        this.foto = foto;
        this.idAdministrador = idAdministrador;
    }

    public String getIdMonitor() {
        return idMonitor;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getDni() {
        return dni;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public String getFoto() {
        return foto;
    }

    public String getIdAdministrador() {
        return idAdministrador;
    }
}
