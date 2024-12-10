/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package epd_evaluable_2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author elena
 */
public class EPD_EVALUABLE_2 {

    public static double[][] inicializarMatrizDistanciaDesdeTSP(String file) {
        List<double[]> ciudades = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isCoordSection = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                //Iniciar lectura en la sección de coordenadas
                if (line.equals("NODE_COORD_SECTION")) {
                    isCoordSection = true;
                    continue;
                }

                // Leer las coordenadas de las ciudades
                if (isCoordSection) {
                    if (line.equals("EOF")) {
                        break; // Termina si llega al final del archivo
                    }
                    String[] parts = line.split("\\s+");
                    int id = Integer.parseInt(parts[0]);
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    ciudades.add(new double[]{id, x, y});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int numCiudades = ciudades.size();
        double[][] distancias = new double[numCiudades][numCiudades];

        // Calcula la distancia entre cada par de ciudades
        for (int i = 0; i < numCiudades; i++) {
            for (int j = 0; j < numCiudades; j++) {
                distancias[i][j] = calculaDistancia(ciudades.get(i), ciudades.get(j));
            }
        }
        return distancias;
    }

    public static double calculaDistancia(double[] ciudad1, double[] ciudad2) {
        return Math.sqrt(Math.pow(ciudad1[1] - ciudad2[1], 2) + Math.pow(ciudad1[2] - ciudad2[2], 2));
    }

    // Calcula el coste total de un camino completo
    public static double getDistanciaTotal(double[][] distancias, int[] camino) {
        double coste = 0;
        for (int i = 0; i < camino.length - 1; i++) {
            coste += distancias[camino[i]][camino[i + 1]];
        }
        coste += distancias[camino[camino.length - 1]][camino[0]];// Volver al inicio
        return coste;
    }

    // Genera un tour aleatorio inicial
    public static int[] getTour(int NMaxCiudades) {
        int[] tour = new int[NMaxCiudades];
        for (int i = 0; i < NMaxCiudades; i++) {
            tour[i] = i;
        }
        Random random = new Random(12345);
        // Barajar el tour de manera aleatoria
        for (int i = NMaxCiudades - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = tour[i];
            tour[i] = tour[j];
            tour[j] = temp;
        }
        return tour;
    }

    public static int[] algVoraz(double[][] distancias) {
        int numCiudades = distancias.length;
        boolean[] visitadas = new boolean[numCiudades]; //Para saber que ciudades han sido ya visitadas
        int[] camino = new int[numCiudades]; //Guarda el orden de las ciudades que vamos a visitar

        //Inicializamos la primera ciudad como visitada y la añadimos al camino
        int actual = 0;
        visitadas[actual] = true;
        camino[0] = actual;

        for (int i = 1; i < numCiudades; i++) {
            int siguiente = -1; //Va a ir guardando la siguiente ciudad para visitar
            double minDistancia = Double.MAX_VALUE; //Va a ir guardando la distancia minima encontrada
            for (int j = 0; j < numCiudades; j++) {
                //Encuentra la ciudad no visitada mas cercana
                if (!visitadas[j] && distancias[actual][j] < minDistancia) {
                    siguiente = j;
                    minDistancia = distancias[actual][j];
                }
            }
            actual = siguiente; //Actualiza la ciudad siguiente
            camino[i] = actual; //Añade la ciudad al camino
            visitadas[actual] = true; //Marca la ciudad como visitada
        }
        return camino;
    }

    public static int[] algDivideYVenceras(double[][] distancias, int ini, int fin) {
        if (fin - ini == 1) {
            // Caso base: Si hay solo 2 ciudades, devolverlas como camino
            return new int[]{ini, fin};
        } else if (fin - ini == 0) {
            // Caso base: Si hay solo 1 ciudad, devolver esa única ciudad
            return new int[]{ini};
        }

        // Dividir el problema en dos mitades
        int mid = (ini + fin) / 2;

        // Resolver ambas mitades recursivamente
        int[] parteIzquierda = algDivideYVenceras(distancias, ini, mid);
        int[] parteDerecha = algDivideYVenceras(distancias, mid + 1, fin);

        // Combinar los resultados
        return combina(parteIzquierda, parteDerecha, distancias);
    }

    public static int[] combina(int[] parteIzquierda, int[] parteDerecha, double[][] distancias) {
        int n = parteIzquierda.length; //Numero de ciudades en la parte izquierda
        int m = parteDerecha.length; //Numero de ciudades en la parte derecha

        // Encontrar el mejor punto de conexión entre ambas partes
        double minDistancia = Double.MAX_VALUE;
        int mejorIzq = -1, mejorDer = -1; // Variables para almacenar las ciudades con la menor distancia entre izquierda y derecha

        // Iteramos por todas las ciudades de la parte izquierda y derecha
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                // Calculamos la distancia entre cada ciudad de la izquierda y cada ciudad de la derecha
                double dist = distancias[parteIzquierda[i]][parteDerecha[j]];
                // Si encontramos una distancia menor, actualizamos los mejores puntos de conexión
                if (dist < minDistancia) {
                    minDistancia = dist;
                    mejorIzq = i; //Ciudad en la izquierda
                    mejorDer = j; //Ciudad en la derecha
                }
            }
        }

