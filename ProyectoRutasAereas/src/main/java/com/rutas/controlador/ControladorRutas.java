package com.rutas.controlador;

import com.rutas.modelo.Aeropuerto;
import com.rutas.modelo.CriterioRuta;
import com.rutas.modelo.ResultadoRuta;
import com.rutas.modelo.Ruta;
import com.rutas.servicio.GestorRutas;
import com.rutas.visual.AnimacionVuelo;
import com.rutas.visual.MapaAeropuertos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Controlador de la ventana principal (principal.fxml).
 *
 * Conecta la interfaz con el backend: toda consulta se hace a través de
 * GestorRutas. Este controlador NO implementa algoritmos; solo pide los
 * resultados y los muestra en el panel y en el mapa.
 */
public class ControladorRutas {

    // Archivos que se cargan al abrir el programa (relativos a la carpeta del proyecto)
    private static final String AEROPUERTOS_POR_DEFECTO = "datos/aeropuertos.txt";
    private static final String RUTAS_POR_DEFECTO = "datos/rutas.txt";

    // ---------- Elementos del FXML ----------
    @FXML private StackPane contenedorMapa;
    @FXML private Label lblArchivos;

    @FXML private ComboBox<String> cbOrigen;
    @FXML private ComboBox<String> cbDestino;
    @FXML private RadioButton rbEscalas;
    @FXML private RadioButton rbRapida;
    @FXML private RadioButton rbBarata;

    @FXML private Label lblSecuencia;
    @FXML private Label lblVuelos;
    @FXML private Label lblMinutos;
    @FXML private Label lblCosto;

    @FXML private Label lblEstadoVuelo;
    @FXML private Label lblTramo;
    @FXML private Label lblMinutosAcum;
    @FXML private Label lblCostoAcum;
    @FXML private Button btnPausar;
    @FXML private Slider sliderVelocidad;
    @FXML private Label lblVelocidad;

    @FXML private ComboBox<String> cbCancelarOrigen;
    @FXML private ComboBox<String> cbCancelarDestino;
    @FXML private ListView<String> lstCanceladas;

    @FXML private TextArea txtDetalle;
    @FXML private Label lblMensaje;

    // ---------- Lógica ----------
    private GestorRutas gestor;
    private MapaAeropuertos mapa;
    private AnimacionVuelo animacion;

    // Última ruta encontrada (para animarla y para recalcular tras cancelar)
    private ResultadoRuta resultadoActual;
    private CriterioRuta criterioActual;  // null = menos escalas (BFS)
    private String origenActual;
    private String destinoActual;

    // Si en pantalla se están mostrando alcanzables, de qué aeropuerto
    private String alcanzablesDe;
    private boolean alcanzablesInverso;

    // =========================================
    // INICIO
    // =========================================

    /** JavaFX llama a este método automáticamente después de cargar el FXML. */
    @FXML
    private void initialize() {
        gestor = new GestorRutas();

        mapa = new MapaAeropuertos();
        contenedorMapa.getChildren().add(mapa);
        animacion = new AnimacionVuelo(mapa);

        // Clic en el mapa: izquierdo = origen, derecho = destino
        mapa.setAlHacerClic(this::alHacerClicEnAeropuerto);

        // Al cambiar el origen de la cancelación, mostrar solo sus destinos reales
        cbCancelarOrigen.valueProperty().addListener((obs, viejo, nuevo) -> llenarDestinosCancelacion());

        // Marcar en el mapa lo que se va escribiendo/eligiendo
        cbOrigen.getEditor().textProperty().addListener((obs, v, n) -> actualizarSeleccionMapa());
        cbDestino.getEditor().textProperty().addListener((obs, v, n) -> actualizarSeleccionMapa());

        // Velocidad de la animación
        sliderVelocidad.valueProperty().addListener((obs, v, n) -> {
            animacion.setVelocidad(n.doubleValue());
            lblVelocidad.setText(String.format("%.1fx", n.doubleValue()));
        });

        // Intentar cargar los archivos por defecto
        try {
            cargar(new File(AEROPUERTOS_POR_DEFECTO), new File(RUTAS_POR_DEFECTO));
        } catch (Exception e) {
            lblArchivos.setText("No se encontraron los archivos por defecto.");
            mensaje("No se pudo cargar " + AEROPUERTOS_POR_DEFECTO + ": " + e.getMessage()
                    + ". Use \"Cargar otros archivos…\".");
        }
    }

