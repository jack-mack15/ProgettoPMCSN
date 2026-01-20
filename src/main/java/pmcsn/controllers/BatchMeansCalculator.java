package pmcsn.controllers;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class BatchMeansCalculator {

    private static final double T_SCORE = 1.97196;
    private static final double SQRT_N = 14.142135; // Radice quadrata di 200
    private static final String delimiter = ",";
    private static final String inputCsvPath = "Batch.csv";
    private static final String outputCsvPath = "BatchResults.csv";


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


    public void calculate(double lambda) {

        // Struttura dati per raggruppare i valori:
        // Map<Componente, Map<Metrica, ListaDiValoriBatch>>
        // Uso TreeMap per avere l'output ordinato alfabeticamente per componente e metrica
        Map<String, Map<String, List<Double>>> dataStore = new TreeMap<>();

        // --- FASE 1: LETTURA E RAGGRUPPAMENTO DATI ---
        try (BufferedReader br = new BufferedReader(new FileReader(inputCsvPath))) {
            String line;
            // Legge e salta la riga di intestazione
            String header = br.readLine();
            if (header == null) {
                System.out.println("Errore: Il file è vuoto.");
                return;
            }

            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                lineCount++;
                // Divide la riga in colonne
                String[] parts = line.split(delimiter);

                // Controllo di sicurezza sulla struttura della riga
                if (parts.length < 5) {
                    System.err.println("Riga " + lineCount + " ignorata: formato non valido o dati mancanti.");
                    continue;
                }

                try {
                    // Estrazione dati basata sull'ordine delle colonne nell'immagine:
                    // Time[0], Metric[1], Value[2], Batch[3], Component[4]
                    String metricStr = parts[1].trim();
                    // Parsing del valore double (gestisce potenziali spazi)
                    double value = Double.parseDouble(parts[2].trim());
                    String componentStr = parts[4].trim();

                    // Logica di inserimento nella mappa nidificata:
                    // 1. Se il componente non esiste, crea la sua mappa interna.
                    dataStore.computeIfAbsent(componentStr, k -> new TreeMap<>());
                    // 2. Se la metrica per quel componente non esiste, crea la lista.
                    dataStore.get(componentStr).computeIfAbsent(metricStr, k -> new ArrayList<>());
                    // 3. Aggiunge il valore alla lista appropriata.
                    dataStore.get(componentStr).get(metricStr).add(value);

                } catch (NumberFormatException e) {
                    System.err.println("Errore di parsing numerico alla riga " + lineCount + ": " + parts[2]);
                } catch (Exception e) {
                    System.err.println("Errore generico alla riga " + lineCount + ": " + e.getMessage());
                }
            }
            System.out.println("Lettura completata. Righe processate: " + lineCount + "\n");

        } catch (IOException e) {
            System.err.println("Errore critico durante la lettura del file: " + e.getMessage());
            return;
        }

        //apertura file
        PrintWriter batchesFile;

        try {
            //true serve per append dei dati
            FileWriter fw = new FileWriter(outputCsvPath, true);
            batchesFile = new PrintWriter(fw);


            // Itera sui Componenti (chiavi esterne)
            for (Map.Entry<String, Map<String, List<Double>>> componentEntry : dataStore.entrySet()) {
                String componentName = componentEntry.getKey();
                Map<String, List<Double>> metricsMap = componentEntry.getValue();

                // Itera sulle Metriche (chiavi interne)
                for (Map.Entry<String, List<Double>> metricEntry : metricsMap.entrySet()) {
                    String metricName = metricEntry.getKey();
                    List<Double> batchValues = metricEntry.getValue();

                    int n = batchValues.size();
                    if (n < 2) {
                        System.out.printf("%-10s | %-10s | %-6d | Dati insufficienti per calcolare la Dev. Std.%n", componentName, metricName, n);
                        continue;
                    }

                    // Calcolo statistiche usando metodi helper
                    double grandMean = calculateMean(batchValues);
                    double stdDevS = calculateSampleStdDev(batchValues, grandMean);

                    double CI = (stdDevS * T_SCORE)/SQRT_N;
                    batchesFile.printf(java.util.Locale.US, "%s,%.2f,%s,%.6f,%s%n", componentName, lambda, metricName, grandMean, CI);
                }
            }

            batchesFile.flush();
            batchesFile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- METODI HELPER PER I CALCOLI STATISTICI ---

    /**
     * Calcola la media aritmetica di una lista di double.
     */
    private static double calculateMean(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    /**
     * Calcola la Deviazione Standard CAMPIONARIA (S), dividendo per (N-1).
     * Fondamentale per l'analisi Batch Means per la stima dell'intervallo di confidenza.
     */
    private static double calculateSampleStdDev(List<Double> values, double mean) {
        if (values == null || values.size() < 2) return 0.0; // Non definita per N<2
        double sumSquaredDiffs = 0;
        for (double v : values) {
            double diff = v - mean;
            sumSquaredDiffs += (diff * diff);
        }
        // Varianza campionaria = somma differenze quadratiche / (N - 1)
        double sampleVariance = sumSquaredDiffs / (values.size() - 1);
        return Math.sqrt(sampleVariance);
    }

}