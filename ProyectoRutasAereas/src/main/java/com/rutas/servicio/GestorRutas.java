package com.rutas.servicio;

import com.rutas.modelo.Aeropuerto;
import com.rutas.modelo.CriterioRuta;
import com.rutas.modelo.Grafo;
import com.rutas.modelo.ResultadoRuta;
import com.rutas.modelo.Ruta;

import com.rutas.algoritmos.BFS;
import com.rutas.algoritmos.DFS;
import com.rutas.algoritmos.Dijkstra;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;

public class GestorRutas {

    // Grafo principal del proyecto
    private Grafo grafo;

    // Algoritmos de búsqueda
    private BFS bfs;
    private DFS dfs;
    private Dijkstra dijkstra;

    // Constructor
    public GestorRutas() {
        grafo = new Grafo();

        bfs = new BFS();
        dfs = new DFS();
        dijkstra = new Dijkstra();
    }

    // Cargar los archivos de aeropuertos y rutas
    public void cargarDatos(String archivoAeropuertos, String archivoRutas) throws IOException {

        Grafo nuevoGrafo = new Grafo();

        leerAeropuertos(archivoAeropuertos, nuevoGrafo);
        leerRutas(archivoRutas, nuevoGrafo);

        grafo = nuevoGrafo;
    }

    // Leer el archivo de aeropuertos
    private void leerAeropuertos(String archivo, Grafo grafo) throws IOException {

        try (Scanner lector = new Scanner(new File(archivo))) {

            if (!lector.hasNextInt()) {
                throw new IOException("El archivo de aeropuertos no tiene un contador válido.");
            }
            int cantidad = lector.nextInt();

            for (int i = 0; i < cantidad; i++) {

                if (!lector.hasNext()) {
                    throw new IOException("Faltan datos de aeropuertos en el archivo.");
                }

                String codigo = lector.next();
                int x = lector.nextInt();
                int y = lector.nextInt();
                String ciudad = lector.next();
                String pais = lector.next();

                Aeropuerto aeropuerto = new Aeropuerto(codigo, x, y, ciudad, pais);

                grafo.agregarAeropuerto(aeropuerto);
            }
        }
    }

    // Leer el archivo de rutas
    private void leerRutas(String archivo, Grafo grafo) throws IOException {

        try (Scanner lector = new Scanner(new File(archivo))) {

            if (!lector.hasNextInt()) {
                throw new IOException("El archivo de rutas no tiene un contador válido.");
            }
            int cantidad = lector.nextInt();

            for (int i = 0; i < cantidad; i++) {

                if (!lector.hasNext()) {
                    throw new IOException("Faltan datos de rutas en el archivo.");
                }
                String origen = lector.next();
                String destino = lector.next();
                int minutos = lector.nextInt();
                double costo = lector.nextDouble();

                Ruta ruta = new Ruta(origen, destino, minutos, costo);

                grafo.agregarRuta(ruta);
            }
        }
    }

    // Consultar un aeropuerto
    public Aeropuerto consultarAeropuerto(String codigo) {
        return grafo.obtenerAeropuerto(codigo);
    }

    // Consultar las rutas salientes de un aeropuerto
    public List<Ruta> consultarRutasSalientes(String codigo) {
        return grafo.obtenerRutasSalientes(codigo);
    }

    // Buscar la ruta con menos vuelos
    public ResultadoRuta buscarMenosVuelos(String origen, String destino) {
        return bfs.buscarRuta(grafo, origen, destino);
    }

    // Buscar la ruta más rápida o más barata
    public ResultadoRuta buscarRutaOptima(
            String origen, String destino, CriterioRuta criterio) {
        return dijkstra.buscarRuta(grafo, origen, destino, criterio);
    }

    // Obtener los aeropuertos alcanzables
    public Set<String> obtenerAlcanzables(String origen) {
        return dfs.obtenerAlcanzables(grafo, origen);
    }

    // Obtener los aeropuertos no alcanzables
    public Set<String> obtenerNoAlcanzables(String origen) {
        return dfs.obtenerNoAlcanzables(grafo, origen);
    }

    // Cancelar una ruta
    public boolean cancelarRuta(String origen, String destino) {
        return grafo.cancelarRuta(origen, destino);
    }

    // Restaurar todas las rutas canceladas
    public void restaurarRutas() {
        grafo.restaurarRutas();
    }

    // Obtener todos los aeropuertos
    public Map<String, Aeropuerto> obtenerTodosAeropuertos() {
        return grafo.getAeropuertos();
    }
}