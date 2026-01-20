package pmcsn.files;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class BatchResultsFileGenerator {

    private PrintWriter batchesFile;
    private static final BatchResultsFileGenerator istanza = new BatchResultsFileGenerator();

    public static BatchResultsFileGenerator getInstance() {
        return istanza;
    }


    public void setFile(String filename) {
        try {
            //true serve per append dei dati
            FileWriter fw = new FileWriter(filename, false);
            batchesFile = new PrintWriter(fw);
            batchesFile.println("Component,Lambda,Metric,Value,CI");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //metodo che salva il record sul file batch means
    public void logBatchRecord(String component, double lambda, String metric, double value, double CI) {
        // time: tempo simulato (es. in ore)
        // metric: nome (stringa) della metrica
        // value: valore delle metrica (misurazione)
        // batch: indice del batch
        // componente: "System", "A", "B", o "P"
        if(this.batchesFile != null) {
            this.batchesFile.printf(java.util.Locale.US, "%s,%.2f,%s,%.6f,%s%n", component, lambda,metric, value, CI);
        }
    }

    public void closeFile() {
        if (batchesFile != null) {
            batchesFile.flush();
            batchesFile.close();
        }
    }
}