    // =========================================
    // CARGA DE ARCHIVOS
    // =========================================

    /** Carga los archivos en el gestor y redibuja todo. */
    private void cargar(File archivoAeropuertos, File archivoRutas) throws Exception {
        gestor.cargarDatos(archivoAeropuertos.getPath(), archivoRutas.getPath());

        // Reiniciar el estado de la interfaz
        animacion.detener();
        resultadoActual = null;
        origenActual = null;
        alcanzablesDe = null;
        lstCanceladas.getItems().clear();
        limpiarResultado();
        limpiarPanelVuelo();
        txtDetalle.clear();

        mapa.dibujar(gestor.obtenerTodosAeropuertos(), obtenerTodasLasRutas());

        // Llenar las listas de códigos
        List<String> codigos = mapa.getCodigos();
        cbOrigen.getItems().setAll(codigos);
        cbDestino.getItems().setAll(codigos);
        cbCancelarOrigen.getItems().setAll(codigos);
        cbOrigen.setValue(null);
        cbDestino.setValue(null);
        cbCancelarOrigen.setValue(null);
        cbCancelarDestino.getItems().clear();

        int cantidadRutas = obtenerTodasLasRutas().size();
        lblArchivos.setText(archivoAeropuertos.getName() + " y " + archivoRutas.getName()
                + "\n" + codigos.size() + " aeropuertos, " + cantidadRutas + " rutas");
        mensaje("Datos cargados: " + codigos.size() + " aeropuertos y " + cantidadRutas + " rutas.");
    }

    @FXML
    private void cargarArchivos() {
        Window ventana = contenedorMapa.getScene().getWindow();

        FileChooser selector = new FileChooser();
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Texto", "*.txt"));
        File carpetaDatos = new File("datos");
        if (carpetaDatos.isDirectory()) {
            selector.setInitialDirectory(carpetaDatos);
        }

        selector.setTitle("1/2 · Seleccione el archivo de AEROPUERTOS");
        File aeropuertos = selector.showOpenDialog(ventana);
        if (aeropuertos == null) {
            return;
        }

        selector.setInitialDirectory(aeropuertos.getParentFile());
        selector.setTitle("2/2 · Seleccione el archivo de RUTAS");
        File rutas = selector.showOpenDialog(ventana);
        if (rutas == null) {
            return;
        }

        try {
            cargar(aeropuertos, rutas);
        } catch (Exception e) {
            // El gestor solo reemplaza el grafo si la carga fue exitosa,
            // así que los datos anteriores siguen disponibles.
            mensaje("Error al cargar: " + e.getMessage());
            txtDetalle.setText("No se pudieron cargar los archivos.\n\n" + e.getMessage()
                    + "\n\nSe mantienen los datos anteriores.");
        }
    }

    /** Recorre todos los aeropuertos y junta sus rutas salientes. */
    private List<Ruta> obtenerTodasLasRutas() {
        List<Ruta> rutas = new ArrayList<>();
        for (String codigo : gestor.obtenerTodosAeropuertos().keySet()) {
            rutas.addAll(gestor.consultarRutasSalientes(codigo));
        }
        return rutas;
    }

    // =========================================
    // SELECCIÓN DE ORIGEN Y DESTINO
    // =========================================

    private void alHacerClicEnAeropuerto(String codigo, MouseButton boton) {
        if (boton == MouseButton.SECONDARY) {
            cbDestino.setValue(codigo);
            mensaje("Destino: " + codigo);
        } else {
            cbOrigen.setValue(codigo);
            mensaje("Origen: " + codigo);
            mostrarDatosAeropuerto(codigo);
        }
    }

    @FXML
    private void intercambiar() {
        String origen = leerCodigo(cbOrigen);
        String destino = leerCodigo(cbDestino);
        cbOrigen.setValue(destino.isEmpty() ? null : destino);
        cbDestino.setValue(origen.isEmpty() ? null : origen);
    }

