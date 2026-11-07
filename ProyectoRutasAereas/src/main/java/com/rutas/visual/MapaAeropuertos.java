package com.rutas.visual;

import com.rutas.modelo.Aeropuerto;
import com.rutas.modelo.Ruta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.control.Tooltip;
import javafx.scene.transform.Scale;

/**
 * Dibuja el mapa: cada aeropuerto es un punto con su código en la posición
 * (x, y) del archivo, y cada ruta es una línea.
 *
 * El mapa se dibuja siempre en coordenadas de 1000 x 1000 (las del archivo)
 * y luego se escala para que quepa en el espacio disponible de la ventana.
 *
 * Los colores y estilos se definen en estilos.css usando clases de estilo
 * (por ejemplo "ruta", "ruta-cancelada", "aeropuerto-alcanzable").
 */
public class MapaAeropuertos extends Pane {

    // Tamaño lógico del mapa, igual al de las coordenadas del archivo
    public static final double TAMANO = 1000;

    // Separación entre las dos líneas de una ruta de ida y vuelta (A->B y B->A)
    private static final double DESPLAZAMIENTO = 2.5;

    // Contenedor de todo el dibujo; es el que se escala
    private final Group lienzo = new Group();

    // Capas: el orden en que se agregan define qué queda encima
    private final Group capaRutas = new Group();
    private final Group capaResaltado = new Group();
    private final Group capaAeropuertos = new Group();
    private final Group capaAvion = new Group();

    private final Scale escala = new Scale(1, 1, 0, 0);

    // Para encontrar rápido el dibujo de cada aeropuerto y de cada ruta
    private final Map<String, Aeropuerto> aeropuertos = new HashMap<>();
    private final Map<String, Circle> puntos = new HashMap<>();
    private final Map<String, Line> lineas = new HashMap<>(); // clave "ORIGEN-DESTINO"

    // Acción que se ejecuta al hacer clic en un aeropuerto (código, botón)
    private BiConsumer<String, MouseButton> alHacerClic;

    public MapaAeropuertos() {
        getStyleClass().add("mapa");

        Rectangle fondo = new Rectangle(0, 0, TAMANO, TAMANO);
        fondo.getStyleClass().add("mapa-fondo");

        lienzo.getChildren().addAll(fondo, capaRutas, capaResaltado,
                capaAeropuertos, capaAvion);
        lienzo.getTransforms().add(escala);
        getChildren().add(lienzo);

        // Permite que el mapa se encoja y recorta lo que se salga
        setMinSize(0, 0);
        Rectangle recorte = new Rectangle();
        recorte.widthProperty().bind(widthProperty());
        recorte.heightProperty().bind(heightProperty());
        setClip(recorte);
    }

    // Ajusta la escala cada vez que cambia el tamaño del panel
    @Override
    protected void layoutChildren() {
        double factor = Math.min(getWidth(), getHeight()) / TAMANO;
        escala.setX(factor);
        escala.setY(factor);

        // Centrar el mapa en el espacio disponible
        lienzo.setLayoutX((getWidth() - TAMANO * factor) / 2);
        lienzo.setLayoutY((getHeight() - TAMANO * factor) / 2);
    }

    // DIBUJO INICIAL

    /** Dibuja todos los aeropuertos y rutas. Borra lo que hubiera antes. */
    public void dibujar(Map<String, Aeropuerto> todos, List<Ruta> rutas) {
        capaRutas.getChildren().clear();
        capaResaltado.getChildren().clear();
        capaAeropuertos.getChildren().clear();
        aeropuertos.clear();
        puntos.clear();
        lineas.clear();

        aeropuertos.putAll(todos);

        // Primero las rutas, para que queden debajo de los puntos
        for (Ruta ruta : rutas) {
            Line linea = crearLinea(ruta.getOrigen(), ruta.getDestino());
            linea.getStyleClass().add("ruta");
            Tooltip.install(linea, new Tooltip(ruta.toString()));  // formato de Ruta.toString()
            lineas.put(clave(ruta.getOrigen(), ruta.getDestino()), linea);
            capaRutas.getChildren().add(linea);
        }

        // Luego los aeropuertos con su código
        for (Aeropuerto a : todos.values()) {
            Circle punto = new Circle(a.getX(), a.getY(), 6);
            punto.getStyleClass().add("aeropuerto");

            Text codigo = new Text(a.getX() + 8, a.getY() - 6, a.getCodigo());
            codigo.getStyleClass().add("codigo-aeropuerto");
            codigo.setMouseTransparent(true);

            Tooltip.install(punto, new Tooltip(a.toString().replace('_', ' ')
                    + "\nClic izquierdo: origen · Clic derecho: destino"));

            punto.setOnMouseClicked(e -> {
                if (alHacerClic != null) {
                    alHacerClic.accept(a.getCodigo(), e.getButton());
                }
            });

            puntos.put(a.getCodigo(), punto);
            capaAeropuertos.getChildren().addAll(punto, codigo);
        }
    }

    /**
     * Crea una línea de origen a destino, corrida un poco hacia un lado.
     * Así, si existen A->B y B->A, se ven como dos líneas paralelas
     * y se puede marcar una sola como cancelada.
     */
    private Line crearLinea(String origen, String destino) {
        Aeropuerto a = aeropuertos.get(origen);
        Aeropuerto b = aeropuertos.get(destino);

        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double largo = Math.max(Math.hypot(dx, dy), 1);

        // Vector perpendicular a la dirección del vuelo
        double px = -dy / largo * DESPLAZAMIENTO;
        double py = dx / largo * DESPLAZAMIENTO;

        return new Line(a.getX() + px, a.getY() + py, b.getX() + px, b.getY() + py);
    }

