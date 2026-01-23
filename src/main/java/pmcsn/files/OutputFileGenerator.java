package pmcsn.files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class OutputFileGenerator {
    private PrintWriter stats;
    private PrintWriter batch;
    private PrintWriter copy;
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
            stats = new PrintWriter(fw);
            //qui scrivo l'header
            if (simType == 0) {
                //header per transitorio. qui stampo una sola metrica
                stats.println("Time,Metric,Value,Seed,Component");
            } else if (simType == 1 || simType == 2){
                //header per batch means
                stats.close();
                fw = new FileWriter(filename, false);
                batch = new PrintWriter(fw);
                batch.println("Time,Metric,Value,Batch,Component");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFileCopy() {
        try {
            FileWriter fw = new FileWriter("CopyBatch.csv",false);
            copy = new PrintWriter(fw);
            copy.println("Time,Metric,Value,Batch,Component");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logCopy(double time, double value, int batch) {
        copy.printf(java.util.Locale.US, "%.6f,%s,%.6f,%d,%s%n", time, "CopyNum", value, batch, "B");
    }

    public void logRecordACS(double responseTime) {
        if (type == 0) {
            if(dataIndex == acs) {
                double temp = 0.0;
                for (double d: data) {
                    temp += d;
                }
                double rt = temp / acs;
                stats.printf(java.util.Locale.US, "%.4f%n", rt);
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

        stats.printf(java.util.Locale.US, "%.6f,%s,%.6f,%d,%s%n", time, metric, value, seed, component);
    }

    //metodo che salva il record sul file batch means
    public void logRecordBatchMeans(double time, String metric, double value, int batch, String component) {
        // time: tempo simulato (es. in ore)
        // metric: nome (stringa) della metrica
        // value: valore delle metrica (misurazione)
        // batch: indice del batch
        // componente: "System", "A", "B", o "P"
        if(this.batch != null) {
            this.batch.printf(java.util.Locale.US, "%.6f,%s,%.6f,%d,%s%n", time, metric, value, batch, component);
        }
    }

    public void closeFile() {
        if (stats != null) {
            stats.flush();
            stats.close();
        }
        if (batch != null) {
            batch.flush();
            batch.close();
        }
        if (copy != null) {
            copy.flush();
            copy.close();
        }
    }

    public void flushFiles() {
        if (stats != null) {
            stats.flush();
        }
        if (batch != null) {
            batch.flush();
        }
        if (copy != null) {
            copy.flush();
        }
    }

    public void deleteCopy() {
        File fileDaEliminare = new File("CopyBatch.csv"); // Usa la stessa stringa 'filename' usata nel FileWriter

        if (fileDaEliminare.exists()) {
            boolean cancellato = fileDaEliminare.delete();

            if (cancellato) {
                System.out.println("File eliminato con successo.");
            } else {
                System.err.println("Impossibile eliminare il file. Potrebbe essere ancora aperto o mancano i permessi.");
            }
        }
    }
    public void deleteBatch() {
        File fileDaEliminare = new File("Batch.csv"); // Usa la stessa stringa 'filename' usata nel FileWriter

        if (fileDaEliminare.exists()) {
            boolean cancellato = fileDaEliminare.delete();

            if (cancellato) {
                System.out.println("File eliminato con successo.");
            } else {
                System.err.println("Impossibile eliminare il file. Potrebbe essere ancora aperto o mancano i permessi.");
            }
        }
    }
}
