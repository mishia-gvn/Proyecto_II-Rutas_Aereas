package com.rutas;

import com.rutas.modelo.Aeropuerto;
import com.rutas.modelo.CriterioRuta;
import com.rutas.modelo.ResultadoRuta;
import com.rutas.modelo.Ruta;
import com.rutas.servicio.GestorRutas;

import java.util.Scanner;

public class PruebaBackend {

    // =========================================
    // 1. METODO PRINCIPAL
    // =========================================

    public static void main(String[] args) {

        try {
            GestorRutas gestor = new GestorRutas();

            // Cargar los datos originales
            gestor.cargarDatos(
                    "datos/aeropuertos.txt",
                    "datos/rutas.txt"
            );

            System.out.println("\nDatos cargados correctamente.");
            System.out.println("Aeropuertos: "
                    + gestor.obtenerTodosAeropuertos().size());
            System.out.println("Rutas: " + contarRutas(gestor));

            Scanner entrada = new Scanner(System.in);
            int opcion = -1;

            do {
                mostrarMenu();

                System.out.print("Seleccione una opcion: ");

                if (!entrada.hasNextInt()) {
                    System.out.println("\nIngrese un numero valido.");
                    entrada.nextLine();
                    continue;
                }

                opcion = entrada.nextInt();

                switch (opcion) {

                    case 1:
                        mostrarTitulo("1. CARGA Y CONSULTA DE DATOS");
                        probarConsultaAeropuerto(gestor, "SJO");
                        break;

                    case 2:
                        mostrarTitulo("2. PRUEBAS DE BFS");
                        ejecutarPruebasBFS(gestor);
                        break;

                    case 3:
                        mostrarTitulo("3. PRUEBAS DE DFS");
                        probarDFS(gestor, "SJO");
                        break;

                    case 4:
                        mostrarTitulo("4. PRUEBAS DE DIJKSTRA");
                        ejecutarPruebasDijkstra(gestor);
                        break;

                    case 5:
                        mostrarTitulo("5. CANCELACION Y RESTAURACION");
                        probarCancelacionRestauracion(gestor);
                        break;

                    case 6:
                        ejecutarTodasLasPruebas(gestor);
                        break;

                    case 7:
                        mostrarTitulo("7. CASOS ESPECIALES");
                        probarCasosEspeciales(gestor);
                        break;

                    case 8:
                        mostrarTitulo("8. ARCHIVOS ALTERNATIVOS");
                        probarArchivosAlternativos();
                        break;

                    case 0:
                        System.out.println("\nSaliendo del menu de pruebas...");
                        break;

                    default:
                        System.out.println("\nOpcion no valida.");
                }

            } while (opcion != 0);

        } catch (Exception e) {
            System.out.println("\nERROR AL EJECUTAR LAS PRUEBAS:");
            e.printStackTrace();
        }
    }

    // =========================================
    // 2. MENU PRINCIPAL
    // =========================================

    public static void mostrarMenu() {

        System.out.println("\n");
        System.out.println("========================================");
        System.out.println("             MENU DE PRUEBAS");
        System.out.println("========================================");
        System.out.println("1. Carga y consulta de datos");
        System.out.println("2. Pruebas de BFS - Menos vuelos");
        System.out.println("3. Pruebas de DFS - Alcanzabilidad");
        System.out.println("4. Pruebas de Dijkstra");
        System.out.println("5. Cancelacion y restauracion");
        System.out.println("6. Ejecutar todas las pruebas");
        System.out.println("7. Pruebas de casos especiales");
        System.out.println("8. Pruebas con archivos alternativos");
        System.out.println("0. Salir");
        System.out.println("========================================");
    }

    // =========================================
    // 3. EJECUTAR TODAS LAS PRUEBAS
    // =========================================

    public static void ejecutarTodasLasPruebas(GestorRutas gestor) {

        mostrarTitulo("EJECUTANDO TODAS LAS PRUEBAS");

        mostrarTitulo("1. CONSULTA DE DATOS");
        probarConsultaAeropuerto(gestor, "SJO");

        mostrarTitulo("2. PRUEBAS DE BFS");
        ejecutarPruebasBFS(gestor);

        mostrarTitulo("3. PRUEBAS DE DFS");
        probarDFS(gestor, "SJO");

        mostrarTitulo("4. PRUEBAS DE DIJKSTRA");
        ejecutarPruebasDijkstra(gestor);

        mostrarTitulo("5. CANCELACION Y RESTAURACION");
        probarCancelacionRestauracion(gestor);

        mostrarTitulo("6. CASOS ESPECIALES");
        probarCasosEspeciales(gestor);

        mostrarTitulo("7. ARCHIVOS ALTERNATIVOS");
        probarArchivosAlternativos();

        mostrarTitulo("TODAS LAS PRUEBAS FINALIZADAS");
    }

