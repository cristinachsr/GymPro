package edu.pmdm.gympro.model;

public class Empleado {
    private String idempleado;
    private String nombreEmpleado;
    private String apellidoEmpleado;
    private String fechaNacimiento;
    private String email;
    private String dni;
    private String photo;
    private String rol;

    // Constructor vacío requerido por Firestore
    public Empleado() {
    }

    // Constructor con parámetros
    public Empleado(String idempleado, String nombre, String apellidos, String fecha, String email, String dni) {
        this.idempleado = idempleado;
        this.nombreEmpleado = nombre;
        this.apellidoEmpleado = apellidos;
        this.fechaNacimiento = fecha;
        this.email = email;
        this.dni = dni;
        this.photo = "";
        this.rol = "empleado";
    }

    // Getters necesarios para Firestore
    public String getIdempleado() {
        return idempleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public String getApellidoEmpleado() {
        return apellidoEmpleado;
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

    public String getPhoto() {
        return photo;
    }

    public String getRol() {
        return rol;
    }
}
