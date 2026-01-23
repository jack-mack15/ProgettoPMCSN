package pmcsn.estimators;

import pmcsn.entities.Job;
import pmcsn.events.Event;

public class Statistics {

    private static final Statistics istanza = new Statistics();

    public static Statistics getInstance() {
        return istanza;
    }

    //istanza del batch means
    private BatchMeansEstimator batchMeansEstimator = BatchMeansEstimator.getInstance();
    //estimator per esperimenti non batch means
    private ClassicStatistics simpleRun = new ClassicStatistics();
    //experiment type
    private int type;
    //seeds usati dalle componenti
    private long[] seeds = new long[6];

    //metodo che viene invocato in caso di evento sampling
    public void onSampling(double time) {
        if (type == 0) {
            simpleRun.logSampling(time,seeds[0]);
        }
    }

    //metodo per ottenere i seed della run utili per il logging
    public void setSeedsForOutput(long[] seeds) {
        this.seeds = seeds;
    }

    //questo metodo viene invocato al completamento di ogni job da parte di ogni nodo, indici locali e in caso anche globali
    public void updateEstimators(String node, Job job) {
        if (type == 0) {
            //simple experiments
            simpleRun.updateStatistics(node,job);
        } else if (type == 1) {
            //batch means
            batchMeansEstimator.updateBatchStats(node,job);
        }
    }

    //metodo per rimuovere i dati del job dal sistema
    //usato solo in fase verifica SCALING
    public void finalizeDroppedJob(Event event) {
        if (type == 0) {
            simpleRun.dropJob(event.getIdRequest());
        } else if (type == 1) {
            BatchMeansEstimator.getInstance().dropJob(event);
        }
    }


    //metodo per impostare il tipo di esperimento e dunque statistiche.
    //0 è un esperimento semplice
    //1 è esperimento batch means
    public void setType(int type) {
        PopulationEstimator.getInstance().setPopulations();
        this.type = type;
    }

    //metodo per resettare tutti gli estimators per poi riusare la classe statistic per un'altra run
    public void resetStatistics() {
        if (type == 0) {
            simpleRun.resetStatistics();
        } else if (type == 1) {
            batchMeansEstimator.resetBatch(0.0);
        }
    }

    //utile per debug
    public void outputStatistics(double finish) {
        if (type == 0) {
            //stampa risultato della run semplice
            simpleRun.printStatistics(finish);
        }
    }

}