        // Construir el camino combinado
        int[] camino = new int[n + m];
        int index = 0;

        // Añadir la parte izquierda desde el inicio hasta la mejor ciudad
        for (int i = 0; i <= mejorIzq; i++) {
            camino[index++] = parteIzquierda[i]; // Añadimos las ciudades de izquierda al camino
        }

        // Añadir la parte derecha desde la mejor ciudad en adelante
        for (int j = mejorDer; j < m; j++) {
            camino[index++] = parteDerecha[j]; // Añadimos las ciudades de derecha al camino
        }

        // Añadir la parte derecha desde el principio hasta la mejor ciudad
        for (int j = 0; j < mejorDer; j++) {
            camino[index++] = parteDerecha[j]; // Añadimos las ciudades restantes de derecha
        }

        // Añadir la parte izquierda desde la mejor ciudad + 1 hasta el final
        for (int i = mejorIzq + 1; i < n; i++) {
            camino[index++] = parteIzquierda[i]; // Añadimos las ciudades restantes de izquierda
        }

        return camino; //Devolvemos el camino combinado
    }

    // El algoritmo BA1 ejecuta iteraciones con caminos aleatorios y guarda la mejor solución
    public static int[] algoritmoAleatorioBA1(double[][] distancias, int iteraciones) {
        int NMaxCiudades = distancias.length;
        int[] mejorCamino = getTour(NMaxCiudades); // Camino inicial aleatorio
        double mejorCoste = getDistanciaTotal(distancias, mejorCamino);

        for (int i = 0; i < iteraciones; i++) {
            int[] caminoActual = getTour(NMaxCiudades);
            double costeActual = getDistanciaTotal(distancias, caminoActual);
            // Si el camino actual es mejor, lo guardamos
            if (costeActual < mejorCoste) {
                mejorCamino = caminoActual;
                mejorCoste = costeActual;
            }
        }
        System.out.println("Mejor camino: " + Arrays.toString(mejorCamino) + "\nCon coste " + mejorCoste);
        return mejorCamino;
    }

    // El algoritmo BA2 ejecuta iteraciones hasta que alcanza un máximo sin mejora
    public static int[] algoritmoAleatorioBA2(double[][] distancias, int maxIteraciones, int maxIterSinMejora) {
        int NMaxCiudades = distancias.length;
        int[] mejorCamino = getTour(NMaxCiudades);// Camino inicial aleatorio
        double mejorCoste = getDistanciaTotal(distancias, mejorCamino);
        int iterSinMejora = 0; //Contador de iteraciones sin mejora

        for (int i = 0; i < maxIteraciones; i++) {
            int[] caminoActual = getTour(NMaxCiudades);
            double costeActual = getDistanciaTotal(distancias, caminoActual);
            // Si el camino actual mejora la solución, actualizamos la mejor
            if (costeActual < mejorCoste) {
                mejorCamino = caminoActual;
                mejorCoste = costeActual;
                iterSinMejora = 0; // Reiniciar el contador si hay mejora
            } else {
                iterSinMejora++;
            }
            // Parar si se alcanzó el máximo de iteraciones sin mejora
            if (iterSinMejora >= maxIterSinMejora) {
                System.out.println("Parando por falta de mejoras tras " + maxIterSinMejora + " iteraciones.");
                break;
            }
        }

        System.out.println("Mejor camino encontrado: " + Arrays.toString(mejorCamino) + "\nCon coste " + mejorCoste);
        return mejorCamino;
    }

    public static void main(String[] args) {
        // Ruta relativa a la carpeta 'data'
        String folderPath = "src/data";

        // Listar todos los archivos en la carpeta
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        // Verificar si hay archivos en la carpeta
        if (files == null || files.length == 0) {
            System.out.println("No se encontraron archivos en la carpeta: " + folderPath);
            return;
        }

        // Leer y procesar cada archivo
        for (File file : files) {
            String filePath = file.getPath();
            System.out.println("Procesando archivo: " + filePath);

            // Inicializar la matriz de distancias
            double[][] distancias = inicializarMatrizDistanciaDesdeTSP(filePath);

            int[] n = {100, 500, 1000, 5000}; // Valores de iteraciones para BA1
            int[] p = {50, 100, 250, 500};    // Valores de iteraciones sin mejora para BA2

            // Medir el tiempo de ejecución y calcular el coste para el algoritmo voraz 
            System.out.println("\n---- Algoritmo Voraz ----");
            long inicio = System.nanoTime();
            int[] caminoVoraz = algVoraz(distancias);  // Aquí se llama al algoritmo voraz
            long fin = System.nanoTime();
            long tiempoTotal = (fin - inicio) / 1000000;  // Convertir a milisegundos 
            double costeTotalVoraz = getDistanciaTotal(distancias, caminoVoraz);  // Obtener el coste total
            System.out.println("Coste total del camino voraz: " + costeTotalVoraz);
            System.out.println("Tiempo de ejecución: " + tiempoTotal + " ms");  // Imprimir el tiempo de ejecución
            System.out.print("Camino voraz: ");
            for (int ciudad : caminoVoraz) {
                System.out.print(ciudad + " ");
            }
            System.out.println();

            System.out.println("\n---- Algoritmo Divide y Vencerás ----");
            inicio = System.nanoTime();
            int[] caminoDivide = algDivideYVenceras(distancias, 0, distancias.length - 1); // Ahora devuelve el camino
            fin = System.nanoTime();

            long tiempoDivide = (fin - inicio) / 1000000;  // Convertir a milisegundos
            double costeTotalDivide = getDistanciaTotal(distancias, caminoDivide);  // Calcular el coste total

            System.out.println("Coste total divide y vencerás: " + costeTotalDivide);
            System.out.println("Tiempo de ejecución divide y vencerás: " + tiempoDivide + " ms");
            System.out.print("Camino divide y vencerás: ");
            for (int ciudad : caminoDivide) {
                System.out.print(ciudad + " ");
            }
            System.out.println();

            // Ejecutar BA1 con diferentes valores de iteraciones y medir el tiempo
            System.out.println("\n---- Algoritmo BA1 ----");
            for (int maxIteraciones : n) {
                System.out.println("\nBA1 - Iteraciones: " + maxIteraciones);
                long inicioBA1 = System.nanoTime();
                algoritmoAleatorioBA1(distancias, maxIteraciones);
                long finBA1 = System.nanoTime();
                long tiempoTotalBA1 = (finBA1 - inicioBA1) / 1000000;
                System.out.println("Tiempo de ejecución: " + tiempoTotalBA1 + " ms");
                System.out.println("------------------------");
            }

            // Ejecutar BA2 con diferentes valores de iteraciones sin mejora y medir el tiempo
            System.out.println("\n---- Algoritmo BA2 ----");
            for (int maxIterSinMejora : p) {
                System.out.println("\nBA2 - p (Iteraciones sin mejora): " + maxIterSinMejora);
                long inicioBA2 = System.nanoTime();
                algoritmoAleatorioBA2(distancias, 5000, maxIterSinMejora);  //maxIteraciones limita la ejecución a 5000 iteraciones si no se cumple antes el criterio de p.
                long finBA2 = System.nanoTime();
                long tiempoTotalBA2 = (finBA2 - inicioBA2) / 1000000; // Convertir a milisegundos
                System.out.println("Tiempo de ejecución: " + tiempoTotalBA2 + " ms");
                System.out.println("------------------------");
            }
        }

    }

}