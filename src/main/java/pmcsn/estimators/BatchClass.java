package pmcsn.estimators;

import pmcsn.entities.Job;
import pmcsn.files.OutputFileGenerator;

import static java.lang.System.out;

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

    public BatchClass(int size, boolean isSystem, String name, int batchNum) {
        this.size = size;
        if(name == "A") {
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

    public void updateBatch(Job job) {
        if (isSystem) {
            updateGlobalBatch(job);
            return;
        }
        update(job);
        cursor++;
        if (cursor >= size) {
            logSampling(job.getCompleteTime());
            batchIndex++;
            resetBatch(job.getCompleteTime());
            cursor = 0;
        }
    }

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

    public void resetBatch(double newTime) {
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

    public void logSampling(double time) {
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