    private String clave(String origen, String destino) {
        return origen + "-" + destino;
    }
    
    // RUTA RESALTADA

    /** Resalta la ruta encontrada: líneas gruesas con flecha en cada tramo. */
    public void resaltarRuta(List<String> codigos) {
        capaResaltado.getChildren().clear();

        for (int i = 0; i + 1 < codigos.size(); i++) {
            Aeropuerto a = aeropuertos.get(codigos.get(i));
            Aeropuerto b = aeropuertos.get(codigos.get(i + 1));

            Line tramo = new Line(a.getX(), a.getY(), b.getX(), b.getY());
            tramo.getStyleClass().add("ruta-resaltada");
            capaResaltado.getChildren().addAll(tramo, crearFlecha(a, b));
        }

        quitarClases("aeropuerto-origen", "aeropuerto-destino", "aeropuerto-escala");
        for (int i = 0; i < codigos.size(); i++) {
            Circle punto = puntos.get(codigos.get(i));
            if (i == 0) {
                punto.getStyleClass().add("aeropuerto-origen");
            } else if (i == codigos.size() - 1) {
                punto.getStyleClass().add("aeropuerto-destino");
            } else {
                punto.getStyleClass().add("aeropuerto-escala");
            }
        }
    }

    // Triángulo pequeño a mitad del tramo que indica la dirección
    private Polygon crearFlecha(Aeropuerto a, Aeropuerto b) {
        double mx = (a.getX() + b.getX()) / 2.0;
        double my = (a.getY() + b.getY()) / 2.0;
        double angulo = Math.atan2(b.getY() - a.getY(), b.getX() - a.getX());

        double tam = 9;
        Polygon flecha = new Polygon(
                mx + tam * Math.cos(angulo), my + tam * Math.sin(angulo),
                mx + tam * Math.cos(angulo + 2.5), my + tam * Math.sin(angulo + 2.5),
                mx + tam * Math.cos(angulo - 2.5), my + tam * Math.sin(angulo - 2.5));
        flecha.getStyleClass().add("flecha");
        return flecha;
    }

    public void limpiarResaltado() {
        capaResaltado.getChildren().clear();
        quitarClases("aeropuerto-origen", "aeropuerto-destino", "aeropuerto-escala");
    }

    // SELECCIÓN DE ORIGEN Y DESTINO

    /** Marca con un borde el origen y el destino elegidos (antes de buscar). */
    public void marcarSeleccion(String origen, String destino) {
        quitarClases("seleccion-origen", "seleccion-destino");
        if (origen != null && puntos.containsKey(origen)) {
            puntos.get(origen).getStyleClass().add("seleccion-origen");
        }
        if (destino != null && puntos.containsKey(destino)) {
            puntos.get(destino).getStyleClass().add("seleccion-destino");
        }
    }

    // ALCANZABLES

    /**
     * Colorea los aeropuertos: los del conjunto en verde y el resto en rojo.
     * El aeropuerto consultado se marca aparte.
     */
    public void colorearAlcanzables(String consultado, Set<String> alcanzables) {
        limpiarAlcanzables();
        for (Map.Entry<String, Circle> e : puntos.entrySet()) {
            if (e.getKey().equals(consultado)) {
                e.getValue().getStyleClass().add("aeropuerto-consultado");
            } else if (alcanzables.contains(e.getKey())) {
                e.getValue().getStyleClass().add("aeropuerto-alcanzable");
            } else {
                e.getValue().getStyleClass().add("aeropuerto-no-alcanzable");
            }
        }
    }

    public void limpiarAlcanzables() {
        quitarClases("aeropuerto-consultado", "aeropuerto-alcanzable", "aeropuerto-no-alcanzable");
    }

    // RUTAS CANCELADAS

    public void marcarCancelada(String origen, String destino) {
        Line linea = lineas.get(clave(origen, destino));
        if (linea != null && !linea.getStyleClass().contains("ruta-cancelada")) {
            linea.getStyleClass().add("ruta-cancelada");
            linea.toFront(); // que se vea encima de las demás rutas
        }
    }

    public void restaurarCanceladas() {
        for (Line linea : lineas.values()) {
            linea.getStyleClass().remove("ruta-cancelada");
        }
    }

    // =========================================
    // UTILIDADES
    // =========================================

    private void quitarClases(String... clases) {
        for (Circle punto : puntos.values()) {
            punto.getStyleClass().removeAll(clases);
        }
    }

    public void setAlHacerClic(BiConsumer<String, MouseButton> accion) {
        this.alHacerClic = accion;
    }

    /** Capa donde AnimacionVuelo coloca el avión (mismas coordenadas del mapa). */
    public Group getCapaAvion() {
        return capaAvion;
    }

    public Aeropuerto getAeropuerto(String codigo) {
        return aeropuertos.get(codigo);
    }

    public List<String> getCodigos() {
        List<String> codigos = new ArrayList<>(aeropuertos.keySet());
        codigos.sort(null);
        return codigos;
    }
}