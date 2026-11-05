package com.rutas.modelo;

import java.util.List;

public class ResultadoRuta {
    private List<String> aeropuertos;
    private int cantidadVuelos;
    private int minutosTotales;
    private double costoTotal;
    private boolean existeRuta;
    
    public ResultadoRuta(List<String> aeropuertos, int cantidadVuelos, 
                        int minutosTotales, double costoTotal, 
                        boolean existeRuta) {

        this.aeropuertos = aeropuertos;
        this.cantidadVuelos = cantidadVuelos;
        this.minutosTotales = minutosTotales;
        this.costoTotal = costoTotal;
        this.existeRuta = existeRuta;
    }

    public List<String> getAeropuertos() {
        return aeropuertos;
    }

    public int getCantidadVuelos() {
        return cantidadVuelos;
    }

    public int getMinutosTotales() {
        return minutosTotales;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public boolean existeRuta() {
        return existeRuta;
    }
}
