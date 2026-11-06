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

            if (cantidad < 0) {
                throw new IOException("La cantidad de aeropuertos no puede ser negativa.");
            }            
            for (int i = 0; i < cantidad; i++) {

                if (!lector.hasNext()) {
                    throw new IOException("Faltan datos del aeropuerto " + (i + 1) + ".");
                }
                String codigo = lector.next();
                
                int x = leerEntero(lector, "coordenada X del aeropuerto " + codigo);
                int y = leerEntero(lector, "coordenada Y del aeropuerto " + codigo);
                
                if (!lector.hasNext()) {
                    throw new IOException("Falta la ciudad del aeropuerto " + codigo + ".");
                }
                String ciudad = lector.next();
                
                if (!lector.hasNext()) {
                    throw new IOException("Falta el país del aeropuerto " + codigo + ".");
                }
                String pais = lector.next();

                if (grafo.obtenerAeropuerto(codigo) != null) {
                    throw new IOException("El aeropuerto " + codigo + " está duplicado.");
                }
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
            lector.nextLine();

            if (cantidad < 0) {
                throw new IOException("La cantidad de rutas no puede ser negativa.");
            }

            for (int i = 0; i < cantidad; i++) {

                if (!lector.hasNextLine()) {
                    throw new IOException("Faltan datos de la ruta " + (i + 1) + ".");
                }

                String linea = lector.nextLine();

                if (linea.trim().isEmpty()) {
                    throw new IOException("Faltan datos de la ruta " + (i + 1) + ".");
                }

                Scanner datosRuta = new Scanner(linea);

                if (!datosRuta.hasNext()) {
                    throw new IOException("Faltan datos de la ruta " + (i + 1) + ".");
                }
                String origen = datosRuta.next();

                if (!datosRuta.hasNext()) {
                    datosRuta.close();
                    throw new IOException("Falta el destino de la ruta " + origen + ".");
                }
                String destino = datosRuta.next();

                int minutos = leerEntero(datosRuta, "tiempo de la ruta " + origen + " -> " + destino);
                double costo = leerDecimal(datosRuta, "costo de la ruta " + origen + " -> " + destino);

                if (datosRuta.hasNext()) {
                    datosRuta.close();
                    throw new IOException("La ruta " + origen + " -> " + destino + " contiene datos adicionales.");
                }

                datosRuta.close();

                if (minutos < 0) {
                    throw new IOException("El tiempo de una ruta no puede ser negativo.");
                }
                if (costo < 0) {
                    throw new IOException("El costo de una ruta no puede ser negativo.");
                }

                Ruta ruta = new Ruta(origen, destino, minutos, costo);

                try {
                    grafo.agregarRuta(ruta);
                } catch (IllegalArgumentException e) {
                    throw new IOException("No se pudo agregar la ruta " 
                            + origen + " -> " + destino + ": " + e.getMessage());
                }
            }

            if (lector.hasNextLine()) {
                while (lector.hasNextLine()) {
                    if (!lector.nextLine().trim().isEmpty()) {
                        throw new IOException("El archivo de rutas contiene datos adicionales.");
                    }
                }
            }
        }
    }

    // Leer un número entero y validar su formato
    private int leerEntero(Scanner lector, String dato) throws IOException {

        if (!lector.hasNext()) {
            throw new IOException("Falta el dato: " + dato + ".");
        }

        String valor = lector.next();

        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IOException("El dato " + dato + " debe ser un número entero.");
        }
    }
    
    // Leer un número decimal y validar su formato
    private double leerDecimal(Scanner lector, String dato) throws IOException {

        if (!lector.hasNext()) {
            throw new IOException("Falta el dato: " + dato + ".");
        }
        String valor = lector.next();

        try {
            double numero = Double.parseDouble(valor);

            if (!Double.isFinite(numero)) {
                throw new IOException("El dato " + dato + " no es un número válido.");
            }
            return numero;
        } 
        catch (NumberFormatException e) {
            throw new IOException("El dato " + dato + " debe ser un número decimal.");
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
    
    // Obtener los aeropuertos desde los que se puede llegar a un destino
    public Set<String> obtenerOrigenesAlcanzables(String destino) {
        return dfs.obtenerOrigenesAlcanzables(grafo, destino);
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