    private void actualizarSeleccionMapa() {
        mapa.marcarSeleccion(leerCodigo(cbOrigen), leerCodigo(cbDestino));
    }

    /** Lee lo escrito en un ComboBox editable, en mayúsculas y sin espacios. */
    private String leerCodigo(ComboBox<String> combo) {
        String texto = combo.isEditable() ? combo.getEditor().getText() : combo.getValue();
        if (texto == null || texto.isBlank()) {
            texto = combo.getValue();
        }
        return texto == null ? "" : texto.trim().toUpperCase();
    }

    /**
     * Valida un código: que no esté vacío y que exista en el grafo.
     * Devuelve true si es válido; si no, muestra el mensaje y devuelve false.
     */
    private boolean validarCodigo(String codigo, String nombreCampo) {
        if (codigo.isEmpty()) {
            mensaje("Indique el " + nombreCampo + ".");
            return false;
        }
        if (gestor.consultarAeropuerto(codigo) == null) {
            mensaje("El aeropuerto \"" + codigo + "\" no existe.");
            txtDetalle.setText("El código " + codigo + " no corresponde a ningún aeropuerto cargado.");
            return false;
        }
        return true;
    }

    // =========================================
    // BUSCAR RUTA (BFS o Dijkstra)
    // =========================================

    @FXML
    private void buscarRuta() {
        String origen = leerCodigo(cbOrigen);
        String destino = leerCodigo(cbDestino);

        if (!validarCodigo(origen, "origen") || !validarCodigo(destino, "destino")) {
            return;
        }

        CriterioRuta criterio = null;  // null = menos escalas
        if (rbRapida.isSelected()) {
            criterio = CriterioRuta.MENOR_TIEMPO;
        } else if (rbBarata.isSelected()) {
            criterio = CriterioRuta.MENOR_COSTO;
        }

        ejecutarBusqueda(origen, destino, criterio);
    }

    /** Hace la búsqueda en el gestor y muestra el resultado. */
    private void ejecutarBusqueda(String origen, String destino, CriterioRuta criterio) {
        animacion.detener();
        limpiarPanelVuelo();
        mapa.limpiarAlcanzables();

        ResultadoRuta resultado = buscar(origen, destino, criterio);

        origenActual = origen;
        destinoActual = destino;
        criterioActual = criterio;
        alcanzablesDe = null;

        if (!resultado.existeRuta()) {
            resultadoActual = null;
            mapa.limpiarResaltado();
            limpiarResultado();
            lblSecuencia.setText("No existe ruta de " + origen + " a " + destino);
            mensaje("No existe ruta de " + origen + " a " + destino + ".");
            return;
        }

        resultadoActual = resultado;
        mostrarResultado(resultado);
        mapa.resaltarRuta(resultado.getAeropuertos());

        if (origen.equals(destino)) {
            mensaje("El origen y el destino son el mismo aeropuerto (" + origen + "): 0 vuelos.");
        } else {
            mensaje("Ruta " + nombreCriterio(criterio) + " de " + origen + " a " + destino
                    + ": " + resultado.getCantidadVuelos() + " vuelo(s).");
        }
    }

    private ResultadoRuta buscar(String origen, String destino, CriterioRuta criterio) {
        if (criterio == null) {
            return gestor.buscarMenosVuelos(origen, destino);
        }
        return gestor.buscarRutaOptima(origen, destino, criterio);
    }

    private void mostrarResultado(ResultadoRuta r) {
        lblSecuencia.setText(String.join(" → ", r.getAeropuertos()));
        lblVuelos.setText(String.valueOf(r.getCantidadVuelos()));
        lblMinutos.setText(formatoMinutos(r.getMinutosTotales()));
        lblCosto.setText("$" + AnimacionVuelo.formatoCosto(r.getCostoTotal()));
    }

    private void limpiarResultado() {
        lblSecuencia.setText("—");
        lblVuelos.setText("—");
        lblMinutos.setText("—");
        lblCosto.setText("—");
    }

