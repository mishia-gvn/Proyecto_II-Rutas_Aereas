package com.rutas.visual;

import com.rutas.modelo.Aeropuerto;
import com.rutas.modelo.Ruta;

import java.util.List;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

/**
 * Animación del avión sobre la ruta encontrada.
 *
 * La animación es una secuencia (SequentialTransition) de dos tipos de pasos:
 *   - TramoVuelo: mueve el avión de un aeropuerto al siguiente,
 *     interpolando la posición: x = x1 + (x2 - x1) * f, con f de 0 a 1.
 *   - Escala: el avión se queda quieto un momento en el aeropuerto intermedio.
 *
 * En cada cuadro se avisa al "Oyente" (el controlador) para que actualice
 * el panel con el tramo actual y los minutos y el costo acumulados.
 */
public class AnimacionVuelo {

    /** Lo implementa el controlador para mostrar el progreso en el panel. */
    public interface Oyente {
        void actualizar(String tramoActual, int minutosAcumulados,
                        double costoAcumulado, String estado);
    }

    // Tiempo que el avión se queda en cada escala
    private static final Duration DURACION_ESCALA = Duration.seconds(1.0);

    private final Polygon avion;
    private SequentialTransition secuencia;
    private double velocidad = 1.0;

    public AnimacionVuelo(MapaAeropuertos mapa) {
        // Silueta simple de avión apuntando hacia la derecha, centrada en (0, 0)
        avion = new Polygon(
                14, 0,      // nariz
                4, -3,
                -2, -13,    // ala superior
                -6, -13,
                -3, -3,
                -11, -3,
                -14, -8,    // cola superior
                -16, -8,
                -14, 0,
                -16, 8,     // cola inferior
                -14, 8,
                -11, 3,
                -3, 3,
                -6, 13,     // ala inferior
                -2, 13,
                4, 3);
        avion.getStyleClass().add("avion");
        avion.setVisible(false);
        avion.setMouseTransparent(true);
        mapa.getCapaAvion().getChildren().add(avion);
    }

    /**
     * Arma la animación para una ruta.
     *
     * @param paradas aeropuertos en orden (origen, escalas..., destino)
     * @param tramos  rutas entre paradas consecutivas (paradas.size() - 1)
     * @param oyente  recibe las actualizaciones para el panel
     */
    public void preparar(List<Aeropuerto> paradas, List<Ruta> tramos, Oyente oyente) {
        detener();

        secuencia = new SequentialTransition();
        int minutosPrevios = 0;
        double costoPrevio = 0;

        for (int i = 0; i < tramos.size(); i++) {
            Aeropuerto desde = paradas.get(i);
            Aeropuerto hasta = paradas.get(i + 1);
            Ruta ruta = tramos.get(i);

            String texto = "Tramo " + (i + 1) + " de " + tramos.size() + ": "
                    + desde.getCodigo() + " → " + hasta.getCodigo()
                    + " (" + ruta.getMinutos() + " min, $" + formatoCosto(ruta.getCosto()) + ")";

            secuencia.getChildren().add(
                    new TramoVuelo(desde, hasta, ruta, minutosPrevios, costoPrevio, texto, oyente));

            minutosPrevios += ruta.getMinutos();
            costoPrevio += ruta.getCosto();

            // Detenerse un momento en cada escala (no en el destino final)
            boolean esEscala = i < tramos.size() - 1;
            if (esEscala) {
                secuencia.getChildren().add(new Escala(hasta, minutosPrevios, costoPrevio, oyente));
            }
        }

        // Al terminar se informa la llegada
        final int minutosFinales = minutosPrevios;
        final double costoFinal = costoPrevio;
        final String destino = paradas.get(paradas.size() - 1).getCodigo();
        secuencia.setOnFinished(e -> oyente.actualizar("Vuelo completado",
                minutosFinales, costoFinal, "Llegó a " + destino));

        secuencia.setRate(velocidad);

        // Poner el avión en el origen
        if (!paradas.isEmpty()) {
            Aeropuerto origen = paradas.get(0);
            avion.setTranslateX(origen.getX());
            avion.setTranslateY(origen.getY());
            avion.setVisible(true);
            avion.toFront();
        }
    }

    // =========================================
    // CONTROLES
    // =========================================

    /** Inicia la animación o la continúa si estaba en pausa. */
    public void reproducir() {
        if (secuencia != null) {
            secuencia.play();
        }
    }

