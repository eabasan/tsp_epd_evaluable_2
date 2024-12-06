/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package epd_evaluable_2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
        Random random = new Random();
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

    public static void main(String[] args) {
        // Ruta relativa
        String file = "src/data/a280.tsp";
        //String file = "src/data/berlin52.tsp";
        //String file = "src/data/kroA100.tsp";
        //String file = "src/data/kroA150.tsp";
        //String file = "src/data/kroA200.tsp";
        //String file = "src/data/vm1084.tsp";
        //String file = "src/data/vm1748.tsp";
        double[][] distancias = inicializarMatrizDistanciaDesdeTSP(file);

        // Medir el tiempo de ejecución y calcular el coste para el algoritmo voraz 
        long inicio = System.nanoTime();
        int[] caminoVoraz = algVoraz(distancias);
        long fin = System.nanoTime();
        long tiempoTotal = (fin - inicio) / 1000000; // Convertir a milisegundos 
        double costeTotalVoraz = getDistanciaTotal(distancias, caminoVoraz); // Mostrar los resultados 
        System.out.println("Coste total del camino voraz: " + costeTotalVoraz);
        System.out.println("Tiempo de ejecución: " + tiempoTotal + " ms"); // Imprimir el camino voraz 
        System.out.print("Camino voraz: ");
        for (int ciudad : caminoVoraz) {
            System.out.print(ciudad + " ");
        }
        System.out.println();

    }

}
