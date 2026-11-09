# Proyecto II - Rutas Aéreas

Proyecto II - Estructuras de Datos. Planificador de rutas aéreas mediante grafos, BFS, DFS y Dijkstra.

## Datos técnicos

- **Lenguaje:** Java 25
- **Entorno:** Apache NetBeans
- **Biblioteca gráfica:** JavaFX 25 (controls y fxml)
- **Gestor de proyecto:** Maven

## Descripción

Aplicación de escritorio que representa una red de aeropuertos como un grafo dirigido y ponderado. Cada aeropuerto es un vértice identificado por su código IATA y cada ruta aérea es una arista dirigida que almacena duración en minutos y costo en dólares.

El programa permite consultar aeropuertos y sus rutas salientes, encontrar rutas con menos vuelos, encontrar rutas de menor duración o costo, consultar alcanzabilidad y cancelar o restaurar rutas. Los resultados se presentan en un mapa interactivo con una animación del vuelo.

## Requisitos

- JDK 25.
- Apache Maven.
- Apache NetBeans (recomendado para abrir y ejecutar el proyecto).

## Estructura del proyecto

La carpeta principal del proyecto Maven es `ProyectoRutasAereas/`.

``` text
ProyectoRutasAereas/
├── datos/
│   ├── aeropuertos.txt
│   ├── rutas.txt
│   ├── prueba_aeropuertos.txt
│   ├── prueba_rutas.txt
│   ├── mapa_referencia.png
│   └── LEEME.txt
├── src/main/
│   ├── java/com/rutas/
│   │   ├── ProyectoRutasAereas.java
│   │   ├── algoritmos/
│   │   │   ├── BFS.java
│   │   │   ├── DFS.java
│   │   │   └── Dijkstra.java
│   │   ├── controlador/
│   │   │   └── ControladorRutas.java
│   │   ├── modelo/
│   │   │   ├── Aeropuerto.java
│   │   │   ├── CriterioRuta.java
│   │   │   ├── Grafo.java
│   │   │   ├── ResultadoRuta.java
│   │   │   └── Ruta.java
│   │   ├── servicio/
│   │   │   └── GestorRutas.java
│   │   └── visual/
│   │       ├── AnimacionVuelo.java
│   │       └── MapaAeropuertos.java
│   └── resources/com/rutas/vista/
│       ├── principal.fxml
│       └── estilos.css
├── pom.xml
└── README.md
```

## Organización y responsabilidades

### Modelo (`modelo`)

- `Aeropuerto`: almacena código IATA, coordenadas, ciudad y país.
- `Ruta`: representa una conexión dirigida y almacena origen, destino, minutos y costo.
- `Grafo`: mantiene los aeropuertos y las rutas salientes mediante mapas y listas de adyacencia. También administra las rutas canceladas.
- `CriterioRuta`: define los criterios de optimización por tiempo o costo.
- `ResultadoRuta`: contiene la secuencia de aeropuertos, cantidad de vuelos, minutos, costo y estado de existencia de la ruta.

### Algoritmos (`algoritmos`)

- `BFS`: encuentra una ruta con la menor cantidad de vuelos.
- `DFS`: obtiene los aeropuertos alcanzables y no alcanzables; también permite identificar desde cuáles aeropuertos se puede llegar a un destino mediante un recorrido sobre el grafo inverso.
- `Dijkstra`: encuentra la ruta de menor tiempo o costo utilizando una misma implementación parametrizada por `CriterioRuta` y una cola de prioridad.

### Servicio (`servicio`)

- `GestorRutas`: carga y valida los archivos, coordina las consultas del grafo y comunica la interfaz con los algoritmos.

### Interfaz (`controlador`, `visual` y `vista`)

- `ControladorRutas`: conecta los controles de JavaFX con el gestor de rutas.
- `MapaAeropuertos`: dibuja los aeropuertos y las conexiones según las coordenadas de los datos.
- `AnimacionVuelo`: representa el desplazamiento del avión entre los aeropuertos de la ruta.
- `principal.fxml` y `estilos.css`: definen la estructura y presentación de la ventana.

## Archivos de datos

Los archivos se encuentran en `ProyectoRutasAereas/datos/`.

- `aeropuertos.txt`: el primer valor indica la cantidad de aeropuertos. Cada registro contiene `codigo x y ciudad pais`.
- `rutas.txt`: el primer valor indica la cantidad de rutas. Cada registro contiene `origen destino minutos costo`.

Las rutas son dirigidas: que exista una conexión de A hacia B no significa que exista una conexión de B hacia A. Los datos se cargan desde archivos y no se encuentran escritos como una red fija dentro de los algoritmos.

Para probar otros conjuntos de datos, se pueden seleccionar los archivos desde la opción **Cargar otros archivos…** de la interfaz. Los archivos alternativos deben conservar el formato indicado.

## Funcionalidades

1.  Consultar los datos de un aeropuerto y sus rutas salientes.
2.  Buscar la ruta con menos vuelos mediante BFS.
3.  Buscar la ruta más rápida o más barata mediante Dijkstra.
4.  Consultar aeropuertos alcanzables y no alcanzables mediante DFS.
5.  Consultar qué aeropuertos pueden llegar al aeropuerto seleccionado.
6.  Cancelar una ruta durante la ejecución y restaurar todas las rutas canceladas.
7.  Visualizar la red, la ruta seleccionada y los resultados en el mapa.
8.  Animar el vuelo tramo por tramo, con controles para pausar, continuar y reiniciar.

## Compilar y ejecutar

### Desde NetBeans

1.  Abrir la carpeta `ProyectoRutasAereas` como maven.
2.  Ejecutar → *Clean and Build*.
3.  Ejecutar el proyecto → *Run*.

### Desde la terminal

Ubicarse dentro de la carpeta `ProyectoRutasAereas/` y ejecutar:

``` bash
mvn clean javafx:run
```

## Pruebas realizadas

Se realizaron pruebas con los archivos originales y con archivos alternativos de menor tamaño. Las pruebas del backend contemplan búsquedas BFS y Dijkstra, recorridos DFS, cancelación y restauración de rutas, códigos inexistentes, origen igual al destino y destinos sin ruta posible. También se probaron errores de formato y validaciones de los archivos de entrada.

## Tecnologías y restricciones

El proyecto utiliza Java y colecciones estándar de `java.util`. Los algoritmos BFS, DFS y Dijkstra están implementados en el proyecto; no se utiliza una biblioteca externa de grafos.