    public void pausar() {
        if (secuencia != null) {
            secuencia.pause();
        }
    }

    /** Vuelve al origen y empieza de nuevo. */
    public void reiniciar() {
        if (secuencia != null) {
            secuencia.playFromStart();
        }
    }

    /** Detiene la animación y oculta el avión. */
    public void detener() {
        if (secuencia != null) {
            secuencia.stop();
            secuencia = null;
        }
        avion.setVisible(false);
    }

    public boolean estaVolando() {
        return secuencia != null && secuencia.getStatus() == Animation.Status.RUNNING;
    }

    public boolean estaPausada() {
        return secuencia != null && secuencia.getStatus() == Animation.Status.PAUSED;
    }

    public boolean hayAnimacion() {
        return secuencia != null;
    }

    /** 1.0 = velocidad normal, 2.0 = el doble de rápido, etc. */
    public void setVelocidad(double velocidad) {
        this.velocidad = velocidad;
        if (secuencia != null) {
            secuencia.setRate(velocidad);
        }
    }

    // =========================================
    // PASOS DE LA ANIMACIÓN
    // =========================================

    /** Mueve el avión de un aeropuerto al siguiente. */
    private class TramoVuelo extends Transition {

        private final Aeropuerto desde;
        private final Aeropuerto hasta;
        private final Ruta ruta;
        private final int minutosPrevios;
        private final double costoPrevio;
        private final String texto;
        private final Oyente oyente;
        private final double angulo;

        TramoVuelo(Aeropuerto desde, Aeropuerto hasta, Ruta ruta,
                   int minutosPrevios, double costoPrevio, String texto, Oyente oyente) {
            this.desde = desde;
            this.hasta = hasta;
            this.ruta = ruta;
            this.minutosPrevios = minutosPrevios;
            this.costoPrevio = costoPrevio;
            this.texto = texto;
            this.oyente = oyente;

            // Ángulo hacia donde apunta el avión en este tramo
            this.angulo = Math.toDegrees(Math.atan2(
                    hasta.getY() - desde.getY(), hasta.getX() - desde.getX()));

            // Duración proporcional a los minutos del vuelo (entre 1 y 4 segundos)
            double segundos = Math.max(1.0, Math.min(4.0, ruta.getMinutos() / 60.0));
            setCycleDuration(Duration.seconds(segundos));
            setInterpolator(Interpolator.EASE_BOTH);
        }

        /** f va de 0 (salida) a 1 (llegada). */
        @Override
        protected void interpolate(double f) {
            // Interpolación lineal de la posición
            double x = desde.getX() + (hasta.getX() - desde.getX()) * f;
            double y = desde.getY() + (hasta.getY() - desde.getY()) * f;
            avion.setTranslateX(x);
            avion.setTranslateY(y);
            avion.setRotate(angulo);

            // Acumulados: lo de los tramos anteriores + la parte recorrida de este
            int minutos = minutosPrevios + (int) Math.round(ruta.getMinutos() * f);
            // (el costo se redondea hacia abajo mientras vuela; al llegar es exacto)
            double costo = (f >= 1.0)
                    ? costoPrevio + ruta.getCosto()
                    : costoPrevio + Math.floor(ruta.getCosto() * f);
            oyente.actualizar(texto, minutos, costo, "En vuelo");
        }
    }

    /** Pausa en un aeropuerto intermedio. */
    private class Escala extends Transition {

        private final Aeropuerto aeropuerto;
        private final int minutos;
        private final double costo;
        private final Oyente oyente;

        Escala(Aeropuerto aeropuerto, int minutos, double costo, Oyente oyente) {
            this.aeropuerto = aeropuerto;
            this.minutos = minutos;
            this.costo = costo;
            this.oyente = oyente;
            setCycleDuration(DURACION_ESCALA);
        }

        @Override
        protected void interpolate(double f) {
            avion.setTranslateX(aeropuerto.getX());
            avion.setTranslateY(aeropuerto.getY());
            oyente.actualizar("Escala en " + aeropuerto.getCodigo(), minutos, costo,
                    "Escala en " + aeropuerto.getCodigo());
        }
    }

    // Muestra el costo sin decimales si es entero (190 en vez de 190.0)
    public static String formatoCosto(double costo) {
        if (costo == Math.rint(costo)) {
            return String.format("%,.0f", costo);
        }
        return String.format("%,.2f", costo);
    }
}