    // =========================================
    // 4. PRUEBAS DE BFS
    // =========================================

    public static void ejecutarPruebasBFS(GestorRutas gestor) {

        probarBFS(gestor, "DEN", "SJO");
        probarBFS(gestor, "LIR", "SJU");
        probarBFS(gestor, "SJO", "MVD");
    }

    public static void probarBFS(
            GestorRutas gestor, String origen, String destino) {

        System.out.println("\nViaje: " + origen + " -> " + destino);

        ResultadoRuta resultado =
                gestor.buscarMenosVuelos(origen, destino);

        mostrarResultado(resultado);
    }

    // =========================================
    // 5. PRUEBAS DE DFS
    // =========================================

    public static void probarDFS(GestorRutas gestor, String origen) {

        System.out.println("\nOrigen: " + origen);

        System.out.println("\nAeropuertos alcanzables:");

        for (String codigo : gestor.obtenerAlcanzables(origen)) {
            System.out.println("  " + codigo);
        }

        System.out.println("\nAeropuertos no alcanzables:");

        for (String codigo : gestor.obtenerNoAlcanzables(origen)) {
            System.out.println("  " + codigo);
        }

        System.out.println("\nTotal alcanzables: "
                + gestor.obtenerAlcanzables(origen).size());

        System.out.println("Total no alcanzables: "
                + gestor.obtenerNoAlcanzables(origen).size());

        // Consultar los aeropuertos desde los que se puede llegar al destino
        System.out.println("\nAeropuertos desde los que se puede llegar a " + origen + ":");

        for (String codigo : gestor.obtenerOrigenesAlcanzables(origen)) {
            System.out.println("  " + codigo);
        }

        System.out.println("\nTotal de aeropuertos que pueden llegar a "
                + origen + ": "
                + gestor.obtenerOrigenesAlcanzables(origen).size());
    }

    // =========================================
    // 6. PRUEBAS DE DIJKSTRA
    // =========================================

    public static void ejecutarPruebasDijkstra(GestorRutas gestor) {

        probarDijkstra(gestor, "DEN", "SJO");
        probarDijkstra(gestor, "LIR", "SJU");
        probarDijkstra(gestor, "SJO", "MVD");
    }

    public static void probarDijkstra(
            GestorRutas gestor, String origen, String destino) {

        System.out.println("\nViaje: " + origen + " -> " + destino);

        System.out.println("\n--- MENOR TIEMPO ---");

        ResultadoRuta resultadoTiempo =
                gestor.buscarRutaOptima(
                        origen, destino, CriterioRuta.MENOR_TIEMPO
                );

        mostrarResultado(resultadoTiempo);

        System.out.println("\n--- MENOR COSTO ---");

        ResultadoRuta resultadoCosto =
                gestor.buscarRutaOptima(
                        origen, destino, CriterioRuta.MENOR_COSTO
                );

        mostrarResultado(resultadoCosto);
    }

    // =========================================
    // 7. CONSULTA DE AEROPUERTOS Y RUTAS
    // =========================================

    public static void probarConsultaAeropuerto(
            GestorRutas gestor, String codigo) {

        System.out.println("\nAeropuerto consultado: " + codigo);

        Aeropuerto aeropuerto = gestor.consultarAeropuerto(codigo);

        if (aeropuerto == null) {
            System.out.println("Aeropuerto no encontrado.");
            return;
        }

        System.out.println(aeropuerto);
        System.out.println("Rutas salientes:");

        for (Ruta ruta : gestor.consultarRutasSalientes(codigo)) {
            System.out.println("  " + ruta);
        }
    }

    // =========================================
    // 8. CANCELACION Y RESTAURACION DE RUTAS
    // =========================================

