package epd_evaluable_2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EPD_EVALUABLE_2 {

    // Inicializa la matriz de distancias a partir de un archivo TSP
    public static double[][] inicializarMatrizDistanciaDesdeTSP(String file) {
        List<double[]> ciudades = new ArrayList<>(); // Almacena coordenadas de ciudades
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isCoordSection = false; // Indica si se ha alcanzado la sección de coordenadas
            while ((line = br.readLine()) != null) {
                line = line.trim(); // Elimina espacios en blanco alrededor de la línea
                // Marca el inicio de la sección de coordenadas
                if (line.equals("NODE_COORD_SECTION")) {
                    isCoordSection = true;
                    continue;
                }
                // Procesa las coordenadas de las ciudades
                if (isCoordSection) {
                    if (line.equals("EOF")) {
                        break; // Fin del archivo
                    }
                    String[] parts = line.split("\\s+"); 
                    int id = Integer.parseInt(parts[0]);
                    double x = Double.parseDouble(parts[1]); // Coordenada X
                    double y = Double.parseDouble(parts[2]); // Coordenada Y
                    ciudades.add(new double[]{id, x, y}); // Almacena las coordenadas en la lista
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int numCiudades = ciudades.size(); // Número total de ciudades
        double[][] distancias = new double[numCiudades][numCiudades];

        // Calcula distancias euclidianas entre todas las ciudades
        for (int i = 0; i < numCiudades; i++) {
            for (int j = 0; j < numCiudades; j++) {
                distancias[i][j] = calculaDistancia(ciudades.get(i), ciudades.get(j));
            }
        }
        return distancias; // Devuelve la matriz de distancias
    }

    // Calcula la distancia euclidiana entre dos ciudades dadas por sus coordenadas
    public static double calculaDistancia(double[] ciudad1, double[] ciudad2) {
        return Math.sqrt(Math.pow(ciudad1[1] - ciudad2[1], 2) + Math.pow(ciudad1[2] - ciudad2[2], 2));
    }

    // Calcula el coste total
    public static double getDistanciaTotal(double[][] distancias, int[] camino) {
        double coste = 0;
        for (int i = 0; i < camino.length - 1; i++) {
            coste += distancias[camino[i]][camino[i + 1]]; // Suma las distancias entre ciudades consecutivas
        }
        coste += distancias[camino[camino.length - 1]][camino[0]]; // Suma la distancia de retorno a la ciudad inicial
        return coste;
    }

    // Genera un tour aleatorio inicial que incluye todas las ciudades
    public static int[] getTour(int NMaxCiudades) {
        int[] tour = new int[NMaxCiudades];
        for (int i = 0; i < NMaxCiudades; i++) {
            tour[i] = i;
        }
        Random random = new Random(12345); // Generador de números aleatorios con semilla fija
        for (int i = NMaxCiudades - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = tour[i];
            tour[i] = tour[j];
            tour[j] = temp;
        }
        return tour;
    }

    // Implementa el algoritmo voraz para resolver el problema del TSP
    public static int[] algVoraz(double[][] distancias) {
        int numCiudades = distancias.length; // Número total de ciudades
        boolean[] visitadas = new boolean[numCiudades];
        int[] camino = new int[numCiudades]; // Almacena el camino resultante

        int actual = 0; // Comienza en la primera ciudad
        visitadas[actual] = true;
        camino[0] = actual;

        for (int i = 1; i < numCiudades; i++) {
            int siguiente = -1;
            double minDistancia = Double.MAX_VALUE;
            for (int j = 0; j < numCiudades; j++) {
                if (!visitadas[j] && distancias[actual][j] < minDistancia) {
                    siguiente = j;
                    minDistancia = distancias[actual][j]; // Actualiza la distancia mínima
                }
            }
            actual = siguiente; // Avanza a la siguiente ciudad
            camino[i] = actual; // Agrega la ciudad al camino
            visitadas[actual] = true; // Marca la ciudad como visitada
        }
        return camino; // Devuelve el camino generado
    }

    // Implementa el enfoque divide y vencerás para resolver el problema del TSP
    public static int[] algDivideYVenceras(double[][] distancias, int ini, int fin) {
        if (fin - ini == 1) { // Caso base: dos ciudades
            return new int[]{ini, fin};
        } else if (fin - ini == 0) { // Caso base: una sola ciudad
            return new int[]{ini};
        }
        int mid = (ini + fin) / 2; // Encuentra el punto medio
        int[] parteIzquierda = algDivideYVenceras(distancias, ini, mid); // Resuelve para la primera mitad
        int[] parteDerecha = algDivideYVenceras(distancias, mid + 1, fin); // Resuelve para la segunda mitad
        return combina(parteIzquierda, parteDerecha, distancias); // Combina las soluciones
    }

    // Combina dos subcaminos generados por el enfoque divide y vencerás
    public static int[] combina(int[] parteIzquierda, int[] parteDerecha, double[][] distancias) {
        int n = parteIzquierda.length;
        int m = parteDerecha.length;

        double minDistancia = Double.MAX_VALUE;
        int mejorIzq = -1, mejorDer = -1;

        // Encuentra el par de ciudades más cercano entre las dos partes
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                double dist = distancias[parteIzquierda[i]][parteDerecha[j]];
                if (dist < minDistancia) {
                    minDistancia = dist;
                    mejorIzq = i;
                    mejorDer = j;
                }
            }
        }

        int[] camino = new int[n + m]; // Almacena el camino combinado
        int index = 0;
        // Añade las ciudades de la parte izquierda
        for (int i = 0; i <= mejorIzq; i++) {
            camino[index++] = parteIzquierda[i];
        }
        // Añade las ciudades de la parte derecha
        for (int j = mejorDer; j < m; j++) {
            camino[index++] = parteDerecha[j];
        }
        // Añade el resto de las ciudades de la parte derecha
        for (int j = 0; j < mejorDer; j++) {
            camino[index++] = parteDerecha[j];
        }
        // Añade el resto de las ciudades de la parte izquierda
        for (int i = mejorIzq + 1; i < n; i++) {
            camino[index++] = parteIzquierda[i];
        }

        return camino; // Devuelve el camino combinado
    }

    // Implementa un algoritmo aleatorio básico para el TSP (BA1)
    public static int[] algoritmoAleatorioBA1(double[][] distancias, int iteraciones) {
        int NMaxCiudades = distancias.length;
        int[] mejorCamino = getTour(NMaxCiudades); // Genera un tour inicial aleatorio
        double mejorCoste = getDistanciaTotal(distancias, mejorCamino); // Calcula su coste

        for (int i = 0; i < iteraciones; i++) {
            int[] caminoActual = getTour(NMaxCiudades); // Genera otro tour aleatorio
            double costeActual = getDistanciaTotal(distancias, caminoActual); // Calcula su coste
            if (costeActual < mejorCoste) { // Actualiza el mejor camino si el actual es mejor
                mejorCamino = caminoActual;
                mejorCoste = costeActual;
            }
        }
        return mejorCamino; // Devuelve el mejor camino encontrado
    }

    // Implementa un algoritmo aleatorio con criterio de mejora para el TSP (BA2)
    public static int[] algoritmoAleatorioBA2(double[][] distancias, int maxIteraciones, int maxIterSinMejora) {
        int NMaxCiudades = distancias.length;
        int[] mejorCamino = getTour(NMaxCiudades); // Genera un tour inicial aleatorio
        double mejorCoste = getDistanciaTotal(distancias, mejorCamino); // Calcula su coste
        int iterSinMejora = 0; // Contador de iteraciones sin mejora

        for (int i = 0; i < maxIteraciones; i++) {
            int[] caminoActual = getTour(NMaxCiudades); // Genera otro tour aleatorio
            double costeActual = getDistanciaTotal(distancias, caminoActual); // Calcula su coste
            if (costeActual < mejorCoste) { // Actualiza el mejor camino si el actual es mejor
                mejorCamino = caminoActual;
                mejorCoste = costeActual;
                iterSinMejora = 0; // Reinicia el contador de iteraciones sin mejora
            } else {
                iterSinMejora++;
            }
            if (iterSinMejora >= maxIterSinMejora) { // Finaliza si se supera el máximo permitido
                break;
            }
        }
        return mejorCamino; // Devuelve el mejor camino encontrado
    }

    // Calcula el promedio de una lista de valores
    public static double calcularMedia(List<Double> valores) {
        double suma = 0;
        for (double valor : valores) {
            suma += valor; // Suma todos los valores
        }
        return suma / valores.size(); // Devuelve el promedio
    }

    public static void main(String[] args) {
        // Ruta de la carpeta donde se encuentran los archivos
        String folderPath = "src/data";

        // Crear un objeto File que representa la carpeta
        File folder = new File(folderPath);
        File[] files = folder.listFiles(); // Obtener la lista de archivos en la carpeta

        // Procesar cada archivo en la carpeta
        for (File file : files) {
            String filePath = file.getPath(); // Obtener la ruta completa del archivo
            System.out.println("\n\nProcesando archivo: " + filePath);

            // Inicializar la matriz de distancias a partir del archivo
            double[][] distancias = inicializarMatrizDistanciaDesdeTSP(filePath);

            // Parámetros de iteración para los algoritmos
            int[] n = {100, 500, 1000, 5000}; // Número de iteraciones para BA1
            int[] p = {50, 100, 250, 500};   // Iteraciones sin mejora para BA2

            // Algoritmo Voraz
            System.out.println("\n---- Algoritmo Voraz ----");
            List<Double> tiemposVoraz = new ArrayList<>(); // Lista para tiempos de ejecución
            List<Double> costesVoraz = new ArrayList<>();  // Lista para costes
            int[] caminoVoraz = algVoraz(distancias); // Calcular el camino una vez
            System.out.println("Camino Voraz: " + java.util.Arrays.toString(caminoVoraz));
            for (int i = 0; i < 5; i++) { // Repetir la ejecución para obtener promedios
                long inicio = System.nanoTime();
                long fin = System.nanoTime();
                tiemposVoraz.add((fin - inicio) / 1000000.0); // Tiempo en milisegundos
                costesVoraz.add(getDistanciaTotal(distancias, caminoVoraz));
            }
            System.out.println("Media del coste voraz: " + calcularMedia(costesVoraz));
            System.out.println("Media del tiempo voraz (ms): " + calcularMedia(tiemposVoraz));

            // Algoritmo Divide y Vencerás
            System.out.println("\n---- Algoritmo Divide y Venceras ----");
            List<Double> tiemposDivide = new ArrayList<>(); // Lista para tiempos de ejecución
            List<Double> costesDivide = new ArrayList<>();  // Lista para costes
            int[] caminoDivide = algDivideYVenceras(distancias, 0, distancias.length - 1); // Calcular el camino una vez
            System.out.println("Camino Divide y Venceras: " + java.util.Arrays.toString(caminoDivide));
            for (int i = 0; i < 5; i++) { // Repetir la ejecución para obtener promedios
                long inicio = System.nanoTime();
                long fin = System.nanoTime();
                tiemposDivide.add((fin - inicio) / 1000000.0); // Tiempo en milisegundos
                costesDivide.add(getDistanciaTotal(distancias, caminoDivide));
            }
            System.out.println("Media del coste divide y venceras: " + calcularMedia(costesDivide));
            System.out.println("Media del tiempo divide y venceras (ms): " + calcularMedia(tiemposDivide));

            // Algoritmo BA1
            System.out.println("\n---- Algoritmo BA1 ----");
            for (int maxIteraciones : n) { // Iterar por cada configuración de iteraciones
                System.out.println("\nBA1 - Iteraciones: " + maxIteraciones);
                List<Double> tiemposBA1 = new ArrayList<>(); // Lista para tiempos
                List<Double> costesBA1 = new ArrayList<>();  // Lista para costes
                int[] caminoBA1 = algoritmoAleatorioBA1(distancias, maxIteraciones); // Calcular el camino una vez
                System.out.println("Camino BA1: " + java.util.Arrays.toString(caminoBA1));
                for (int i = 0; i < 5; i++) { // Repetir la ejecución para obtener promedios
                    long inicioBA1 = System.nanoTime();
                    long finBA1 = System.nanoTime();
                    tiemposBA1.add((finBA1 - inicioBA1) / 1000000.0); // Tiempo en milisegundos
                    costesBA1.add(getDistanciaTotal(distancias, caminoBA1));
                }
                System.out.println("Media del coste BA1: " + calcularMedia(costesBA1));
                System.out.println("Media del tiempo BA1 (ms): " + calcularMedia(tiemposBA1));
            }

            // Algoritmo BA2
            System.out.println("\n---- Algoritmo BA2 ----");
            for (int maxIterSinMejora : p) { // Iterar por cada configuración de iteraciones sin mejora
                System.out.println("\nBA2 - p (Iteraciones sin mejora): " + maxIterSinMejora);
                List<Double> tiemposBA2 = new ArrayList<>(); // Lista para tiempos
                List<Double> costesBA2 = new ArrayList<>();  // Lista para costes
                int[] caminoBA2 = algoritmoAleatorioBA2(distancias, 5000, maxIterSinMejora); // Calcular el camino una vez
                System.out.println("Camino BA2: " + java.util.Arrays.toString(caminoBA2));
                for (int i = 0; i < 5; i++) { // Repetir la ejecución para obtener promedios
                    long inicioBA2 = System.nanoTime();
                    long finBA2 = System.nanoTime();
                    tiemposBA2.add((finBA2 - inicioBA2) / 1000000.0); // Tiempo en milisegundos
                    costesBA2.add(getDistanciaTotal(distancias, caminoBA2));
                }
                System.out.println("Media del coste BA2: " + calcularMedia(costesBA2));
                System.out.println("Media del tiempo BA2 (ms): " + calcularMedia(tiemposBA2));
            }
        }
    }
}
