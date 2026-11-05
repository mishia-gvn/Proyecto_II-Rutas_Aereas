package com.rutas.modelo;

public class Ruta {
    private String origen;
    private String destino;
    private int minutos;
    private double costo;
    
    public Ruta(String origen, String destino, int minutos, double costo) {
        this.origen = origen;
        this.destino = destino;
        this.minutos = minutos;
        this.costo = costo;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public int getMinutos() {
        return minutos;
    }

    public double getCosto() {
        return costo;
    }

    @Override
    public String toString() {
        return origen + " -> " + destino 
                + " | Tiempo: " + minutos + " minutos"
                + " | Costo: $" + costo;
    }
}
