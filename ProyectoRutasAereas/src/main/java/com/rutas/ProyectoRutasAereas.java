package com.rutas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProyectoRutasAereas {

    public static void main(String[] args) {
        Application.launch(Ventana.class, args);
    }

    public static class Ventana extends Application {

        @Override
        public void start(Stage escenario) throws Exception {
            FXMLLoader cargador = new FXMLLoader(
                    ProyectoRutasAereas.class.getResource("/com/rutas/vista/principal.fxml"));
            Parent raiz = cargador.load();

            Scene escena = new Scene(raiz);
            escena.getStylesheets().add(
                    ProyectoRutasAereas.class.getResource("/com/rutas/vista/estilos.css").toExternalForm());

            escenario.setTitle("Planificador de Rutas Aéreas - EIF207");
            escenario.setScene(escena);
            escenario.setMinWidth(1000);
            escenario.setMinHeight(700);
            escenario.setMaximized(true);
            escenario.show();
        }
    }
}
