package pmcsn.estimators;

import pmcsn.centers.BLoadBalancer;
import pmcsn.controllers.BatchMeansCalculator;
import pmcsn.entities.Job;
import pmcsn.files.OutputFileGenerator;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

//classe che implementa logica gestione del batch aggiornamento e scrittura su file

public class BatchClass {
    //parametro b delle slide
    private int size;
    //parametro k delle slide
    private int batchNum;
    //indice del batch corrente
    private int batchIndex;
    //indice del job corrente all'interno del corrente batch
    private int cursor;
    //nome del batch, associato ai nodi del sistema
    private String component;


    //estimators
    private Estimator responseTimeEstimator;
    private Estimator waitTimeEstimator;
    private PopulationEstimator populationEstimator;
    private  GlobalEstimator globalEstimator;

    //variabile che indica se il batch è del sistema
    private boolean isSystem;
    //variabile che indica se ho modello SCALING
    private boolean isScaling;
    //per avere numero di copie sono costretto ad aggiungere loadbalancer qui
    private BLoadBalancer bLoad = null;
    //lista di ploss (verifica modello SCALING) di ogni batch di b
    private List<Double> batchesPLoss = new ArrayList<>();

    public BatchClass(int size, boolean isSystem, String name, int batchNum) {
        this.size = size;
        if(name == "A") {
            //A riceve il triplo di job, dunque triplico la size del batch A.
            this.size = 3*size;
        }
        batchIndex = 0;
        cursor = 0;
        component = name;
        this.isSystem = isSystem;
        this.batchNum = batchNum;
        if (isSystem) {
            globalEstimator = new GlobalEstimator();
        } else {
            waitTimeEstimator = new Estimator();
            responseTimeEstimator = new Estimator();
        }
        populationEstimator = PopulationEstimator.getInstance();
    }

    //metodo che aggiorna gli estimator del batch
    //questo metodo viene invocato da uno dei server (o globalmente) e quindi aggiorna solo
    //gli estimator di quel server
    public void updateBatch(Job job) {
        if (isSystem) {
            updateGlobalBatch(job);
            return;
        }
        update(job);
        cursor++;
        if (cursor >= size) {
            //se entro ho complettato un batch, quindi stampo medie e resetto batch
            logSampling(job.getCompleteTime());
            batchIndex++;
            resetBatch(job.getCompleteTime());
            cursor = 0;
        }
    }


    //metodo utile solo in fase verifica SCALING
    public void dropJob(long id) {
        if (isSystem) {
            globalEstimator.onRemove(id);
        }
    }

    //metodo che calcola i tempi del job appena completato
    private void update(Job job) {
        //tempi significativi del job
        double arrivalTime = job.getArrivalTime();
        double completeTime = job.getCompleteTime();
        double responseTime = completeTime-arrivalTime;
        double waitTime = responseTime-job.getServiceTime();

        waitTimeEstimator.update(component, waitTime);
        responseTimeEstimator.update(component,responseTime);

    }

    //metodo per aggiornare il batch globale (quello del sistema)
    //invocato solo dal batch system
    private void updateGlobalBatch(Job job) {
        if (job.getJobClass() == -1) {
            //arrivo, solo salvataggio del job
            globalEstimator.update(job);
        } else {

            //completamento del job
            globalEstimator.update(job);
            cursor++;
            if (cursor >= size) {
                logSampling(job.getCompleteTime());
                batchIndex++;
                resetBatch(job.getCompleteTime());
                cursor = 0;
            }
        }
    }

    //metodo per resettare il batch durante batch means
    public void resetBatch(double newTime) {
        if(component == "B" && bLoad != null) {
            int currCopies = bLoad.getCurrNumOfCopy();
            CopyEstimator.getInstance().resetForBatch(currCopies, newTime);
        }

        populationEstimator.resetPopulation(component,newTime,true);
        if (isSystem) {
            globalEstimator.resetGlobalEstimator();
        } else {
            waitTimeEstimator = new Estimator();
            responseTimeEstimator = new Estimator();
        }
        if (batchIndex == batchNum) {
            batchIndex = 0;
        }
    }

    //metodo utile solo pr verifica SCALING
    public void setLossInfo(BLoadBalancer bLoadBalancer) {
        isScaling = true;
        this.bLoad = bLoadBalancer;
    }

    //metodo che scrive i valori del batch una volta completato
    public void logSampling(double time) {
        if (component == "B" && isScaling && bLoad != null) {
            //parte utile solo in fase di verifica o per numero medio copie
            long discarded = bLoad.getAndResetDiscardedJobs();
            long seen = bLoad.getAndResetSeenJobs();
            double ploss = (double)discarded/seen;
            batchesPLoss.add(ploss);

            if ((batchIndex) == (batchNum-1)) {
                double batchMean = BatchMeansCalculator.calculateMean(batchesPLoss);
                double batchStd = BatchMeansCalculator.calculateSampleStdDev(batchesPLoss,batchMean);

                out.println("\n\nPLOSS VALUE FOR BATCH MEANS:::::::::::::::::::::::::::::::");
                out.println("Mean: "+batchMean);
                out.println("STD: "+batchStd+"\n\n");
            }
            CopyEstimator.getInstance().setFinishTime(time);
            double copies = CopyEstimator.getInstance().getNumCopyMean();
            OutputFileGenerator.getInstance().logCopy(time,copies,batchIndex);
        }
        //recupero metriche e logging
        if(isSystem) {
            //in caso sia batch del sistema
            double rtSystem = globalEstimator.getResponseTimeMean();
            double popSystem = populationEstimator.getPopulationMean("System",time,true);
            double thrSystem = populationEstimator.getThroughput("System",time,true);
            double utSystem = populationEstimator.getUtilization("System",time,true);

            OutputFileGenerator istance = OutputFileGenerator.getInstance();

            istance.logRecordBatchMeans(time,"rt",rtSystem,batchIndex,component);
            istance.logRecordBatchMeans(time,"pop",popSystem,batchIndex,component);
            istance.logRecordBatchMeans(time,"util",utSystem,batchIndex,component);
            istance.logRecordBatchMeans(time,"thr",thrSystem,batchIndex,component);

        } else {
            //in caso sia batch di un server
            double rt = responseTimeEstimator.getMean(component);
            double pop = populationEstimator.getPopulationMean(component,time,true);
            double wait = waitTimeEstimator.getMean(component);
            double thr = populationEstimator.getThroughput(component,time,true);
            double ut = populationEstimator.getUtilization(component,time,true);

            OutputFileGenerator istance = OutputFileGenerator.getInstance();

            istance.logRecordBatchMeans(time,"rt",rt,batchIndex,component);
            istance.logRecordBatchMeans(time,"wait",wait,batchIndex,component);
            istance.logRecordBatchMeans(time,"util",ut,batchIndex,component);
            istance.logRecordBatchMeans(time,"pop",pop,batchIndex,component);
            istance.logRecordBatchMeans(time,"thr",thr,batchIndex,component);
        }
    }
}
