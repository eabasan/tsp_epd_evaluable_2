package epd_evaluable_2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EPD_EVALUABLE_2 {

    public static double[][] inicializarMatrizDistanciaDesdeTSP(String file) {
        List<double[]> ciudades = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isCoordSection = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("NODE_COORD_SECTION")) {
                    isCoordSection = true;
                    continue;
                }
                if (isCoordSection) {
                    if (line.equals("EOF")) {
                        break;
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

    public static double getDistanciaTotal(double[][] distancias, int[] camino) {
        double coste = 0;
        for (int i = 0; i < camino.length - 1; i++) {
            coste += distancias[camino[i]][camino[i + 1]];
        }
        coste += distancias[camino[camino.length - 1]][camino[0]];
        return coste;
    }

    public static int[] getTour(int NMaxCiudades) {
        int[] tour = new int[NMaxCiudades];
        for (int i = 0; i < NMaxCiudades; i++) {
            tour[i] = i;
        }
        Random random = new Random();
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
        boolean[] visitadas = new boolean[numCiudades];
        int[] camino = new int[numCiudades];

        int actual = 0;
        visitadas[actual] = true;
        camino[0] = actual;

        for (int i = 1; i < numCiudades; i++) {
            int siguiente = -1;
            double minDistancia = Double.MAX_VALUE;
            for (int j = 0; j < numCiudades; j++) {
                if (!visitadas[j] && distancias[actual][j] < minDistancia) {
                    siguiente = j;
                    minDistancia = distancias[actual][j];
                }
            }
            actual = siguiente;
            camino[i] = actual;
            visitadas[actual] = true;
        }
        return camino;
    }

    public static int[] algDivideYVenceras(double[][] distancias, int ini, int fin) {
        if (fin - ini == 1) {
            return new int[]{ini, fin};
        } else if (fin - ini == 0) {
            return new int[]{ini};
        }
        int mid = (ini + fin) / 2;
        int[] parteIzquierda = algDivideYVenceras(distancias, ini, mid);
        int[] parteDerecha = algDivideYVenceras(distancias, mid + 1, fin);
        return combina(parteIzquierda, parteDerecha, distancias);
    }

    public static int[] combina(int[] parteIzquierda, int[] parteDerecha, double[][] distancias) {
        int n = parteIzquierda.length;
        int m = parteDerecha.length;

        double minDistancia = Double.MAX_VALUE;
        int mejorIzq = -1, mejorDer = -1;

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

        int[] camino = new int[n + m];
        int index = 0;
        for (int i = 0; i <= mejorIzq; i++) {
            camino[index++] = parteIzquierda[i];
        }
        for (int j = mejorDer; j < m; j++) {
            camino[index++] = parteDerecha[j];
        }
        for (int j = 0; j < mejorDer; j++) {
            camino[index++] = parteDerecha[j];
        }
        for (int i = mejorIzq + 1; i < n; i++) {
            camino[index++] = parteIzquierda[i];
        }

        return camino;
    }

    public static int[] algoritmoAleatorioBA1(double[][] distancias, int iteraciones) {
        int NMaxCiudades = distancias.length;
        int[] mejorCamino = getTour(NMaxCiudades);
        double mejorCoste = getDistanciaTotal(distancias, mejorCamino);

        for (int i = 0; i < iteraciones; i++) {
            int[] caminoActual = getTour(NMaxCiudades);
            double costeActual = getDistanciaTotal(distancias, caminoActual);
            if (costeActual < mejorCoste) {
                mejorCamino = caminoActual;
                mejorCoste = costeActual;
            }
        }
        return mejorCamino;
    }

    public static int[] algoritmoAleatorioBA2(double[][] distancias, int maxIteraciones, int maxIterSinMejora) {
        int NMaxCiudades = distancias.length;
        int[] mejorCamino = getTour(NMaxCiudades);
        double mejorCoste = getDistanciaTotal(distancias, mejorCamino);
        int iterSinMejora = 0;

        for (int i = 0; i < maxIteraciones; i++) {
            int[] caminoActual = getTour(NMaxCiudades);
            double costeActual = getDistanciaTotal(distancias, caminoActual);
            if (costeActual < mejorCoste) {
                mejorCamino = caminoActual;
                mejorCoste = costeActual;
                iterSinMejora = 0;
            } else {
                iterSinMejora++;
            }
            if (iterSinMejora >= maxIterSinMejora) {
                break;
            }
        }
        return mejorCamino;
    }

    public static double calcularMedia(List<Double> valores) {
        double suma = 0;
        for (double valor : valores) {
            suma += valor;
        }
        return suma / valores.size();
    }

    public static void main(String[] args) {
        String folderPath = "src/data";

        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No se encontraron archivos en la carpeta: " + folderPath);
            return;
        }

        for (File file : files) {
            String filePath = file.getPath();
            System.out.println("\n\nProcesando archivo: " + filePath);

            double[][] distancias = inicializarMatrizDistanciaDesdeTSP(filePath);

            int[] n = {100, 500, 1000, 5000};
            int[] p = {50, 100, 250, 500};

            System.out.println("\n---- Algoritmo Voraz ----");
            List<Double> tiemposVoraz = new ArrayList<>();
            List<Double> costesVoraz = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                long inicio = System.nanoTime();
                int[] caminoVoraz = algVoraz(distancias);
                long fin = System.nanoTime();
                tiemposVoraz.add((fin - inicio) / 1000000.0);
                costesVoraz.add(getDistanciaTotal(distancias, caminoVoraz));
            }
            System.out.println("Media del coste voraz: " + calcularMedia(costesVoraz));
            System.out.println("Media del tiempo voraz (ms): " + calcularMedia(tiemposVoraz));

            System.out.println("\n---- Algoritmo Divide y Venceras ----");
            List<Double> tiemposDivide = new ArrayList<>();
            List<Double> costesDivide = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                long inicio = System.nanoTime();
                int[] caminoDivide = algDivideYVenceras(distancias, 0, distancias.length - 1);
                long fin = System.nanoTime();
                tiemposDivide.add((fin - inicio) / 1000000.0);
                costesDivide.add(getDistanciaTotal(distancias, caminoDivide));
            }
            System.out.println("Media del coste divide y venceras: " + calcularMedia(costesDivide));
            System.out.println("Media del tiempo divide y venceras (ms): " + calcularMedia(tiemposDivide));

            System.out.println("\n---- Algoritmo BA1 ----");
            for (int maxIteraciones : n) {
                System.out.println("\nBA1 - Iteraciones: " + maxIteraciones);
                List<Double> tiemposBA1 = new ArrayList<>();
                List<Double> costesBA1 = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    long inicioBA1 = System.nanoTime();
                    int[] caminoBA1 = algoritmoAleatorioBA1(distancias, maxIteraciones);
                    long finBA1 = System.nanoTime();
                    tiemposBA1.add((finBA1 - inicioBA1) / 1000000.0);
                    costesBA1.add(getDistanciaTotal(distancias, caminoBA1));
                }
                System.out.println("Media del coste BA1: " + calcularMedia(costesBA1));
                System.out.println("Media del tiempo BA1 (ms): " + calcularMedia(tiemposBA1));
            }

            System.out.println("\n---- Algoritmo BA2 ----");
            for (int maxIterSinMejora : p) {
                System.out.println("\nBA2 - p (Iteraciones sin mejora): " + maxIterSinMejora);
                List<Double> tiemposBA2 = new ArrayList<>();
                List<Double> costesBA2 = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    long inicioBA2 = System.nanoTime();
                    int[] caminoBA2 = algoritmoAleatorioBA2(distancias, 5000, maxIterSinMejora);
                    long finBA2 = System.nanoTime();
                    tiemposBA2.add((finBA2 - inicioBA2) / 1000000.0);
                    costesBA2.add(getDistanciaTotal(distancias, caminoBA2));
                }
                System.out.println("Media del coste BA2: " + calcularMedia(costesBA2));
                System.out.println("Media del tiempo BA2 (ms): " + calcularMedia(tiemposBA2));
            }
        }
    }
}
