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
    private List<String> gruposSeleccionados;

    public Cliente() {
    }

    public Cliente(String idCliente, String nombre, String apellidos, String dni, String fechaNacimiento,
                   String telefono, String correo, String foto, String idAdministrador, List<String> gruposSeleccionados) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.correo = correo;
        this.foto = foto;
        this.idAdministrador = idAdministrador;
        this.gruposSeleccionados = gruposSeleccionados;
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
    public List<String> getGruposSeleccionados() { return gruposSeleccionados; }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public void setIdAdministrador(String idAdministrador) {
        this.idAdministrador = idAdministrador;
    }

    public void setGruposSeleccionados(List<String> gruposSeleccionados) {
        this.gruposSeleccionados = gruposSeleccionados;
    }
}
