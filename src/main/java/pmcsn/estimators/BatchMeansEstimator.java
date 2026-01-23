package pmcsn.estimators;

import pmcsn.centers.BLoadBalancer;
import pmcsn.entities.Job;
import pmcsn.events.Event;

//classe che contiene le istanze di BatchClass

public class BatchMeansEstimator {

    private static final BatchMeansEstimator istanza = new BatchMeansEstimator();

    public static BatchMeansEstimator getInstance() {
        return istanza;
    }

    //tipi di batch per componente
    private BatchClass batchA;
    private BatchClass batchB;
    private BatchClass batchP;
    private BatchClass batchSystem;

    //metodo per aggionrare il batch locale (quello dei nodi)
    public void updateBatchStats(String node, Job job) {
        getBatch(node).updateBatch(job);
        if(node == "A" && (job.getJobClass() == -1 || job.getJobClass() == 2)) {
            getBatch("System").updateBatch(job);
        }
    }

    //metodo per creare le istanze dei batchclass
    public void setBatches(int size, int batchNum) {
        batchA = new BatchClass(size,false,"A",batchNum);
        batchB = new BatchClass(size,false,"B",batchNum);
        batchP = new BatchClass(size,false,"P",batchNum);
        batchSystem = new BatchClass(size,true,"System",batchNum);
    }

    //metodo per resettare tutto
    public void resetBatch(double newTime) {
        batchA.resetBatch(newTime);
        batchB.resetBatch(newTime);
        batchP.resetBatch(newTime);
        batchSystem.resetBatch(newTime);
    }

    //utile solo in fase verifica SCALING
    public void setBatchForLoss(BLoadBalancer bLoadBalancer) {
        getBatch("B").setLossInfo(bLoadBalancer);
    }

    //metodo ausiliario per ottenere il batchclass corretto
    public BatchClass getBatch(String node) {
        switch (node) {
            case "A":
                return batchA;
            case "B":
                return batchB;
            case "P":
                return batchP;
            default:
                return batchSystem;
        }
    }

    //metodo utile solo inf ase di SCALING verifica
    public void dropJob(Event event) {
        batchSystem.dropJob(event.getIdRequest());
    }
}
