package pmcsn.controllers;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class BatchMeansCalculator {

    private static final double T_SCORE = 1.97196;
    private static final double SQRT_N = 14.142135; // radice quadrata di 200
    private static final String delimiter = ",";
    private static String inputCsvPath = "Batch.csv";
    private static String outputCsvPath = "BatchResults.csv";
    private int maxNumCopy;


    public void setFile() {
        try {
            //true serve per append dei dati
            FileWriter fw = new FileWriter(outputCsvPath, false);
            PrintWriter batchesFile = new PrintWriter(fw);
            batchesFile.println("Component,Lambda,Metric,Value,CI");
            batchesFile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFileCopy(int maxNumCopy) {
        try {
            this.maxNumCopy = maxNumCopy;
            //true serve per append dei dati
            FileWriter fw = new FileWriter("CopyMeans.csv", false);
            PrintWriter copyB = new PrintWriter(fw);
            copyB.println("Component,MaxNumCopy,Metric,CopyMean,CI");
            copyB.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void calculateCopy(int maxNumCopy) {
        String tempIn = inputCsvPath;
        String tempOut = outputCsvPath;
        outputCsvPath = "CopyMeans.csv";
        inputCsvPath = "CopyBatch.csv";

        calculate(maxNumCopy);

        inputCsvPath = tempIn;
        outputCsvPath = tempOut;
    }

    public void calculate(double lambda) {

        // Struttura dati per raggruppare i valori:
        // Map<Componente, Map<Metrica, ListaDiValoriBatch>>
        // Uso TreeMap per avere l'output ordinato alfabeticamente per componente e metrica
        Map<String, Map<String, List<Double>>> dataStore = new TreeMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(inputCsvPath))) {
            String line;
            // ricordati che devi saltare l'intestazione
            String header = br.readLine();
            if (header == null) {
                System.out.println("errore: file vuoto");
                return;
            }

            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                lineCount++;
                //trimming delle righe
                String[] parts = line.split(delimiter);

                // controllo per sicurezza, potrebbe essere cancellato
                if (parts.length < 5) {
                    System.err.println("Riga " + lineCount + " problema");
                    continue;
                }

                try {
                    // dati estratti sulla base di fil batch.csv:
                    // Time[0], Metric[1], Value[2], Batch[3], Component[4]
                    String metricStr = parts[1].trim();
                    double value = Double.parseDouble(parts[2].trim());
                    String componentStr = parts[4].trim();


                    dataStore.computeIfAbsent(componentStr, k -> new TreeMap<>());
                    dataStore.get(componentStr).computeIfAbsent(metricStr, k -> new ArrayList<>());
                    dataStore.get(componentStr).get(metricStr).add(value);

                } catch (NumberFormatException e) {
                    System.err.println("errore di parsing numerico alla riga " + lineCount + ": " + parts[2]);
                } catch (Exception e) {
                    System.err.println("errore alla riga " + lineCount + ": " + e.getMessage());
                }
            }
            System.out.println("Lettura completata");

        } catch (IOException e) {
            System.err.println("errore critico durante la lettura");
            return;
        }

        //apertura file
        PrintWriter batchesFile;

        try {
            //true serve per append dei dati
            FileWriter fw = new FileWriter(outputCsvPath, true);
            batchesFile = new PrintWriter(fw);



            for (Map.Entry<String, Map<String, List<Double>>> componentEntry : dataStore.entrySet()) {
                String componentName = componentEntry.getKey();
                Map<String, List<Double>> metricsMap = componentEntry.getValue();


                for (Map.Entry<String, List<Double>> metricEntry : metricsMap.entrySet()) {
                    String metricName = metricEntry.getKey();
                    List<Double> batchValues = metricEntry.getValue();

                    int n = batchValues.size();
                    if (n < 2) {
                        System.out.printf("%-10s | %-10s | %-6d | Dati insufficienti per calcolare la Dev. Std.%n", componentName, metricName, n);
                        continue;
                    }

                    // statistiche
                    double grandMean = calculateMean(batchValues);
                    double stdDevS = calculateSampleStdDev(batchValues, grandMean);

                    double CI = (stdDevS * T_SCORE)/SQRT_N;
                    // scrittura
                    batchesFile.printf(java.util.Locale.US, "%s,%.2f,%s,%.6f,%s%n", componentName, lambda, metricName, grandMean, CI);
                }
            }

            batchesFile.flush();
            batchesFile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //calcola la media totale
    public static double calculateMean(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    // calcola la deviazione standard
    public static double calculateSampleStdDev(List<Double> values, double mean) {
        if (values == null || values.size() < 2) return 0.0;
        double sumSquaredDiffs = 0;
        for (double v : values) {
            double diff = v - mean;
            sumSquaredDiffs += (diff * diff);
        }
        // varianza campionaria = somma differenze quadratiche / (N - 1)
        double sampleVariance = sumSquaredDiffs / (values.size() - 1);
        return Math.sqrt(sampleVariance);
    }

}