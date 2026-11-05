package com.rutas.algoritmos;

import com.rutas.modelo.Aeropuerto;
import com.rutas.modelo.Grafo;
import com.rutas.modelo.ResultadoRuta;
import com.rutas.modelo.Ruta;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

// COMPLEJIDAD: O(n + m)
// n: aeropuertos.
// m: rutas.

public class BFS {

    // Buscar la ruta con menor cantidad de vuelos
    public ResultadoRuta buscarRuta(Grafo grafo, String origen, String destino) {

        // Verificar que existan los aeropuertos
        Aeropuerto aeropuertoOrigen = grafo.obtenerAeropuerto(origen);
        Aeropuerto aeropuertoDestino = grafo.obtenerAeropuerto(destino);

        if (aeropuertoOrigen == null || aeropuertoDestino == null) {
            return new ResultadoRuta(new ArrayList<>(), 0, 0, 0.0, false);
        }

        // Si el origen y el destino son iguales
        if (origen.equals(destino)) {
            List<String> camino = new ArrayList<>();
            camino.add(origen);

            return new ResultadoRuta(camino, 0, 0, 0.0, true);
        }

        // Estructuras necesarias
        Queue<String> cola = new ArrayDeque<>();
        Set<String> visitados = new HashSet<>();
        Map<String, Ruta> rutaAnterior = new HashMap<>();

        // Comenzar desde el aeropuerto de origen
        cola.add(origen);
        visitados.add(origen);

        boolean encontrada = false;

        // Recorrer el grafo
        while (!cola.isEmpty() && !encontrada) {

            String actual = cola.poll();

            // Revisar las rutas salientes
            for (Ruta ruta : grafo.obtenerRutasSalientes(actual)) {

                String siguiente = ruta.getDestino();

                if (visitados.add(siguiente)) {

                    rutaAnterior.put(siguiente, ruta);

                    // Comprobar si encontramos el destino
                    if (siguiente.equals(destino)) {
                        encontrada = true;
                        break;
                    }
                    cola.add(siguiente);
                }
            }
        }

        // Si no se encontró una ruta
        if (!encontrada) {
            return new ResultadoRuta(new ArrayList<>(), 0, 0, 0.0, false);
        }

        // Reconstruir el camino encontrado
        List<String> camino = new ArrayList<>();
        List<Ruta> tramos = new ArrayList<>();

        String actual = destino;
        camino.add(actual);

        while (!actual.equals(origen)) {

            Ruta ruta = rutaAnterior.get(actual);

            if (ruta == null) {
                return new ResultadoRuta(new ArrayList<>(), 0, 0, 0.0, false);
            }

            tramos.add(ruta);

            actual = ruta.getOrigen();
            camino.add(actual);
        }

        // Invertir el recorrido para obtener origen -> destino
        Collections.reverse(camino);
        Collections.reverse(tramos);

        // Calcular los totales de la ruta
        int minutosTotales = 0;
        double costoTotal = 0.0;

        for (Ruta ruta : tramos) {
            minutosTotales += ruta.getMinutos();
            costoTotal += ruta.getCosto();
        }

        // Devolver el resultado
        return new ResultadoRuta(camino, tramos.size(), minutosTotales, 
                                costoTotal, true);
    }
}