    public static void probarCancelacionRestauracion(GestorRutas gestor) {

        int rutasAntes = contarRutas(gestor);

        System.out.println("Rutas antes de cancelar: " + rutasAntes);

        // BUSQUEDAS ANTES DE CANCELAR
        mostrarTitulo("BUSQUEDAS ANTES DE CANCELAR");

        System.out.println("\nBFS:");

        probarBFS(gestor, "DEN", "SJO");

        System.out.println("\nDIJKSTRA:");

        probarDijkstra(gestor, "DEN", "SJO");

        // CANCELAR LA RUTA
        mostrarTitulo("CANCELACION DE RUTA");

        System.out.println("\nCancelando la ruta DEN -> LAX...");

        boolean cancelada = gestor.cancelarRuta("DEN", "LAX");

        if (!cancelada) {
            System.out.println("No se encontro la ruta para cancelar.");
            return;
        }

        System.out.println("Ruta cancelada correctamente.");

        System.out.println("Rutas despues de cancelar: " + contarRutas(gestor));

        // BUSQUEDAS DESPUES DE CANCELAR
        mostrarTitulo("BUSQUEDAS DESPUES DE CANCELAR");

        System.out.println("\nBFS:");

        probarBFS(gestor, "DEN", "SJO");

        System.out.println("\nDIJKSTRA:");

        probarDijkstra(gestor, "DEN", "SJO");

        // RESTAURAR LAS RUTAS
        mostrarTitulo("RESTAURACION DE RUTAS");

        gestor.restaurarRutas();

        System.out.println("Rutas despues de restaurar: " + contarRutas(gestor));

        // BUSQUEDAS DESPUES DE RESTAURAR
        mostrarTitulo("BUSQUEDAS DESPUES DE RESTAURAR");

        System.out.println("\nBFS:");

        probarBFS(gestor, "DEN", "SJO");

        System.out.println("\nDIJKSTRA:");

        probarDijkstra(gestor, "DEN", "SJO");

        // COMPROBAR RESTAURACION
        if (contarRutas(gestor) == rutasAntes) {
            System.out.println("\nRestauracion comprobada correctamente.");
        } else {
            System.out.println("\nError: la cantidad de rutas no coincide.");
        }
    }

    // =========================================
    // 9. CASOS ESPECIALES
    // =========================================

    public static void probarCasosEspeciales(GestorRutas gestor) {

        mostrarTitulo("CASO 1: CODIGO INEXISTENTE");

        System.out.println("\nBusqueda: XXX -> SJO");

        mostrarResultado(
                gestor.buscarMenosVuelos("XXX", "SJO")
        );

        mostrarTitulo("CASO 2: ORIGEN Y DESTINO IGUALES");

        System.out.println("\nBusqueda: SJO -> SJO");

        mostrarResultado(
                gestor.buscarMenosVuelos("SJO", "SJO")
        );

        mostrarTitulo("CASO 3: NO EXISTE UNA RUTA");

        System.out.println("\nBusqueda: SJO -> KIN");

        mostrarResultado(
                gestor.buscarMenosVuelos("SJO", "KIN")
        );

        System.out.println("\n--- COMPROBACION CON DIJKSTRA ---");

        System.out.println("\nXXX -> SJO:");

        mostrarResultado(
                gestor.buscarRutaOptima(
                        "XXX", "SJO", CriterioRuta.MENOR_TIEMPO
                )
        );

        System.out.println("\nSJO -> SJO:");

        mostrarResultado(
                gestor.buscarRutaOptima(
                        "SJO", "SJO", CriterioRuta.MENOR_TIEMPO
                )
        );

        System.out.println("\nSJO -> KIN:");

        mostrarResultado(
                gestor.buscarRutaOptima(
                        "SJO", "KIN", CriterioRuta.MENOR_TIEMPO
                )
        );
    }

    // =========================================
    // 10. PRUEBAS CON ARCHIVOS ALTERNATIVOS
    // =========================================

    public static void probarArchivosAlternativos() {

        try {
            GestorRutas gestorPrueba = new GestorRutas();

            gestorPrueba.cargarDatos(
                    "datos/prueba_aeropuertos.txt",
                    "datos/prueba_rutas.txt"
            );

            System.out.println("\nDatos alternativos cargados correctamente.");

            System.out.println("Aeropuertos: "
                    + gestorPrueba.obtenerTodosAeropuertos().size());

            System.out.println("\n--- BFS: MENOS VUELOS ---");

            probarBFS(gestorPrueba, "AAA", "DDD");

            System.out.println("\n--- DIJKSTRA: MENOR TIEMPO Y COSTO ---");

            probarDijkstra(gestorPrueba, "AAA", "DDD");

        } catch (Exception e) {
            System.out.println("\nError al cargar los archivos alternativos:");
            e.printStackTrace();
        }
    }

    // =========================================
    // 11. METODOS AUXILIARES
    // =========================================

    public static int contarRutas(GestorRutas gestor) {

        int cantidad = 0;

        for (String codigo : gestor.obtenerTodosAeropuertos().keySet()) {
            cantidad += gestor.consultarRutasSalientes(codigo).size();
        }

        return cantidad;
    }

    public static void mostrarTitulo(String titulo) {

        System.out.println("\n");
        System.out.println("========================================");
        System.out.println(titulo);
        System.out.println("========================================");
    }

    public static void mostrarResultado(ResultadoRuta resultado) {

        if (resultado.existeRuta()) {

            System.out.println("Recorrido: "
                    + String.join(" -> ", resultado.getAeropuertos()));

            System.out.println("Cantidad de vuelos: "
                    + resultado.getCantidadVuelos());

            System.out.println("Minutos totales: "
                    + resultado.getMinutosTotales());

            System.out.println("Costo total: $"
                    + resultado.getCostoTotal());

        } else {
            System.out.println("No existe una ruta disponible.");
        }
    }
}
