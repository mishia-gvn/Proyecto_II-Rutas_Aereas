package com.rutas.algoritmos;

import com.rutas.modelo.Aeropuerto;
import com.rutas.modelo.CriterioRuta;
import com.rutas.modelo.Grafo;
import com.rutas.modelo.ResultadoRuta;
import com.rutas.modelo.Ruta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

// COMPLEJIDAD: O( (n + m) log(n + m) )
// n: aeropuertos.
// m: rutas.

public class Dijkstra {

    // Buscar la ruta más rápida o más barata
    public ResultadoRuta buscarRuta(Grafo grafo, String origen, 
                                    String destino, CriterioRuta criterio) {

        // Verificar que los aeropuertos existan
        Aeropuerto aeropuertoOrigen = grafo.obtenerAeropuerto(origen);
        Aeropuerto aeropuertoDestino = grafo.obtenerAeropuerto(destino);

        if (aeropuertoOrigen == null || aeropuertoDestino == null) {
            return new ResultadoRuta(
                    new ArrayList<>(), 0, 0, 0.0, false);
        }

        // Si el origen y el destino son iguales
        if (origen.equals(destino)) {
            List<String> camino = new ArrayList<>();
            camino.add(origen);

            return new ResultadoRuta(camino, 0, 0, 0.0, true);
        }

        // Distancia mínima conocida para cada aeropuerto
        Map<String, Double> distancias = new HashMap<>();

        // Ruta utilizada para llegar a cada aeropuerto
        Map<String, Ruta> rutaAnterior = new HashMap<>();

        // Cola de prioridad para seleccionar la menor distancia
        PriorityQueue<Entrada> cola = new PriorityQueue<>(
                Comparator.comparingDouble(entrada -> entrada.distancia));

        // Inicializar el origen
        distancias.put(origen, 0.0);
        cola.add(new Entrada(origen, 0.0));

        // Ejecutar Dijkstra
        while (!cola.isEmpty()) {

            Entrada entrada = cola.poll();

            String actual = entrada.codigo;
            double distanciaActual = entrada.distancia;

            // Ignorar entradas que ya tienen una distancia mejor
            if (Double.compare(distanciaActual, 
                    distancias.getOrDefault(actual, Double.POSITIVE_INFINITY)) > 0) {
                continue;
            }

            // Si llegamos al destino, terminamos
            if (actual.equals(destino)) {
                break;
            }

            // Explorar las rutas salientes
            for (Ruta ruta : grafo.obtenerRutasSalientes(actual)) {

                String siguiente = ruta.getDestino();

                double peso = obtenerPeso(ruta, criterio);

                double nuevaDistancia = distanciaActual + peso;

                double distanciaAnterior = distancias.getOrDefault(
                        siguiente, Double.POSITIVE_INFINITY);

                // Actualizar si encontramos una ruta mejor
                if (nuevaDistancia < distanciaAnterior) {

                    distancias.put(siguiente, nuevaDistancia);
                    rutaAnterior.put(siguiente, ruta);

                    cola.add(new Entrada(siguiente, nuevaDistancia));
                }
            }
        }

        // Comprobar si encontramos el destino
        if (!distancias.containsKey(destino)) {
            return new ResultadoRuta(new ArrayList<>(), 0, 0, 0.0, false);
        }

        // Reconstruir el camino
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

        // Invertir los recorridos
        Collections.reverse(camino);
        Collections.reverse(tramos);

        // Calcular los minutos y el costo totales
        int minutosTotales = 0;
        double costoTotal = 0.0;

        for (Ruta ruta : tramos) {
            minutosTotales += ruta.getMinutos();
            costoTotal += ruta.getCosto();
        }

        // Devolver el resultado
        return new ResultadoRuta(camino, tramos.size(), 
                                minutosTotales, costoTotal, true);
    }

    // Seleccionar el peso según el criterio
    private double obtenerPeso(Ruta ruta, CriterioRuta criterio) {

        switch (criterio) {
            case MENOR_TIEMPO:
                return ruta.getMinutos();

            case MENOR_COSTO:
                return ruta.getCosto();

            default:
                throw new IllegalArgumentException("Criterio de búsqueda no válido.");
        }
    }

    // Clase auxiliar para almacenar los elementos de la cola
    private static class Entrada {

        private String codigo;
        private double distancia;

        public Entrada(String codigo, double distancia) {
            this.codigo = codigo;
            this.distancia = distancia;
        }
    }
}