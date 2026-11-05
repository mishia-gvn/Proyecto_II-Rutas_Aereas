package com.rutas.modelo;

public class Aeropuerto {
    private String codigo;
    private int x;
    private int y;
    private String ciudad;
    private String pais;    
    
    public Aeropuerto(String codigo, int x, int y, String ciudad, String pais){
        this.codigo = codigo;
        this.x = x;
        this.y = y;
        this.ciudad = ciudad;
        this.pais = pais;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getPais() {
        return pais;
    }

    @Override
    public String toString() {
        return codigo + " - " + ciudad + ", " + pais;
    }
}
