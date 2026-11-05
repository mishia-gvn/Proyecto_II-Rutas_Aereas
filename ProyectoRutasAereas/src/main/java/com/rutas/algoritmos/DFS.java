package com.rutas.algoritmos;

import com.rutas.modelo.Aeropuerto;
import com.rutas.modelo.Grafo;
import com.rutas.modelo.Ruta;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Deque;

// COMPLEJIDAD: O(n + m)
// n: aeropuertos.
// m: rutas.

public class DFS {

    // Obtener los aeropuertos alcanzables desde un origen
    public Set<String> obtenerAlcanzables(Grafo grafo, String origen) {

        Set<String> visitados = new HashSet<>();

        // Comprobar que el aeropuerto de origen exista
        if (grafo.obtenerAeropuerto(origen) == null) {
            return visitados;
        }

        // Pila para realizar el recorrido
        Deque<String> pila = new ArrayDeque<>();

        // Comenzar desde el origen
        pila.push(origen);

        // Recorrer el grafo
        while (!pila.isEmpty()) {

            String actual = pila.pop();

            if (visitados.add(actual)) {

                // Explorar las rutas salientes
                for (Ruta ruta : grafo.obtenerRutasSalientes(actual)) {

                    String siguiente = ruta.getDestino();

                    if (!visitados.contains(siguiente)) {
                        pila.push(siguiente);
                    }
                }
            }
        }
        return visitados;
    }

    // Obtener los aeropuertos que no son alcanzables
    public Set<String> obtenerNoAlcanzables(Grafo grafo, String origen) {

        Set<String> alcanzables = obtenerAlcanzables(grafo, origen);

        Set<String> noAlcanzables = new HashSet<>();

        // Si el origen no existe, no se puede realizar la consulta
        if (grafo.obtenerAeropuerto(origen) == null) {
            return noAlcanzables;
        }
        // Obtener todos los aeropuertos
        Map<String, Aeropuerto> todos = grafo.getAeropuertos();

        // Identificar los que no fueron visitados
        for (String codigo : todos.keySet()) {

            if (!alcanzables.contains(codigo)) {
                noAlcanzables.add(codigo);
            }
        }
        return noAlcanzables;
    }
}