    /** Muestra las tres rutas (menos escalas, más rápida, más barata) en el detalle. */
    @FXML
    private void compararCriterios() {
        String origen = leerCodigo(cbOrigen);
        String destino = leerCodigo(cbDestino);

        if (!validarCodigo(origen, "origen") || !validarCodigo(destino, "destino")) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Comparación ").append(origen).append(" → ").append(destino).append("\n\n");

        CriterioRuta[] criterios = {null, CriterioRuta.MENOR_TIEMPO, CriterioRuta.MENOR_COSTO};
        for (CriterioRuta c : criterios) {
            ResultadoRuta r = buscar(origen, destino, c);
            sb.append("■ ").append(nombreCriterio(c).toUpperCase()).append("\n");
            if (r.existeRuta()) {
                sb.append("  ").append(String.join(" → ", r.getAeropuertos())).append("\n");
                sb.append("  Vuelos: ").append(r.getCantidadVuelos())
                  .append("   Minutos: ").append(r.getMinutosTotales())
                  .append("   Costo: $").append(AnimacionVuelo.formatoCosto(r.getCostoTotal()))
                  .append("\n\n");
            } else {
                sb.append("  No existe ruta de ").append(origen).append(" a ").append(destino).append("\n\n");
            }
        }
        txtDetalle.setText(sb.toString());
        mensaje("Comparación de los tres criterios en el panel Detalle.");
    }

    private String nombreCriterio(CriterioRuta criterio) {
        if (criterio == CriterioRuta.MENOR_TIEMPO) {
            return "más rápida";
        }
        if (criterio == CriterioRuta.MENOR_COSTO) {
            return "más barata";
        }
        return "con menos escalas";
    }

    // =========================================
    // ANIMACIÓN
    // =========================================

    @FXML
    private void volar() {
        if (resultadoActual == null) {
            mensaje("Primero busque una ruta.");
            return;
        }
        if (resultadoActual.getCantidadVuelos() == 0) {
            mensaje("No hay vuelos que animar: el origen y el destino son el mismo.");
            return;
        }
        // Si estaba en pausa, "Volar" continúa
        if (animacion.estaPausada()) {
            pausarContinuar();
            return;
        }

        // Paradas (objetos Aeropuerto) y tramos (objetos Ruta) de la ruta encontrada
        List<Aeropuerto> paradas = new ArrayList<>();
        for (String codigo : resultadoActual.getAeropuertos()) {
            paradas.add(gestor.consultarAeropuerto(codigo));
        }
        List<Ruta> tramos = obtenerTramos(resultadoActual.getAeropuertos(), criterioActual);

        animacion.preparar(paradas, tramos, this::actualizarPanelVuelo);
        animacion.reproducir();
        btnPausar.setText("⏸ Pausar");
    }

    @FXML
    private void pausarContinuar() {
        if (animacion.estaVolando()) {
            animacion.pausar();
            btnPausar.setText("▶ Continuar");
            lblEstadoVuelo.setText("En pausa");
        } else if (animacion.estaPausada()) {
            animacion.reproducir();
            btnPausar.setText("⏸ Pausar");
        }
    }

    @FXML
    private void reiniciarVuelo() {
        if (!animacion.hayAnimacion()) {
            volar();
            return;
        }
        animacion.reiniciar();
        btnPausar.setText("⏸ Pausar");
    }

    /** Lo llama AnimacionVuelo en cada cuadro de la animación. */
    private void actualizarPanelVuelo(String tramo, int minutos, double costo, String estado) {
        lblTramo.setText(tramo);
        lblMinutosAcum.setText(formatoMinutos(minutos));
        lblCostoAcum.setText("$" + AnimacionVuelo.formatoCosto(costo));
        if (!animacion.estaPausada()) {
            lblEstadoVuelo.setText(estado);
        }
    }

    private void limpiarPanelVuelo() {
        lblEstadoVuelo.setText("—");
        lblTramo.setText("—");
        lblMinutosAcum.setText("—");
        lblCostoAcum.setText("—");
        btnPausar.setText("⏸ Pausar");
    }

