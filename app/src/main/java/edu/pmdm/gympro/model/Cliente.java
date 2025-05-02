package edu.pmdm.gympro.model;

import java.util.List;

public class Cliente {

    private String idCliente;
    private String nombre;
    private String apellidos;
    private String dni;
    private String fechaNacimiento;
    private String telefono;
    private String correo;
    private String foto;
    private String idAdministrador;
    private List<String> clasesSeleccionadas; // ← Lista de clases del cliente

    public Cliente() {
        // Constructor vacío requerido por Firestore
    }

    public Cliente(String idCliente, String nombre, String apellidos, String dni, String fechaNacimiento,
                   String telefono, String correo, String foto, String idAdministrador, List<String> clasesSeleccionadas) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.correo = correo;
        this.foto = foto;
        this.idAdministrador = idAdministrador;
        this.clasesSeleccionadas = clasesSeleccionadas;
    }

    public String getIdCliente() { return idCliente; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getDni() { return dni; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public String getFoto() { return foto; }
    public String getIdAdministrador() { return idAdministrador; }
    public List<String> getClasesSeleccionadas() { return clasesSeleccionadas; }
}
