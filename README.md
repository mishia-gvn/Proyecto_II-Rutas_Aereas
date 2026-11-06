# Proyecto II - Rutas Aéreas
Proyecto II - Estructuras de Datos. Planificador de rutas aéreas mediante grafos, BFS, DFS y Dijkstra.

## Datos técnicos

- **Lenguaje:** Java 25
- **Entorno:** Apache NetBeans
- **Biblioteca gráfica:** JavaFX 25 (controls y fxml)
- **Gestor de proyecto:** Maven

## Cómo compilar y ejecutar

Requisitos: JDK 25 y Maven (NetBeans ya incluye Maven).

**Desde NetBeans:**
1. Abrir la carpeta `ProyectoRutasAereas` como proyecto.
2. Clic derecho sobre el proyecto → *Clean and Build*.
3. Clic derecho → *Run*.

**Desde la terminal** (dentro de la carpeta `ProyectoRutasAereas`):

    mvn clean javafx:run

Al iniciar, el programa carga `datos/aeropuertos.txt` y `datos/rutas.txt`.
Para usar otros archivos con el mismo formato: botón **Cargar otros archivos…**.

## Uso de la interfaz

- **Seleccionar origen y destino:** escribir el código IATA en los campos o
  hacer clic en el mapa (clic izquierdo = origen, clic derecho = destino).
- **Buscar ruta:** elegir el criterio (*Menos escalas*, *Más rápida* o
  *Más barata*) y presionar **Buscar ruta**. La ruta se resalta en naranja y
  el panel muestra la secuencia, cantidad de vuelos, minutos y costo.
- **Comparar las 3:** muestra las tres rutas para el mismo viaje.
- **Animación:** **Volar** inicia el vuelo tramo por tramo, con una pausa en
  cada escala. El panel muestra el tramo actual y los minutos y costo
  acumulados. **Pausar/Continuar** y **Reiniciar** controlan la animación;
  la barra de velocidad la acelera o la hace más lenta.
- **Datos y rutas salientes:** información del aeropuerto de origen.
- **Alcanzables:** colorea en verde los aeropuertos a los que se puede llegar
  desde el origen y en gris los que no. También se puede consultar qué
  aeropuertos pueden llegar al origen.
- **Cancelar y restaurar rutas:** elegir origen y destino de la ruta y
  presionar **Cancelar ruta**; se muestra roja y punteada en el mapa y las
  consultas se recalculan. **Restaurar todas** devuelve las rutas canceladas.

## Módulo de interfaz (paquetes `visual` y `controlador`)

| Archivo | Responsabilidad |
|---|---|
| `ProyectoRutasAereas.java` | Punto de entrada; abre la ventana JavaFX. |
| `vista/principal.fxml` | Estructura de la ventana: mapa al centro, panel de controles a la derecha. |
| `vista/estilos.css` | Colores de rutas, aeropuertos, ruta resaltada, canceladas y avión. |
| `controlador/ControladorRutas.java` | Conecta la interfaz con `GestorRutas`; no implementa algoritmos. |
| `visual/MapaAeropuertos.java` | Dibuja aeropuertos y rutas en coordenadas 1000×1000 y las escala a la ventana. |
| `visual/AnimacionVuelo.java` | Mueve el avión interpolando su posición entre aeropuertos, con escalas, pausa y reinicio. |