    /**
     * Para cada par de aeropuertos consecutivos busca la Ruta (arista)
     * correspondiente en las rutas salientes, para conocer sus minutos y costo.
     * Si hubiera varias rutas entre los mismos aeropuertos, se toma la que
     * corresponde al criterio usado (la de menos minutos o menor costo).
     */
    private List<Ruta> obtenerTramos(List<String> codigos, CriterioRuta criterio) {
        List<Ruta> tramos = new ArrayList<>();
        for (int i = 0; i + 1 < codigos.size(); i++) {
            String desde = codigos.get(i);
            String hasta = codigos.get(i + 1);

            Ruta elegida = null;
            for (Ruta r : gestor.consultarRutasSalientes(desde)) {
                if (!r.getDestino().equals(hasta)) {
                    continue;
                }
                if (elegida == null
                        || (criterio == CriterioRuta.MENOR_TIEMPO && r.getMinutos() < elegida.getMinutos())
                        || (criterio == CriterioRuta.MENOR_COSTO && r.getCosto() < elegida.getCosto())) {
                    elegida = r;
                }
            }
            tramos.add(elegida);
        }
        return tramos;
    }

    // =========================================
    // CONSULTAS: DATOS, ALCANZABLES
    // =========================================

    @FXML
    private void consultarAeropuerto() {
        String codigo = leerCodigo(cbOrigen);
        if (validarCodigo(codigo, "origen")) {
            mostrarDatosAeropuerto(codigo);
            mensaje("Datos de " + codigo + " en el panel Detalle.");
        }
    }

