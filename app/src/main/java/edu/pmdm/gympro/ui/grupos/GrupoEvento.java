package edu.pmdm.gympro.ui.grupos;

public class GrupoEvento {
    private final String nombre;
    private final int diaSemana; // Usa Calendar.MONDAY, etc.
    private final int horaInicio;
    private final int minutoInicio;
    private final int horaFin;
    private final int minutoFin;

    public GrupoEvento(String nombre, int diaSemana, int horaInicio, int minutoInicio, int horaFin, int minutoFin) {
        this.nombre = nombre;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.minutoInicio = minutoInicio;
        this.horaFin = horaFin;
        this.minutoFin = minutoFin;
    }

    // Getters
    public String getNombre() { return nombre; }
    public int getDiaSemana() { return diaSemana; }
    public int getHoraInicio() { return horaInicio; }
    public int getMinutoInicio() { return minutoInicio; }
    public int getHoraFin() { return horaFin; }
    public int getMinutoFin() { return minutoFin; }
}
