package com.rutas.modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grafo {

    // Almacena los aeropuertos usando su código como identificador
    private Map<String, Aeropuerto> aeropuertos;

    // Almacena las rutas salientes de cada aeropuerto
    private Map<String, List<Ruta>> adyacencias;

    // Almacena los objetos de las rutas canceladas
    private List<Ruta> rutasCanceladas;

    // Constructor
    public Grafo() {
        aeropuertos = new HashMap<>();
        adyacencias = new HashMap<>();
        rutasCanceladas = new ArrayList<>();
    }

    // Agregar un aeropuerto
    public void agregarAeropuerto(Aeropuerto aeropuerto) {
        String codigo = aeropuerto.getCodigo();

        aeropuertos.put(codigo, aeropuerto);
        adyacencias.putIfAbsent(codigo, new ArrayList<>());
    }

    // Agregar una ruta
    public void agregarRuta(Ruta ruta) {
        String origen = ruta.getOrigen();
        String destino = ruta.getDestino();

        if (!aeropuertos.containsKey(origen) || !aeropuertos.containsKey(destino)) {
            throw new IllegalArgumentException(
                    "El aeropuerto de origen o destino no existe.");
        }
        adyacencias.get(origen).add(ruta);
    }

    // Consultar un aeropuerto por su código
    public Aeropuerto obtenerAeropuerto(String codigo) {
        return aeropuertos.get(codigo);
    }

    // Obtener las rutas salientes de un aeropuerto
    public List<Ruta> obtenerRutasSalientes(String codigo) {

        if (!adyacencias.containsKey(codigo)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(adyacencias.get(codigo));
    }

    // Cancelar una ruta
    public boolean cancelarRuta(String origen, String destino) {

        List<Ruta> rutas = adyacencias.get(origen);

        if (rutas == null) {
            return false;
        }
        for (int i = 0; i < rutas.size(); i++) {

            Ruta ruta = rutas.get(i);

            if (ruta.getDestino().equals(destino)) {
                rutasCanceladas.add(ruta);
                rutas.remove(i);
                return true;
            }
        }
        return false;
    }

    // Restaurar todas las rutas canceladas
    public void restaurarRutas() {

        for (Ruta ruta : rutasCanceladas) {
            adyacencias.get(ruta.getOrigen()).add(ruta);
        }
        rutasCanceladas.clear();
    }

    // Consultar todos los aeropuertos
    public Map<String, Aeropuerto> getAeropuertos() {
        return new HashMap<>(aeropuertos);
    }
}