    /** Muestra los datos del aeropuerto y todas sus rutas salientes. */
    private void mostrarDatosAeropuerto(String codigo) {
        Aeropuerto a = gestor.consultarAeropuerto(codigo);
        if (a == null) {
            txtDetalle.setText("El aeropuerto " + codigo + " no existe.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(a.getCodigo()).append(" — ").append(a.getCiudad().replace('_', ' '))
          .append(", ").append(a.getPais().replace('_', ' ')).append("\n");
        sb.append("Posición en el mapa: (").append(a.getX()).append(", ").append(a.getY()).append(")\n\n");

        List<Ruta> salientes = gestor.consultarRutasSalientes(codigo);
        sb.append("Rutas salientes (").append(salientes.size()).append("):\n");
        if (salientes.isEmpty()) {
            sb.append("  (ninguna)\n");
        }
        for (Ruta r : salientes) {
            sb.append(String.format("  → %-4s %5d min   $%s%n",
                    r.getDestino(), r.getMinutos(), AnimacionVuelo.formatoCosto(r.getCosto())));
        }
        txtDetalle.setText(sb.toString());
    }

    @FXML
    private void mostrarAlcanzables() {
        String origen = leerCodigo(cbOrigen);
        if (validarCodigo(origen, "origen")) {
            calcularAlcanzables(origen, false);
        }
    }

    /** Aeropuertos desde los que se puede llegar al origen seleccionado. */
    @FXML
    private void mostrarQuienLlega() {
        String codigo = leerCodigo(cbOrigen);
        if (validarCodigo(codigo, "origen")) {
            calcularAlcanzables(codigo, true);
        }
    }

    /**
     * Colorea el mapa y llena el detalle.
     *   inverso = false: a qué aeropuertos se puede llegar DESDE codigo (DFS).
     *   inverso = true:  desde qué aeropuertos se puede llegar HACIA codigo.
     */
    private void calcularAlcanzables(String codigo, boolean inverso) {
        // Ya no hay una ruta en pantalla: ahora la vista son los alcanzables
        animacion.detener();
        limpiarPanelVuelo();
        mapa.limpiarResaltado();
        limpiarResultado();
        resultadoActual = null;
        origenActual = null;
        alcanzablesDe = codigo;
        alcanzablesInverso = inverso;

        Set<String> conectados;
        if (inverso) {
            conectados = new TreeSet<>(gestor.obtenerOrigenesAlcanzables(codigo));
        } else {
            conectados = new TreeSet<>(gestor.obtenerAlcanzables(codigo));
        }
        conectados.remove(codigo);  // el propio aeropuerto no se cuenta

        Set<String> noConectados = new TreeSet<>(gestor.obtenerTodosAeropuertos().keySet());
        noConectados.removeAll(conectados);
        noConectados.remove(codigo);

        mapa.colorearAlcanzables(codigo, conectados);

        String si = inverso ? "Pueden llegar a " + codigo : "Alcanzables desde " + codigo;
        String no = inverso ? "NO pueden llegar a " + codigo : "NO alcanzables desde " + codigo;
        txtDetalle.setText(si + " (" + conectados.size() + "):\n" + String.join(", ", conectados) + "\n\n"
                + no + " (" + noConectados.size() + "):\n"
                + (noConectados.isEmpty() ? "(ninguno)" : String.join(", ", noConectados)));
        mensaje(si + ": " + conectados.size() + ". " + no + ": " + noConectados.size() + ".");
    }

    @FXML
    private void limpiarMapa() {
        animacion.detener();
        mapa.limpiarResaltado();
        mapa.limpiarAlcanzables();
        resultadoActual = null;
        origenActual = null;
        alcanzablesDe = null;
        limpiarResultado();
        limpiarPanelVuelo();
        mensaje("Mapa limpio.");
    }

    // =========================================
    // CANCELAR Y RESTAURAR RUTAS
    // =========================================

    /** Llena el combo de destinos con las rutas salientes actuales del origen elegido. */
    private void llenarDestinosCancelacion() {
        String origen = cbCancelarOrigen.getValue();
        cbCancelarDestino.getItems().clear();
        if (origen == null) {
            return;
        }
        Set<String> destinos = new TreeSet<>();
        for (Ruta r : gestor.consultarRutasSalientes(origen)) {
            destinos.add(r.getDestino());
        }
        cbCancelarDestino.getItems().setAll(destinos);
    }

    @FXML
    private void cancelarRuta() {
        String origen = cbCancelarOrigen.getValue();
        String destino = cbCancelarDestino.getValue();
        if (origen == null || destino == null) {
            mensaje("Elija el origen y el destino de la ruta a cancelar.");
            return;
        }

        if (!gestor.cancelarRuta(origen, destino)) {
            mensaje("La ruta " + origen + " → " + destino + " no existe o ya fue cancelada.");
            return;
        }

        lstCanceladas.getItems().add(origen + " → " + destino);
        mapa.marcarCancelada(origen, destino);
        llenarDestinosCancelacion();

        String efecto = recalcularVistaActual();
        mensaje("Ruta " + origen + " → " + destino + " cancelada." + efecto);
    }

    @FXML
    private void restaurarRutas() {
        if (lstCanceladas.getItems().isEmpty()) {
            mensaje("No hay rutas canceladas.");
            return;
        }
        gestor.restaurarRutas();
        lstCanceladas.getItems().clear();
        mapa.restaurarCanceladas();
        llenarDestinosCancelacion();

        String efecto = recalcularVistaActual();
        mensaje("Se restauraron todas las rutas." + efecto);
    }

    /**
     * Después de cancelar o restaurar, el grafo cambió. Se vuelve a calcular
     * lo que esté en pantalla (la ruta buscada o los alcanzables) para que se
     * vea el efecto. Devuelve un texto corto para la barra de mensajes.
     */
    private String recalcularVistaActual() {
        if (origenActual != null) {
            String antes = (resultadoActual == null)
                    ? "no existe ruta" : String.join(" → ", resultadoActual.getAeropuertos());
            ejecutarBusqueda(origenActual, destinoActual, criterioActual);
            String despues = (resultadoActual == null)
                    ? "no existe ruta" : String.join(" → ", resultadoActual.getAeropuertos());

            if (antes.equals(despues)) {
                return " La ruta mostrada no cambió.";
            }
            txtDetalle.setText("La ruta " + nombreCriterio(criterioActual) + " de " + origenActual
                    + " a " + destinoActual + " cambió:\n\n"
                    + "Antes:  " + antes + "\nAhora:  " + despues);
            return " La ruta mostrada cambió (ver Detalle).";
        }
        if (alcanzablesDe != null) {
            calcularAlcanzables(alcanzablesDe, alcanzablesInverso);
            return " Alcanzables recalculados (ver Detalle).";
        }
        return "";
    }

    // =========================================
    // UTILIDADES
    // =========================================

    private void mensaje(String texto) {
        lblMensaje.setText(texto);
    }

    /** 545 -> "545 min (9 h 05 min)" */
    private String formatoMinutos(int minutos) {
        return String.format("%d min (%d h %02d min)", minutos, minutos / 60, minutos % 60);
    }
}
