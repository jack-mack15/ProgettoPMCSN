package pmcsn;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvAnalyzer {

    public static void main(String[] args) {

        String csvFile = "transitorio.csv";
        String splitBy = ",";


        List<Double> responseTimes = new ArrayList<>();
        List<Double> populations = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;



            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) continue;

                String[] data = line.split(splitBy);

                if (data.length >= 5) {
                    try {
                        String metric = data[1].trim();
                        String valueStr = data[2].trim();
                        String component = data[4].trim();

                        // solo system
                        if (component.equalsIgnoreCase("System")) {
                            double value = Double.parseDouble(valueStr);

                            if (metric.equalsIgnoreCase("ResponseTime")) {
                                responseTimes.add(value);
                            } else if (metric.equalsIgnoreCase("Population")) {
                                populations.add(value);
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Ignora righe con numeri malformati (o intestazioni ripetute)
                        continue;
                    }
                }
            }


            System.out.println("ANALISI COMPONENTE: System--------------------------------");
            printStats("ResponseTime", responseTimes);
            System.out.println("----------------------------------");
            printStats("Population", populations);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printStats(String metricName, List<Double> values) {
        if (values.isEmpty()) {
            System.out.println(metricName + ": Nessun dato trovato.");
            return;
        }

        // 1. Calcolo Media
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        double mean = sum / values.size();

        // 2. Calcolo Deviazione Standard
        double standardDeviation = 0.0;
        for (double v : values) {
            standardDeviation += Math.pow(v - mean, 2);
        }
        standardDeviation = Math.sqrt(standardDeviation / values.size());

        System.out.println("METRICA: " + metricName);
        System.out.println("  -> Campioni trovati: " + values.size());
        System.out.printf("  -> Media (Mean):     %.6f%n", mean);
        System.out.printf("  -> Dev. Std (Sigma): %.6f%n", standardDeviation);
    }
}
