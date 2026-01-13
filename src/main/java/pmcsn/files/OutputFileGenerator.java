package pmcsn.files;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class OutputFileGenerator {
    private PrintWriter writer;
    private int type;
    private static final OutputFileGenerator istanza = new OutputFileGenerator();

    public static OutputFileGenerator getInstance() {
        return istanza;
    }

    private final int acs = 4096;
    public double[] data = new double[acs];
    public int dataIndex = 0;

    public void setFile(String filename, int simType) {
        try {
            this.type = simType;
            //true serve per append dei dati
            FileWriter fw = new FileWriter(filename, false);
            writer = new PrintWriter(fw);
            //qui scrivo l'header
            if (simType == 0) {
                //header per transitorio. qui stampo una sola metrica
                writer.println("Time,Metric,Value,Seed,Component");
            } else if (simType == 1){
                //header per batch means
                writer.println("Time,Metric,Value,Batch,Component");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logRecordACS(double responseTime) {
        if (type == 0) {
            if(dataIndex == acs) {
                double temp = 0.0;
                for (double d: data) {
                    temp += d;
                }
                double rt = temp / acs;
                writer.printf(java.util.Locale.US, "%.4f%n", rt);
                data = new double[acs];
                dataIndex = 0;
            } else {
                data[dataIndex] = responseTime;
                dataIndex++;
            }
        }
    }

    //metodo che salva il record sul file transitorio
    public void logRecordTransient(double time, String metric, double value, long seed, String component) {
        // time: tempo simulato (es. in ore)
        // metric: stringa che indica quale misura è
        // value: valore metrica
        // seed: il seed corrente
        // componente: "System", "A", "B", o "P"

        writer.printf(java.util.Locale.US, "%.6f,%s,%.6f,%d,%s%n", time, metric, value, seed, component);
    }

    //metodo che salva il record sul file batch means
    public void logRecordBatchMeans(double time, String metric, double value, int batch, String component) {
        // time: tempo simulato (es. in ore)
        // metric: nome (stringa) della metrica
        // value: valore delle metrica (misurazione)
        // batch: indice del batch
        // componente: "System", "A", "B", o "P"
        writer.printf(java.util.Locale.US, "%.6f,%s,%.6f,%d,%s%n", time, metric,value,batch,component);
    }

    public void closeFile() {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }
}
